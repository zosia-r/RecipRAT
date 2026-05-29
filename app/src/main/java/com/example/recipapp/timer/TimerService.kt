package com.example.recipapp.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.recipapp.R
import com.example.recipapp.util.toTimeString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Foreground Service responsible for managing multiple cooking timers concurrently.
 * * Architecture Overview:
 * 1. UI Communication: Outwardly exposes timer states via a reactive StateFlow stream.
 * Accepts control commands (START, STOP, DISMISS) through static Intent factory helpers.
 * 2. Background Execution: Runs independent countdown loops on a background thread pool
 * using Coroutines with isolated Supervisor jobs.
 * 3. System Integration: Maintains a persistent foreground notification during active countdowns
 * and fires high-priority alerts with sound and hardware vibration upon completion.
 * 4. Resource Lifecycle: Automatically shuts down via stopSelf() when no active timers
 * or ringing alarms remain, preventing resource and memory leaks.
 */

class TimerService : Service() {
    // Delegate calculations to a background thread, isolate timer jobs from each other
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    // recipeId -> timer
    private val timerJobs    = mutableMapOf<Long, Job>()
    // for playing music when timer finished
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        // Actions for timer service
        const val ACTION_START   = "START_TIMER"
        const val ACTION_STOP    = "STOP_TIMER"
        const val ACTION_DISMISS = "DISMISS_ALARM"

        const val EXTRA_RECIPE_ID    = "recipeId"
        const val EXTRA_RECIPE_TITLE = "recipeTitle"
        const val EXTRA_DURATION_SEC = "durationSec"

        const val CHANNEL_TIMER = "timer_channel"
        const val CHANNEL_ALARM = "alarm_channel"
        const val NOTIF_ONGOING = 1000

        // Application state - UI can observe this
        private val _timers = MutableStateFlow<Map<Long, TimerState>>(emptyMap())
        val timers: StateFlow<Map<Long, TimerState>> = _timers.asStateFlow()

        // ********** Intent factory helpers **********
        fun startTimer(context: Context, recipeId: Long, recipeTitle: String, durationSec: Int) {
            context.startForegroundService(Intent(context, TimerService::class.java).apply {
                action = ACTION_START                                       // label the intent
                putExtra(EXTRA_RECIPE_ID,    recipeId)      // add extra info to the intent
                putExtra(EXTRA_RECIPE_TITLE, recipeTitle)
                putExtra(EXTRA_DURATION_SEC, durationSec)
            })
        }

        fun stopTimer(context: Context, recipeId: Long) {
            context.startService(Intent(context, TimerService::class.java).apply {
                action = ACTION_STOP
                putExtra(EXTRA_RECIPE_ID, recipeId)
            })
        }

        fun dismissAlarm(context: Context, recipeId: Long) {
            context.startService(Intent(context, TimerService::class.java).apply {
                action = ACTION_DISMISS
                putExtra(EXTRA_RECIPE_ID, recipeId)
            })
        }
    }

    // ********** Receives all intents (actions) **********
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannels()    // Every notification needs a channel

        // Action = start new timer
        if (intent?.action == ACTION_START) {
            val recipeId = intent.getLongExtra(EXTRA_RECIPE_ID, -1)
            val title       = intent.getStringExtra(EXTRA_RECIPE_TITLE) ?: "Recipe"
            val durationSec = intent.getIntExtra(EXTRA_DURATION_SEC, 0)
            if (recipeId != -1L && durationSec > 0) {
                startTimerFor(recipeId, title, durationSec)
            }
        }

        // We need a notification for a background task
        // Get existing or create new notification
        val initialNotification = buildOngoingNotification() ?: NotificationCompat.Builder(this, CHANNEL_TIMER)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("RecipRAT Timer")
            .setContentText("Starting countdown...")
            .setContentIntent(getOpenAppPendingIntent(-1))
            .setOngoing(true)
            .setSilent(true)
            .build()

        // safe start foreground service
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIF_ONGOING,
                    initialNotification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIF_ONGOING, initialNotification)
            }
        } catch (_: Exception) {
            // Failed to start notification
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent?.action) { // Action = cancel timer
            ACTION_STOP -> {
                val recipeId = intent.getLongExtra(EXTRA_RECIPE_ID, -1)
                if (recipeId != -1L) cancelTimerFor(recipeId)
            }
            ACTION_DISMISS -> { // Action = dismiss alarm
                val recipeId = intent.getLongExtra(EXTRA_RECIPE_ID, -1)
                if (recipeId != -1L) dismissAlarmFor(recipeId)
            }
        }

        return START_NOT_STICKY
    }


    // ********** Timer Core Logic **********
    private fun startTimerFor(recipeId: Long, title: String, durationSec: Int) {
        timerJobs[recipeId]?.cancel()   // Cancel any existing timer for this recipe

        updateTimer(recipeId, TimerState.Running(
            recipeId     = recipeId,
            recipeTitle  = title,
            totalSec     = durationSec,
            remainingSec = durationSec
        ))

        // Start a new timer job
        timerJobs[recipeId] = serviceScope.launch {
            var remaining = durationSec
            while (remaining > 0) { // Countdown
                delay(1000)
                remaining--
                updateTimer(recipeId, TimerState.Running(
                    recipeId     = recipeId,
                    recipeTitle  = title,
                    totalSec     = durationSec,
                    remainingSec = remaining
                ))
                updateOngoingNotification()
            }
            onTimerFinished(recipeId, title)
        }
    }

    private fun onTimerFinished(recipeId: Long, title: String) {
        timerJobs.remove(recipeId)
        updateTimer(recipeId, TimerState.Finished(recipeId, title))
        playAlarmSound()
        vibrateDevice()
        showAlarmNotification(recipeId, title)
        updateOngoingNotification()
    }

    private fun cancelTimerFor(recipeId: Long) {
        timerJobs[recipeId]?.cancel()
        timerJobs.remove(recipeId)
        removeTimer(recipeId)
        updateOngoingNotification()
        stopIfNoTimers()
    }

    private fun dismissAlarmFor(recipeId: Long) {
        stopAlarmSound()
        removeTimer(recipeId)
        getSystemService(NotificationManager::class.java).cancel(alarmNotifId(recipeId))
        updateOngoingNotification()
        stopIfNoTimers()
    }

    private fun stopIfNoTimers() {
        if (_timers.value.isEmpty()) {
            stopSelf()
        }
    }

    // ********** State management **********
    private fun updateTimer(recipeId: Long, state: TimerState) {
        _timers.value = _timers.value.toMutableMap().also { it[recipeId] = state }
    }

    private fun removeTimer(recipeId: Long) {
        _timers.value = _timers.value.toMutableMap().also { it.remove(recipeId) }
    }

    // ********** Alarm - sound and vibration **********
    private fun playAlarmSound() {
        stopAlarmSound()
        mediaPlayer = MediaPlayer.create(this, R.raw.timer_alarm)?.apply {
            isLooping = true
            start()
        }
    }

    private fun stopAlarmSound() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    private fun vibrateDevice() {
        val pattern = VibrationEffect.createWaveform(longArrayOf(0, 500, 300, 500, 300, 500), -1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator.vibrate(pattern)
        } else { // Legacy API
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java).vibrate(pattern)
        }
    }

    // ********** Notifications **********
    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)

        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_TIMER, "Active Timers", NotificationManager.IMPORTANCE_LOW)
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ALARM, "Timer Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null)
                enableVibration(false)
            }
        )
    }

    // When user clicks on notification, open app on the correct recipe
    private fun getOpenAppPendingIntent(recipeId: Long): PendingIntent {
        val intent = Intent()
            .setClassName(packageName, "com.example.recipapp.MainActivity")
            .apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (recipeId != -1L) {
                    putExtra("LAUNCH_RECIPE_ID", recipeId)
                }
            }

        return PendingIntent.getActivity(
            this,
            recipeId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildOngoingNotification(): android.app.Notification? {
        val running = _timers.value.values.filterIsInstance<TimerState.Running>()

        if (running.isEmpty()) return null

        val text = running.joinToString(" | ") {
            "${it.recipeTitle}: ${it.remainingSec.toTimeString()}"
        }

        return NotificationCompat.Builder(this, CHANNEL_TIMER)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("RecipRAT Timer")
            .setContentText(text)
            .setContentIntent(getOpenAppPendingIntent(-1))
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateOngoingNotification() {
        val notification = buildOngoingNotification()
        val nm = getSystemService(NotificationManager::class.java)

        if (notification == null) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIF_ONGOING, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIF_ONGOING, notification)
            }
            nm.notify(NOTIF_ONGOING, notification)
        }
    }

    private fun showAlarmNotification(recipeId: Long, title: String) {
        val dismissPending = PendingIntent.getService(
            this,
            recipeId.toInt(),
            Intent(this, TimerService::class.java).apply {
                action = ACTION_DISMISS
                putExtra(EXTRA_RECIPE_ID, recipeId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        getSystemService(NotificationManager::class.java).notify(
            alarmNotifId(recipeId),
            NotificationCompat.Builder(this, CHANNEL_ALARM)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("⏰ Time's up!")
                .setContentText("\"$title\" is ready!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(getOpenAppPendingIntent(recipeId))
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_delete, "Dismiss", dismissPending)
                .build()
        )
    }

    private fun alarmNotifId(recipeId: Long) = (2000 + recipeId).toInt()

    // ********** Lifecycle **********
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopAlarmSound()
        serviceScope.cancel()
        super.onDestroy()
    }
}

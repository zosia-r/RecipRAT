package com.example.recipapp.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ForegroundService – działa nawet gdy aplikacja jest w tle.
 * Zarządza timerami dla wielu przepisów jednocześnie (key = recipeId).
 *
 * Komunikacja z UI przez companion object (singleton flows) –
 * proste rozwiązanie bez potrzeby bindowania serwisu.
 */
class TimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Joby tickowania dla każdego przepisu – żeby móc je anulować
    private val timerJobs = mutableMapOf<Long, Job>()

    companion object {
        const val ACTION_START  = "START_TIMER"
        const val ACTION_STOP   = "STOP_TIMER"
        const val ACTION_DISMISS = "DISMISS_ALARM"

        const val EXTRA_RECIPE_ID    = "recipeId"
        const val EXTRA_RECIPE_TITLE = "recipeTitle"
        const val EXTRA_DURATION_SEC = "durationSec"

        const val CHANNEL_TIMER   = "timer_channel"
        const val CHANNEL_ALARM   = "alarm_channel"
        const val NOTIF_ONGOING   = 1000  // powiadomienie z odliczaniem

        // Stany timerów dostępne dla całej aplikacji (singleton)
        private val _timers = MutableStateFlow<Map<Long, TimerState>>(emptyMap())
        val timers: StateFlow<Map<Long, TimerState>> = _timers.asStateFlow()

        fun startTimer(context: Context, recipeId: Long, recipeTitle: String, durationSec: Int) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RECIPE_ID,    recipeId)
                putExtra(EXTRA_RECIPE_TITLE, recipeTitle)
                putExtra(EXTRA_DURATION_SEC, durationSec)
            }
            context.startForegroundService(intent)
        }

        fun stopTimer(context: Context, recipeId: Long) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_STOP
                putExtra(EXTRA_RECIPE_ID, recipeId)
            }
            context.startService(intent)
        }

        fun dismissAlarm(context: Context, recipeId: Long) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_DISMISS
                putExtra(EXTRA_RECIPE_ID, recipeId)
            }
            context.startService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannels()

        // Serwis musi mieć aktywne powiadomienie zaraz po starcie
        startForeground(NOTIF_ONGOING, buildOngoingNotification())

        when (intent?.action) {
            ACTION_START -> {
                val recipeId    = intent.getLongExtra(EXTRA_RECIPE_ID, -1)
                val title       = intent.getStringExtra(EXTRA_RECIPE_TITLE) ?: "Recipe"
                val durationSec = intent.getIntExtra(EXTRA_DURATION_SEC, 0)
                if (recipeId != -1L && durationSec > 0) {
                    startTimerFor(recipeId, title, durationSec)
                }
            }
            ACTION_STOP -> {
                val recipeId = intent.getLongExtra(EXTRA_RECIPE_ID, -1)
                if (recipeId != -1L) cancelTimerFor(recipeId)
            }
            ACTION_DISMISS -> {
                val recipeId = intent.getLongExtra(EXTRA_RECIPE_ID, -1)
                if (recipeId != -1L) dismissAlarmFor(recipeId)
            }
        }

        return START_STICKY
    }

    private fun startTimerFor(recipeId: Long, title: String, durationSec: Int) {
        // Anuluj poprzedni timer dla tego przepisu jeśli istnieje
        timerJobs[recipeId]?.cancel()

        // Ustaw stan początkowy
        updateTimer(recipeId, TimerState.Running(
            recipeId    = recipeId,
            recipeTitle = title,
            totalSec    = durationSec,
            remainingSec = durationSec
        ))

        timerJobs[recipeId] = serviceScope.launch {
            var remaining = durationSec
            while (remaining > 0) {
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
            // Timer dobiegł końca
            onTimerFinished(recipeId, title)
        }
    }

    private fun onTimerFinished(recipeId: Long, title: String) {
        updateTimer(recipeId, TimerState.Finished(recipeId, title))
        playAlarmNotification(recipeId, title)
    }

    private fun cancelTimerFor(recipeId: Long) {
        timerJobs[recipeId]?.cancel()
        timerJobs.remove(recipeId)
        removeTimer(recipeId)
        updateOngoingNotification()
        stopIfNoTimers()
    }

    private fun dismissAlarmFor(recipeId: Long) {
        removeTimer(recipeId)
        val nm = getSystemService(NotificationManager::class.java)
        nm.cancel(alarmNotifId(recipeId))
        stopIfNoTimers()
    }

    private fun stopIfNoTimers() {
        if (_timers.value.isEmpty()) stopSelf()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun updateTimer(recipeId: Long, state: TimerState) {
        _timers.value = _timers.value.toMutableMap().also { it[recipeId] = state }
    }

    private fun removeTimer(recipeId: Long) {
        _timers.value = _timers.value.toMutableMap().also { it.remove(recipeId) }
    }

    private fun alarmNotifId(recipeId: Long) = (2000 + recipeId).toInt()

    // ── Powiadomienia ────────────────────────────────────────────────────────

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)

        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_TIMER, "Cooking Timers",
            NotificationManager.IMPORTANCE_LOW   // cichy, bez dźwięku
        ))

        val alarmChannel = NotificationChannel(
            CHANNEL_ALARM, "Timer Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), attrs)
            enableVibration(true)
        }
        nm.createNotificationChannel(alarmChannel)
    }

    private fun buildOngoingNotification(): android.app.Notification {
        val running = _timers.value.values.filterIsInstance<TimerState.Running>()
        val text = if (running.isEmpty()) "Timer active"
        else running.joinToString(" | ") { "${it.recipeTitle}: ${it.remainingSec.toTimeString()}" }

        return NotificationCompat.Builder(this, CHANNEL_TIMER)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("RecipRAT Timer")
            .setContentText(text)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateOngoingNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ONGOING, buildOngoingNotification())
    }

    private fun playAlarmNotification(recipeId: Long, title: String) {
        // Intent do wylączenia alarmu przez powiadomienie
        val dismissIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_DISMISS
            putExtra(EXTRA_RECIPE_ID, recipeId)
        }
        val dismissPending = PendingIntent.getService(
            this, recipeId.toInt(), dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ALARM)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ Timer finished!")
            .setContentText("$title is ready!")
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_delete, "Dismiss", dismissPending)
            .build()

        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(alarmNotifId(recipeId), notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}

/** Formatuje sekundy jako MM:SS */
fun Int.toTimeString(): String {
    val m = this / 60
    val s = this % 60
    return "%02d:%02d".format(m, s)
}
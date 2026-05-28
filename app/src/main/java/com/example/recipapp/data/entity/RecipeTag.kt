package com.example.recipapp.data.entity

/**
 * Predefined tags for recipes.
 */

enum class RecipeTag(val label: String, val category: TagCategory) {

    // Meal type
    BREAKFAST("🍳 Breakfast",   TagCategory.MEAL_TYPE),
    LUNCH("🥗 Lunch",           TagCategory.MEAL_TYPE),
    DINNER("🍽️ Dinner",        TagCategory.MEAL_TYPE),
    SNACK("🍎 Snack",           TagCategory.MEAL_TYPE),
    DESSERT("🍰 Dessert",       TagCategory.MEAL_TYPE),
    DRINK("🥤 Drink",           TagCategory.MEAL_TYPE),

    // Taste
    SWEET("🍬 Sweet",           TagCategory.TASTE),
    SAVORY("🧂 Savory",         TagCategory.TASTE),
    SPICY("🌶️ Spicy",          TagCategory.TASTE),
    SOUR("🍋 Sour",             TagCategory.TASTE),

    // Prep time
    QUICK("⚡ Under 15 min",    TagCategory.PREP_TIME),
    MEDIUM("🕐 15–45 min",      TagCategory.PREP_TIME),
    LONG("⏳ Over 45 min",      TagCategory.PREP_TIME),

    // Diet
    VEGETARIAN("🥦 Vegetarian", TagCategory.DIET),
    VEGAN("🌱 Vegan",           TagCategory.DIET),
    GLUTEN_FREE("🌾 Gluten-free", TagCategory.DIET),
    DAIRY_FREE("🥛 Dairy-free", TagCategory.DIET),
}

enum class TagCategory(val label: String) {
    MEAL_TYPE("Meal type"),
    TASTE("Taste"),
    PREP_TIME("Prep time"),
    DIET("Diet"),
}
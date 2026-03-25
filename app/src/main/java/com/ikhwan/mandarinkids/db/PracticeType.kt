package com.ikhwan.mandarinkids.db

enum class PracticeType(val displayName: String, val emoji: String) {
    /** Shows the Chinese character and auto-plays audio. */
    DEFAULT("Default", "🔊字"),
    /** Auto-plays audio only — character hidden until answered. */
    LISTENING("Listening", "🔊"),
    /** Shows the Chinese character only — no audio auto-play. */
    READING("Reading", "字")
}

package com.ikhwan.mandarinkids.practice

enum class PracticeMode {
    /** All words — lower mastery levels appear more frequently. */
    ALL,
    /** Only the 3 lowest mastery levels currently present in the library. */
    WEAK,
    /** Only the 3 highest mastery levels that are >= ★4. */
    MASTERY
}

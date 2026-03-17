package com.ikhwan.mandarinkids

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressManagerTest {

    // ── calculateStars ────────────────────────────────────────────────────

    @Test
    fun calculateStars_perfectScore_returns3() {
        assertEquals(3, ProgressManager.calculateStars(3, 3))
    }

    @Test
    fun calculateStars_allCorrect_returns3() {
        assertEquals(3, ProgressManager.calculateStars(10, 10))
    }

    @Test
    fun calculateStars_above70Percent_returns2() {
        assertEquals(2, ProgressManager.calculateStars(8, 10))
    }

    @Test
    fun calculateStars_exactly70Percent_returns2() {
        // 7/10 = 70.0 % → threshold is inclusive
        assertEquals(2, ProgressManager.calculateStars(7, 10))
    }

    @Test
    fun calculateStars_below70Percent_returns1() {
        assertEquals(1, ProgressManager.calculateStars(6, 10))
    }

    @Test
    fun calculateStars_zeroScore_returns1() {
        assertEquals(1, ProgressManager.calculateStars(0, 5))
    }

    @Test
    fun calculateStars_oneOfThree_returns1() {
        // 1/3 ≈ 33 % → below 70
        assertEquals(1, ProgressManager.calculateStars(1, 3))
    }

    @Test
    fun calculateStars_twoOfThree_returns2() {
        // 2/3 ≈ 66.7 % → below 70
        assertEquals(1, ProgressManager.calculateStars(2, 3))
    }

    // ── getLevel ──────────────────────────────────────────────────────────

    @Test
    fun getLevel_atThreshold180_returnsTopLevel() {
        assertEquals("中文小明星", ProgressManager.getLevel(180))
    }

    @Test
    fun getLevel_above180_returnsTopLevel() {
        assertEquals("中文小明星", ProgressManager.getLevel(500))
    }

    @Test
    fun getLevel_atThreshold60_returnsMidLevel() {
        assertEquals("小达人", ProgressManager.getLevel(60))
    }

    @Test
    fun getLevel_between60and179_returnsMidLevel() {
        assertEquals("小达人", ProgressManager.getLevel(120))
    }

    @Test
    fun getLevel_justBelow60_returnsBeginner() {
        assertEquals("初学者", ProgressManager.getLevel(59))
    }

    @Test
    fun getLevel_zero_returnsBeginner() {
        assertEquals("初学者", ProgressManager.getLevel(0))
    }

    // ── getLevelLabel ─────────────────────────────────────────────────────

    @Test
    fun getLevelLabel_highXp_containsMandarinStar() {
        assertTrue(ProgressManager.getLevelLabel(200).contains("Mandarin Star"))
    }

    @Test
    fun getLevelLabel_midXp_containsJuniorExpert() {
        assertTrue(ProgressManager.getLevelLabel(100).contains("Junior Expert"))
    }

    @Test
    fun getLevelLabel_lowXp_containsBeginner() {
        assertTrue(ProgressManager.getLevelLabel(10).contains("Beginner"))
    }

    @Test
    fun getLevelLabel_boundaryXp_matchesGetLevel() {
        // getLevelLabel and getLevel must agree at every boundary
        listOf(0, 59, 60, 179, 180, 300).forEach { xp ->
            val level = ProgressManager.getLevel(xp)
            val label = ProgressManager.getLevelLabel(xp)
            assertTrue(
                "getLevel($xp)=$level has no matching label in getLevelLabel($xp)=$label",
                when (level) {
                    "中文小明星" -> label.contains("Mandarin Star")
                    "小达人"    -> label.contains("Junior Expert")
                    else        -> label.contains("Beginner")
                }
            )
        }
    }
}

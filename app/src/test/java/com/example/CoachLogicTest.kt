package com.example

import com.example.ui.CoachVitals
import com.example.ui.offlineCoachReply
import com.example.ui.remedySteps
import com.example.ui.screens.sessionResultText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachLogicTest {
    @Test
    fun remedySplitsIntoStepsButKeepsParenthesesTogether() {
        assertEquals(
            listOf(
                "Sit down comfortably",
                "Practice slow 4-7-8 breathing (inhale 4s, hold 7s, exhale 8s)",
                "Drink a glass of cool water",
                "Avoid caffeine"
            ),
            remedySteps("Sit down comfortably, practice slow 4-7-8 breathing (inhale 4s, hold 7s, exhale 8s), drink a glass of cool water, and avoid caffeine.")
        )
        assertEquals(3, remedySteps("Limit exceeded. Ensure 3.3V airflow, and verify rails.").size)
    }

    @Test
    fun offlineReplyAnswersTheTopicAsked() {
        val v = CoachVitals(live = true, heartRate = 112, spo2 = 97, temperature = 36.8, gsr = 4.5, steps = 1200)
        assertTrue(offlineCoachReply("why is my heart rate high?", v).contains("112 BPM"))
        assertTrue(offlineCoachReply("I feel stressed", v).contains("elevated stress"))
        assertTrue(offlineCoachReply("what should I eat", v).contains("Morning"))
        // "great" must not trigger the food answer via "eat"
        assertTrue(!offlineCoachReply("I feel great today", v).contains("Morning"))
    }

    @Test
    fun sessionResultReportsTheMeasuredChange() {
        assertTrue(sessionResultText(88, 76, 300).contains("dropped by 12 BPM (88 → 76)"))
        assertTrue(sessionResultText(70, 75, 300).contains("rose by 5 BPM"))
        assertTrue(sessionResultText(null, 75, 61).contains("couldn't be measured"))
        assertTrue(sessionResultText(72, 72, 61).startsWith("You practiced for 1:01."))
    }
}

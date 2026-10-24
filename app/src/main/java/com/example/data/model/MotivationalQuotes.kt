package com.example.data.model

import java.time.LocalDate

data class MotivationalQuote(
    val id: Int,
    val quote: String,
    val author: String,
    val category: String = "Daily Wisdom"
)

object MotivationalQuotesRepository {
    val quotes: List<MotivationalQuote> = listOf(
        MotivationalQuote(1, "Small daily efforts compound into lifelong excellence.", "Consistency"),
        MotivationalQuote(2, "Consistency is the quiet secret of true achievers.", "Discipline"),
        MotivationalQuote(3, "Success begins with showing up, every single day.", "Presence"),
        MotivationalQuote(4, "Focus on progress, not perfection. Every session matters.", "Growth"),
        MotivationalQuote(5, "Dedication in the classroom transforms into mastery in the industry.", "Excellence"),
        MotivationalQuote(6, "The expert in anything was once a beginner who showed up.", "Perseverance"),
        MotivationalQuote(7, "Your future is crafted by what you learn and practice today.", "Purpose"),
        MotivationalQuote(8, "Discipline is choosing between what you want now and what you want most.", "Focus"),
        MotivationalQuote(9, "Continuous presence and curiosity unlock extraordinary potential.", "Dedication"),
        MotivationalQuote(10, "Every thread in the loom counts; every lecture builds your craft.", "Textile Wisdom"),
        MotivationalQuote(11, "Patience, precision, and presence turn challenges into triumph.", "Resilience"),
        MotivationalQuote(12, "Excellence is not a singular act, but a relentless daily habit.", "Aristotle"),
        MotivationalQuote(13, "Show up with passion, learn with purpose, and lead with character.", "Inspiration"),
        MotivationalQuote(14, "One hour of focused learning today saves weeks of regret tomorrow.", "Mindset"),
        MotivationalQuote(15, "Knowledge is the fabric that we weave through daily dedication.", "Craftsmanship"),
        MotivationalQuote(16, "Stay committed to your craft; greatness is built one day at a time.", "Persistence"),
        MotivationalQuote(17, "When you show up consistently, momentum takes care of the rest.", "Momentum"),
        MotivationalQuote(18, "Mastery begins with being present and fully engaged.", "Focus"),
        MotivationalQuote(19, "A disciplined student today is an industry leader tomorrow.", "Aspiration"),
        MotivationalQuote(20, "Every experiment in the lab brings you closer to discovery.", "Innovation")
    )

    fun getDailyQuote(): MotivationalQuote {
        val dayOfYear = try {
            LocalDate.now().dayOfYear
        } catch (_: Exception) {
            java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        }
        val index = (dayOfYear - 1).coerceAtLeast(0) % quotes.size
        return quotes[index]
    }

    fun getQuoteByIndex(index: Int): MotivationalQuote {
        val safeIndex = Math.floorMod(index, quotes.size)
        return quotes[safeIndex]
    }
}

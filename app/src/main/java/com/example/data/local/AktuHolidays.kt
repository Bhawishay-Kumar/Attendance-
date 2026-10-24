package com.example.data.local

/**
 * AKTU Official University Holiday model.
 * Based on official circular from Registrar (Dr. A.P.J. Abdul Kalam Technical University, Lucknow).
 * Notification: ए०के०टी०यू०/कुस०का०/स्था०/2025/9743
 */
data class AktuHoliday(
    val date: String, // "YYYY-MM-DD"
    val name: String,
    val description: String = "AKTU Official Holiday",
    val dayOfWeek: String = ""
)

object AktuHolidays {
    /**
     * Official Public Holidays (सार्वजनिक अवकाश) that can be taken without permission.
     * Excludes commercial bank annual closing (Note 2: only for banks/treasuries).
     * Excludes restricted holidays (Note 5: requires prior employee selection/permission).
     */
    val OFFICIAL_HOLIDAYS = listOf(
        // ==================== Year 2026 (Official Circular) ====================
        AktuHoliday("2026-01-03", "Hazrat Ali Jayanti (मो० हजरत अली जन्म दिवस)", "Public Holiday", "Saturday"),
        AktuHoliday("2026-01-26", "Republic Day (गणतंत्र दिवस)", "National Holiday", "Monday"),
        AktuHoliday("2026-02-15", "Maha Shivratri (महाशिवरात्रि)", "Public Holiday", "Sunday"),
        AktuHoliday("2026-03-02", "Holika Dahan (होलिका दहन)", "Public Holiday", "Monday"),
        AktuHoliday("2026-03-04", "Holi (होली)", "Public Holiday", "Wednesday"),
        AktuHoliday("2026-03-05", "Holi - Second Day (होली)", "Local Public Holiday", "Thursday"),
        AktuHoliday("2026-03-11", "Sheetla Ashtami (शीतलाष्टमी)", "Local Public Holiday", "Wednesday"),
        AktuHoliday("2026-03-21", "Eid-ul-Fitr (ईद-उल-फितर)", "Public Holiday (Moon Sighting)", "Saturday"),
        AktuHoliday("2026-03-26", "Ram Navami (राम नवमी)", "Public Holiday", "Thursday"),
        AktuHoliday("2026-03-31", "Mahavir Jayanti (महावीर जयंती)", "Public Holiday", "Tuesday"),
        AktuHoliday("2026-04-03", "Good Friday (गुड फ्राइडे)", "Public Holiday", "Friday"),
        AktuHoliday("2026-04-14", "Dr. B.R. Ambedkar Jayanti (डा० भीमराव अम्बेडकर जन्म दिवस)", "Public Holiday", "Tuesday"),
        AktuHoliday("2026-05-01", "Buddha Purnima (बुद्ध पूर्णिमा)", "Public Holiday", "Friday"),
        AktuHoliday("2026-05-05", "Mahaveer Ji Mela / Bada Mangal (महावीर जी का मेला - प्रथम बड़ा मंगल)", "Local Public Holiday", "Tuesday"),
        AktuHoliday("2026-05-27", "Bakrid / Eid-ul-Adha (ईदुज्जुहा - बकरीद)", "Public Holiday (Moon Sighting)", "Wednesday"),
        AktuHoliday("2026-06-26", "Muharram (मोहर्रम)", "Public Holiday (Moon Sighting)", "Thursday"),
        AktuHoliday("2026-08-15", "Independence Day (स्वतंत्रता दिवस)", "National Holiday", "Saturday"),
        AktuHoliday("2026-08-26", "Eid-e-Milad / Barawafat (ईद-ए-मिलाद / बारावफात)", "Public Holiday (Moon Sighting)", "Wednesday"),
        AktuHoliday("2026-08-28", "Raksha Bandhan (रक्षा बंधन)", "Public Holiday", "Friday"),
        AktuHoliday("2026-09-04", "Janmashtami (जन्माष्टमी)", "Public Holiday", "Friday"),
        AktuHoliday("2026-10-02", "Mahatma Gandhi Jayanti (महात्मा गांधी जयन्ती)", "National Holiday", "Friday"),
        AktuHoliday("2026-10-20", "Dussehra / Mahanavami / Vijayadashami (दशहरा महानवमी / विजयदशमी)", "Public Holiday", "Tuesday"),
        AktuHoliday("2026-11-08", "Diwali / Deepawali (दीपावली)", "Public Holiday", "Sunday"),
        AktuHoliday("2026-11-09", "Govardhan Puja (गोवर्धन पूजा)", "Public Holiday", "Monday"),
        AktuHoliday("2026-11-11", "Bhai Dooj / Chitragupta Jayanti (भैयादूज / चित्रगुप्त जयन्ती)", "Public Holiday", "Wednesday"),
        AktuHoliday("2026-11-24", "Guru Nanak Jayanti / Kartik Purnima (गुरू नानक जयंती / कार्तिक पूर्णिमा)", "Public Holiday", "Tuesday"),
        AktuHoliday("2026-12-25", "Christmas Day (क्रिसमस-डे)", "Public Holiday", "Friday"),

        // ==================== Year 2025 ====================
        AktuHoliday("2025-01-26", "Republic Day", "National Holiday", "Sunday"),
        AktuHoliday("2025-02-26", "Maha Shivratri", "Public Holiday", "Wednesday"),
        AktuHoliday("2025-03-14", "Holi", "Public Holiday", "Friday"),
        AktuHoliday("2025-03-15", "Holi (Second Day)", "Public Holiday", "Saturday"),
        AktuHoliday("2025-03-31", "Eid-ul-Fitr", "Public Holiday", "Monday"),
        AktuHoliday("2025-04-06", "Ram Navami", "Public Holiday", "Sunday"),
        AktuHoliday("2025-04-10", "Mahavir Jayanti", "Public Holiday", "Thursday"),
        AktuHoliday("2025-04-14", "Dr. B.R. Ambedkar Jayanti", "Public Holiday", "Monday"),
        AktuHoliday("2025-04-18", "Good Friday", "Public Holiday", "Friday"),
        AktuHoliday("2025-05-12", "Buddha Purnima", "Public Holiday", "Monday"),
        AktuHoliday("2025-06-07", "Bakrid / Eid-ul-Adha", "Public Holiday", "Saturday"),
        AktuHoliday("2025-07-06", "Muharram", "Public Holiday", "Sunday"),
        AktuHoliday("2025-08-15", "Independence Day", "National Holiday", "Friday"),
        AktuHoliday("2025-08-16", "Janmashtami", "Public Holiday", "Saturday"),
        AktuHoliday("2025-09-05", "Eid-e-Milad", "Public Holiday", "Friday"),
        AktuHoliday("2025-10-02", "Mahatma Gandhi Jayanti", "National Holiday", "Thursday"),
        AktuHoliday("2025-10-20", "Diwali / Deepawali", "Public Holiday", "Monday"),
        AktuHoliday("2025-10-21", "Govardhan Puja", "Public Holiday", "Tuesday"),
        AktuHoliday("2025-10-22", "Bhai Dooj", "Public Holiday", "Wednesday"),
        AktuHoliday("2025-11-05", "Guru Nanak Jayanti", "Public Holiday", "Wednesday"),
        AktuHoliday("2025-12-25", "Christmas Day", "Public Holiday", "Thursday")
    )

    fun getHoliday(dateStr: String): AktuHoliday? {
        return OFFICIAL_HOLIDAYS.find { it.date == dateStr }
    }

    fun isOfficialHoliday(dateStr: String): Boolean {
        return getHoliday(dateStr) != null
    }

    fun getHolidayName(dateStr: String): String? {
        return getHoliday(dateStr)?.name
    }

    fun getHolidaysForYear(year: Int): List<AktuHoliday> {
        return OFFICIAL_HOLIDAYS.filter { it.date.startsWith("$year-") }
    }
}

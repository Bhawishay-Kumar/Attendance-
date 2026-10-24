package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Attendance", appName)
  }

  @Test
  fun `verify motivational quotes and daily quote retrieval`() {
    val quote = com.example.data.model.MotivationalQuotesRepository.getDailyQuote()
    org.junit.Assert.assertNotNull(quote)
    org.junit.Assert.assertTrue(quote.quote.isNotBlank())
  }

  @Test
  fun `verify Developer Info resources exist`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val fontRes = context.resources.getIdentifier("caveat", "font", context.packageName)
    org.junit.Assert.assertNotEquals(0, fontRes)

    val logoRes = context.resources.getIdentifier("ic_bk_monogram_logo", "drawable", context.packageName)
    org.junit.Assert.assertNotEquals(0, logoRes)
  }

  @Test
  fun `verify notification models and categories`() {
    val category = com.example.data.model.NotificationCategory.ATTENDANCE_REMINDER
    assertEquals("Attendance Reminder", category.displayName)

    val item = com.example.data.model.NotificationItem(
      notificationKey = "test_key",
      title = "Test Reminder",
      message = "Test Message",
      category = category.name,
      actionType = com.example.data.model.NotificationActionType.NAVIGATE_DAILY.name
    )
    assertEquals("test_key", item.notificationKey)
    org.junit.Assert.assertFalse(item.isRead)

    val prefs = com.example.data.model.NotificationPreferences()
    org.junit.Assert.assertTrue(prefs.masterEnabled)
    org.junit.Assert.assertTrue(prefs.markRemindersEnabled)
  }

  @Test
  fun `verify attendance viewmodel initialization without null pointer exception`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    if (com.google.firebase.FirebaseApp.getApps(app).isEmpty()) {
      com.google.firebase.FirebaseApp.initializeApp(
        app,
        com.google.firebase.FirebaseOptions.Builder()
          .setApplicationId(app.packageName)
          .setProjectId("attendancetracker-app")
          .setApiKey("fake-key")
          .build()
      )
    }
    val vm = com.example.ui.viewmodel.AttendanceViewModel(app)
    org.junit.Assert.assertNotNull(vm.overallStats.value)
    org.junit.Assert.assertNotNull(vm.profile.value)
    org.junit.Assert.assertNotNull(vm.allAttendanceRecords.value)
    org.junit.Assert.assertNotNull(vm.currentMotivationalQuote.value)
    org.junit.Assert.assertEquals("A", vm.profile.value.batch)
  }

  @Test
  fun `verify notification badge formatting for zero single double and 100+ counts`() {
    fun formatBadge(count: Int): String? = when {
      count <= 0 -> null
      count <= 99 -> "$count"
      else -> "99+"
    }

    org.junit.Assert.assertNull(formatBadge(0))
    org.junit.Assert.assertNull(formatBadge(-1))
    assertEquals("1", formatBadge(1))
    assertEquals("9", formatBadge(9))
    assertEquals("10", formatBadge(10))
    assertEquals("42", formatBadge(42))
    assertEquals("99", formatBadge(99))
    assertEquals("99+", formatBadge(100))
    assertEquals("99+", formatBadge(250))
  }
}


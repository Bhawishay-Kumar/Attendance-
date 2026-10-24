package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

object HapticManager {
    private const val PREFS_NAME = "iiht_haptic_settings"
    private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
    private const val KEY_HAPTIC_STRENGTH = "haptic_strength"
    private const val KEY_PARTICLES_ENABLED = "particles_enabled"

    private val _hapticsEnabled = mutableStateOf(true)
    val hapticsEnabled: State<Boolean> = _hapticsEnabled

    private val _hapticStrength = mutableStateOf("medium")
    val hapticStrength: State<String> = _hapticStrength

    private val _particlesEnabled = mutableStateOf(true)
    val particlesEnabled: State<Boolean> = _particlesEnabled

    private var vibrator: Vibrator? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        val app = context.applicationContext
        appContext = app
        val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _hapticsEnabled.value = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        _hapticStrength.value = prefs.getString(KEY_HAPTIC_STRENGTH, "medium") ?: "medium"
        _particlesEnabled.value = prefs.getBoolean(KEY_PARTICLES_ENABLED, true)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()
            ?.putBoolean(KEY_HAPTICS_ENABLED, enabled)?.apply()
    }

    fun setHapticStrength(strength: String) {
        _hapticStrength.value = strength
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()
            ?.putString(KEY_HAPTIC_STRENGTH, strength)?.apply()
    }

    fun setParticlesEnabled(enabled: Boolean) {
        _particlesEnabled.value = enabled
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()
            ?.putBoolean(KEY_PARTICLES_ENABLED, enabled)?.apply()
    }

    private fun getMultiplier(): Float {
        return when (_hapticStrength.value.lowercase()) {
            "light" -> 0.6f
            "strong" -> 1.7f
            else -> 1.0f
        }
    }

    private fun isSystemHapticsEnabled(): Boolean {
        val ctx = appContext ?: return true
        return try {
            Settings.System.getInt(ctx.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0
        } catch (_: Exception) {
            true
        }
    }

    fun tap() {
        vibrateSingle(8)
    }

    fun select() {
        vibrateSingle(6)
    }

    fun heavy() {
        vibrateSingle(22)
    }

    fun success() {
        // Pattern: 10ms on, 40ms off, 14ms on
        vibratePattern(longArrayOf(0, 10, 40, 14), intArrayOf(0, 200, 0, 255))
    }

    fun warn() {
        // Pattern: 28ms on, 60ms off, 28ms on
        vibratePattern(longArrayOf(0, 28, 60, 28), intArrayOf(0, 220, 0, 255))
    }

    private fun vibrateSingle(durationMs: Long) {
        if (!_hapticsEnabled.value || !isSystemHapticsEnabled()) return
        val v = vibrator ?: return
        val scaled = (durationMs * getMultiplier()).toLong().coerceAtLeast(1L)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = ((180 * getMultiplier()).toInt()).coerceIn(1, 255)
                v.vibrate(VibrationEffect.createOneShot(scaled, amplitude))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(scaled)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(timings: LongArray, amplitudes: IntArray) {
        if (!_hapticsEnabled.value || !isSystemHapticsEnabled()) return
        val v = vibrator ?: return
        val m = getMultiplier()
        val scaledTimings = LongArray(timings.size) { i ->
            if (i % 2 == 1) (timings[i] * m).toLong().coerceAtLeast(1L) else timings[i]
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val scaledAmplitudes = IntArray(amplitudes.size) { i ->
                    ((amplitudes[i] * m).toInt()).coerceIn(0, 255)
                }
                v.vibrate(VibrationEffect.createWaveform(scaledTimings, scaledAmplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(scaledTimings, -1)
            }
        } catch (_: Exception) {}
    }
}

@Composable
fun rememberHaptic(): HapticManager {
    val context = LocalContext.current
    return remember {
        HapticManager.init(context)
        HapticManager
    }
}

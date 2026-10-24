package com.example.ui.screens.whatif

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object WhatIfStorage {
    private const val PREFS_NAME = "what_if_scenarios_store"
    private const val KEY_SCENARIOS = "saved_scenarios_json"

    fun loadScenarios(context: Context): List<WhatIfSavedScenario> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_SCENARIOS, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<WhatIfSavedScenario>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val dateRangeText = obj.getString("dateRangeText")
                val datesArray = obj.getJSONArray("dates")
                val dates = mutableListOf<String>()
                for (j in 0 until datesArray.length()) {
                    dates.add(datesArray.getString(j))
                }
                val mode = if (obj.getString("mode") == "SELECTED_CLASSES") {
                    WhatIfPlanningMode.SELECTED_CLASSES
                } else {
                    WhatIfPlanningMode.WHOLE_DAY
                }
                val missedCount = obj.getInt("missedCount")
                val predictedPct = obj.getDouble("predictedPct").toFloat()
                val targetPct = obj.getInt("targetPct")
                val kindSummary = obj.optString("kindSummary", "Leave")

                val dayKinds = mutableMapOf<String, String>()
                val dayKindsObj = obj.optJSONObject("dayKinds")
                if (dayKindsObj != null) {
                    val keys = dayKindsObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        dayKinds[k] = dayKindsObj.getString(k)
                    }
                }

                val dayPicks = mutableMapOf<String, List<Long>>()
                val dayPicksObj = obj.optJSONObject("dayPicks")
                if (dayPicksObj != null) {
                    val keys = dayPicksObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val arr = dayPicksObj.getJSONArray(k)
                        val slotList = mutableListOf<Long>()
                        for (p in 0 until arr.length()) {
                            slotList.add(arr.getLong(p))
                        }
                        dayPicks[k] = slotList
                    }
                }

                list.add(
                    WhatIfSavedScenario(
                        id = id,
                        name = name,
                        dateRangeText = dateRangeText,
                        dates = dates,
                        mode = mode,
                        missedCount = missedCount,
                        predictedPct = predictedPct,
                        targetPct = targetPct,
                        kindSummary = kindSummary,
                        dayKinds = dayKinds,
                        dayPicks = dayPicks
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveScenarios(context: Context, list: List<WhatIfSavedScenario>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val jsonArray = JSONArray()
            for (item in list) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("name", item.name)
                obj.put("dateRangeText", item.dateRangeText)
                val datesArray = JSONArray()
                item.dates.forEach { datesArray.put(it) }
                obj.put("dates", datesArray)
                obj.put("mode", item.mode.name)
                obj.put("missedCount", item.missedCount)
                obj.put("predictedPct", item.predictedPct.toDouble())
                obj.put("targetPct", item.targetPct)
                obj.put("kindSummary", item.kindSummary)

                val dayKindsObj = JSONObject()
                item.dayKinds.forEach { (k, v) -> dayKindsObj.put(k, v) }
                obj.put("dayKinds", dayKindsObj)

                val dayPicksObj = JSONObject()
                item.dayPicks.forEach { (k, v) ->
                    val arr = JSONArray()
                    v.forEach { arr.put(it) }
                    dayPicksObj.put(k, arr)
                }
                obj.put("dayPicks", dayPicksObj)

                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_SCENARIOS, jsonArray.toString()).apply()
        } catch (_: Exception) {
        }
    }
}

package com.nia.assistant

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class NiaRoutine(
    val id: Long,
    var name: String,
    var actions: MutableList<String>,
    var enabled: Boolean = true,
    var scheduleTime: String? = null,
    var repeatDays: MutableList<Int> = mutableListOf(),
    var lastStatus: String = "Not Run",
    var lastRunTime: Long = 0L
)

object RoutineManager {

    private const val PREF_NAME = "nia_routines"
    private const val KEY_ROUTINES = "routines"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getAll(context: Context): MutableList<NiaRoutine> {
        val result = mutableListOf<NiaRoutine>()

        val raw = prefs(context).getString(KEY_ROUTINES, null)
            ?: return result

        try {
            val array = JSONArray(raw)

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)

                val actions = mutableListOf<String>()
                val actionArray = obj.optJSONArray("actions")

                if (actionArray != null) {
                    for (j in 0 until actionArray.length()) {
                        actions.add(actionArray.optString(j))
                    }
                }

                val days = mutableListOf<Int>()
                val dayArray = obj.optJSONArray("repeatDays")

                if (dayArray != null) {
                    for (j in 0 until dayArray.length()) {
                        days.add(dayArray.optInt(j))
                    }
                }

                result.add(
                    NiaRoutine(
                        id = obj.optLong("id"),
                        name = obj.optString("name"),
                        actions = actions,
                        enabled = obj.optBoolean("enabled", true),
                        scheduleTime = obj.optString(
                            "scheduleTime",
                            ""
                        ).ifBlank { null },
                        repeatDays = days,
                        lastStatus = obj.optString(
                            "lastStatus",
                            "Not Run"
                        ),
                        lastRunTime = obj.optLong(
                            "lastRunTime",
                            0L
                        )
                    )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }

    fun save(
        context: Context,
        routine: NiaRoutine
    ) {
        val routines = getAll(context)

        val index = routines.indexOfFirst {
            it.id == routine.id
        }

        if (index >= 0) {
            routines[index] = routine
        } else {
            routines.add(routine)
        }

        saveAll(context, routines)
    }

    fun create(
        context: Context,
        name: String,
        actions: List<String>,
        scheduleTime: String? = null,
        repeatDays: List<Int> = emptyList()
    ): NiaRoutine {

        val routine = NiaRoutine(
            id = System.currentTimeMillis(),
            name = name,
            actions = actions.toMutableList(),
            enabled = true,
            scheduleTime = scheduleTime,
            repeatDays = repeatDays.toMutableList()
        )

        save(context, routine)

        return routine
    }

    fun delete(
        context: Context,
        routineId: Long
    ) {
        val routines = getAll(context)

        routines.removeAll {
            it.id == routineId
        }

        saveAll(context, routines)
    }

    fun setEnabled(
        context: Context,
        routineId: Long,
        enabled: Boolean
    ) {
        val routines = getAll(context)

        routines.find {
            it.id == routineId
        }?.let {
            it.enabled = enabled
        }

        saveAll(context, routines)
    }

    fun updateStatus(
        context: Context,
        routineId: Long,
        status: String
    ) {
        val routines = getAll(context)

        routines.find {
            it.id == routineId
        }?.let {
            it.lastStatus = status
            it.lastRunTime = System.currentTimeMillis()
        }

        saveAll(context, routines)
    }

    fun findById(
        context: Context,
        routineId: Long
    ): NiaRoutine? {
        return getAll(context).find {
            it.id == routineId
        }
    }

    private fun saveAll(
        context: Context,
        routines: List<NiaRoutine>
    ) {
        val array = JSONArray()

        routines.forEach { routine ->

            val obj = JSONObject()

            obj.put("id", routine.id)
            obj.put("name", routine.name)
            obj.put("enabled", routine.enabled)
            obj.put(
                "scheduleTime",
                routine.scheduleTime ?: ""
            )
            obj.put(
                "lastStatus",
                routine.lastStatus
            )
            obj.put(
                "lastRunTime",
                routine.lastRunTime
            )

            val actions = JSONArray()

            routine.actions.forEach { action ->
                actions.put(action)
            }

            obj.put("actions", actions)

            val days = JSONArray()

            routine.repeatDays.forEach { day ->
                days.put(day)
            }

            obj.put("repeatDays", days)

            array.put(obj)
        }

        prefs(context)
            .edit()
            .putString(
                KEY_ROUTINES,
                array.toString()
            )
            .apply()
    }
}

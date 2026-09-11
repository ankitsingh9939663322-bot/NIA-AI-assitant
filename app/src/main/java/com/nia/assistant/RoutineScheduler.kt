package com.nia.assistant

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object RoutineScheduler {

    private const val ACTION_RUN_ROUTINE =
        "com.nia.assistant.ACTION_RUN_ROUTINE"

    fun schedule(
        context: Context,
        routine: NiaRoutine
    ): Boolean {

        val time = routine.scheduleTime ?: return false

        if (!routine.enabled) {
            cancel(context, routine.id)
            return false
        }

        val parts = time.split(":")

        if (parts.size != 2) return false

        val hour = parts[0].toIntOrNull() ?: return false
        val minute = parts[1].toIntOrNull() ?: return false

        if (hour !in 0..23 || minute !in 0..59) {
            return false
        }

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent = Intent(
            context,
            RoutineReceiver::class.java
        ).apply {
            action = ACTION_RUN_ROUTINE
            putExtra(
                "routine_id",
                routine.id
            )
        }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                routine.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <=
            System.currentTimeMillis()
        ) {
            calendar.add(
                Calendar.DAY_OF_YEAR,
                1
            )
        }

        /*
         * If no specific days are selected,
         * run every day.
         */
        if (routine.repeatDays.isEmpty()) {

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            return true
        }

        /*
         * Find the next selected weekday.
         *
         * Calendar:
         * Sunday = 1
         * Monday = 2
         * ...
         * Saturday = 7
         */
        var found = false

        for (offset in 0..7) {

            val candidate =
                Calendar.getInstance().apply {
                    timeInMillis =
                        calendar.timeInMillis

                    add(
                        Calendar.DAY_OF_YEAR,
                        offset
                    )
                }

            val day =
                candidate.get(
                    Calendar.DAY_OF_WEEK
                )

            if (routine.repeatDays.contains(day)) {

                calendar.timeInMillis =
                    candidate.timeInMillis

                found = true
                break
            }
        }

        if (!found) {
            return false
        }

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        return true
    }

    fun cancel(
        context: Context,
        routineId: Long
    ) {

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent = Intent(
            context,
            RoutineReceiver::class.java
        ).apply {
            action = ACTION_RUN_ROUTINE
        }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                routineId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun scheduleAll(
        context: Context
    ) {

        val routines =
            RoutineManager.getAll(context)

        routines.forEach { routine ->

            if (
                routine.enabled &&
                routine.scheduleTime != null
            ) {
                schedule(
                    context,
                    routine
                )
            }
        }
    }

    fun cancelAll(
        context: Context
    ) {

        RoutineManager
            .getAll(context)
            .forEach { routine ->
                cancel(
                    context,
                    routine.id
                )
            }
    }
}

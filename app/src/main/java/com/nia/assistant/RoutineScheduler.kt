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

        val time =
            routine.scheduleTime
                ?: return false

        if (!routine.enabled) {
            cancel(
                context,
                routine.id
            )
            return false
        }

        val parts =
            time.split(":")

        if (parts.size != 2) {
            return false
        }

        val hour =
            parts[0].toIntOrNull()
                ?: return false

        val minute =
            parts[1].toIntOrNull()
                ?: return false

        if (
            hour !in 0..23 ||
            minute !in 0..59
        ) {
            return false
        }

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent =
            Intent(
                context,
                RoutineReceiver::class.java
            ).apply {

                action =
                    ACTION_RUN_ROUTINE

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

        /*
         * Start with today's date and selected time.
         */
        val now =
            Calendar.getInstance()

        val target =
            Calendar.getInstance().apply {

                set(
                    Calendar.HOUR_OF_DAY,
                    hour
                )

                set(
                    Calendar.MINUTE,
                    minute
                )

                set(
                    Calendar.SECOND,
                    0
                )

                set(
                    Calendar.MILLISECOND,
                    0
                )
            }

        /*
         * If there are no selected days,
         * routine runs every day.
         */
        if (
            routine.repeatDays.isEmpty()
        ) {

            if (
                target.timeInMillis <=
                now.timeInMillis
            ) {

                target.add(
                    Calendar.DAY_OF_YEAR,
                    1
                )
            }

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                target.timeInMillis,
                pendingIntent
            )

            return true
        }

        /*
         * Selected-day scheduling.
         *
         * Finds the next occurrence of
         * any selected weekday.
         */
        for (
            offset in 0..7
        ) {

            val candidate =
                Calendar.getInstance().apply {

                    timeInMillis =
                        target.timeInMillis

                    add(
                        Calendar.DAY_OF_YEAR,
                        offset
                    )
                }

            val candidateDay =
                candidate.get(
                    Calendar.DAY_OF_WEEK
                )

            val isSelected =
                routine.repeatDays.contains(
                    candidateDay
                )

            if (!isSelected) {
                continue
            }

            /*
             * If candidate is today but
             * scheduled time has already passed,
             * don't select today.
             */
            if (
                offset == 0 &&
                candidate.timeInMillis <=
                now.timeInMillis
            ) {
                continue
            }

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                candidate.timeInMillis,
                pendingIntent
            )

            return true
        }

        /*
         * Safety fallback:
         * find the next selected day starting
         * from tomorrow.
         */
        for (
            offset in 1..7
        ) {

            val candidate =
                Calendar.getInstance().apply {

                    timeInMillis =
                        target.timeInMillis

                    add(
                        Calendar.DAY_OF_YEAR,
                        offset
                    )
                }

            val candidateDay =
                candidate.get(
                    Calendar.DAY_OF_WEEK
                )

            if (
                routine.repeatDays.contains(
                    candidateDay
                )
            ) {

                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    candidate.timeInMillis,
                    pendingIntent
                )

                return true
            }
        }

        return false
    }

    fun cancel(
        context: Context,
        routineId: Long
    ) {

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent =
            Intent(
                context,
                RoutineReceiver::class.java
            ).apply {

                action =
                    ACTION_RUN_ROUTINE
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                routineId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.cancel(
            pendingIntent
        )

        pendingIntent.cancel()
    }

    fun scheduleAll(
        context: Context
    ) {

        val routines =
            RoutineManager.getAll(
                context
            )

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

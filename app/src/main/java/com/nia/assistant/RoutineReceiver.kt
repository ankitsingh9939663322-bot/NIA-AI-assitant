package com.nia.assistant

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RoutineReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {

        val routineId =
            intent?.getLongExtra(
                "routine_id",
                -1L
            ) ?: -1L

        if (routineId == -1L) {
            return
        }

        val routine =
            RoutineManager.findById(
                context,
                routineId
            ) ?: return

        if (!routine.enabled) {
            return
        }

        /*
         * Schedule the next occurrence before
         * executing the current routine.
         */
        if (routine.scheduleTime != null) {

            RoutineScheduler.schedule(
                context,
                routine
            )
        }

        /*
         * Execute the routine.
         */
        RoutineExecutor.run(
            context,
            routine
        )
    }
}

package com.nia.assistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings
import java.util.Locale

object RoutineExecutor {

    interface Listener {
        fun onStarted(routine: NiaRoutine)
        fun onActionStarted(
            routine: NiaRoutine,
            action: String,
            index: Int
        )
        fun onActionCompleted(
            routine: NiaRoutine,
            action: String,
            index: Int
        )
        fun onCompleted(routine: NiaRoutine)
        fun onFailed(
            routine: NiaRoutine,
            action: String,
            reason: String
        )
        fun onSkipped(
            routine: NiaRoutine,
            action: String,
            reason: String
        )
        fun onPermissionRequired(
            routine: NiaRoutine,
            action: String,
            reason: String
        )
    }

    fun run(
        context: Context,
        routine: NiaRoutine,
        listener: Listener? = null
    ) {
        if (!routine.enabled) {
            listener?.onSkipped(
                routine,
                "",
                "Routine disabled hai."
            )

            RoutineManager.updateStatus(
                context,
                routine.id,
                "Skipped"
            )

            return
        }

        if (routine.actions.isEmpty()) {
            listener?.onFailed(
                routine,
                "",
                "Routine mein koi action nahi hai."
            )

            RoutineManager.updateStatus(
                context,
                routine.id,
                "Failed"
            )

            return
        }

        listener?.onStarted(routine)

        RoutineManager.updateStatus(
            context,
            routine.id,
            "Running"
        )

        executeNext(
            context = context,
            routine = routine,
            index = 0,
            listener = listener
        )
    }

    private fun executeNext(
        context: Context,
        routine: NiaRoutine,
        index: Int,
        listener: Listener?
    ) {
        if (index >= routine.actions.size) {

            RoutineManager.updateStatus(
                context,
                routine.id,
                "Completed"
            )

            listener?.onCompleted(routine)
            return
        }

        val action = routine.actions[index].trim()

        if (action.isEmpty()) {
            executeNext(
                context,
                routine,
                index + 1,
                listener
            )
            return
        }

        listener?.onActionStarted(
            routine,
            action,
            index
        )

        executeAction(
            context = context,
            action = action,
            onCompleted = {

                listener?.onActionCompleted(
                    routine,
                    action,
                    index
                )

                android.os.Handler(
                    android.os.Looper.getMainLooper()
                ).postDelayed(
                    {
                        executeNext(
                            context,
                            routine,
                            index + 1,
                            listener
                        )
                    },
                    700L
                )
            },
            onSkipped = { reason ->

                listener?.onSkipped(
                    routine,
                    action,
                    reason
                )

                android.os.Handler(
                    android.os.Looper.getMainLooper()
                ).postDelayed(
                    {
                        executeNext(
                            context,
                            routine,
                            index + 1,
                            listener
                        )
                    },
                    300L
                )
            },
            onPermissionRequired = { reason ->

                RoutineManager.updateStatus(
                    context,
                    routine.id,
                    "Permission Required"
                )

                listener?.onPermissionRequired(
                    routine,
                    action,
                    reason
                )
            },
            onFailed = { reason ->

                RoutineManager.updateStatus(
                    context,
                    routine.id,
                    "Failed"
                )

                listener?.onFailed(
                    routine,
                    action,
                    reason
                )
            }
        )
    }

    private fun executeAction(
        context: Context,
        action: String,
        onCompleted: () -> Unit,
        onSkipped: (String) -> Unit,
        onPermissionRequired: (String) -> Unit,
        onFailed: (String) -> Unit
    ) {

        val command = action
            .lowercase(Locale.getDefault())
            .trim()

        try {

            when {

                command == "open youtube" ||
                        command.contains("open youtube") -> {

                    openUrl(
                        context,
                        "https://www.youtube.com"
                    )

                    onCompleted()
                }

                command == "open google" ||
                        command.contains("open google") -> {

                    openUrl(
                        context,
                        "https://www.google.com"
                    )

                    onCompleted()
                }

                command.contains("open instagram") -> {

                    openUrl(
                        context,
                        "https://www.instagram.com"
                    )

                    onCompleted()
                }

                command.contains("open gmail") -> {

                    openUrl(
                        context,
                        "https://mail.google.com"
                    )

                    onCompleted()
                }

                command.contains("open whatsapp") -> {

                    openUrl(
                        context,
                        "https://web.whatsapp.com"
                    )

                    onCompleted()
                }

                command.contains("open maps") ||
                        command.contains("open google maps") -> {

                    openUrl(
                        context,
                        "https://maps.google.com"
                    )

                    onCompleted()
                }

                command.contains("open settings") -> {

                    context.startActivity(
                        Intent(
                            Settings.ACTION_SETTINGS
                        ).apply {
                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        }
                    )

                    onCompleted()
                }

                command.contains("open wifi") ||
                        command.contains("open wi-fi") -> {

                    context.startActivity(
                        Intent(
                            Settings.ACTION_WIFI_SETTINGS
                        ).apply {
                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        }
                    )

                    onCompleted()
                }

                command.contains("open bluetooth") -> {

                    context.startActivity(
                        Intent(
                            Settings.ACTION_BLUETOOTH_SETTINGS
                        ).apply {
                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        }
                    )

                    onCompleted()
                }

                command.contains("open camera") -> {

                    context.startActivity(
                        Intent(
                            "android.media.action.IMAGE_CAPTURE"
                        ).apply {
                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        }
                    )

                    onCompleted()
                }

                command.contains("open calculator") -> {

                    val intent =
                        Intent(Intent.ACTION_MAIN).apply {
                            addCategory(
                                Intent.CATEGORY_APP_CALCULATOR
                            )
                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        }

                    context.startActivity(intent)

                    onCompleted()
                }

                command.contains("set alarm") -> {

                    context.startActivity(
                        Intent(
                            AlarmClock.ACTION_SET_ALARM
                        ).apply {
                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        }
                    )

                    onCompleted()
                }

                command.contains("set timer") -> {

                    context.startActivity(
                        Intent(
                            AlarmClock.ACTION_SET_TIMER
                        ).apply {
                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        }
                    )

                    onCompleted()
                }

                command.startsWith("search ") ||
                        command.startsWith("google ") -> {

                    val query =
                        command.substringAfter(" ")
                            .trim()

                    if (query.isEmpty()) {
                        onSkipped(
                            "Search query empty hai."
                        )
                        return
                    }

                    openUrl(
                        context,
                        "https://www.google.com/search?q=" +
                                Uri.encode(query)
                    )

                    onCompleted()
                }

                command.contains("notification access") -> {

                    onPermissionRequired(
                        "Notification Access Android Settings se manually enable karna hoga."
                    )
                }

                command.contains("accessibility") -> {

                    onPermissionRequired(
                        "Accessibility permission Android Settings se manually enable karna hoga."
                    )
                }

                command.contains("send message") ||
                        command.contains("send whatsapp") ||
                        command.contains("send sms") -> {

                    onPermissionRequired(
                        "Message sending ke liye required Android permission/confirmation chahiye."
                    )
                }

                command.contains("call ") -> {

                    val number =
                        command.substringAfter("call ")
                            .trim()

                    if (
                        number.matches(
                            Regex("[0-9+ ()-]{5,}")
                        )
                    ) {

                        context.startActivity(
                            Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse(
                                    "tel:" +
                                            Uri.encode(number)
                                )
                            ).apply {
                                addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                )
                            }
                        )

                        onCompleted()

                    } else {

                        onSkipped(
                            "Valid phone number nahi mila."
                        )
                    }
                }

                else -> {

                    onSkipped(
                        "Is action ka executor abhi available nahi hai."
                    )
                }
            }

        } catch (e: SecurityException) {

            onPermissionRequired(
                "Android ne is action ke liye permission maangi."
            )

        } catch (e: Exception) {

            onFailed(
                e.message
                    ?: "Action execute nahi ho paya."
            )
        }
    }

    private fun openUrl(
        context: Context,
        url: String
    ) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            ).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }
        )
    }
}

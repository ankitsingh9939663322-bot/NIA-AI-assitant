package com.nia.assistant

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*

class RoutineActivity : Activity() {

    private lateinit var routineNameInput: EditText
    private lateinit var actionInput: EditText
    private lateinit var actionList: LinearLayout
    private lateinit var scheduleSwitch: Switch
    private lateinit var scheduleTimeInput: EditText
    private lateinit var daySelectionLayout: LinearLayout
    private lateinit var saveButton: Button

    private val actions = mutableListOf<String>()

    private var editingRoutineId: Long? = null

    /*
     * Calendar weekday values:
     *
     * Sunday    = 1
     * Monday    = 2
     * Tuesday   = 3
     * Wednesday = 4
     * Thursday  = 5
     * Friday    = 6
     * Saturday  = 7
     */
    private val selectedDays =
        mutableSetOf<Int>()

    private val dayNames =
        listOf(
            1 to "Sunday",
            2 to "Monday",
            3 to "Tuesday",
            4 to "Wednesday",
            5 to "Thursday",
            6 to "Friday",
            7 to "Saturday"
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        editingRoutineId =
            intent.getLongExtra(
                "routine_id",
                -1L
            ).takeIf {
                it != -1L
            }

        buildUi()

        if (editingRoutineId != null) {
            loadRoutine(
                editingRoutineId!!
            )
        }
    }

    private fun buildUi() {

        val root = LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL

            setPadding(
                24,
                28,
                24,
                24
            )

            setBackgroundColor(
                Color.rgb(
                    7,
                    10,
                    16
                )
            )
        }

        val header = TextView(this).apply {
            text = "NIA"
            textSize = 30f
            typeface =
                Typeface.DEFAULT_BOLD
            gravity =
                Gravity.CENTER
            setTextColor(
                Color.WHITE
            )
        }

        val subtitle = TextView(this).apply {
            text =
                "Smart Routine Builder"

            textSize = 15f

            gravity =
                Gravity.CENTER

            setTextColor(
                Color.LTGRAY
            )

            setPadding(
                0,
                4,
                0,
                24
            )
        }

        root.addView(header)
        root.addView(subtitle)

        val scroll = ScrollView(this)

        val content =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
            }

        /*
         * Routine name
         */
        content.addView(
            createLabel(
                "Routine Name"
            )
        )

        routineNameInput =
            EditText(this).apply {

                hint =
                    "Example: Morning Routine"

                setHintTextColor(Color.GRAY)

                setTextColor(
                    Color.WHITE
                )

                setSingleLine(true)

                setPadding(
                    18,
                    12,
                    18,
                    12
                )

                setBackgroundColor(
                    Color.rgb(
                        18,
                        23,
                        34
                    )
                )
            }

        content.addView(
            routineNameInput,
            LinearLayout.LayoutParams(
                -1,
                58
            ).apply {
                bottomMargin = 20
            }
        )

        /*
         * Actions
         */
        content.addView(
            createLabel(
                "Routine Actions"
            )
        )

        val actionRow =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.HORIZONTAL
            }

        actionInput =
            EditText(this).apply {

                hint =
                    "Example: Open YouTube"

                setHintTextColor(Color.GRAY)

                setTextColor(
                    Color.WHITE
                )

                setSingleLine(true)

                setPadding(
                    16,
                    8,
                    16,
                    8
                )

                setBackgroundColor(
                    Color.rgb(
                        18,
                        23,
                        34
                    )
                )
            }

        val addActionButton =
            Button(this).apply {

                text = "Add"

                setOnClickListener {
                    addAction()
                }
            }

        actionRow.addView(
            actionInput,
            LinearLayout.LayoutParams(
                0,
                58,
                1f
            )
        )

        actionRow.addView(
            addActionButton,
            LinearLayout.LayoutParams(
                90,
                58
            ).apply {
                leftMargin = 8
            }
        )

        content.addView(actionRow)

        actionList =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    0,
                    12,
                    0,
                    12
                )
            }

        content.addView(actionList)

        /*
         * Automatic schedule
         */
        content.addView(
            createLabel(
                "Automatic Schedule"
            )
        )

        scheduleSwitch =
            Switch(this).apply {

                text =
                    "Enable automatic schedule"

                textSize = 15f

                setTextColor(
                    Color.WHITE
                )

                isChecked = false

                setOnCheckedChangeListener {
                        _,
                        enabled ->

                    scheduleTimeInput.visibility =
                        if (enabled) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }

                    daySelectionLayout.visibility =
                        if (enabled) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                }
            }

        content.addView(
            scheduleSwitch,
            LinearLayout.LayoutParams(
                -1,
                56
            )
        )

        /*
         * Schedule time
         */
        scheduleTimeInput =
            EditText(this).apply {

                hint =
                    "Time — example: 08:00"

                setHintTextColor(Color.GRAY)

                setTextColor(
                    Color.WHITE
                )

                setSingleLine(true)

                visibility =
                    View.GONE

                setPadding(
                    18,
                    8,
                    18,
                    8
                )

                setBackgroundColor(
                    Color.rgb(
                        18,
                        23,
                        34
                    )
                )
            }

        content.addView(
            scheduleTimeInput,
            LinearLayout.LayoutParams(
                -1,
                58
            ).apply {
                topMargin = 8
            }
        )

        /*
         * Repeat days
         */
        daySelectionLayout =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                visibility =
                    View.GONE

                setPadding(
                    0,
                    12,
                    0,
                    8
                )
            }

        val repeatLabel =
            TextView(this).apply {

                text =
                    "Repeat On"

                textSize = 14f

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(
                    Color.LTGRAY
                )

                setPadding(
                    0,
                    4,
                    0,
                    8
                )
            }

        daySelectionLayout.addView(
            repeatLabel
        )

        /*
         * Daily option
         */
        val dailyCheckBox =
            CheckBox(this).apply {

                text =
                    "Every day"

                textSize = 15f

                setTextColor(
                    Color.WHITE
                )

                setOnCheckedChangeListener {
                        _,
                        checked ->

                    if (checked) {

                        selectedDays.clear()

                        /*
                         * Uncheck individual days
                         * when Every day is selected.
                         */
                        for (
                            i in 1..7
                        ) {
                            val child =
                                daySelectionLayout
                                    .findViewWithTag<CheckBox>(
                                        "day_$i"
                                    )

                            child?.setOnCheckedChangeListener(
                                null
                            )

                            child?.isChecked =
                                false

                            child?.setOnCheckedChangeListener {
                                    button,
                                    value ->

                                if (value) {
                                    selectedDays.add(
                                        button.tag
                                            .toString()
                                            .removePrefix(
                                                "day_"
                                            )
                                            .toInt()
                                    ) 
                                } else {
                                    selectedDays.remove(
                                        button.tag
                                            .toString()
                                            .removePrefix(
                                                "day_"
                                            )
                                            .toInt()
                                    )
                                }
                            }
                        }
                    }
                }
            }

        daySelectionLayout.addView(
            dailyCheckBox
        )

        /*
         * Individual day checkboxes
         */
        dayNames.forEach {
            (dayNumber, dayName) ->

            val checkBox =
                CheckBox(this).apply {

                    text =
                        dayName

                    textSize = 15f

                    setTextColor(
                        Color.WHITE
                    )

                    tag =
                        "day_$dayNumber"

                    setOnCheckedChangeListener {
                            _,
                            checked ->

                        if (checked) {
                            selectedDays.add(
                                dayNumber
                            )
                        } else {
                            selectedDays.remove(
                                dayNumber
                            )
                        }

                        /*
                         * If any individual day
                         * is selected, Every day
                         * becomes unchecked.
                         */
                        dailyCheckBox.setOnCheckedChangeListener(
                            null
                        )

                        dailyCheckBox.isChecked =
                            false

                        dailyCheckBox.setOnCheckedChangeListener {
                                _,
                                dailyChecked ->

                            if (dailyChecked) {

                                selectedDays.clear()

                                for (
                                    i in 1..7
                                ) {
                                    val child =
                                        daySelectionLayout
                                            .findViewWithTag<CheckBox>(
                                                "day_$i"
                                            )

                                    child?.setOnCheckedChangeListener(
                                        null
                                    )

                                    child?.isChecked =
                                        false

                                    child?.setOnCheckedChangeListener {
                                            button,
                                            value ->

                                        val number =
                                            button.tag
                                                .toString()
                                                .removePrefix(
                                                    "day_"
                                                )
                                                .toInt()

                                        if (value) {
                                            selectedDays.add(
                                                number
                                            )
                                        } else {
                                            selectedDays.remove(
                                                number
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            daySelectionLayout.addView(
                checkBox
            )
        }

        content.addView(
            daySelectionLayout
        )

        /*
         * Save button
         */
        saveButton =
            Button(this).apply {

                text =
                    if (
                        editingRoutineId == null
                    ) {
                        "Save Routine"
                    } else {
                        "Update Routine"
                    }

                setOnClickListener {
                    saveRoutine()
                }
            }

        content.addView(
            saveButton,
            LinearLayout.LayoutParams(
                -1,
                58
            ).apply {
                topMargin = 12
                bottomMargin = 20
            }
        )

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        /*
         * Close button
         */
        val closeButton =
            Button(this).apply {

                text = "Close"

                setOnClickListener {
                    finish()
                }
            }

        root.addView(
            closeButton,
            LinearLayout.LayoutParams(
                -1,
                54
            ).apply {
                topMargin = 12
            }
        )

        setContentView(root)
    }

    private fun createLabel(
        text: String
    ): TextView {

        return TextView(this).apply {

            this.text = text

            textSize = 14f

            typeface =
                Typeface.DEFAULT_BOLD

            setTextColor(
                Color.LTGRAY
            )

            setPadding(
                0,
                4,
                0,
                8
            )
        }
    }

    private fun addAction() {

        val action =
            actionInput.text
                .toString()
                .trim()

        if (action.isEmpty()) {

            Toast.makeText(
                this,
                "Action enter karo.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        actions.add(action)

        actionInput.setText("")

        refreshActionList()
    }

    private fun refreshActionList() {

        actionList.removeAllViews()

        actions.forEachIndexed {
                index,
                action ->

            val row =
                LinearLayout(this).apply {

                    orientation =
                        LinearLayout.HORIZONTAL

                    gravity =
                        Gravity.CENTER_VERTICAL

                    setPadding(
                        12,
                        8,
                        8,
                        8
                    )

                    setBackgroundColor(
                        Color.rgb(
                            18,
                            23,
                            34
                        )
                    )
                }

            val number =
                TextView(this).apply {

                    text =
                        "${index + 1}."

                    textSize = 15f

                    typeface =
                        Typeface.DEFAULT_BOLD

                    setTextColor(
                        Color.WHITE
                    )
                }

            val actionText =
                TextView(this).apply {

                    text = action

                    textSize = 15f

                    setTextColor(
                        Color.WHITE
                    )

                    setPadding(
                        12,
                        0,
                        8,
                        0
                    )
                }

            val upButton =
                Button(this).apply {

                    text = "↑"

                    isEnabled =
                        index > 0

                    setOnClickListener {
                        moveActionUp(index)
                    }
                }

            val downButton =
                Button(this).apply {

                    text = "↓"

                    isEnabled =
                        index < actions.lastIndex

                    setOnClickListener {
                        moveActionDown(index)
                    }
                }

            val deleteButton =
                Button(this).apply {

                    text = "×"

                    setOnClickListener {

                        actions.removeAt(
                            index
                        )

                        refreshActionList()
                    }
                }

            row.addView(
                number,
                LinearLayout.LayoutParams(
                    32,
                    52
                )
            )

            row.addView(
                actionText,
                LinearLayout.LayoutParams(
                    0,
                    52,
                    1f
                )
            )

            row.addView(
                upButton,
                LinearLayout.LayoutParams(
                    52,
                    52
                )
            )

            row.addView(
                downButton,
                LinearLayout.LayoutParams(
                    52,
                    52
                )
            )

            row.addView(
                deleteButton,
                LinearLayout.LayoutParams(
                    52,
                    52
                )
            )

            actionList.addView(
                row,
                LinearLayout.LayoutParams(
                    -1,
                    60
                ).apply {
                    bottomMargin = 8
                }
            )
        }
    }

    private fun moveActionUp(
        index: Int
    ) {

        if (index <= 0) return

        val item =
            actions.removeAt(index)

        actions.add(
            index - 1,
            item
        )

        refreshActionList()
    }

    private fun moveActionDown(
        index: Int
    ) {

        if (
            index >= actions.lastIndex
        ) {
            return
        }

        val item =
            actions.removeAt(index)

        actions.add(
            index + 1,
            item
        )

        refreshActionList()
    }

    private fun saveRoutine() {

        val name =
            routineNameInput.text
                .toString()
                .trim()

        if (name.isEmpty()) {

            Toast.makeText(
                this,
                "Routine name enter karo.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (actions.isEmpty()) {

            Toast.makeText(
                this,
                "Kam se kam ek action add karo.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val scheduleEnabled =
            scheduleSwitch.isChecked

        val scheduleTime =
            if (scheduleEnabled) {

                scheduleTimeInput.text
                    .toString()
                    .trim()
                    .ifEmpty {
                        null
                    }

            } else {
                null
            }

        if (
            scheduleEnabled &&
            scheduleTime == null
        ) {

            Toast.makeText(
                this,
                "Schedule time enter karo.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (
            scheduleTime != null &&
            !isValidTime(
                scheduleTime
            )
        ) {

            Toast.makeText(
                this,
                "Time HH:mm format mein enter karo. Example: 08:00",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        /*
         * Selected days.
         *
         * Empty = every day.
         */
        val repeatDays =
            if (scheduleEnabled) {
                selectedDays
                    .toList()
                    .sorted()
            } else {
                emptyList()
            }

        if (editingRoutineId == null) {

            val routine =
                RoutineManager.create(
                    context = this,
                    name = name,
                    actions = actions,
                    scheduleTime =
                        scheduleTime,
                    repeatDays =
                        repeatDays
                )

            if (scheduleTime != null) {

                val scheduled =
                    RoutineScheduler.schedule(
                        this,
                        routine
                    )

                if (!scheduled) {

                    Toast.makeText(
                        this,
                        "Routine saved, lekin schedule set nahi ho paaya.",
                        Toast.LENGTH_LONG
                    ).show()

                    setResult(
                        RESULT_OK
                    )

                    finish()

                    return
                }
            }

            Toast.makeText(
                this,
                if (
                    scheduleTime != null
                ) {
                    if (
                        repeatDays.isEmpty()
                    ) {
                        "Routine saved & daily scheduled."
                    } else {
                        "Routine saved & scheduled."
                    }
                } else {
                    "Routine saved."
                },
                Toast.LENGTH_SHORT
            ).show()

        } else {

            val existing =
                RoutineManager.findById(
                    this,
                    editingRoutineId!!
                )

            if (existing == null) {

                Toast.makeText(
                    this,
                    "Routine nahi mili.",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

            /*
             * Old alarm remove first.
             */
            RoutineScheduler.cancel(
                this,
                existing.id
            )

            existing.name =
                name

            existing.actions =
                actions.toMutableList()

            existing.scheduleTime =
                scheduleTime

            existing.repeatDays =
                repeatDays.toMutableList()

            RoutineManager.save(
                this,
                existing
            )

            /*
             * New alarm create.
             */
            if (scheduleTime != null) {

                val scheduled =
                    RoutineScheduler.schedule(
                        this,
                        existing
                    )

                if (!scheduled) {

                    Toast.makeText(
                        this,
                        "Routine updated, lekin schedule set nahi ho paaya.",
                        Toast.LENGTH_LONG
                    ).show()

                    setResult(
                        RESULT_OK
                    )

                    finish()

                    return
                }
            }

            Toast.makeText(
                this,
                if (
                    scheduleTime != null
                ) {
                    "Routine updated & scheduled."
                } else {
                    "Routine updated."
                },
                Toast.LENGTH_SHORT
            ).show()
        }

        setResult(
            RESULT_OK
        )

        finish()
    }

    private fun loadRoutine(
        routineId: Long
    ) {

        val routine =
            RoutineManager.findById(
                this,
                routineId
            ) ?: return

        routineNameInput.setText(
            routine.name
        )

        actions.clear()

        actions.addAll(
            routine.actions
        )

        refreshActionList()

        if (
            routine.scheduleTime != null
        ) {

            scheduleSwitch.isChecked =
                true

            scheduleTimeInput.setText(
                routine.scheduleTime
            )

            scheduleTimeInput.visibility =
                View.VISIBLE

            daySelectionLayout.visibility =
                View.VISIBLE

            /*
             * Restore selected days.
             */
            selectedDays.clear()

            selectedDays.addAll(
                routine.repeatDays
            )

            /*
             * If no days were stored,
             * it means every day.
             */
            if (
                routine.repeatDays.isEmpty()
            ) {

                val daily =
                    daySelectionLayout
                        .getChildAt(1)

                        as? CheckBox

                daily?.isChecked =
                    true

            } else {

                routine.repeatDays
                    .forEach { day ->

                        val checkBox =
                            daySelectionLayout
                                .findViewWithTag<CheckBox>(
                                    "day_$day"
                                )

                        checkBox?.isChecked =
                            true
                    }
            }
        }
    }

    private fun isValidTime(
        value: String
    ): Boolean {

        if (
            !value.matches(
                Regex(
                    """^\d{2}:\d{2}$"""
                )
            )
        ) {
            return false
        }

        val parts =
            value.split(":")

        val hour =
            parts[0].toIntOrNull()
                ?: return false

        val minute =
            parts[1].toIntOrNull()
                ?: return false

        return hour in 0..23 &&
                minute in 0..59
    }
}

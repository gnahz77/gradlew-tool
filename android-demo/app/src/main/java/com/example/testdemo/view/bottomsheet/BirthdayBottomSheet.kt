package com.example.testdemo.view.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.aigestudio.wheelpicker.WheelPicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar
import com.example.testdemo.R

/**
 * 生日选择底部弹窗
 */
class BirthdayBottomSheet : BottomSheetDialogFragment() {

    private var onConfirmListener: ((year: Int, month: Int, day: Int) -> Unit)? = null
    private var initialYear: Int = 2000
    private var initialMonth: Int = 1
    private var initialDay: Int = 1

    private lateinit var yearPicker: WheelPicker
    private lateinit var monthPicker: WheelPicker
    private lateinit var dayPicker: WheelPicker

    private val years = mutableListOf<String>()
    private val months = (1..12).map { String.format("%02d月", it) }
    private var days = mutableListOf<String>()

    private var selectedYear = 2026
    private var selectedMonth = 1
    private var selectedDay = 1

    companion object {
        fun newInstance(year: Int, month: Int, day: Int): BirthdayBottomSheet {
            return BirthdayBottomSheet().apply {
                initialYear = year
                initialMonth = month
                initialDay = day
            }
        }
    }

    fun setOnConfirmListener(listener: (year: Int, month: Int, day: Int) -> Unit) {
        onConfirmListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_birthday, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yearPicker = view.findViewById(R.id.wheel_year)
        monthPicker = view.findViewById(R.id.wheel_month)
        dayPicker = view.findViewById(R.id.wheel_day)

        selectedYear = initialYear
        selectedMonth = initialMonth
        selectedDay = initialDay

        setupPickers()

        view.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dismiss()
        }

        view.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            val year = selectedYear
            val month = selectedMonth
            val day = selectedDay
            
            onConfirmListener?.invoke(year, month, day)
            dismiss()
        }
    }

    /**
     * 初始化滚轮选择器
     */
    private fun setupPickers() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        years.clear()
        for (year in 1900..currentYear) {
            years.add("${year}年")
        }

        yearPicker.data = years
        monthPicker.data = months
        updateDays()

        yearPicker.selectedItemPosition = (selectedYear - 1900).coerceIn(0, years.size - 1)
        monthPicker.selectedItemPosition = (selectedMonth - 1).coerceIn(0, 11)
        dayPicker.selectedItemPosition = (selectedDay - 1).coerceIn(0, days.size - 1)

        yearPicker.setOnItemSelectedListener(object : WheelPicker.OnItemSelectedListener {
            override fun onItemSelected(picker: WheelPicker, data: Any?, position: Int) {
                val yearText = data?.toString()?.replace("年", "") ?: return
                val year = yearText.toIntOrNull() ?: return
                selectedYear = year
                updateDays()
            }
        })

        monthPicker.setOnItemSelectedListener(object : WheelPicker.OnItemSelectedListener {
            override fun onItemSelected(picker: WheelPicker, data: Any?, position: Int) {
                val monthText = data?.toString()?.replace("月", "") ?: return
                val month = monthText.toIntOrNull() ?: return
                selectedMonth = month
                updateDays()
            }
        })

        dayPicker.setOnItemSelectedListener(object : WheelPicker.OnItemSelectedListener {
            override fun onItemSelected(picker: WheelPicker, data: Any?, position: Int) {
                val dayText = data?.toString()?.replace("日", "") ?: return
                val day = dayText.toIntOrNull() ?: return
                selectedDay = day
            }
        })
    }

    /**
     * 根据当前选择的年月更新天数列表
     */
    private fun updateDays() {
        val daysInMonth = getDaysInMonth(selectedYear, selectedMonth)
        days = (1..daysInMonth).map { String.format("%02d日", it) }.toMutableList()
        dayPicker.data = days

        if (selectedDay > daysInMonth) {
            selectedDay = daysInMonth
        }
        dayPicker.selectedItemPosition = (selectedDay - 1).coerceIn(0, days.size - 1)
    }

    /**
     * 获取指定年月的天数
     */
    private fun getDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 30
        }
    }

    /**
     * 判断是否为闰年
     */
    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}

package com.sharkaboi.cusatresultschecker.main

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.work.PeriodicWorkRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sharkaboi.cusatresultschecker.R
import com.sharkaboi.cusatresultschecker.constants.Constants
import com.sharkaboi.cusatresultschecker.data.CusatResult
import com.sharkaboi.cusatresultschecker.data.Params
import com.sharkaboi.cusatresultschecker.data.getFormatted
import com.sharkaboi.cusatresultschecker.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUi()
    }

    private fun initUi() {
        initObservers()
        initListeners()
    }

    private fun initListeners() {
        binding.btnRun.setOnClickListener {
            mainViewModel.toggleWorkerStatus()
        }
        binding.btnSave.setOnClickListener {
            val params = validateForm() ?: return@setOnClickListener
            mainViewModel.saveNewParams(params)
        }
    }

    private fun validateForm(): Params? {
        val timeUnit = runCatching {
            TimeUnit.valueOf(binding.etRepeatTimeUnit.text.toString())
        }.getOrNull() ?: run {
            binding.etRepeatTimeUnit.error = "Time unit not selected"
            return null
        }
        val timeInterval = binding.etRepeatInterval.text?.toString()?.toLong() ?: run {
            binding.etRepeatInterval.error = "Repeat interval not entered"
            return null
        }
        val duration = timeUnit.toMillis(timeInterval)
        if (duration < PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS) {
            binding.etRepeatInterval.error = "Repeat interval < 15 mins not possible"
            return null
        }

        val regNo = binding.etRegNo.text?.toString() ?: run {
            binding.etRegNo.error = "Reg no not entered"
            return null
        }
        if (regNo.length != 8) {
            binding.etRegNo.error = "Invalid Reg no"
            return null
        }

        val sem = binding.etSem.text?.toString() ?: run {
            binding.etRegNo.error = "Semester not entered"
            return null
        }

        val examType = binding.etExamType.text?.toString() ?: run {
            binding.etExamType.error = "Exam type not entered"
            return null
        }

        val scheme = binding.etScheme.text?.toString() ?: run {
            binding.etScheme.error = "Scheme not entered"
            return null
        }

        val examDate = binding.etExamDate.text?.toString() ?: run {
            binding.etExamDate.error = "Exam date not entered"
            return null
        }

        return Params(
            sem = sem,
            exam = examType,
            edate = examDate,
            repeatTimeUnit = timeUnit,
            repeatInterval = timeInterval,
            regNo = regNo,
            scheme = scheme
        )
    }

    private fun initObservers() {
        mainViewModel.isWorkerRunning.observe(this) { isWorkerRunning ->
            binding.btnRun.text = if (isWorkerRunning) "Stop" else "Start"
        }
        mainViewModel.result.observe(this) { result ->
            setupResultCard(result)
        }
        mainViewModel.params.observe(this) { params ->
            setupParamsSaved(params)
        }
    }

    private fun setupResultCard(result: CusatResult?) {
        when (result) {
            is CusatResult.FailedResult -> setFailedResult(result)
            is CusatResult.PassedResult -> setPassedResult(result)
            else -> setNoResult()
        }
    }

    private fun setFailedResult(result: CusatResult.FailedResult) {
        binding.result.tvMessage.text = "Failed"
        binding.result.btnShow.isVisible = true
        showResultFetched(result, "Failed")
    }

    private fun setPassedResult(result: CusatResult.PassedResult) {
        binding.result.tvMessage.text = "Passed"
        binding.result.btnShow.isVisible = true
        showResultFetched(result, "Passed")
    }

    private fun showResultFetched(result: CusatResult, title: String) {
        val message = result.getFormatted()
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.cancel()
            }.show()
    }

    private fun setNoResult() {
        binding.result.tvMessage.text = "No result"
        binding.result.btnShow.isVisible = false
    }

    private fun setupParamsSaved(savedParams: Params?) {
        clearAllFields()

        if (savedParams == null) return

        binding.etRegNo.setText(savedParams.regNo)
        binding.etSem.setText(savedParams.sem)
        binding.etExamType.setText(savedParams.exam)
        binding.etScheme.setText(savedParams.scheme)
        binding.etExamDate.setText(savedParams.edate)
        binding.etRepeatInterval.setText(savedParams.repeatInterval.toString())
        binding.etRepeatTimeUnit.setText(savedParams.repeatTimeUnit.name)

        binding.etSem.setOnClickListener { openSemDialog() }
        binding.etExamType.setOnClickListener { openExamTypeDialog() }
        binding.etScheme.setOnClickListener { openSchemeDialog() }
        val adapter = ArrayAdapter(this, R.layout.list_item, Constants.examDates)
        binding.etExamDate.setAdapter(adapter)
        binding.etRepeatTimeUnit.setOnClickListener { openRepeatTimeUnitDialog() }
    }

    private fun openRepeatTimeUnitDialog() {
        val timeUnits = TimeUnit.values().map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle("Choose time unit")
            .setItems(timeUnits) { _, which ->
                binding.etScheme.setText(timeUnits[which])
            }.show()
    }

    private fun openSchemeDialog() {
        val schemes = Constants.schemes.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle("Choose scheme")
            .setItems(schemes) { _, which ->
                binding.etScheme.setText(schemes[which])
            }.show()
    }

    private fun openExamTypeDialog() {
        val examTypes = Constants.examTypes.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle("Choose exam type")
            .setItems(examTypes) { _, which ->
                binding.etExamType.setText(examTypes[which])
            }.show()
    }

    private fun openSemDialog() {
        val sems = Constants.semesters.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle("Choose semester")
            .setItems(sems) { _, which ->
                binding.etSem.setText(sems[which])
            }.show()
    }

    private fun clearAllFields() {
        binding.etRegNo.text = null
        binding.etRegNo.error = null
        binding.etSem.text = null
        binding.etSem.error = null
        binding.etExamType.text = null
        binding.etExamType.error = null
        binding.etScheme.text = null
        binding.etScheme.error = null
        binding.etExamDate.text = null
        binding.etExamDate.error = null
        binding.etRepeatInterval.text = null
        binding.etRepeatInterval.error = null
        binding.etRepeatTimeUnit.text = null
        binding.etRepeatTimeUnit.error = null
    }
}
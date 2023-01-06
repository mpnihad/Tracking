package com.androiddevs.runningappyt.ui.presenter

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.db.Run
import com.androiddevs.runningappyt.other.CustomMarkerView
import com.androiddevs.runningappyt.other.TrackingUtility
import com.androiddevs.runningappyt.repository.MainRepository
import com.androiddevs.runningappyt.ui.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlinx.android.synthetic.main.item_run.*
import kotlin.math.round


@AndroidEntryPoint
class StatisticFragment : Fragment(R.layout.fragment_statistics) {
    private val viewModel: StatisticsViewModel by viewModels()


    private fun setupBarChart() {

        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            setDrawGridLines(false)
        }

        barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        barChart.apply {
            description.text = "Avg Speed over time"
            legend.isEnabled = false

        }

    }

    private fun setUpLineChart() {
        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            setDrawGridLines(false)
        }

        lineChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        lineChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        lineChart.apply {
            description.text = "Avg Speed over time"
            legend.isEnabled = false

        }
    }


    private fun subscribeToObservers() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner) {

            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopwatchTime(it)
                tvTotalTime.text = totalTimeRun
            }

        }


        viewModel.totalAverageSpeed.observe(viewLifecycleOwner) {
            it?.let {
                val avgSpeed = round(it * 10f) / 10f

                tvAverageSpeed.text = "${avgSpeed}km/h"


            }

        }
        viewModel.totalDistance.observe(viewLifecycleOwner) {
            it?.let {
                val km = it / 1000f

                tvTotalDistance.text = "${round(km * 10f) / 10f}km"

            }
        }
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner) {
            it?.let {
                Log.d("TAG", "subscribeToObservers: $it")

                tvTotalCalories.text = "$it kcal"

            }
        }

        viewModel.runSortedByDate.observe(viewLifecycleOwner) {
            it?.let {

                if(viewModel.getFirebaseConfigShowLineGraph()){

                    setDataToLineChart(it)
                }
                else
                {
                    setDataToBarChart(it)

                }
//



            }
        }
    }

    private fun setDataToBarChart(it: List<Run>) {
        var allAvgSpeed = it.indices.map { i ->
            BarEntry(i.toFloat(), it[i].avgSpeedInKMH)

        }


        var barDataSet = BarDataSet(allAvgSpeed, "Avg Speed Over time").apply {
            valueTextColor = Color.WHITE

            color = ContextCompat.getColor(requireContext(), R.color.colorAccent)

        }
        barChart.data = BarData(barDataSet)
        barChart.marker = CustomMarkerView(it.reversed(), requireContext(), R.layout.marker_view)
        barChart.invalidate()
    }


    private fun setDataToLineChart(it: List<Run>) {
        var allAvgSpeed = it.indices.map { i ->


            var value: MutableList<Entry>
            Entry(i.toFloat(), it[i].avgSpeedInKMH)


        }



        var lineDataSet = LineDataSet(allAvgSpeed, "Avg Speed Over time").apply {
            valueTextColor = Color.WHITE

            color = ContextCompat.getColor(requireContext(), R.color.colorAccent)

        }
        val dataSet: MutableList<ILineDataSet> = mutableListOf()
        dataSet.add(lineDataSet)
        lineChart.data = LineData(dataSet)
        lineChart.marker = CustomMarkerView(it.reversed(), requireContext(), R.layout.marker_view)
        lineChart.invalidate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()

        //Set Up the Firebase Remote Config

        if(viewModel.getFirebaseConfigShowLineGraph()){

            setUpLineChart()

            lineChart.visibility=View.VISIBLE
            barChart.visibility=View.GONE
        }
        else
        {
            setupBarChart()
            lineChart.visibility=View.GONE
            barChart.visibility=View.VISIBLE

        }




    }

}



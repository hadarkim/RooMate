package com.example.roomate.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.example.roomate.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsFragment extends Fragment {
    private PieChart pieChart;
    private TextView tvSummary;
    private StatsViewModel statsVM;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // טוען את ה-XML
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // מאתחל שדות
        pieChart  = view.findViewById(R.id.pieChart);
        tvSummary = view.findViewById(R.id.tvSummary);

        // קובע מאפייני עוגה בסיסיים
        setupPieChartAppearance();

        // יוצר את ה-ViewModel ומאזין לשינויים בנתונים
        statsVM = new ViewModelProvider(this).get(StatsViewModel.class);
        statsVM.getChartData().observe(getViewLifecycleOwner(), this::renderChart);

        return view;
    }

    // מגדיר את הסגנון של ה-PieChart
    private void setupPieChartAppearance() {
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setCenterText("מטלות");
        pieChart.setCenterTextSize(18f);
        pieChart.getLegend().setEnabled(true);
    }

    // מקבל Map עם מפת “done” ו-“todo” ומעדכן את העוגה והטקסט
    private void renderChart(Map<String, Integer> data) {
        int doneCount = data.getOrDefault("done", 0);
        int todoCount = data.getOrDefault("todo", 0);

        // בונה רשימת PieEntry
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(doneCount, "בוצע"));
        entries.add(new PieEntry(todoCount, "פתוח"));

        // מגדיר DataSet
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(14f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        // מכניס ל-PieData ומעדכן תצוגה
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

        // עדכון סיכום טקסטואלי
        int total = doneCount + todoCount;
        String summary = String.format(
                Locale.getDefault(),
                "בוצעו %d מתוך %d מטלות",
                doneCount, total
        );
        tvSummary.setText(summary);
    }
}

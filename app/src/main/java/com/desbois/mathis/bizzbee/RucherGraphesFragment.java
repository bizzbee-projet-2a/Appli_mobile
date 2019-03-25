package com.desbois.mathis.bizzbee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;

// Instances of this class are fragments representing a single
// object in our collection.
public class RucherGraphesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View view =inflater.inflate(R.layout.fragment_tab_graphes, container, false);

        BarChart barChart = (BarChart)view.findViewById(R.id.graph);

        int group1[] = {1,2,4,5,9};
        int group2[] = {3,5,11,4,22};
        float groupSpace = 0.06f;
        float barSpace = 0.02f; // x2 dataset
        float barWidth = 0.45f; // x2 dataset
        // (0.02 + 0.45) * 2 + 0.06 = 1.00 -> interval per "group"

        List<BarEntry> entriesGroup1 = new ArrayList<>();
        List<BarEntry> entriesGroup2 = new ArrayList<>();

        // fill the lists
        for(int i = 0; i < group1.length; i++) {
            entriesGroup1.add(new BarEntry(i, group1[i]));
            entriesGroup2.add(new BarEntry(i, group2[i]));
        }

        BarDataSet set1 = new BarDataSet(entriesGroup1, "Group 1");
        BarDataSet set2 = new BarDataSet(entriesGroup2, "Group 2");

        BarData data = new BarData(set1, set2);
        set1.setColors(ColorTemplate.MATERIAL_COLORS);
        set2.setColors(ColorTemplate.MATERIAL_COLORS);

        data.setBarWidth(barWidth); // set the width of each bar
        barChart.setData(data);
        barChart.groupBars(-0.5f, groupSpace, barSpace); // perform the "explicit" grouping
        barChart.invalidate(); // refresh

        return view;


    }
}

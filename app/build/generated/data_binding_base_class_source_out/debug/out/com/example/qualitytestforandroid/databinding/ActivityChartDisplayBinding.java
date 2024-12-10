// Generated by view binder compiler. Do not edit!
package com.example.qualitytestforandroid.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.qualitytestforandroid.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityChartDisplayBinding implements ViewBinding {
  @NonNull
  private final ScrollView rootView;

  @NonNull
  public final BarChart barChart;

  @NonNull
  public final LineChart lineChart;

  @NonNull
  public final PieChart pieChart;

  private ActivityChartDisplayBinding(@NonNull ScrollView rootView, @NonNull BarChart barChart,
      @NonNull LineChart lineChart, @NonNull PieChart pieChart) {
    this.rootView = rootView;
    this.barChart = barChart;
    this.lineChart = lineChart;
    this.pieChart = pieChart;
  }

  @Override
  @NonNull
  public ScrollView getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityChartDisplayBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityChartDisplayBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_chart_display, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityChartDisplayBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.barChart;
      BarChart barChart = ViewBindings.findChildViewById(rootView, id);
      if (barChart == null) {
        break missingId;
      }

      id = R.id.lineChart;
      LineChart lineChart = ViewBindings.findChildViewById(rootView, id);
      if (lineChart == null) {
        break missingId;
      }

      id = R.id.pieChart;
      PieChart pieChart = ViewBindings.findChildViewById(rootView, id);
      if (pieChart == null) {
        break missingId;
      }

      return new ActivityChartDisplayBinding((ScrollView) rootView, barChart, lineChart, pieChart);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
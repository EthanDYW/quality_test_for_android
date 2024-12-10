// Generated by view binder compiler. Do not edit!
package com.example.qualitytestforandroid.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.qualitytestforandroid.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityProductionLineBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Spinner defectLocationSpinner;

  @NonNull
  public final Spinner defectSeveritySpinner;

  @NonNull
  public final Spinner defectTypeSpinner;

  @NonNull
  public final TextView productionLineTextView;

  @NonNull
  public final Button submitButton;

  private ActivityProductionLineBinding(@NonNull LinearLayout rootView,
      @NonNull Spinner defectLocationSpinner, @NonNull Spinner defectSeveritySpinner,
      @NonNull Spinner defectTypeSpinner, @NonNull TextView productionLineTextView,
      @NonNull Button submitButton) {
    this.rootView = rootView;
    this.defectLocationSpinner = defectLocationSpinner;
    this.defectSeveritySpinner = defectSeveritySpinner;
    this.defectTypeSpinner = defectTypeSpinner;
    this.productionLineTextView = productionLineTextView;
    this.submitButton = submitButton;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityProductionLineBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityProductionLineBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_production_line, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityProductionLineBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.defectLocationSpinner;
      Spinner defectLocationSpinner = ViewBindings.findChildViewById(rootView, id);
      if (defectLocationSpinner == null) {
        break missingId;
      }

      id = R.id.defectSeveritySpinner;
      Spinner defectSeveritySpinner = ViewBindings.findChildViewById(rootView, id);
      if (defectSeveritySpinner == null) {
        break missingId;
      }

      id = R.id.defectTypeSpinner;
      Spinner defectTypeSpinner = ViewBindings.findChildViewById(rootView, id);
      if (defectTypeSpinner == null) {
        break missingId;
      }

      id = R.id.productionLineTextView;
      TextView productionLineTextView = ViewBindings.findChildViewById(rootView, id);
      if (productionLineTextView == null) {
        break missingId;
      }

      id = R.id.submitButton;
      Button submitButton = ViewBindings.findChildViewById(rootView, id);
      if (submitButton == null) {
        break missingId;
      }

      return new ActivityProductionLineBinding((LinearLayout) rootView, defectLocationSpinner,
          defectSeveritySpinner, defectTypeSpinner, productionLineTextView, submitButton);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}

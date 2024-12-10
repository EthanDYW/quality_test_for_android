// Generated by view binder compiler. Do not edit!
package com.example.qualitytestforandroid.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.qualitytestforandroid.R;
import com.google.android.material.button.MaterialButton;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityAdminBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final MaterialButton connectServerButton;

  @NonNull
  public final MaterialButton startServerButton;

  @NonNull
  public final MaterialButton stopServerButton;

  @NonNull
  public final MaterialButton viewStatsButton;

  private ActivityAdminBinding(@NonNull LinearLayout rootView,
      @NonNull MaterialButton connectServerButton, @NonNull MaterialButton startServerButton,
      @NonNull MaterialButton stopServerButton, @NonNull MaterialButton viewStatsButton) {
    this.rootView = rootView;
    this.connectServerButton = connectServerButton;
    this.startServerButton = startServerButton;
    this.stopServerButton = stopServerButton;
    this.viewStatsButton = viewStatsButton;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityAdminBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityAdminBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_admin, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityAdminBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.connectServerButton;
      MaterialButton connectServerButton = ViewBindings.findChildViewById(rootView, id);
      if (connectServerButton == null) {
        break missingId;
      }

      id = R.id.startServerButton;
      MaterialButton startServerButton = ViewBindings.findChildViewById(rootView, id);
      if (startServerButton == null) {
        break missingId;
      }

      id = R.id.stopServerButton;
      MaterialButton stopServerButton = ViewBindings.findChildViewById(rootView, id);
      if (stopServerButton == null) {
        break missingId;
      }

      id = R.id.viewStatsButton;
      MaterialButton viewStatsButton = ViewBindings.findChildViewById(rootView, id);
      if (viewStatsButton == null) {
        break missingId;
      }

      return new ActivityAdminBinding((LinearLayout) rootView, connectServerButton,
          startServerButton, stopServerButton, viewStatsButton);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}

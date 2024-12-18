// Generated by view binder compiler. Do not edit!
package com.example.qualitytestforandroid.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.qualitytestforandroid.R;
import com.google.android.material.button.MaterialButton;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityAdminMainBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final MaterialButton addLineButton;

  @NonNull
  public final MaterialButton deleteLineButton;

  @NonNull
  public final MaterialButton exitWithoutSaveButton;

  @NonNull
  public final RecyclerView recyclerView;

  @NonNull
  public final MaterialButton saveAndExitButton;

  @NonNull
  public final MaterialButton saveButton;

  @NonNull
  public final TextView titleText;

  @NonNull
  public final LinearLayout topButtonsLayout;

  private ActivityAdminMainBinding(@NonNull ConstraintLayout rootView,
      @NonNull MaterialButton addLineButton, @NonNull MaterialButton deleteLineButton,
      @NonNull MaterialButton exitWithoutSaveButton, @NonNull RecyclerView recyclerView,
      @NonNull MaterialButton saveAndExitButton, @NonNull MaterialButton saveButton,
      @NonNull TextView titleText, @NonNull LinearLayout topButtonsLayout) {
    this.rootView = rootView;
    this.addLineButton = addLineButton;
    this.deleteLineButton = deleteLineButton;
    this.exitWithoutSaveButton = exitWithoutSaveButton;
    this.recyclerView = recyclerView;
    this.saveAndExitButton = saveAndExitButton;
    this.saveButton = saveButton;
    this.titleText = titleText;
    this.topButtonsLayout = topButtonsLayout;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityAdminMainBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityAdminMainBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_admin_main, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityAdminMainBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.addLineButton;
      MaterialButton addLineButton = ViewBindings.findChildViewById(rootView, id);
      if (addLineButton == null) {
        break missingId;
      }

      id = R.id.deleteLineButton;
      MaterialButton deleteLineButton = ViewBindings.findChildViewById(rootView, id);
      if (deleteLineButton == null) {
        break missingId;
      }

      id = R.id.exitWithoutSaveButton;
      MaterialButton exitWithoutSaveButton = ViewBindings.findChildViewById(rootView, id);
      if (exitWithoutSaveButton == null) {
        break missingId;
      }

      id = R.id.recyclerView;
      RecyclerView recyclerView = ViewBindings.findChildViewById(rootView, id);
      if (recyclerView == null) {
        break missingId;
      }

      id = R.id.saveAndExitButton;
      MaterialButton saveAndExitButton = ViewBindings.findChildViewById(rootView, id);
      if (saveAndExitButton == null) {
        break missingId;
      }

      id = R.id.saveButton;
      MaterialButton saveButton = ViewBindings.findChildViewById(rootView, id);
      if (saveButton == null) {
        break missingId;
      }

      id = R.id.titleText;
      TextView titleText = ViewBindings.findChildViewById(rootView, id);
      if (titleText == null) {
        break missingId;
      }

      id = R.id.topButtonsLayout;
      LinearLayout topButtonsLayout = ViewBindings.findChildViewById(rootView, id);
      if (topButtonsLayout == null) {
        break missingId;
      }

      return new ActivityAdminMainBinding((ConstraintLayout) rootView, addLineButton,
          deleteLineButton, exitWithoutSaveButton, recyclerView, saveAndExitButton, saveButton,
          titleText, topButtonsLayout);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}

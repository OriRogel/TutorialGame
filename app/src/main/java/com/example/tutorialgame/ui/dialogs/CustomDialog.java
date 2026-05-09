package com.example.tutorialgame.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.audio.SoundManager;

import java.util.Objects;

/**
 * An abstract base class for managing custom-styled dialog windows.
 * Supports three modes: Input, Confirmation (Two buttons), and Alert (Single button).
 */
public abstract class CustomDialog {
    private final Dialog customDialog;
    private final TextView tvTitle, tvMessage;
    private final EditText etInput;
    private final Button btnCancel, btnContinue, btnOk;
    private final LinearLayout layoutDualButtons;
    private final DialogKeys dialogKey;

    public CustomDialog(Context context, DialogKeys dialogKey) {
        customDialog = new Dialog(context);
        customDialog.setContentView(R.layout.dialog_edit);
        Objects.requireNonNull(customDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        tvTitle = customDialog.findViewById(R.id.tv_title);
        tvMessage = customDialog.findViewById(R.id.tv_message);
        etInput = customDialog.findViewById(R.id.et_input);
        
        btnCancel = customDialog.findViewById(R.id.btn_cancel);
        btnContinue = customDialog.findViewById(R.id.btn_continue);
        btnOk = customDialog.findViewById(R.id.btn_ok);
        
        layoutDualButtons = customDialog.findViewById(R.id.layout_dual_buttons);

        this.dialogKey = dialogKey;

        initListeners();
        setVisibles();
    }

    private void initListeners() {
        // Cancel button: just closes the dialog
        btnCancel.setOnClickListener(v -> {
            dismiss();
            clearInput();
            playSound();
        });

        // Continue button: triggers onClick() and closes
        btnContinue.setOnClickListener(v -> {
            onClick();
            clearInput();
            playSound();
            dismiss();
        });

        // OK button: acts as a single-button confirm
        btnOk.setOnClickListener(v -> {
            onClick();
            clearInput();
            playSound();
            dismiss();
        });
    }

    private void setVisibles() {
        // 1. Title and Message handling
        if (dialogKey.getTitle() == -1) tvTitle.setVisibility(View.GONE);
        else tvTitle.setText(dialogKey.getTitle());

        if (dialogKey.getMessage() == -1) tvMessage.setVisibility(View.GONE);
        else tvMessage.setText(dialogKey.getMessage());

        // 2. Input Field handling
        // If inputType is -1, it's a non-input dialog.
        if (dialogKey.getInputType() == -1) {
            etInput.setVisibility(View.GONE);
        } else {
            etInput.setVisibility(View.VISIBLE);
            etInput.setInputType(dialogKey.getInputType());
            if (dialogKey.getHint() != -1) etInput.setHint(dialogKey.getHint());
        }

        // 3. Button Configuration Logic
        // We use a special convention: if getHint() is -2, it's a single-button "Alert" dialog.
        // You can change this logic to use a specific field in DialogKeys if preferred.
        if (dialogKey.getHint() == -2) {
            layoutDualButtons.setVisibility(View.GONE);
            btnOk.setVisibility(View.VISIBLE);
        } else {
            layoutDualButtons.setVisibility(View.VISIBLE);
            btnOk.setVisibility(View.GONE);
        }
    }

    private void clearInput() {
        if (etInput != null) etInput.setText("");
    }

    private void playSound() {
        SoundManager.getInstance(customDialog.getContext()).playSfx(R.raw.sfx_bloop);
    }

    public EditText getEtInput() {
        return etInput;
    }

    public void show() {
        customDialog.show();
    }

    public void dismiss() {
        customDialog.dismiss();
    }

    /**
     * Method to be overridden by subclasses.
     * Called when the user clicks 'Continue' or 'OK'.
     */
    public void onClick() {}
}
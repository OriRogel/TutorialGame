package com.example.tutorialgame.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;

public final class AlertDialogUtils {
    private AlertDialogUtils() {}

    public static void showErrorDialogAndRestart(Activity ctx) {
        ctx.runOnUiThread(() -> new AlertDialog.Builder(ctx)
                .setTitle(R.string.error_loading)
                .setMessage(R.string.unable_load_message)
                .setPositiveButton(R.string.try_again, (dialog, which) -> resetApp(ctx))
                .setCancelable(false)
                .show());
    }

    public static void resetApp(Activity ctx) {
        MyApp.clearCloudManager();
        Intent launchIntent = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(launchIntent);
            ctx.finishAffinity();
        } else ctx.finishAffinity();
    }
}

package com.example.tutorialgame.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.AnimRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;


public abstract class BaseFragment extends DialogFragment {
    protected boolean blocksTouches = true;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(getLayoutResId(), container, false);

        // 2. אם צריך—חסום נגיעות שמתחת
        if (blocksTouches) {
            root.setClickable(true);
            root.setFocusable(true);
            root.setFocusableInTouchMode(true);
            root.setOnTouchListener((v, e) -> true);
        }

        // 3. תן ל‑subclass לכפות binding/ui init
        onSetupView(root);

        return root;
    }

    protected void replaceFragment(@IdRes int containerId, Fragment fragment, @AnimRes int enter, @AnimRes int exit) {
        if(enter != -1) {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(enter, exit)
                    .replace(containerId, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(null)
                    .commit();
        }
        else {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(containerId, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(null)
                    .commit();
        }
    }

    @LayoutRes
    protected abstract int getLayoutResId();

    /**
     * כל פרגמנט מפרט כאן איפה לקשר view, listener וכו׳
     */
    protected abstract void onSetupView(View root);
}

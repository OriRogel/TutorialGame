package com.example.tutorialgame.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.tutorialgame.R;
import com.example.tutorialgame.managers.BitmapManager;
import com.example.tutorialgame.ui.base.BaseFragment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class LanguageFragment extends BaseFragment implements View.OnClickListener {
    private ImageButton imgBtnClose;
    private Map<Button, String> langMap;
    private SharedPreferences spLanguage;
    private SharedPreferences.Editor languageEditor;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_language;
    }

    @Override
    protected void onSetupView(View root) {
        bindViews(root);
        initPrefs();
        initListeners();
    }

    private void initPrefs() {
        spLanguage = requireContext().getSharedPreferences("app_language", Context.MODE_PRIVATE);
        languageEditor = spLanguage.edit();
        imgBtnClose.setImageBitmap(BitmapManager.getBitmap(R.drawable.ic_shuriken, 0.35f, false));
    }

    private void bindViews(View v) {
        imgBtnClose = v.findViewById(R.id.imgBtnClose);

        langMap = new HashMap<>();
        langMap.put(v.findViewById(R.id.btnEnglishLang), "en");
        langMap.put(v.findViewById(R.id.btnHebrewLang), "iw");
        langMap.put(v.findViewById(R.id.btnGreekLang), "el");
    }

    private void initListeners() {
        for (Button b : langMap.keySet()) {
            b.setOnClickListener(this);
            // שינוי: ברירת מחדל "en"
            if (Objects.equals(langMap.get(b), spLanguage.getString("app_language", "en"))) {
                b.setEnabled(false);
                b.setGravity(Gravity.CENTER);
            }
        }
        imgBtnClose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == imgBtnClose) {
            if (getShowsDialog()) dismiss();
            else {replaceFragment(R.id.settings_container,
                    new SettingsFragment(),
                    R.anim.enter_from_right,
                    R.anim.exit_to_left);
            }
        } else if (v instanceof Button) {
            Button b = (Button) v;
            String langCode = langMap.get(b);
            if (langCode != null) {
                changeLanguage(langCode, b);
            }
            if (getShowsDialog()) dismiss();
        }
    }

    private void changeLanguage(String langKey, Button b) {
        languageEditor.putString("app_language", langKey).apply();
        b.setEnabled(false);
        b.setGravity(Gravity.CENTER);
        requireActivity().recreate();
    }
}
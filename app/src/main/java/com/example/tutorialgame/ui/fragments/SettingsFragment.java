package com.example.tutorialgame.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.core.content.ContextCompat;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.ui.activities.authentication.LoginActivity;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.example.tutorialgame.ui.base.BaseFragment;
import com.example.tutorialgame.ui.dialogs.CustomDialog;
import com.example.tutorialgame.ui.dialogs.DialogKeys;
import com.google.firebase.auth.FirebaseAuth;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends BaseFragment implements View.OnTouchListener,
        SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private SeekBar sbMusic, sbSound;
    private Button btnLogout, btnLanguage;
    private ImageView ivSound;
    private SharedPreferences spIcons;
    private SharedPreferences.Editor iconsEditor;
    private long lastTouch;
    private CustomDialog dialogLogout;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_settings;
    }

    @Override
    protected void onSetupView(View root) {
        initPrefs();
        bindViews(root);
        initUI();
        initListeners();
    }

    private void initPrefs() {
        spIcons = requireContext().getSharedPreferences("icons", Context.MODE_PRIVATE);
        iconsEditor = spIcons.edit();
    }

    private void bindViews(View v) {
        sbMusic = v.findViewById(R.id.sbMusic);
        sbSound = v.findViewById(R.id.sbSound);
        btnLogout = v.findViewById(R.id.btnLogout);
        btnLanguage = v.findViewById(R.id.btnLanguage);
        ivSound = v.findViewById(R.id.ivSound);
        dialogLogout = new CustomDialog(requireActivity(), DialogKeys.SETTINGS_LOGOUT) {
            @Override
            public void onClick() {
                MusicManager.getInstance(requireContext()).release();
                MyApp.clearCloudManager();
                FirebaseAuth.getInstance().signOut();
                MusicManager.getInstance(requireContext()).play(R.raw.sfx_success4);
                MusicManager.getInstance(requireContext()).setLooping(false);
                ContextCompat.startActivity(requireContext(), new Intent(requireContext(), LoginActivity.class), null);
                requireActivity().finish();
            }
        };
    }

    private void initUI() {
        // Restore values
        float soundVolume = SoundManager.getInstance(BaseActivity.getContext()).getVolume();
        float musicVolume = MusicManager.getInstance(BaseActivity.getContext()).getVolume();
        sbSound.setProgress((int) (soundVolume * 100));
        sbMusic.setProgress((int) (musicVolume * 100));

        ivSound.setBackgroundResource(spIcons.getInt("sound_icon", R.drawable.ic_sound_2));

        // Apply volumes
        SoundManager.getInstance(requireContext()).setVolume(soundVolume);
        MusicManager.getInstance(requireContext()).setVolume(musicVolume);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListeners() {
        sbMusic.setOnTouchListener(this);
        sbSound.setOnTouchListener(this);
        sbMusic.setOnSeekBarChangeListener(this);
        sbSound.setOnSeekBarChangeListener(this);
        btnLogout.setOnClickListener(this);
        btnLanguage.setOnClickListener(this);
    }

    @SuppressWarnings("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Allow SeekBar inside scrollable container
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            v.getParent().requestDisallowInterceptTouchEvent(true);
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP ||
                event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            v.getParent().requestDisallowInterceptTouchEvent(false);
        }
        return false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float volume = progress / 100f;
        long now = System.currentTimeMillis();

        if (seekBar == sbSound) {
            SoundManager.getInstance(requireContext()).setVolume(volume);
            if (now - lastTouch >= 100) {
                SoundManager.getInstance(requireContext()).playSfx(R.raw.sfx_bloop);
                lastTouch = now;
            }
            updateSoundIcon(progress);
        } else {
            MusicManager.getInstance(requireContext()).setVolume(volume);
            if (now - lastTouch >= 100) {
                SoundManager.getInstance(requireContext()).playSfx(R.raw.sfx_bloop);
                lastTouch = now;
            }
        }
    }

    private void updateSoundIcon(int progress) {
        int iconRes;

        if (progress == 0) iconRes = R.drawable.ic_sound_0;
        else if (progress <= 49) iconRes = R.drawable.ic_sound_1;
        else if (progress <= 99) iconRes = R.drawable.ic_sound_2;
        else iconRes = R.drawable.ic_sound_3;

        ivSound.setBackgroundResource(iconRes);
        iconsEditor.putInt("sound_icon", iconRes).apply();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        lastTouch = System.currentTimeMillis();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // no-op
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnLogout) {
            dialogLogout.show();
        } else if (v == btnLanguage) {
            replaceFragment(R.id.settings_container,
                    new LanguageFragment(),
                    R.anim.enter_from_right,
                    R.anim.exit_to_left);
        }
    }
}

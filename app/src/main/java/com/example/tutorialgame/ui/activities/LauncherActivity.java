package com.example.tutorialgame.ui.activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.example.tutorialgame.R;
import com.example.tutorialgame.cloud.UserRepository;
import com.example.tutorialgame.engine.ui.PlayerFaceset;
import com.example.tutorialgame.engine.ui.circleframes.CircleFrames;
import com.example.tutorialgame.managers.BitmapManager;
import com.example.tutorialgame.managers.MapManager;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.example.tutorialgame.ui.fragments.ProfileFragment;
import com.example.tutorialgame.ui.fragments.SettingsFragment;

import java.text.MessageFormat;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LauncherActivity extends BaseActivity implements View.OnClickListener {
    @Inject UserRepository userRepository;

    private DrawerLayout drawerLayout;
    private View settingsContainer;
    private Button btnPlay;
    private ImageButton imgBtnMenu;
    private ImageView ivPic, ivFrame, ivBackground, iv_coin;
    private TextView tvNickname, tvCoins, tvLevel, tvXp;
    private View profileHeader;

    // רפרנסים חדשים לרכיבים שהוספנו
    private FrameLayout profileContainer;
    private View dimBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        initViews();
        initListeners();
        setFragmentDrawer();

        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.settings_container, new SettingsFragment());
            ft.commit();
        }

        musicManager.play(R.raw.music_launcher);
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        settingsContainer = findViewById(R.id.settings_container);
        btnPlay = findViewById(R.id.btnPlay);
        imgBtnMenu = findViewById(R.id.btnMenu);
        ivPic = findViewById(R.id.iv_pic);
        ivFrame = findViewById(R.id.iv_profile_frame);
        ivBackground = findViewById(R.id.iv_profile_background);
        iv_coin = findViewById(R.id.iv_coin);
        profileHeader = findViewById(R.id.profile_header);

        tvNickname = findViewById(R.id.tv_launcher_nickname);
        tvCoins = findViewById(R.id.tv_launcher_coins);
        tvLevel = findViewById(R.id.tv_level);
        tvXp = findViewById(R.id.tv_xp);

        // אתחול הרכיבים החדשים
        profileContainer = findViewById(R.id.profile_container);
        dimBackground = findViewById(R.id.dim_background);

        // קריאה למתודה שמציבה את תמונת הפרופיל
        setProfileBackground();
    }

    private void initListeners() {
        btnPlay.setOnClickListener(this);
        imgBtnMenu.setOnClickListener(this);
        ivFrame.setOnClickListener(this);
        profileHeader.setOnClickListener(this);

        // הוסף Listener לרקע המושחר כדי לסגור את הפרופיל בלחיצה עליו
        dimBackground.setOnClickListener(v -> closeProfileFragment());
    }

    private void setProfileBackground() {
        if (userRepository.getCosmetic() == null || userRepository.getProgress() == null || userRepository.getProfile() == null) return;

        Bitmap frameBitmap = CircleFrames.valueOf(userRepository.getCosmetic().getCurrentFrame()).getCircleFrame();
        Bitmap faceBitmap = PlayerFaceset.IDLE.getFace();
        Bitmap bg = CircleFrames.BACKGROUND.getCircleFrame();

        ivBackground.setImageBitmap(bg);
        ivFrame.setImageBitmap(frameBitmap);
        ivPic.setImageBitmap(faceBitmap);

        tvNickname.setText(userRepository.getProfile().getNickname());
        tvCoins.setText(String.valueOf(userRepository.getCosmetic().getCoinsLeft()));
        tvLevel.setText(String.valueOf(userRepository.getProgress().getLevel()));
        tvXp.setText(MessageFormat.format("{0}/{1}", userRepository.getProgress().getXp(), userRepository.getProgress().neededXpForLevelUp()));

        iv_coin.setImageBitmap(Objects.requireNonNull(BitmapManager.getSpritesheet(R.drawable.spr_coin, 10, 10, 4, 1, false))[0]);
    }

    private void setFragmentDrawer() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int drawerWidth = (int) (screenWidth * 0.40);
        DrawerLayout.LayoutParams lp = (DrawerLayout.LayoutParams) settingsContainer.getLayoutParams();
        lp.width = drawerWidth;
        settingsContainer.setLayoutParams(lp);
    }

    @Override
    public void onClick(View v) {
        if (v == btnPlay) {
            // Ensure fresh map loading for the current slot
            MapManager.clearCache();
            MapManager.initStartingWorld();

            startActivity(new Intent(this, GameActivity.class));
            finish();
        }
        else if (v == imgBtnMenu) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.settings_container, new SettingsFragment());
            ft.commit();
            drawerLayout.openDrawer(GravityCompat.END);
        }
        else if (v == ivFrame || v == profileHeader) {
            openProfileFragment(); // קריאה למתודה חדשה שמטפלת בהצגת הפרופיל
        }
    }

    private void openProfileFragment() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.profile_container, new ProfileFragment())
                .commit();

        profileContainer.setVisibility(VISIBLE);
        dimBackground.setAlpha(0f);
        dimBackground.setVisibility(VISIBLE);
        dimBackground.animate().alpha(1f).setDuration(300).start();
    }

    private void closeProfileFragment() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        setProfileBackground();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.profile_container, new ProfileFragment())
                .commit();

        profileContainer.setVisibility(GONE);
        dimBackground.animate().alpha(0f).setDuration(300).withEndAction(() -> dimBackground.setVisibility(GONE)).start();
    }

    @Override
    protected void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // עדיפות 1: סגור את ה-ProfileFragment אם הוא פתוח
                if (profileContainer.getVisibility() == View.VISIBLE) {
                    closeProfileFragment();
                    return;
                }

                // עדיפות 2: סגור את ה-Drawer אם הוא פתוח
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                    return;
                }

                handleExitWithDoubleClick();
            }
        });
    }
}

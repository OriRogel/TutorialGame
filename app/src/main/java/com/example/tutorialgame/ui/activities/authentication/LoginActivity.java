package com.example.tutorialgame.ui.activities.authentication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.cloud.UserDataManager;
import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.ui.activities.LauncherActivity;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.example.tutorialgame.ui.dialogs.AlertDialogUtils;
import com.example.tutorialgame.ui.fragments.LanguageFragment;
import com.example.tutorialgame.utils.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static final int REQ_READ_PHONE_STATE = 1234;
    private FirebaseAuth mAuth;
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private ImageButton imgBtnLanguage;
    private TextView tvRegisterNow, tvForgotPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initListeners();
        initTextWatcher();

        MusicManager.getInstance(this).play(R.raw.music_login);
        MusicManager.getInstance(this).setLooping(true);

        askPermission();
    }

    private void initViews() {
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btnLogin);
        imgBtnLanguage = findViewById(R.id.imgBtnLanguage);
        progressBar = findViewById(R.id.progressBar);
        tvRegisterNow = findViewById(R.id.tvRegisterNow);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setEnabled(false);
        mAuth = FirebaseAuth.getInstance();
        initData(getIntent());
    }

    private void initData(Intent intent) {
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");

        etEmail.setText(email);
        etPassword.setText(password);
    }

    private void initListeners() {
        btnLogin.setOnClickListener(this);
        imgBtnLanguage.setOnClickListener(this);
        tvRegisterNow.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);
    }

    private void initTextWatcher() {
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
                String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

                if (!TextUtils.isEmpty(email) && !ValidationUtils.isEmailValid(email))
                    setError(etEmail, getString(R.string.invalid_email), R.drawable.king);
                else etEmail.setError(null);

                if (!TextUtils.isEmpty(password) && !ValidationUtils.isPasswordValid(password))
                    setError(etPassword, getString(R.string.invalid_password), R.drawable.king);
                else etPassword.setError(null);

                btnLogin.setEnabled(ValidationUtils.isEmailValid(email) && ValidationUtils.isPasswordValid(password));
            }
        };

        etEmail.addTextChangedListener(afterTextChangedListener);
        etPassword.addTextChangedListener(afterTextChangedListener);
    }

    @Override
    public void onClick(View v) {
        if (v == btnLogin) loginUser();
        else if (v == imgBtnLanguage) {
            LanguageFragment langDialog = new LanguageFragment();
            langDialog.show(getSupportFragmentManager(), "language_picker");
        }
        else if (v == tvForgotPassword) resetPassword();
        else if (v == tvRegisterNow) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        }
    }

    private void loginUser() {
        progressBar.setVisibility(View.VISIBLE);
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        SoundManager.getInstance(this).playSfx(R.raw.sfx_error);
                        showToast(getString(R.string.error) + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT);
                    }
                    else {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            showToast(getString(R.string.loading_data), Toast.LENGTH_SHORT);
                            MyApp.initializeCloudManager();
                            
                            // שלב 1: טעינת נתוני חשבון (פרופיל)
                            MyApp.startLoadingAccountData(new UserDataManager.OnDataLoadedListener() {
                                @Override
                                public void onDataLoadSuccess() {
                                    // שלב 2: בחירת הסלוט האחרון שנשמר בענן
                                    int lastSlotId = MyApp.getProfile().getLastSelectedSlot();
                                    
                                    MyApp.getCloudManager().selectSlot(lastSlotId, new UserDataManager.OnDataLoadedListener() {
                                        @Override
                                        public void onDataLoadSuccess() {
                                            progressBar.setVisibility(View.GONE);
                                            SoundManager.getInstance(getContext()).playSfx(R.raw.sfx_success4);
                                            showToast(getString(R.string.login_successful), Toast.LENGTH_SHORT);
                                            startActivity(new Intent(getContext(), LauncherActivity.class));
                                            finish();
                                        }

                                        @Override
                                        public void onDataLoadFailed() {
                                            progressBar.setVisibility(View.GONE);
                                            AlertDialogUtils.showErrorDialogAndRestart(LoginActivity.this);
                                        }
                                    });
                                }

                                @Override
                                public void onDataLoadFailed() {
                                    progressBar.setVisibility(View.GONE);
                                    AlertDialogUtils.showErrorDialogAndRestart(LoginActivity.this);
                                }
                            });
                        } else if (user != null) {
                            progressBar.setVisibility(View.GONE);
                            user.sendEmailVerification();
                            showToast(getString(R.string.verify_email), Toast.LENGTH_LONG);
                        }
                    }
                });
    }

    private void resetPassword() {
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        if (email.isEmpty()) showToast(getString(R.string.enter_email), Toast.LENGTH_SHORT);
        else {
            FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                            showToast(getString(R.string.reset_password_email_sent), Toast.LENGTH_SHORT);
                        else
                            showToast(getString(R.string.error) + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG);
                    });
        }
    }

    private void askPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQ_READ_PHONE_STATE);
        }
    }
}

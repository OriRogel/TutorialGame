package com.example.tutorialgame.ui.activities.authentication;

import android.content.Intent;
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
import com.example.tutorialgame.R;
import com.example.tutorialgame.cloud.UserDataManager;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.example.tutorialgame.ui.fragments.LanguageFragment;
import com.example.tutorialgame.utils.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    private TextView tvLoginNow;
    private Button btnRegister;
    private ImageButton imgBtnLanguage;
    private TextInputEditText etEmail, etPassword, etNickname;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initListeners();
        initTextWatcher();
    }

    private void initViews() {
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etNickname = findViewById(R.id.nickname);
        btnRegister = findViewById(R.id.btnRegister);
        imgBtnLanguage = findViewById(R.id.imgBtnLanguage);
        tvLoginNow = findViewById(R.id.tvLoginNow);
        progressBar = findViewById(R.id.progressBar);

        btnRegister.setEnabled(false);
        mAuth = FirebaseAuth.getInstance();
    }

    private void initListeners() {
        tvLoginNow.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        imgBtnLanguage.setOnClickListener(this);
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
                String nickname = Objects.requireNonNull(etNickname.getText()).toString().trim();

                if (!TextUtils.isEmpty(email) && !ValidationUtils.isEmailValid(email))
                    setError(etEmail, getString(R.string.invalid_email), R.drawable.king);
                else etEmail.setError(null);

                if (!TextUtils.isEmpty(password) && !ValidationUtils.isPasswordValid(password))
                    setError(etPassword, getString(R.string.invalid_password), R.drawable.king);
                else etPassword.setError(null);

                if (!TextUtils.isEmpty(nickname) && !ValidationUtils.isNicknameValid(nickname))
                    setError(etNickname, getString(R.string.invalid_nickname), R.drawable.king);
                else etNickname.setError(null);

                btnRegister.setEnabled(ValidationUtils.isEmailValid(email)
                        && ValidationUtils.isPasswordValid(password)
                        && ValidationUtils.isNicknameValid(nickname));
            }
        };

        etEmail.addTextChangedListener(afterTextChangedListener);
        etPassword.addTextChangedListener(afterTextChangedListener);
        etNickname.addTextChangedListener(afterTextChangedListener);
    }

    @Override
    public void onClick(View v) {
        if (v == btnRegister) {
            registerUser();
        } else if (v == imgBtnLanguage) {
            LanguageFragment langDialog = new LanguageFragment();
            langDialog.show(getSupportFragmentManager(), "language_picker");
        } else if (v == tvLoginNow) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void registerUser() {
        progressBar.setVisibility(View.VISIBLE);

        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();
        String nickname = Objects.requireNonNull(etNickname.getText()).toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (!task.isSuccessful()) {
                        handleRegisterFailure(task.getException());
                        return;
                    }
                    handleRegisterSuccess(email, nickname, password);
                });
    }

    private void handleRegisterSuccess(String email, String nickname, String password) {
        userRepository.initializeFromAuth();
        if (userRepository.getCloudManager() == null) return;

        // 1. יצירת פרופיל גלובלי
        userRepository.getProfile().createProfile(nickname, email);
        
        // 2. יצירת סלוט ראשון אוטומטית למשתמש חדש
        userRepository.getCloudManager().createNewSlot(1, new UserDataManager.OnDataLoadedListener() {
            @Override
            public void onDataLoadSuccess() {
                sendVerificationEmail();
                FirebaseAuth.getInstance().signOut();
                userRepository.clear();
                soundManager.playSfx(R.raw.sfx_success4);

                startActivity(getSuccessIntent(email, password));
                finish();
            }

            @Override
            public void onDataLoadFailed() {
                showToast("Failed to initialize game data.", Toast.LENGTH_LONG);
                soundManager.playSfx(R.raw.sfx_error);
            }
        });
    }

    private Intent getSuccessIntent(String email, String password) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        return intent;
    }

    private void sendVerificationEmail() {
        if (mAuth.getCurrentUser() == null) return;
        mAuth.getCurrentUser().sendEmailVerification()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) showToast(getString(R.string.verification_email_sent), Toast.LENGTH_SHORT);
                });
    }

    private void handleRegisterFailure(Exception e) {
        soundManager.playSfx(R.raw.sfx_error);
        if (e instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) showToast(getString(R.string.email_exists), Toast.LENGTH_SHORT);
        else showToast(getString(R.string.error) + " " + (e != null ? e.getMessage() : ""), Toast.LENGTH_SHORT);
    }
}
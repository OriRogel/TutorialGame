package com.example.tutorialgame.ui.fragments;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.cloud.SaveSlotMetadata;
import com.example.tutorialgame.cloud.UserDataManager;
import com.example.tutorialgame.cloud.document.ProfileDoc;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.ui.circleframes.CircleFrames;
import com.example.tutorialgame.managers.BitmapManager;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.example.tutorialgame.ui.base.BaseFragment;
import com.example.tutorialgame.ui.dialogs.AlertDialogUtils;
import com.example.tutorialgame.ui.dialogs.CustomDialog;
import com.example.tutorialgame.ui.dialogs.DialogKeys;
import com.example.tutorialgame.utils.ValidationUtils;

import java.text.MessageFormat;
import java.util.Objects;


import com.example.tutorialgame.cloud.UserRepository;
import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends BaseFragment implements View.OnClickListener {
    @Inject UserRepository userRepository;
    @Inject SoundManager soundManager;

    private ImageView ivFrame, ivBackground, ivCoin;
    private Button btnNickname, btnEmail;
    private TextView tvLevel, tvXp, tvCoins, tvEnemies, tvDays, tvQuests;
    private CustomDialog dialogEditEmail, dialogEditNickname, dialogDeleteSlotNegative;
    
    private View slot1, slot2, slot3;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_profile;
    }

    @Override
    protected void onSetupView(View root) {
        bindViews(root);
        initPrefs();
        updateSlotsUI();
        initListeners();
    }

    private void initPrefs() {
        Bitmap crnFrm = CircleFrames.valueOf(Objects.requireNonNull(userRepository.getCosmetic()).getCurrentFrame()).getCircleFrame();
        Bitmap bgFrame = CircleFrames.BACKGROUND.getCircleFrame();

        ivBackground.setImageBitmap(bgFrame);
        ivFrame.setImageBitmap(crnFrm);
        ivCoin.setImageBitmap(Objects.requireNonNull(BitmapManager.getSpritesheet(R.drawable.spr_coin, 10, 10, 4, 1, false))[0]);

        btnNickname.setText(userRepository.getProfile().getField(ProfileDoc.Field.NICKNAME));
        btnEmail.setText(userRepository.getProfile().getField(ProfileDoc.Field.EMAIL));

        tvLevel.setText(String.valueOf(Objects.requireNonNull(userRepository.getProgress()).getLevel()));
        tvXp.setText(MessageFormat.format("{0}/{1}", userRepository.getProgress().getXp(), userRepository.getProgress().neededXpForLevelUp()));
        tvCoins.setText(String.valueOf(userRepository.getCosmetic().getCoinsLeft()));
        tvEnemies.setText(String.valueOf(userRepository.getProgress().getEnemiesDefeated()));
        tvDays.setText(String.valueOf(userRepository.getProgress().getDaysLoggedIn()));
        tvQuests.setText(String.valueOf(userRepository.getProgress().getQuestsCompleted()));
    }

    private void updateSlotsUI() {
        setupSlotView(1);
        setupSlotView(2);
        setupSlotView(3);
    }

    private void setupSlotView(int slotId) {
        SaveSlotMetadata.Slot slotData = userRepository.getCloudManager().getSlotsMetadata().getSlot(slotId);

        View slotRoot = getSlotRoot(slotId);
        View layoutUnselected = slotRoot.findViewById(R.id.layout_unselected);
        View layoutSelected = slotRoot.findViewById(R.id.layout_selected);
        
        if (userRepository.getCloudManager().getActiveSlotId() == slotId) {
            layoutSelected.setBackgroundResource(R.drawable.btn_pressed);
        } else {
            layoutSelected.setBackgroundResource(R.drawable.btn_normal);
        }

        if (slotData.exists) {
            layoutUnselected.setVisibility(View.GONE);
            layoutSelected.setVisibility(View.VISIBLE);
            
            TextView tvSlotName = slotRoot.findViewById(R.id.tvSlotName);
            TextView tvStats = slotRoot.findViewById(R.id.tv_slot_stats);

            if (userRepository.getCloudManager().getActiveSlotId() == slotId) {
                tvSlotName.setText("Current Adventure");
                tvSlotName.setTextSize(tvSlotName.getTextSize() * 1.1f);
                tvStats.setVisibility(View.GONE);
            } else {
                tvSlotName.setText(slotData.slotName);
                tvStats.setText(MessageFormat.format("LVL: {0} | FRAMES: {1}", slotData.getLevel(), slotData.framesCount));
                layoutSelected.setOnClickListener(v -> handleSlotSelection(slotId));
            }

            slotRoot.findViewById(R.id.btnResetSlot).setOnClickListener(v -> handleDeleteSlot(slotId));
            layoutSelected.setOnLongClickListener(v -> {
                showRenameDialog(slotId, slotData.slotName);
                return true;
            });
        } else {
            layoutUnselected.setVisibility(View.VISIBLE);
            layoutSelected.setVisibility(View.GONE);
            
            Button btnNew = slotRoot.findViewById(R.id.btn_new_adventure);
            btnNew.setOnClickListener(v -> handleNewSlotCreation(slotId));
        }
    }

    private View getSlotRoot(int slotId) {
        switch (slotId) {
            case 1: return slot1;
            case 2: return slot2;
            default: return slot3;
        }
    }

    private void showRenameDialog(int slotId, String currentName) {
        CustomDialog renameDialog = new CustomDialog(requireActivity(), DialogKeys.PROFILE_NICKNAME) {
            @Override
            public void onClick() {
                String newName = Objects.requireNonNull(getEtInput().getText()).toString().trim();
                if (!newName.isEmpty()) {
                    userRepository.getCloudManager().getUserDoc().update("slots_metadata." + slotId + ".slotName", newName)
                            .addOnSuccessListener(aVoid -> {
                                userRepository.getCloudManager().getSlotsMetadata().getSlot(slotId).slotName = newName;
                                updateSlotsUI();
                            });
                }
            }
        };
        renameDialog.show();
        renameDialog.getEtInput().setText(currentName);
    }

    private void handleSlotSelection(int slotId) {
        CustomDialog changeSlot = new CustomDialog(requireActivity(), DialogKeys.CHANGE_SLOT) {
            @Override
            public void onClick() {
                if (userRepository.getCloudManager().getActiveSlotId() == slotId) {
                    return;
                }
                
                // עדכון ה-Preference המקומי למקרה שפעולת הרשת תיקטע
                requireActivity().getSharedPreferences("app_prefs", 0)
                        .edit()
                        .putInt("active_slot_id", slotId)
                        .apply();
                
                // עדכון בענן ורק לאחר מכן ריסטרט לאפליקציה
                userRepository.getProfile().updateLastSelectedSlot(slotId).addOnCompleteListener(task -> {
                    AlertDialogUtils.resetApp(requireActivity());
                    ProfileFragment.this.dismiss();
                });
            }
        };
        changeSlot.show();
    }

    private void handleDeleteSlot(int slotId) {
        if (slotId == userRepository.getCloudManager().getActiveSlotId()) dialogDeleteSlotNegative.show();
        else {
            CustomDialog deleteSlot = new CustomDialog(requireActivity(), DialogKeys.DELETE_SLOT_POSITIVE) {
                @Override
                public void onClick() {
                    userRepository.getCloudManager().deleteInactiveSlot(slotId, new UserDataManager.OnDataLoadedListener() {
                        final BaseActivity activity = (BaseActivity) getActivity();
                        @Override
                        public void onDataLoadSuccess() {
                            setupSlotView(slotId);
                            soundManager.playSfx(R.raw.sfx_success4);
                            if (activity != null)
                                activity.showToast(activity.getString(R.string.delete_save_succeed), Toast.LENGTH_SHORT);

                        }
                        @Override
                        public void onDataLoadFailed() {
                            soundManager.playSfx(R.raw.sfx_error);
                            if (activity != null)
                                activity.showToast(activity.getString(R.string.delete_save_failed), Toast.LENGTH_SHORT);

                        }
                    });
                }
            };
            deleteSlot.show();
        }
    }

    private void handleNewSlotCreation(int slotId) {
        CustomDialog createSlot = new CustomDialog(requireActivity(), DialogKeys.CHANGE_SLOT) {
            @Override
            public void onClick() {
                userRepository.getCloudManager().createNewSlot(slotId, new UserDataManager.OnDataLoadedListener() {
                    @Override
                    public void onDataLoadSuccess() {
                        requireActivity().getSharedPreferences("app_prefs", 0)
                                .edit()
                                .putInt("active_slot_id", slotId)
                                .apply();
                                
                        userRepository.getProfile().updateLastSelectedSlot(slotId).addOnCompleteListener(task ->
                                AlertDialogUtils.resetApp(requireActivity()));
                    }

                    @Override
                    public void onDataLoadFailed() {
                        BaseActivity activity = (BaseActivity) getActivity();
                        if (activity != null) {
                            activity.showToast(getString(R.string.error_loading), Toast.LENGTH_LONG);
                        }
                    }
                });
            }
        };
        createSlot.show();
    }

    private void bindViews(View v) {
        ivBackground = v.findViewById(R.id.iv_background);
        ivFrame = v.findViewById(R.id.iv_frame);
        ivCoin = v.findViewById(R.id.iv_coin);
        tvLevel = v.findViewById(R.id.tv_level);
        tvXp = v.findViewById(R.id.tv_xp);
        tvCoins = v.findViewById(R.id.tv_coins);
        tvEnemies = v.findViewById(R.id.tv_enemies);
        tvDays = v.findViewById(R.id.tv_days);
        tvQuests = v.findViewById(R.id.tv_quests);
        btnNickname = v.findViewById(R.id.btn_nickname);
        btnEmail = v.findViewById(R.id.btn_email);
        slot1 = v.findViewById(R.id.slot_1);
        slot2 = v.findViewById(R.id.slot_2);
        slot3 = v.findViewById(R.id.slot_3);

        dialogEditEmail = new CustomDialog(requireActivity(), DialogKeys.PROFILE_EMAIL) {};
        dialogEditNickname = new CustomDialog(requireActivity(), DialogKeys.PROFILE_NICKNAME) {
            @Override
            public void onClick() {
                String nickname = Objects.requireNonNull(getEtInput().getText()).toString().trim();
                if (ValidationUtils.isNicknameValid(nickname)) {
                    userRepository.getProfile().updateNickname(nickname);
                    soundManager.playSfx(R.raw.sfx_success4);
                    btnNickname.setText(nickname);
                }
            }
        };

        dialogDeleteSlotNegative = new CustomDialog(requireActivity(), DialogKeys.DELETE_SLOT_NEGATIVE) {};
    }

    private void initListeners() {
        ivFrame.setOnClickListener(this);
        btnNickname.setOnClickListener(this);
        btnEmail.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == ivFrame) {
            replaceFragment(R.id.profile_container, new FramesFragment(), -1, -1);
        }
        else if (v == btnNickname) dialogEditNickname.show();
        else if (v == btnEmail) dialogEditEmail.show();
    }
}

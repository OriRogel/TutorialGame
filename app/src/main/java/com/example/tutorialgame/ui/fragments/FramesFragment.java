package com.example.tutorialgame.ui.fragments;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tutorialgame.R;
import com.example.tutorialgame.cloud.UserRepository;
import com.example.tutorialgame.engine.ui.circleframes.CircleFrameSeries;
import com.example.tutorialgame.engine.ui.circleframes.FrameData;
import com.example.tutorialgame.engine.ui.circleframes.FrameSeriesAdapter;
import com.example.tutorialgame.managers.BitmapManager;
import com.example.tutorialgame.ui.base.BaseFragment;

import java.text.MessageFormat;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FramesFragment extends BaseFragment implements View.OnClickListener {
    private RecyclerView recyclerView;
    private ImageButton btnClose;
    private TextView tvFramesCollected;
    @Inject UserRepository userRepository;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_frames;
    }

    @Override
    protected void onSetupView(View root) {
        bindViews(root);
        setupRecyclerView();
        onDataChanged();

        btnClose.setOnClickListener(this);
    }

    private void bindViews(View v) {
        recyclerView = v.findViewById(R.id.rv_main);
        btnClose = v.findViewById(R.id.imgBtnClose);
        btnClose.setImageBitmap(BitmapManager.getBitmap(R.drawable.ic_shuriken, 0.35f, false));
        tvFramesCollected = v.findViewById(R.id.tv_frames_collected);
    }

    private void setupRecyclerView() {
        List<CircleFrameSeries> allSeries = FrameData.getAllSeries();
        FrameSeriesAdapter mainAdapter = new FrameSeriesAdapter(allSeries, this::onDataChanged, userRepository);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mainAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v == btnClose) {
            replaceFragment(R.id.profile_container, new ProfileFragment(), -1, -1);
        }
    }

    private void onDataChanged() {
        // עדכון המונה העליון
        tvFramesCollected.setText(MessageFormat.format("{0}/67", userRepository.getCosmetic().getAvailableFrames().size()));
    }
}

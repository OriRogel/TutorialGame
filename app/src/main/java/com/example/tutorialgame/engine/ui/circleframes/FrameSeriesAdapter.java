package com.example.tutorialgame.engine.ui.circleframes;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tutorialgame.R;
import com.example.tutorialgame.cloud.UserRepository;

import java.util.List;

public class FrameSeriesAdapter extends RecyclerView.Adapter<FrameSeriesAdapter.SeriesViewHolder> implements FramesAdapter.OnDataChangedListener {

    public interface FrameSelectionListener {
        void onFrameSelectionChanged(int seriesPosition, int framePosition);
    }

    private final List<CircleFrameSeries> seriesList;
    // שימוש ב-SparseArray לניהול אדפטרים לפי מיקום השורה
    private final SparseArray<FramesAdapter> childAdapters = new SparseArray<>();
    private int selectedSeriesPosition = -1;
    private int selectedFramePosition = -1;
    private final Runnable globalRefreshTask;
    private final UserRepository userRepository;

    public FrameSeriesAdapter(List<CircleFrameSeries> seriesList, Runnable globalRefreshTask, UserRepository userRepository) {
        this.seriesList = seriesList;
        this.globalRefreshTask = globalRefreshTask;
        this.userRepository = userRepository;
        findInitialSelection();
    }

    private void findInitialSelection() {
        String currentFrameName = userRepository.getCosmetic().getCurrentFrame();
        if (currentFrameName == null) return;

        for (int i = 0; i < seriesList.size(); i++) {
            List<CircleFrames> frames = seriesList.get(i).getFrames();
            for (int j = 0; j < frames.size(); j++) {
                if (frames.get(j).name().equals(currentFrameName)) {
                    selectedSeriesPosition = i;
                    selectedFramePosition = j;
                    return;
                }
            }
        }
    }

    @Override
    public void onDataChanged(int seriesPos) {
        // 1. עדכון המונה העליון בפרגמנט
        if (globalRefreshTask != null) globalRefreshTask.run();

        // 2. עדכון ממוקד של שאר השורות
        for (int i = 0; i < childAdapters.size(); i++) {
            int key = childAdapters.keyAt(i);
            // מדלגים על השורה שבה בוצעה הרכישה כדי לא להרוס לה את האנימציה
            if (key != seriesPos) {
                FramesAdapter adapter = childAdapters.get(key);
                if (adapter.getConditionType() == FrameUnlockCondition.Condition.PURCHASE) {
                    adapter.refreshLockedItemsStatus();
                }
            }
        }
    }

    @NonNull
    @Override
    public SeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_frame_series, parent, false);
        return new SeriesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeriesViewHolder holder, int position) {
        CircleFrameSeries series = seriesList.get(position);
        holder.bind(series, position, (seriesPos, framePos) -> {
            int previousSeriesPos = selectedSeriesPosition;
            selectedSeriesPosition = seriesPos;
            selectedFramePosition = framePos;

            // עדכון השורות הרלוונטיות לבחירה
            notifyItemChanged(seriesPos);
            if (previousSeriesPos != -1 && previousSeriesPos != seriesPos) {
                notifyItemChanged(previousSeriesPos);
            }
        }, this);
    }

    @Override
    public int getItemCount() {
        return seriesList.size();
    }

    public class SeriesViewHolder extends RecyclerView.ViewHolder {
        TextView seriesName;
        RecyclerView framesRecyclerView;

        public SeriesViewHolder(@NonNull View itemView) {
            super(itemView);
            seriesName = itemView.findViewById(R.id.tv_series_name);
            framesRecyclerView = itemView.findViewById(R.id.rv_frames);
        }

        private void bind(CircleFrameSeries series, int currentSeriesPosition, FrameSelectionListener listener, FramesAdapter.OnDataChangedListener dataChangedListener) {
            seriesName.setText(series.getSeriesName());

            String selectedFrameName = (currentSeriesPosition == selectedSeriesPosition)
                    ? series.getFrames().get(selectedFramePosition).name()
                    : null;

            FramesAdapter framesAdapter = new FramesAdapter(series, currentSeriesPosition, selectedFrameName, listener, dataChangedListener, userRepository);
            childAdapters.put(currentSeriesPosition, framesAdapter);
            
            framesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            framesRecyclerView.setAdapter(framesAdapter);
        }
    }
}

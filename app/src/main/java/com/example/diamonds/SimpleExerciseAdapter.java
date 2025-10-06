package com.example.diamonds;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SimpleExerciseAdapter extends RecyclerView.Adapter<SimpleExerciseAdapter.ViewHolder> {

    private List<SimpleExerciseItem> exerciseList;
    private Context context;

    public SimpleExerciseAdapter(List<SimpleExerciseItem> exerciseList, Context context) {
        this.exerciseList = exerciseList;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView exerciseImage;
        TextView titleView, descriptionView;

        public ViewHolder(View itemView) {
            super(itemView);
            exerciseImage = itemView.findViewById(R.id.exerciseImage);
            titleView = itemView.findViewById(R.id.simpleExerciseTitleView);
            descriptionView = itemView.findViewById(R.id.simpleExerciseDescriptionView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.simple_exercise_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SimpleExerciseItem item = exerciseList.get(position);
        holder.exerciseImage.setImageResource(item.getImageResId());
        holder.titleView.setText(item.getTitle());
        holder.descriptionView.setText(item.getShortDescription());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SimpleExerciseDetails.class);
            intent.putExtra("imageResId", item.getImageResId());
            intent.putExtra("title", item.getTitle());
            intent.putExtra("description", item.getFullDescription());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }
}



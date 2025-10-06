package com.example.diamonds;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<VideoItem> videoList;
    private Context context;

    public VideoAdapter(List<VideoItem> videoList, Context context) {
        this.videoList = videoList;
        this.context = context;
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        ImageView thumbnail;

        public VideoViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.simpleExerciseTitleView);
            description = itemView.findViewById(R.id.simpleExerciseDescriptionView);
            thumbnail = itemView.findViewById(R.id.exerciseImage);
        }
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        VideoItem item = videoList.get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getShortDescription());

        // Load thumbnail from YouTube
        String imageUrl = "https://img.youtube.com/vi/" + item.getVideoId() + "/0.jpg";
        Glide.with(context).load(imageUrl).into(holder.thumbnail); // πρόσθεσε το Glide στα dependencies

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.putExtra("videoId", item.getVideoId());
            intent.putExtra("title", item.getTitle());
            intent.putExtra("htmlFile", item.getFullDescription());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }
}


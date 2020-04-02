package com.example.testappsyncmanager.ui.view;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testappsyncmanager.R;
import com.example.testappsyncmanager.ui.openchat.OpenChatActivity;
import com.example.testappsyncmanager.utilities.ImageUtils;

import java.util.ArrayList;

public class HorizontalRecyclerAdapter
        extends RecyclerView.Adapter<HorizontalRecyclerAdapter.HorizontalImageListViewHolder> {

    public void setList(@NonNull ArrayList<ImageListItem> list) {
        this.list = list;
        this.notifyDataSetChanged();
    }

    public interface HorizontalImageListItemClickListener {
        void onItemClick(@NonNull ImageListItem imageMessage);
    }

    private ArrayList<ImageListItem> list;
    private HorizontalImageListItemClickListener itemClickListener;

    public HorizontalRecyclerAdapter(
            @NonNull HorizontalImageListItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public HorizontalRecyclerAdapter.HorizontalImageListViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.horizontal_list_item, parent, false);
        return new HorizontalImageListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull HorizontalRecyclerAdapter.HorizontalImageListViewHolder holder, int position) {
        holder.bind(list.get(position), itemClickListener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class HorizontalImageListViewHolder extends RecyclerView.ViewHolder {

        private final TextView descTextView;
        private final ImageView imageView;
        private final TextView nameTextView;

        HorizontalImageListViewHolder(@NonNull final View itemView) {
            super(itemView);
            this.nameTextView= itemView.findViewById(R.id.horizontal_list_item_name);
            this.descTextView = itemView.findViewById(R.id.horizontal_list_item_desc);
            this.imageView = itemView.findViewById(R.id.horizontal_list_item_iv);
        }

        void bind(@NonNull final ImageListItem imageListItem,
                  @NonNull final HorizontalImageListItemClickListener itemClickListener) {
//            descTextView.setText(imageListItem.imageDesc);
            if (!TextUtils.isEmpty(imageListItem.imageUrl)) {
                nameTextView.setText(imageListItem.imageUrl);

                Log.e("image_disp_url", imageListItem.imageUrl);
                ImageUtils.displayImageFromUrl(itemView.getContext(),
                            imageListItem.imageUrl.startsWith("http")? imageListItem.imageUrl:
                        OpenChatActivity.IMAGE_BASE_URL + imageListItem.imageUrl,
                        imageView, null);
            } else {
                ImageUtils.displayImageFromUri(itemView.getContext(), imageListItem.imageResId,
                        imageView);
                Log.e("image_disp_uri", imageListItem.imageResId);

            }


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickListener.onItemClick(imageListItem);
                }
            });
        }
    }


}

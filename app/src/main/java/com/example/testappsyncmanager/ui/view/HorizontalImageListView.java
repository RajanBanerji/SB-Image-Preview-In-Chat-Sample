package com.example.testappsyncmanager.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testappsyncmanager.R;

import java.util.ArrayList;

public class HorizontalImageListView extends LinearLayout
        implements HorizontalRecyclerAdapter.HorizontalImageListItemClickListener {
    private HorizontalRecyclerAdapter adapter;
    private HorizontalRecyclerAdapter.HorizontalImageListItemClickListener itemClickListener;
    private ImageButton leftButton, rightButton;
    private RecyclerView horizontalList;
    private LinearLayoutManager horizontalLayoutManager;

    public HorizontalImageListView(Context context) {
        super(context);
        init();
    }

    public HorizontalImageListView(Context context,
                                   @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizontalImageListView(Context context, @Nullable AttributeSet attrs,
                                   int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_horizontal_list, this);

        adapter = new HorizontalRecyclerAdapter(this);
         horizontalList= findViewById(R.id.horizontal_list);
        leftButton = findViewById(R.id.left_arrow);
        rightButton = findViewById(R.id.right_arrow);
        horizontalLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        horizontalList.setLayoutManager(horizontalLayoutManager);

        horizontalList.setAdapter(adapter);
        setRecyclerListeners();
    }

    private void setRecyclerListeners() {
        leftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (horizontalLayoutManager.findFirstVisibleItemPosition() > 0) {
                    horizontalList.smoothScrollToPosition(horizontalLayoutManager.findFirstVisibleItemPosition() - 1);
                } else {
                    horizontalList.smoothScrollToPosition(0);
//                    v.setVisibility(View.INVISIBLE);
                }


            }
        } );

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(horizontalLayoutManager.findLastVisibleItemPosition() < adapter.getItemCount() -1)
                    horizontalList.smoothScrollToPosition(horizontalLayoutManager.findLastVisibleItemPosition() + 1);
                else{
                    horizontalList.smoothScrollToPosition(adapter.getItemCount() -1);
//                    v.setVisibility(View.INVISIBLE);
                }


            }
        });

        horizontalList.setOnScrollChangeListener(new OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

              ensureVisibilityOfArrows();
            }
        });
    }

    private void ensureVisibilityOfArrows(){
        if(horizontalLayoutManager.findFirstVisibleItemPosition() > 0){
            leftButton.setVisibility(VISIBLE);
        }else {
            leftButton.setVisibility(INVISIBLE);
        }
        if(horizontalLayoutManager.findLastVisibleItemPosition() < adapter.getItemCount() -1){
            rightButton.setVisibility(VISIBLE);
        }else {
            rightButton.setVisibility(INVISIBLE);
        }
    }

    public void setData(@NonNull ArrayList<ImageListItem> imageList){
        adapter.setList(imageList);
        ensureVisibilityOfArrows();
    }

    public void setItemClickListener(HorizontalRecyclerAdapter.HorizontalImageListItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }


    @Override
    public void onItemClick(@NonNull ImageListItem imageListItem) {
        if(itemClickListener!=null) {
            itemClickListener.onItemClick(imageListItem);
        }
    }
}

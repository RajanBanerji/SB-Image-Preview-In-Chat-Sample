package com.example.testappsyncmanager.ui.view;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class ImageListItem{
    @NonNull String imageTitle;
    @NonNull String imageDesc;
    @NonNull String imageUrl;
    @NonNull String webUrl;
    @Nullable
    String imageResId;
    private boolean isChecked;

    @NonNull
    public String getImageTitle() {
        return imageTitle;
    }

    public void setImageTitle(@NonNull String imageTitle) {
        this.imageTitle = imageTitle;
    }

    @NonNull
    public String getImageDesc() {
        return imageDesc;
    }

    public void setImageDesc(@NonNull String imageDesc) {
        this.imageDesc = imageDesc;
    }

    @NonNull
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(@NonNull String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageResId() {
        return imageResId;
    }

    public void setImageResId(String imageResId) {
        this.imageResId = imageResId;
    }

    @NonNull
    public String getWebUrl() {
        return webUrl;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public ImageListItem(@NonNull String imageTitle, @NonNull String imageDesc,
                         @NonNull String imageUrl, @NonNull String webUrl, String imageResId) {


        this.imageTitle = imageTitle;
        this.imageDesc = imageDesc;
        this.imageUrl = imageUrl;
        this.imageResId = imageResId;
        this.webUrl = webUrl;
    }
}

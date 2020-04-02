package com.example.testappsyncmanager.models;

import androidx.annotation.Nullable;

import com.example.testappsyncmanager.ui.view.ImageMessage;
import com.sendbird.android.BaseMessage;

public class ChatMessage {
    private BaseMessage baseMessage;
    private ImageMessage imageMessage;
    private boolean isImageListType;

    public ChatMessage(@Nullable BaseMessage baseMessage,
                       @Nullable ImageMessage imageMessage, boolean isImageListType) {
        this.baseMessage = baseMessage;
        this.imageMessage = imageMessage;
        this.isImageListType = isImageListType;
    }

    public BaseMessage getBaseMessage() {
        return baseMessage;
    }

    public ImageMessage getImageMessage() {
        return imageMessage;
    }

    public boolean isImageListType() {
        return isImageListType;
    }

    public void setBaseMessage(BaseMessage baseMessage) {
        this.baseMessage = baseMessage;
    }

    public void setImageMessage(ImageMessage imageMessage) {
        this.imageMessage = imageMessage;
    }

    public void setImageListType(boolean imageListType) {
        isImageListType = imageListType;
    }
}

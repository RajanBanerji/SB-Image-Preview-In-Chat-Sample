package com.example.testappsyncmanager.ui.view;

import com.sendbird.android.User;

import java.util.ArrayList;

public class ImageMessage {
    private final ArrayList<ImageListItem> list;

    public ImageMessage(ArrayList<ImageListItem> list, User user) {
        this.list = list;
        this.user = user;
    }
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<ImageListItem> getList() {
        return list;
    }
}

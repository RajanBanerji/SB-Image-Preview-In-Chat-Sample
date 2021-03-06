package com.example.testappsyncmanager.connection;

import android.util.Log;

import com.example.testappsyncmanager.utilities.PreferenceUtils;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.syncmanager.SendBirdSyncManager;


public class ChatConnectionManager {
    public static void login(String userId, final SendBird.ConnectHandler handler) {
        SendBird.connect(userId, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (handler != null) {
                    handler.onConnected(user, e);
                }
            }
        });
    }

    public static void logout(final SendBird.DisconnectHandler handler) {
        SendBird.disconnect(new SendBird.DisconnectHandler() {
            @Override
            public void onDisconnected() {
                if (handler != null) {
                    handler.onDisconnected();
                }
            }
        });
    }
    public static void addConnectionManagementHandler(String handlerId, final ConnectionManagementHandler handler) {
        SendBird.addConnectionHandler(handlerId, new SendBird.ConnectionHandler() {
            @Override
            public void onReconnectStarted() {
                Log.d("LIFECYCLE", "onConnected onReconnectSucceeded1000()");
                SendBirdSyncManager.getInstance().pauseSync();
            }

            @Override
            public void onReconnectSucceeded() {
                SendBirdSyncManager.getInstance().resumeSync();
                if (handler != null) {
                    Log.d("LIFECYCLE", "onConnected onReconnectSucceeded00()");
                    handler.onConnected(true);
                }
            }

            @Override
            public void onReconnectFailed() {
            }
        });

        if (SendBird.getConnectionState() == SendBird.ConnectionState.OPEN) {
            if (handler != null) {
                handler.onConnected(false);
            }
        } else if (SendBird.getConnectionState() == SendBird.ConnectionState.CLOSED) { // push notification or system kill
            String userId = PreferenceUtils.getUserId();
            SendBird.connect(userId, new SendBird.ConnectHandler() {
                @Override
                public void onConnected(User user, SendBirdException e) {
                    if (e != null) {
                        Log.d("LIFECYCLE", "onConnected onReconnectSucceeded10()");
                        return;
                    }

                    SendBirdSyncManager.getInstance().resumeSync();
                    if (handler != null) {
                        Log.d("LIFECYCLE", "onConnected onReconnectSucceeded11()");
                        handler.onConnected(false);
                    }
                }
            });
        }
    }

    public static void removeConnectionManagementHandler(String handlerId) {
        SendBird.removeConnectionHandler(handlerId);
    }

    public interface ConnectionManagementHandler {
        void onConnected(boolean reconnect);
    }
}

package com.example.testappsyncmanager.ui.openchat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testappsyncmanager.R;
import com.example.testappsyncmanager.adapters.GroupImageListAdaptor;
import com.example.testappsyncmanager.adapters.OpenChatAdapter;
import com.example.testappsyncmanager.connection.ChatConnectionManager;
import com.example.testappsyncmanager.models.ChatMessage;
import com.example.testappsyncmanager.ui.view.ImageListItem;
import com.example.testappsyncmanager.ui.view.ImageListItemDrawable;
import com.example.testappsyncmanager.ui.view.ImageMessage;
import com.example.testappsyncmanager.utilities.FileUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.MessageMetaArray;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.PreviousMessageListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

public class OpenChatActivity extends AppCompatActivity {
    private static final String LOG_TAG = OpenChatActivity.class.getSimpleName();

    private static final int CHANNEL_LIST_LIMIT = 30;
    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_OPEN_CHAT";
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHAT";

    private static final int STATE_NORMAL = 0;
    private static final int STATE_EDIT = 1;

    private static final int INTENT_REQUEST_CHOOSE_IMAGE = 300;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 13;

    static final String EXTRA_CHANNEL_URL = "CHANNEL_URL";
    private static final String IMAGE_LIST_CUSTOM_TYPE = "image_list";

    public static final String IMAGE_BASE_URL = "https://sendbird-eu-1.s3.amazonaws.com/361BACE2-A48A-4E5D-BDBF-AFD544877AE4/upload/n/";
    private static final String IMAGE_LIST_AS_DATA_CUSTOM_TYPE = "yoyo";

    InputMethodManager mIMM;

    RecyclerView mRecyclerView;
    OpenChatAdapter mChatAdapter;
    LinearLayoutManager mLayoutManager;
    private View mRootLayout;
    EditText mMessageEditText;
    Button mMessageSendButton;
    private ImageButton mUploadFileButton;
    private View mCurrentEventLayout;
    private TextView mCurrentEventText;

    OpenChannel mChannel;
    String mChannelUrl;
    private PreviousMessageListQuery mPrevMessageListQuery;

    int mCurrentState = STATE_NORMAL;
    BaseMessage mEditingMessage = null;
    public ArrayList<ImageListItem> mSelectedList;

    private boolean isRequestedFileUpload = false;
    private boolean isRequestedGalleryUpload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_chat);
        mRootLayout = findViewById(R.id.layout_open_chat_root);
        mRecyclerView = findViewById(R.id.recycler_open_channel_chat);
        mCurrentEventLayout = findViewById(R.id.layout_open_chat_current_event);
        mCurrentEventText = findViewById(R.id.text_open_chat_current_event);
        setUpChatAdapter();
        setUpRecyclerView();
        mMessageSendButton = findViewById(R.id.button_open_channel_chat_send);
        mMessageEditText = findViewById(R.id.edittext_chat_message);

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    mMessageSendButton.setEnabled(true);
                } else {
                    mMessageSendButton.setEnabled(false);
                }
            }
        });

        mMessageSendButton.setEnabled(false);
        mMessageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentState == STATE_EDIT) {
                    String userInput = mMessageEditText.getText().toString();
                    if (userInput.length() > 0) {
                        if (mEditingMessage != null) {
                            editMessage(mEditingMessage, mMessageEditText.getText().toString());
                        }
                    }
                    setState(STATE_NORMAL, null, -1);
                } else {
                    String userInput = mMessageEditText.getText().toString();
                    if (userInput.length() > 0) {
                        sendUserMessage(userInput);
                        mMessageEditText.setText("");
                    }
                }
            }
        });

        mUploadFileButton = findViewById(R.id.button_open_channel_chat_upload);
        mUploadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUploadFileBtnClick();

            }
        });
        mChannelUrl = getIntent().getStringExtra(OpenChatListActivity.OPEN_CHANNEL_URL);
        mIMM = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
    }

    protected void onUploadFileBtnClick() {
        final BottomSheetDialog dialog = new BottomSheetDialog(OpenChatActivity.this);
        dialog.setContentView(R.layout.open_chat_bottomsheet_layout);
        dialog.setCanceledOnTouchOutside(true);
        AppCompatButton buttonGallery = dialog.findViewById(R.id.button_gallery);
        buttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isRequestedGalleryUpload = true;
                isRequestedFileUpload = false;

                requestImage();
            }
        });
        AppCompatButton buttonGroupItems = dialog.findViewById(R.id.button_group_items);
        buttonGroupItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isRequestedFileUpload = false;
                isRequestedGalleryUpload = false;
                isStoragePermissionGranted();
            }
        });
        dialog.findViewById(R.id.button_group_file_items).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isRequestedFileUpload = true;
                isRequestedGalleryUpload = false;

                isStoragePermissionGranted();
            }
        });
        dialog.show();
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                showGroupImageList();
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
//			isStoragePermissionGranted();
            showGroupImageList();
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_WRITE_EXTERNAL_STORAGE:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Snackbar.make(mRootLayout, "Storage permissions granted. You can now upload or download files.",
                            Snackbar.LENGTH_LONG)
                            .show();
                    if (isRequestedGalleryUpload)
                        requestImage();
                    else
                        isStoragePermissionGranted();
                } else {
                    Snackbar.make(mRootLayout, "Permissions denied.",
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SendBird.setAutoBackgroundDetection(true);
        if (requestCode == INTENT_REQUEST_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            if (data.getData() != null) {
                ArrayList<Uri> uris = new ArrayList<>();
                uris.add(data.getData());
                showUploadConfirmDialog(uris);
            }
            else {
                //If uploaded with the new Android Photos gallery
                ClipData clipData = data.getClipData();
//                files.clear();
                if (clipData != null) {
                    ArrayList<Uri> uris = new ArrayList<>(clipData.getItemCount());
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        uris.add(item.getUri());
//                        String url = FileUtils2.getPath(this, uri);
//                        assert url != null;
//                        File file = new File(url);
//                        files.add(file);
                    }
                    showUploadConfirmDialog(uris);

                }
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        ChatConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID, new ChatConnectionManager.ConnectionManagementHandler() {
            @Override
            public void onConnected(boolean reconnect) {
                if (reconnect) {
                    refresh();
                } else {
                    refreshFirst();
                }
            }
        });
        final ArrayList<ImageListItem> asLinkItems = new ArrayList<>();
        final ArrayList<ImageListItem> asImageItems = new ArrayList<>();

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {

                ChatMessage cm = null;
                if (baseChannel.getUrl().equals(mChannelUrl)) {
                    if(baseMessage.getCustomType().equals(IMAGE_LIST_AS_DATA_CUSTOM_TYPE)){
                        asImageItems.clear();
                        UserMessage msg = (UserMessage) baseMessage;
                        asLinkItems.add(new ImageListItem(msg.getData(), msg.getMessage(),
                                msg.getData(),
                                msg.getData(), null));
                        if(asLinkItems.size() == 1 ){
                            cm = new ChatMessage(baseMessage, new ImageMessage(asLinkItems, msg.getSender()), false);
                            mChatAdapter.addFirst(cm);
                        }else {
                            cm = mChatAdapter.getItemAt(0);
//                            cm = new ChatMessage(cm.getBaseMessage(), new ImageMessage(asLinkItems, msg.getSender()), false);
                            cm.setImageMessage(new ImageMessage(asLinkItems, msg.getSender()));
                            cm.setImageListType(true);
                            mChatAdapter.notifyDataSetChanged();
                        }
                    }else if(baseMessage.getCustomType().equals(IMAGE_LIST_CUSTOM_TYPE)){
                            asLinkItems.clear();
                        FileMessage msg = (FileMessage) baseMessage;
                        asImageItems.add(new ImageListItem(msg.getName(), msg.getName(),
                                msg.getUrl(),
                                msg.getUrl(), null));
                        if(asImageItems.size() == 1){
                            cm = new ChatMessage(baseMessage, new ImageMessage(asImageItems, msg.getSender()), false);
                            mChatAdapter.addFirst(cm);
                        }else {
                            cm = mChatAdapter.getItemAt(0);
//                            cm = new ChatMessage(cm.getBaseMessage(), new ImageMessage(asLinkItems, msg.getSender()), false);
                            cm.setImageMessage(new ImageMessage(asImageItems, msg.getSender()));
                            cm.setImageListType(true);
                            mChatAdapter.notifyDataSetChanged();
                        }
                    }
                    else {
                        asLinkItems.clear();
                        asImageItems.clear();
                        cm = new ChatMessage(baseMessage, null, false);
                        mChatAdapter.addFirst(cm);
                    }

                }
            }

            @Override
            public void onMessageDeleted(BaseChannel baseChannel, long msgId) {
                super.onMessageDeleted(baseChannel, msgId);
                if (baseChannel.getUrl().equals(mChannelUrl)) {
                    mChatAdapter.delete(msgId);
                }
            }

            @Override
            public void onMessageUpdated(BaseChannel channel, BaseMessage message) {
                super.onMessageUpdated(channel, message);
                if (channel.getUrl().equals(mChannelUrl)) {
                    mChatAdapter.update(new ChatMessage(message, null, false));
                }
            }
        });
    }


    @Override
    public void onPause() {
        ChatConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID);
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mChannel != null) {
            mChannel.exit(new OpenChannel.OpenChannelExitHandler() {
                @Override
                public void onResult(SendBirdException e) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }
                }
            });
        }

        super.onDestroy();
    }

    private void setUpChatAdapter() {
        mChatAdapter = new OpenChatAdapter(OpenChatActivity.this);
        mChatAdapter.setOnItemClickListener(new OpenChatAdapter.OnItemClickListener() {
            @Override
            public void onUserMessageItemClick(UserMessage message) {
            }

            @Override
            public void onFileMessageItemClick(FileMessage message) {
                onFileMessageClicked(message);
            }

            @Override
            public void onAdminMessageItemClick(AdminMessage message) {
            }

            @Override
            public void onImageMessageListItemClick(ImageMessage message, ImageListItem clickedItem) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickedItem.getWebUrl()));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        mChatAdapter.setOnItemLongClickListener(new OpenChatAdapter.OnItemLongClickListener() {
            @Override
            public void onBaseMessageLongClick(ChatMessage message, int position) {
                if (!message.isImageListType()) {
                    showMessageOptionsDialog(message.getBaseMessage(), position);
                }

            }
        });
    }

    void onFileMessageClicked(FileMessage message) {
        String type = message.getType().toLowerCase();
        if (type.startsWith("image")) {
            Intent i = new Intent(this, ShowImageActivity.class);
            i.putExtra("url", message.getUrl());
            i.putExtra("type", message.getType());
            startActivity(i);
        } else if (type.startsWith("video")) {
            Toast.makeText(this, "Media player not found....", Toast.LENGTH_SHORT).show();
        } else {
            showDownloadConfirmDialog(message);
        }
    }

    private void showDownloadConfirmDialog(final FileMessage message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermissions();
        } else {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setMessage("Download file?")
                    .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                FileUtils.downloadFile(OpenChatActivity.this, message.getUrl(), message.getName());
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null).show();
        }

    }

    void showMessageOptionsDialog(final BaseMessage message, final int position) {
        String[] options;
        if (message instanceof FileMessage) {
            options = new String[]{"Delete message"};
        } else {
            options = new String[]{"Delete message", "Edit message"};
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(OpenChatActivity.this);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    deleteMessage(message);
                } else if (which == 1) {
                    setState(STATE_EDIT, message, position);
                }
            }
        });
        builder.create().show();
    }

    void setState(int state, BaseMessage editingMessage, final int position) {
        switch (state) {
            case STATE_NORMAL:
                mCurrentState = STATE_NORMAL;
                mEditingMessage = null;

                mUploadFileButton.setVisibility(View.VISIBLE);
                mMessageSendButton.setText("SEND");
                mMessageEditText.setText("");
                break;

            case STATE_EDIT:
                mCurrentState = STATE_EDIT;
                mEditingMessage = editingMessage;

                mUploadFileButton.setVisibility(View.GONE);
                mMessageSendButton.setText("SAVE");
                String messageString = ((UserMessage) editingMessage).getMessage();
                if (messageString == null) {
                    messageString = "";
                }
                mMessageEditText.setText(messageString);
                if (messageString.length() > 0) {
                    mMessageEditText.setSelection(0, messageString.length());
                }

                mMessageEditText.requestFocus();
                mMessageEditText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIMM.showSoftInput(mMessageEditText, 0);

                        mRecyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mRecyclerView.scrollToPosition(position);
                            }
                        }, 500);
                    }
                }, 100);
                break;
        }
    }

    private void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(OpenChatActivity.this);
        mLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mChatAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (mLayoutManager.findLastVisibleItemPosition() == mChatAdapter.getItemCount() - 1) {
                    loadNextMessageList(CHANNEL_LIST_LIMIT);
                }
            }
        });
    }

    private void showUploadConfirmDialog(final List<Uri> uris) {
        new AlertDialog.Builder(OpenChatActivity.this)
                .setMessage("Upload files?")
                .setPositiveButton(R.string.upload, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        ArrayList<Uri> uris = new ArrayList<>();
//                        uris.add(uris);
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            mSelectedList = new ArrayList<>();
                            for (Uri uri : uris
                            ) {
                                Hashtable<String, Object> info = FileUtils.getFileInfo(OpenChatActivity.this, uri);
                                final File file = new File((String) info.get("path"));
                                mSelectedList.add(new ImageListItem(file.getName(), "", "",
                                        "", uri.toString()));
                            }
                            sendImageWithThumbnail(uris, uris.size() == 1, null);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null).show();
    }

    public Uri getUriToDrawable(String drawableId) {
        return Uri.fromFile(new File(drawableId));
    }

    void requestImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermissions();
        } else {
            Intent intent = new Intent();
            intent.setType("image/* video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, "Select Media"), INTENT_REQUEST_CHOOSE_IMAGE);
            SendBird.setAutoBackgroundDetection(false);
        }
    }

    private void requestStoragePermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(mRootLayout, "Storage access permissions are required to upload/download files.",
                    Snackbar.LENGTH_LONG)
                    .setAction("Okay", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    PERMISSION_WRITE_EXTERNAL_STORAGE);
                        }
                    })
                    .show();
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    void refreshFirst() {
        enterChannel(mChannelUrl);
    }

    private void enterChannel(String channelUrl) {
        OpenChannel.getChannel(channelUrl, new OpenChannel.OpenChannelGetHandler() {
            @Override
            public void onResult(final OpenChannel openChannel, SendBirdException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }

                openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
                    @Override
                    public void onResult(SendBirdException e) {
                        if (e != null) {
                            e.printStackTrace();
                            return;
                        }
                        mChannel = openChannel;
                        if (this != null) {
                            setTitle(mChannel.getName());
                        }
                        refresh();
                    }
                });
            }
        });
    }

    void sendUserMessage(String text) {
        mChannel.sendUserMessage(text, new BaseChannel.SendUserMessageHandler() {
            @Override
            public void onSent(UserMessage userMessage, SendBirdException e) {
                if (e != null) {
                    // Error!
                    Toast.makeText(
                            OpenChatActivity.this,
                            "Send failed with error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                // Display sent message to RecyclerView
                mChatAdapter.addFirst(new ChatMessage(userMessage, null, false));
            }
        });
    }

    void sendImageWithThumbnail(final List<Uri> uris, final boolean isSingleImage, @Nullable final String msgIdentifier) {
        if (uris != null && uris.size() > 0) {
            Hashtable<String, Object> info = FileUtils.getFileInfo(this, uris.get(0));
            final String path = (String) info.get("path");
            final File file = new File(path);
            final String name = file.getName();
            final String mime = (String) info.get("mime");
            final int size = (Integer) info.get("size");

            if (path.equals("")) {
                Toast.makeText(this, "File must be located in local storage.", Toast.LENGTH_LONG).show();
            } else {
                Log.e("file_size", size + " ");
                List<FileMessage.ThumbnailSize> thumbnailSizes = new ArrayList<>();
                thumbnailSizes.add(new FileMessage.ThumbnailSize(240, 240));
                thumbnailSizes.add(new FileMessage.ThumbnailSize(320, 320));
                getChannel().sendFileMessage(file, name, mime, size, "", isSingleImage ? null : IMAGE_LIST_CUSTOM_TYPE,
                        thumbnailSizes, new BaseChannel.SendFileMessageHandler() {
                            @Override
                            public void onSent(FileMessage fileMessage, SendBirdException e) {

                                uris.remove(0);
                                if (e != null) {
                                    Toast.makeText(OpenChatActivity.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Log.e("file_url", fileMessage.getUrl());

                                if (isSingleImage) {
                                    mChatAdapter.addFirst(new ChatMessage(fileMessage, null, false));
                                } else {
                                    if (uris.size() == 0) {
                                        mChatAdapter.addFirst(new ChatMessage(fileMessage, new ImageMessage(mSelectedList,
                                                fileMessage.getSender()), true));
                                    }
                                }
                                if (msgIdentifier != null) {
                                    ArrayList<MessageMetaArray> metas = new ArrayList<>(1);
                                    metas.add(new MessageMetaArray(msgIdentifier, new ArrayList<String>(1)));

                                    getChannel().addMessageMetaArrayValues(fileMessage, metas, new BaseChannel.MessageMetaArrayHandler() {
                                        @Override
                                        public void onResult(BaseMessage baseMessage, SendBirdException e) {
                                            if (e != null) {    // Error.
                                                Log.e("meta_exc", "exception", e);
                                                return;
                                            }
                                            Log.e("meta_sent", msgIdentifier);
                                            sendImageWithThumbnail(uris, isSingleImage, msgIdentifier);
                                        }
                                    });
                                } else
                                    sendImageWithThumbnail(uris, isSingleImage, msgIdentifier);
                            }
                        });
            }
        }
    }

    void sendImageInUserMsg(final ArrayList<ImageListItem> imageList, @Nullable final String msgIdentifier) {
        if (imageList.size() > 0) {
            ImageListItem imageListItem = imageList.get(0);
            getChannel().sendUserMessage(null, imageListItem.getImageUrl(),
                    IMAGE_LIST_AS_DATA_CUSTOM_TYPE, null,
                    new BaseChannel.SendUserMessageHandler() {
                        @Override
                        public void onSent(UserMessage userMessage, SendBirdException e) {
                            Log.e("user_sent", userMessage.getData());
                            imageList.remove(0);
                            if (e != null) {
                                Toast.makeText(OpenChatActivity.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                return;
                            }


                            if (imageList.size() == 0) {
                                mChatAdapter.addFirst(new ChatMessage(userMessage, new ImageMessage(mSelectedList,
                                        userMessage.getSender()), true));
                            }

                            if (msgIdentifier != null) {
                                ArrayList<MessageMetaArray> metas = new ArrayList<>(1);
                                metas.add(new MessageMetaArray(msgIdentifier, new ArrayList<String>(1)));

                                getChannel().addMessageMetaArrayValues(userMessage, metas, new BaseChannel.MessageMetaArrayHandler() {
                                    @Override
                                    public void onResult(BaseMessage baseMessage, SendBirdException e) {
                                        if (e != null) {    // Error.
                                            Log.e("meta_exc", "exception", e);
                                            return;
                                        }
                                        Log.e("meta_sent", msgIdentifier);
                                        sendImageInUserMsg(imageList, msgIdentifier);
                                    }
                                });
                            } else
                                sendImageInUserMsg(imageList, msgIdentifier);
                        }
                    });

        }
    }

    @SuppressLint("StaticFieldLeak")
    void sendImageWithThumbnailFromUrls(final ArrayList<ImageListItem> list, final boolean isSingleImage, @Nullable final String msgIdentifier) {
        if (list == null)
            return;
        final ArrayList<ImageListItem> imageList = new ArrayList<>(list.size());
        imageList.addAll(list);
        if (imageList.size() > 0) {
            final ImageListItem image = imageList.get(0);
            final String name = image.getImageTitle();
            final String mime = "image/jpeg";
            new AsyncTask<String, Void, Integer>() {
                @Override
                protected Integer doInBackground(String... strings) {

                    try {
                        Log.e("url", image.getImageUrl() + " kkk");
                        URL myUrl = new URL(image.getImageUrl());
                        URLConnection urlConnection = myUrl.openConnection();
                        urlConnection.connect();

                        return urlConnection.getContentLength();


                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Integer size) {
                    if (image.getImageUrl().isEmpty() || size == null) {
                        Toast.makeText(OpenChatActivity.this, "File must be located in local storage.", Toast.LENGTH_LONG).show();
                    } else {

                        getChannel().sendFileMessage(image.getImageUrl(), name, mime, size, "", isSingleImage ? null : IMAGE_LIST_CUSTOM_TYPE,
                                new BaseChannel.SendFileMessageHandler() {
                                    @Override
                                    public void onSent(FileMessage fileMessage, SendBirdException e) {

                                        imageList.remove(0);
                                        if (e != null) {
                                            Toast.makeText(OpenChatActivity.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        Log.e("file_url", fileMessage.getUrl());

                                        if (isSingleImage) {
                                            mChatAdapter.addFirst(new ChatMessage(fileMessage, null, false));
                                        } else {
                                            Log.e("selected_size", mSelectedList.size() + " hh");

                                            if (imageList.size() == 0) {
                                                mChatAdapter.addFirst(new ChatMessage(fileMessage, new ImageMessage(mSelectedList,
                                                        fileMessage.getSender()), true));
                                            }
                                        }
                                        if (msgIdentifier != null) {
                                            ArrayList<MessageMetaArray> metas = new ArrayList<>(1);
                                            metas.add(new MessageMetaArray(msgIdentifier, new ArrayList<String>(1)));

                                            getChannel().addMessageMetaArrayValues(fileMessage, metas, new BaseChannel.MessageMetaArrayHandler() {
                                                @Override
                                                public void onResult(BaseMessage baseMessage, SendBirdException e) {
                                                    if (e != null) {    // Error.
                                                        Log.e("meta_exc", "exception", e);
                                                        return;
                                                    }
                                                    Log.e("meta_sent", msgIdentifier);
                                                    sendImageWithThumbnailFromUrls(imageList, isSingleImage, msgIdentifier);
                                                }
                                            });
                                        } else
                                            sendImageWithThumbnailFromUrls(imageList, isSingleImage, msgIdentifier);
                                    }
                                });
                    }
                }
            }.execute(image.getImageUrl());

        }
    }


    protected BaseChannel getChannel() {
        return mChannel;
    }

    ArrayList<ImageListItem> getDummyImageList() {
        ArrayList<ImageListItemDrawable> list = new ArrayList<>();
        list.add(new ImageListItemDrawable("Image1", "desc1",
                "d0d87c7679f443cea7e98c86e73dee6f.jpg"
                , "", R.drawable.image_list_1));
        list.add(new ImageListItemDrawable("Image2", "desc1",
                "e4aec03f47d445ab95811fb61dc08602.jpg",
                "", R.drawable.image_list_2));
        list.add(new ImageListItemDrawable("Image3", "desc1",
                "823b7df262af48639117ec6039b915d0.jpg",
                "", R.drawable.image_list_3));
        list.add(new ImageListItemDrawable("Image4", "desc1",
                "efc9527d98ab40b79f94f6caba82ea0d.jpg",
                "", R.drawable.image_list_4));
        list.add(new ImageListItemDrawable("Image5", "desc1",
                "1557be24b1d14e75b9d610491c439d24.jpg",
                "", R.drawable.image_list_5));
        list.add(new ImageListItemDrawable("Image6", "desc1",
                "77bd63285b5f4f2db04ad07f1d20c4a4.jpg",
                "", R.drawable.image_list_6));
        list.add(new ImageListItemDrawable("Image7", "desc1",
                "ad5ed0ca7a4a4bf190b1a62993e9f0a1.jpg",
                "", R.drawable.image_list_7));
        list.add(new ImageListItemDrawable("Image8", "desc1",
                "0c0f8e6d3c5c40cda9fde40411cb6187.jpg",
                "", R.drawable.image_list_8));
        list.add(new ImageListItemDrawable("Image9", "desc1",
                "252a1f76e33c41328b05faf6ba30b7b5.jpg",
                "", R.drawable.image_list_9));
        list.add(new ImageListItemDrawable("Image10", "desc1",
                "edd36e3de0de4430984370f26b23024d.jpg",
                "", R.drawable.image_list_10));
        list.add(new ImageListItemDrawable("Image11", "desc1",
                "9d70df28af4147c49a3970b3ef9c4560.jpg",
                "", R.drawable.image_list_11));
        ArrayList<ImageListItem> listItem = new ArrayList<>();

        FileOutputStream fileoutputstream = null;
        ByteArrayOutputStream bytearrayoutputstream = null;
        Drawable drawable;
        Bitmap bitmap;
        for (ImageListItemDrawable item : list) {
            drawable = getResources().getDrawable(item.getImageResId());
            bytearrayoutputstream = new ByteArrayOutputStream();
            bitmap = ((BitmapDrawable) drawable).getBitmap();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bytearrayoutputstream);
            String path = Environment.getExternalStorageDirectory() + "/" + item.getImageTitle() + ".jpg";
            listItem.add(new ImageListItem(item.getImageTitle(), item.getImageDesc(), item.getImageUrl(), "", path));
            File file = new File(path);

            try {
                file.createNewFile();

                fileoutputstream = new FileOutputStream(file);

                fileoutputstream.write(bytearrayoutputstream.toByteArray());


            } catch (Exception e) {

                e.printStackTrace();

            }
        }
        if (fileoutputstream != null) {
            try {
                fileoutputstream.close();
            } catch (Exception e) {

                e.printStackTrace();

            }
        }
        return listItem;
    }

    ArrayList<ImageListItem> createDummyImageMessage(ArrayList<ImageListItem> items) {
        ArrayList<ImageListItem> list = new ArrayList<>();
        for (ImageListItem item : items) {
            if (item.isChecked()) {
                list.add(item);
            }
        }
        return list;
    }

    void refresh() {
        loadInitialMessageList(CHANNEL_LIST_LIMIT);
    }

    private void loadInitialMessageList(int numMessages) {

        mPrevMessageListQuery = mChannel.createPreviousMessageListQuery();
        mPrevMessageListQuery.load(numMessages, true, new PreviousMessageListQuery.MessageListQueryResult() {
            @Override
            public void onResult(List<BaseMessage> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }

                mChatAdapter.setMessageList(createChatMessageList(list));
            }
        });

    }

    List<ChatMessage> createChatMessageList(List<BaseMessage> list) {
        ArrayList<ChatMessage> chatMessages = new ArrayList<>();
//		for (BaseMessage baseMessage : list) {
//			boolean isImageMessage = baseMessage.getCustomType().equals(IMAGE_LIST_CUSTOM_TYPE);
//			chatMessages.add(new ChatMessage(baseMessage, baseMessage., isImageMessage));
//		}
        int i = 0;
        while (i < list.size()) {
            BaseMessage baseMessage = list.get(i);
            ArrayList<ImageListItem> imageItems = new ArrayList<>();
            BaseMessage imagelistBaseMsg = null;
            Log.e("meta_size", baseMessage.getAllMetaArrays() == null ? "null" : baseMessage.getAllMetaArrays().size() + " ");
            @Nullable String meta = baseMessage.getAllMetaArrays() == null ? null :
                    baseMessage.getAllMetaArrays().isEmpty() ? null : baseMessage.getAllMetaArrays().get(0).getKey();
            Log.e("_receive_meta", meta != null ? meta : "null");
            while (baseMessage.getCustomType().equals(IMAGE_LIST_CUSTOM_TYPE)
                    && baseMessage instanceof FileMessage && i < list.size() /*&& meta != null
			&& baseMessage.getAllMetaArrays() != null && !baseMessage.getAllMetaArrays().isEmpty()
			&& baseMessage.getAllMetaArrays().get(0).getKey().equals(meta)*/) {
                if (imagelistBaseMsg == null)
                    imagelistBaseMsg = baseMessage;
                FileMessage fm = (FileMessage) baseMessage;
                Log.e("chat_" + i, "file message");
                imageItems.add(new ImageListItem(fm.getName(), fm.getName(), fm.getUrl(), fm.getUrl(), null));
//				new ImageMessage()
//				chatMessages.add(new ChatMessage(baseMessage,((FileMessage) baseMessage)., isImageMessage));
                if (i < list.size() - 1)
                    baseMessage = list.get(++i);
                else
                    ++i;
            }

            while (baseMessage.getCustomType().equals(IMAGE_LIST_AS_DATA_CUSTOM_TYPE) && i < list.size()) {
                if (imagelistBaseMsg == null)
                    imagelistBaseMsg = baseMessage;
                UserMessage userMessage = (UserMessage) baseMessage;
                imageItems.add(new ImageListItem(userMessage.getData(), userMessage.getMessage(),
                        userMessage.getData(),
                        userMessage.getData(), null));
                Log.e("chat_" + i, "user image message");
                if (i < list.size() - 1)

                    baseMessage = list.get(++i);
                else
                    ++i;
            }

            if (!imageItems.isEmpty()) {
                chatMessages.add(new ChatMessage(imagelistBaseMsg, new ImageMessage(imageItems,
                        imagelistBaseMsg instanceof FileMessage ? ((FileMessage) imagelistBaseMsg)
                                .getSender() : ((UserMessage) imagelistBaseMsg).getSender()), true));
//				imageItems.clear();
            } else {
                Log.e("chat_" + i, "user data message");

                chatMessages.add(new ChatMessage(baseMessage, null, false));
                ++i;

            }
        }
        return chatMessages;
    }

    void loadNextMessageList(int numMessages) throws NullPointerException {

        if (mChannel == null) {
            throw new NullPointerException("Current channel instance is null.");
        }

        if (mPrevMessageListQuery == null) {
            throw new NullPointerException("Current query instance is null.");
        }

        mPrevMessageListQuery = mChannel.createPreviousMessageListQuery();
        mPrevMessageListQuery.load(numMessages, true, new PreviousMessageListQuery.MessageListQueryResult() {
            @Override
            public void onResult(List<BaseMessage> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }
                mChatAdapter.setMessageList(createChatMessageList(list));
            }
        });
    }

    void editMessage(final BaseMessage message, String editedMessage) {
        mChannel.updateUserMessage(message.getMessageId(), editedMessage, null, null, new BaseChannel.UpdateUserMessageHandler() {
            @Override
            public void onUpdated(UserMessage userMessage, SendBirdException e) {
                if (e != null) {
                    Toast.makeText(OpenChatActivity.this, "Error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                refresh();
            }
        });
    }

    void deleteMessage(final BaseMessage message) {
        mChannel.deleteMessage(message, new BaseChannel.DeleteMessageHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null) {
                    Toast.makeText(OpenChatActivity.this, "Error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                refresh();
            }
        });
    }

    public void showGroupImageList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose items");
        final ArrayList<ImageListItem> items = getDummyImageList();
        builder.setAdapter(new GroupImageListAdaptor(OpenChatActivity.this, R.layout.activity_open_group_list_item, items),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mSelectedList = createDummyImageMessage(items);
                if (mSelectedList.size() > 0) {
//					mChatAdapter.addFirst(new ChatMessage(null, new ImageMessage(mSelectedList), true));
                    sendSelectedItems(mSelectedList);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void sendSelectedItems(ArrayList<ImageListItem> list) {

        if (isRequestedFileUpload) {
            ArrayList<Uri> uris = new ArrayList<>();
            for (ImageListItem item : list) {
                Uri uri = getUriToDrawable(item.getImageResId());
                uris.add(uri);

            }
            sendImageWithThumbnail(uris, false, System.currentTimeMillis() + "");
        } else {
            final ArrayList<ImageListItem> imageList = new ArrayList<ImageListItem>(list);
            sendImageInUserMsg(imageList, System.currentTimeMillis() + "");
        }
    }
}

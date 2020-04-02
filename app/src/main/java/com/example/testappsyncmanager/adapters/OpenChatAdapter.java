package com.example.testappsyncmanager.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testappsyncmanager.R;
import com.example.testappsyncmanager.models.ChatMessage;
import com.example.testappsyncmanager.ui.view.HorizontalImageListView;
import com.example.testappsyncmanager.ui.view.HorizontalRecyclerAdapter;
import com.example.testappsyncmanager.ui.view.ImageListItem;
import com.example.testappsyncmanager.ui.view.ImageMessage;
import com.example.testappsyncmanager.utilities.DateUtils;
import com.example.testappsyncmanager.utilities.FileUtils;
import com.example.testappsyncmanager.utilities.ImageUtils;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;

import java.util.ArrayList;
import java.util.List;

public class OpenChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER_MESSAGE = 10;
    private static final int VIEW_TYPE_FILE_MESSAGE = 20;
    private static final int VIEW_TYPE_ADMIN_MESSAGE = 30;
    private static final int VIEW_TYPE_IMAGE_LIST_MESSAGE = 40;
    private Context mContext;
    private List<ChatMessage> mMessageList;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    /**
     * An interface to implement item click callbacks in the activity or fragment that
     * uses this adapter.
     */
    public interface OnItemClickListener {
        public void onUserMessageItemClick(UserMessage message);

        public void onFileMessageItemClick(FileMessage message);

        public void onAdminMessageItemClick(AdminMessage message);

        void onImageMessageListItemClick(ImageMessage message, ImageListItem clickedItem);

    }

    public interface OnItemLongClickListener {
        void onBaseMessageLongClick(ChatMessage message, int position);
    }


    public OpenChatAdapter(Context context) {
        mMessageList = new ArrayList<>();
        mContext = context;
    }

     public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

     public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mItemLongClickListener = listener;
    }

    public void setMessageList(List<ChatMessage> messages) {
        mMessageList = messages;
        notifyDataSetChanged();
    }

    public void addFirst(ChatMessage message) {
        mMessageList.add(0, message);
        notifyDataSetChanged();
    }

    public void addLast(ChatMessage message) {
        mMessageList.add(message);
        notifyDataSetChanged();
    }

    public void delete(long msgId) {
        for(ChatMessage msg : mMessageList) {
            if(msg.getBaseMessage().getMessageId() == msgId) {
                mMessageList.remove(msg);
                notifyDataSetChanged();
                break;
            }
        }
    }
    public void update(ChatMessage message) {
        ChatMessage msg;
        for (int index = 0; index < mMessageList.size(); index++) {
            msg = mMessageList.get(index);
            if (!msg.isImageListType()) {
                ChatMessage baseMessage = msg;
                if (message.getBaseMessage().getMessageId() == baseMessage.getBaseMessage().getMessageId()) {
                    mMessageList.remove(index);
                    mMessageList.add(index, message);
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    public ChatMessage getItemAt(int pos){
        if(pos <0 || pos >= mMessageList.size())
            return null;
        return mMessageList.get(pos);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER_MESSAGE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_open_chat_user, parent, false);
            return new UserMessageHolder(view);

        } else if (viewType == VIEW_TYPE_ADMIN_MESSAGE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_open_chat_admin, parent, false);
            return new AdminMessageHolder(view);

        } else if (viewType == VIEW_TYPE_FILE_MESSAGE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_open_chat_file, parent, false);
            return new FileMessageHolder(view);
        }else if (viewType == VIEW_TYPE_IMAGE_LIST_MESSAGE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_open_chat_image_list, parent, false);
            return new ImageListHolder(view);
        }

        // Theoretically shouldn't happen.
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = mMessageList.get(position);
        if (chatMessage.isImageListType()) {
            return VIEW_TYPE_IMAGE_LIST_MESSAGE;
        } else {
            if (mMessageList.get(position).getBaseMessage() instanceof UserMessage) {
                return VIEW_TYPE_USER_MESSAGE;
            } else if (mMessageList.get(position).getBaseMessage() instanceof AdminMessage) {
                return VIEW_TYPE_ADMIN_MESSAGE;
            } else if (mMessageList.get(position).getBaseMessage() instanceof FileMessage) {
                return VIEW_TYPE_FILE_MESSAGE;
            }
        }
        // Unhandled message type.
        return -1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = mMessageList.get(position);

        boolean isNewDay = true;

        if (position < mMessageList.size() - 1) {
            if(!message.isImageListType()) {
                BaseMessage baseMessage = message.getBaseMessage();
                BaseMessage prevMessage = mMessageList.get(position + 1).getBaseMessage();

                if (prevMessage!=null&&!DateUtils.hasSameDate(baseMessage.getCreatedAt(), prevMessage.getCreatedAt())) {
                    isNewDay = true;
                }
            }

        } else if (position == mMessageList.size() - 1) {
            isNewDay = true;
        }

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_USER_MESSAGE:
                ((UserMessageHolder) holder).bind(mContext, (UserMessage) message.getBaseMessage(), isNewDay,
                        mItemClickListener, mItemLongClickListener, position);
                break;
            case VIEW_TYPE_ADMIN_MESSAGE:
                ((AdminMessageHolder) holder).bind((AdminMessage) message.getBaseMessage(), isNewDay,
                        mItemClickListener);
                break;
            case VIEW_TYPE_FILE_MESSAGE:
                ((FileMessageHolder) holder).bind(mContext, (FileMessage) message.getBaseMessage(), isNewDay,
                        mItemClickListener, mItemLongClickListener, position);
                break;
            case VIEW_TYPE_IMAGE_LIST_MESSAGE:
                ((ImageListHolder) holder).bind(mContext, message, isNewDay,
                        mItemClickListener, mItemLongClickListener, position);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    private class UserMessageHolder extends RecyclerView.ViewHolder {
        TextView nicknameText, messageText, editedText, timeText, dateText;
        ImageView profileImage;

        UserMessageHolder(View itemView) {
            super(itemView);

            nicknameText = (TextView) itemView.findViewById(R.id.text_open_chat_nickname);
            messageText = (TextView) itemView.findViewById(R.id.text_open_chat_message);
            editedText = (TextView) itemView.findViewById(R.id.text_open_chat_edited);
            timeText = (TextView) itemView.findViewById(R.id.text_open_chat_time);
            profileImage = (ImageView) itemView.findViewById(R.id.image_open_chat_profile);
            dateText = (TextView) itemView.findViewById(R.id.text_open_chat_date);
        }

        // Binds message details to ViewHolder item
        public void bind(Context context, final UserMessage message, boolean isNewDay,
                  @Nullable final OnItemClickListener clickListener,
                  @Nullable final OnItemLongClickListener longClickListener, final int postion) {

            User sender = message.getSender();

            // If current user sent the message, display name in different color
            if (sender.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                nicknameText.setTextColor(ContextCompat.getColor(context, R.color.openChatNicknameMe));
            } else {
                nicknameText.setTextColor(ContextCompat.getColor(context, R.color.openChatNicknameOther));
            }

            // Show the date if the message was sent on a different date than the previous one.
            if (isNewDay) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(DateUtils.formatDate(message.getCreatedAt()));
            } else {
                dateText.setVisibility(View.GONE);
            }

            nicknameText.setText(message.getSender().getNickname());
            messageText.setText(message.getMessage());
            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));

            if (message.getUpdatedAt() > 0) {
                editedText.setVisibility(View.VISIBLE);
            } else {
                editedText.setVisibility(View.GONE);
            }

            // Get profile image and display it
            ImageUtils.displayRoundImageFromUrl(context, message.getSender().getProfileUrl(), profileImage);

            if (clickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                     public void onClick(View v) {
                        clickListener.onUserMessageItemClick(message);
                    }
                });
            }

            if (longClickListener != null) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickListener.onBaseMessageLongClick(new ChatMessage(message, null, false), postion);
                        return true;
                    }
                });
            }
        }
    }

    private class AdminMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, dateText;

        AdminMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_open_chat_message);
            dateText = (TextView) itemView.findViewById(R.id.text_open_chat_date);
        }

        public void bind(final AdminMessage message, boolean isNewDay, final OnItemClickListener listener) {
            messageText.setText(message.getMessage());

            // Show the date if the message was sent on a different date than the previous one.
            if (isNewDay) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(DateUtils.formatDate(message.getCreatedAt()));
            } else {
                dateText.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                 public void onClick(View v) {
                    listener.onAdminMessageItemClick(message);
                }
            });
        }
    }

    private class FileMessageHolder extends RecyclerView.ViewHolder {
        TextView nicknameText, timeText, fileNameText, fileSizeText, dateText;
        ImageView profileImage, fileThumbnail;

        FileMessageHolder(View itemView) {
            super(itemView);

            nicknameText = (TextView) itemView.findViewById(R.id.text_open_chat_nickname);
            timeText = (TextView) itemView.findViewById(R.id.text_open_chat_time);
            profileImage = (ImageView) itemView.findViewById(R.id.image_open_chat_profile);
            fileNameText = (TextView) itemView.findViewById(R.id.text_open_chat_file_name);
            fileSizeText = (TextView) itemView.findViewById(R.id.text_open_chat_file_size);
            fileThumbnail = (ImageView) itemView.findViewById(R.id.image_open_chat_file_thumbnail);
            dateText = (TextView) itemView.findViewById(R.id.text_open_chat_date);
        }

        // Binds message details to ViewHolder item
        public void bind(final Context context, final FileMessage message, boolean isNewDay,
                  @Nullable final OnItemClickListener clickListener,
                  @Nullable final OnItemLongClickListener longClickListener, final int position) {
            User sender = message.getSender();

            // If current user sent the message, display name in different color
            if (sender.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                nicknameText.setTextColor(ContextCompat.getColor(context, R.color.openChatNicknameMe));
            } else {
                nicknameText.setTextColor(ContextCompat.getColor(context, R.color.openChatNicknameOther));
            }

            // Show the date if the message was sent on a different date than the previous one.
            if (isNewDay) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(DateUtils.formatDate(message.getCreatedAt()));
            } else {
                dateText.setVisibility(View.GONE);
            }

            // Get profile image and display it
            ImageUtils.displayRoundImageFromUrl(context, message.getSender().getProfileUrl(), profileImage);

            fileNameText.setText(message.getName());
            fileSizeText.setText(FileUtils.toReadableFileSize(message.getSize()));
            nicknameText.setText(message.getSender().getNickname());
            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));

            // If image, display thumbnail
            if (message.getType().toLowerCase().startsWith("image")) {
                // Get thumbnails from FileMessage
                ArrayList<FileMessage.Thumbnail> thumbnails = (ArrayList<FileMessage.Thumbnail>) message.getThumbnails();

                // If thumbnails exist, get smallest (first) thumbnail and display it in the message
                if (thumbnails.size() > 0) {
                    if (message.getType().toLowerCase().contains("gif")) {
                        ImageUtils.displayGifImageFromUrl(context, message.getUrl(), fileThumbnail, thumbnails.get(0).getUrl(), fileThumbnail.getDrawable());
                    } else {
                        ImageUtils.displayImageFromUrl(context, thumbnails.get(0).getUrl(), fileThumbnail, fileThumbnail.getDrawable());
                    }
                } else {
                    if (message.getType().toLowerCase().contains("gif")) {
                        ImageUtils.displayGifImageFromUrl(context, message.getUrl(), fileThumbnail, (String) null, fileThumbnail.getDrawable());
                    } else {
                        ImageUtils.displayImageFromUrl(context, message.getUrl(), fileThumbnail, fileThumbnail.getDrawable());
                    }
                }

            } else if (message.getType().toLowerCase().startsWith("video")) {
                // Get thumbnails from FileMessage
                ArrayList<FileMessage.Thumbnail> thumbnails = (ArrayList<FileMessage.Thumbnail>) message.getThumbnails();

                // If thumbnails exist, get smallest (first) thumbnail and display it in the message
                if (thumbnails.size() > 0) {
                    ImageUtils.displayImageFromUrlWithPlaceHolder(
                            context, thumbnails.get(0).getUrl(), fileThumbnail, R.drawable.ic_file_message);
                } else {
                    fileThumbnail.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play));
                }

            } else {
                fileThumbnail.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_file_message));
            }

            if (clickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                     public void onClick(View v) {
                        clickListener.onFileMessageItemClick(message);
                    }
                });
            }

            if (longClickListener != null) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickListener.onBaseMessageLongClick(new ChatMessage(message, null, false), position);
                        return true;
                    }
                });
            }

        }
    }
    private class ImageListHolder extends RecyclerView.ViewHolder {
        HorizontalImageListView horizontalImageListView;
        TextView nicknameText, timeText, fileNameText, fileSizeText, dateText;
        ImageView profileImage, fileThumbnail;

        ImageListHolder(View itemView) {
            super(itemView);

            horizontalImageListView = itemView.findViewById(R.id.open_chat_item_horizontal_list);
            nicknameText = (TextView) itemView.findViewById(R.id.text_open_chat_nickname);
            timeText = (TextView) itemView.findViewById(R.id.text_open_chat_time);
            profileImage = (ImageView) itemView.findViewById(R.id.image_open_chat_profile);

            dateText = (TextView) itemView.findViewById(R.id.text_open_chat_date);

        }

        void bind(final Context context, final ChatMessage message, boolean isNewDay,
                  @Nullable final OnItemClickListener clickListener,
                  @Nullable final OnItemLongClickListener longClickListener, final int position) {

            User sender = message.getImageMessage().getUser();

            // If current user sent the message, display name in different color
            if (sender.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                nicknameText.setTextColor(ContextCompat.getColor(context, R.color.openChatNicknameMe));
            } else {
                nicknameText.setTextColor(ContextCompat.getColor(context, R.color.openChatNicknameOther));
            }
            timeText.setText(DateUtils.formatTime(message.getBaseMessage().getCreatedAt()));

            //text_open_chat_time.setText(DateUtils.formatTime(message.getCreatedAt());
            Log.e("horiz_set", message.getImageMessage().getList().size() + " dd");
            horizontalImageListView.setData(message.getImageMessage().getList());
            horizontalImageListView.setItemClickListener(
                    new HorizontalRecyclerAdapter.HorizontalImageListItemClickListener() {
                        @Override
                        public void onItemClick(@NonNull final ImageListItem imageListItem) {
                            if (clickListener != null) {
                                clickListener.onImageMessageListItemClick(message.getImageMessage(),
                                        imageListItem);

                            }
                        }
                    });


            if (longClickListener != null) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickListener.onBaseMessageLongClick(new ChatMessage(null, message.getImageMessage(),
                                true), position);
                        return true;
                    }
                });
            }
            if (isNewDay) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(DateUtils.formatDate(message.getBaseMessage().getCreatedAt()));
            } else {
                dateText.setVisibility(View.GONE);
            }

            // Get profile image and display it
            ImageUtils.displayRoundImageFromUrl(context, sender.getProfileUrl(), profileImage);

            nicknameText.setText(sender.getNickname());


        }
    }
}

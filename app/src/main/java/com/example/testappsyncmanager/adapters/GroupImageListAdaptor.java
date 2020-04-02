package com.example.testappsyncmanager.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.example.testappsyncmanager.R;
import com.example.testappsyncmanager.ui.view.ImageListItem;
import com.example.testappsyncmanager.utilities.ImageUtils;

import java.util.ArrayList;

public class GroupImageListAdaptor extends ArrayAdapter<ImageListItem> {
    private Context mContext;
    private ArrayList<String> checkedImageTitles = new ArrayList();
    public GroupImageListAdaptor(Context context, int resource, ArrayList<ImageListItem> objects) {
        super(context, resource, objects);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.group_image_list_adaptor_item, parent, false);
        }
        final ImageListItem imageListItem = getItem(position);
        TextView item_name = convertView.findViewById(R.id.item_name);
        AppCompatImageView imageView = convertView.findViewById(R.id.imageView);
        TextView item_description = convertView.findViewById(R.id.item_description);
        CheckBox checkbox = convertView.findViewById(R.id.checkbox);
        item_name.setText(imageListItem.getImageTitle());
        ImageUtils.displayImageFromUri(mContext, imageListItem.getImageResId(), imageView);
        item_description.setText(imageListItem.getImageDesc());
        Log.e("checking_for_", imageListItem.getImageTitle());
        checkbox.setOnCheckedChangeListener(null);
        checkbox.setChecked(checkedImageTitles.contains(imageListItem.getImageTitle()));
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                imageListItem.setChecked(isChecked);
                Log.e(isChecked?"checked_":"unchecked_", imageListItem.getImageTitle());

                if(isChecked)
                checkedImageTitles.add(imageListItem.getImageTitle());
                else
                    checkedImageTitles.remove(imageListItem.getImageTitle());
            }
        });
        return convertView;
    }
}

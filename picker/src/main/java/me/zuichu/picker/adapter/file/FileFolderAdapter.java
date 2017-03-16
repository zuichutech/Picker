package me.zuichu.picker.adapter.file;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.zuichu.picker.FilePicker;
import me.zuichu.picker.R;
import me.zuichu.picker.Utils;
import me.zuichu.picker.bean.FileFolder;

/**
 * 谭东增加扩充
 * QQ 852041173
 */

public class FileFolderAdapter extends BaseAdapter {

    private FilePicker filePicker;
    private Activity mActivity;
    private LayoutInflater mInflater;
    private int mAudioSize;
    private List<FileFolder> fileFolders;
    private int lastSelected = 0;

    public FileFolderAdapter(Activity activity, List<FileFolder> folders) {
        mActivity = activity;
        if (folders != null && folders.size() > 0) fileFolders = folders;
        else fileFolders = new ArrayList<>();
        filePicker = FilePicker.getInstance();
        mAudioSize = Utils.getImageItemWidth(mActivity);
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void refreshData(List<FileFolder> folders) {
        if (folders != null && folders.size() > 0) fileFolders = folders;
        else fileFolders.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return fileFolders.size();
    }

    @Override
    public FileFolder getItem(int position) {
        return fileFolders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_folder_audio_item, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        FileFolder folder = getItem(position);
        holder.folderName.setText(folder.name);
        holder.audioCount.setText(mActivity.getString(R.string.folder_audio_count, folder.files.size()));
        Log.i("info", "加载：" + filePicker + "  " + filePicker.getImageLoader() + "  " + mActivity + "  " + folder.cover.path + "  " + holder.cover + "  " + mAudioSize);
        if (filePicker.getImageLoader() != null) {
            filePicker.getImageLoader().displayImage(mActivity, folder.cover.path, holder.cover, mAudioSize, mAudioSize);
        }
        /**
         * 选中文件夹标记可见
         */
        if (lastSelected == position) {
            holder.folderCheck.setVisibility(View.VISIBLE);
        } else {
            holder.folderCheck.setVisibility(View.INVISIBLE);
        }
        holder.fl_cover.setVisibility(View.GONE);
        return convertView;
    }

    public void setSelectIndex(int i) {
        if (lastSelected == i) {
            return;
        }
        lastSelected = i;
        notifyDataSetChanged();
    }

    public int getSelectIndex() {
        return lastSelected;
    }

    private class ViewHolder {
        ImageView cover;
        TextView folderName;
        TextView audioCount;
        ImageView folderCheck;
        FrameLayout fl_cover;

        public ViewHolder(View view) {
            cover = (ImageView) view.findViewById(R.id.iv_cover);
            folderName = (TextView) view.findViewById(R.id.tv_folder_name);
            audioCount = (TextView) view.findViewById(R.id.tv_image_count);
            folderCheck = (ImageView) view.findViewById(R.id.iv_folder_check);
            fl_cover = (FrameLayout) view.findViewById(R.id.fl_cover);
            view.setTag(this);
        }
    }
}

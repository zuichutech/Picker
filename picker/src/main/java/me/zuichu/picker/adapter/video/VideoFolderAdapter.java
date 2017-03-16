package me.zuichu.picker.adapter.video;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.zuichu.picker.R;
import me.zuichu.picker.Utils;
import me.zuichu.picker.VideoPicker;
import me.zuichu.picker.bean.VideoFolder;


public class VideoFolderAdapter extends BaseAdapter {

    private VideoPicker videoPicker;
    private Activity mActivity;
    private LayoutInflater mInflater;
    private int mVideoSize;
    private List<VideoFolder> videoFolders;
    private int lastSelected = 0;

    public VideoFolderAdapter(Activity activity, List<VideoFolder> folders) {
        mActivity = activity;
        if (folders != null && folders.size() > 0) videoFolders = folders;
        else videoFolders = new ArrayList<>();
        videoPicker = VideoPicker.getInstance();
        mVideoSize = Utils.getImageItemWidth(mActivity);
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void refreshData(List<VideoFolder> folders) {
        if (folders != null && folders.size() > 0) videoFolders = folders;
        else videoFolders.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return videoFolders.size();
    }

    @Override
    public VideoFolder getItem(int position) {
        return videoFolders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_folder_video_item, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        VideoFolder folder = getItem(position);
        holder.folderName.setText(folder.name);
        holder.videoCount.setText(mActivity.getString(R.string.folder_image_count, folder.videos.size()));
        videoPicker.getImageLoader().displayImage(mActivity, folder.cover.path, holder.cover, mVideoSize, mVideoSize);
        /**
         * 选中文件夹标记可见
         */
        if (lastSelected == position) {
            holder.folderCheck.setVisibility(View.VISIBLE);
        } else {
            holder.folderCheck.setVisibility(View.INVISIBLE);
        }

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
        TextView videoCount;
        ImageView folderCheck;

        public ViewHolder(View view) {
            cover = (ImageView) view.findViewById(R.id.iv_cover);
            folderName = (TextView) view.findViewById(R.id.tv_folder_name);
            videoCount = (TextView) view.findViewById(R.id.tv_image_count);
            folderCheck = (ImageView) view.findViewById(R.id.iv_folder_check);
            view.setTag(this);
        }
    }
}

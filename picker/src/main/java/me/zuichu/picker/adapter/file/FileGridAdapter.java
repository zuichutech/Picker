package me.zuichu.picker.adapter.file;

import android.Manifest;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import me.zuichu.picker.AudioPicker;
import me.zuichu.picker.FilePicker;
import me.zuichu.picker.R;
import me.zuichu.picker.Utils;
import me.zuichu.picker.bean.FileItem;
import me.zuichu.picker.ui.image.ImageBaseActivity;
import me.zuichu.picker.ui.video.VideoGridActivity;
import me.zuichu.picker.view.SuperCheckBox;

/**
 * 谭东增加扩充
 * QQ 852041173
 */
public class FileGridAdapter extends BaseAdapter {

    private static final int ITEM_TYPE_CAMERA = 0;  //第一个条目是相机
    private static final int ITEM_TYPE_NORMAL = 1;  //第一个条目不是相机

    private FilePicker filePicker;
    private Activity mActivity;
    private ArrayList<FileItem> files;       //当前需要显示的所有的视频数据
    private ArrayList<FileItem> mSelectedFiles; //全局保存的已经选中的视频数据
    private boolean isShowCamera;         //是否显示录像按钮
    private int mAudioSize;               //每个条目的大小
    private OnFileItemClickListener listener;   //视频被点击的监听

    public FileGridAdapter(Activity activity, ArrayList<FileItem> files) {
        this.mActivity = activity;
        if (files == null || files.size() == 0) this.files = new ArrayList<>();
        else this.files = files;

        mAudioSize = Utils.getImageItemWidth(mActivity);
        filePicker = FilePicker.getInstance();
        isShowCamera = filePicker.isShowCamera();
        mSelectedFiles = filePicker.getSelectedFiles();
    }

    public void refreshData(ArrayList<FileItem> audios) {
        if (audios == null || audios.size() == 0) this.files = new ArrayList<>();
        else this.files = audios;
        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowCamera) return position == 0 ? ITEM_TYPE_CAMERA : ITEM_TYPE_NORMAL;
        return ITEM_TYPE_NORMAL;
    }

    @Override
    public int getCount() {
        return isShowCamera ? files.size() + 1 : files.size();
    }

    /**
     * 根据是否显示相机来判断list位置
     *
     * @param position
     * @return
     */
    @Override
    public FileItem getItem(int position) {
        if (isShowCamera) {
            if (position == 0) return null;
            return files.get(position - 1);
        } else {
            return files.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        int itemViewType = getItemViewType(position);
        //相机录像
        if (itemViewType == ITEM_TYPE_CAMERA) {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.adapter_audiorecord_item, parent, false);
            convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mAudioSize)); //让视频是个正方形
            convertView.setTag(null);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((ImageBaseActivity) mActivity).checkPermission(Manifest.permission.CAMERA)) {
                        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA}, VideoGridActivity.REQUEST_PERMISSION_CAMERA);
                    } else {
                        //录像
                        filePicker.takeRecord(mActivity, AudioPicker.REQUEST_AUDIO_TAKE);
                    }
                }
            });
        } else {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mActivity).inflate(R.layout.adapter_audio_item, parent, false);
                convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mAudioSize)); //让视频是个正方形
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final FileItem fileItem = getItem(position);
            /**
             * 点击图片的监听
             */
            holder.ivThumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onFileItemClick(holder.rootView, fileItem, position);
                }
            });

            //显示时长 转化成分秒
            holder.tv_timelong.setText(fileItem.name);

            //视频的选择
            holder.cbCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selectLimit = filePicker.getSelectLimit();
                    if (holder.cbCheck.isChecked() && mSelectedFiles.size() >= selectLimit) {
                        Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.select_video_limit, selectLimit), Toast.LENGTH_SHORT).show();
                        holder.cbCheck.setChecked(false);
                        holder.mask.setVisibility(View.GONE);
                    } else {
                        filePicker.addSelectedFileItem(position, fileItem, holder.cbCheck.isChecked());
                        holder.mask.setVisibility(View.VISIBLE);
                    }
                }
            });
            //根据是否多选，显示或隐藏checkbox
            if (filePicker.isMultiMode()) {
                holder.cbCheck.setVisibility(View.VISIBLE);
                boolean checked = mSelectedFiles.contains(fileItem);
                /**
                 * 判断该项视频是否选中,如果选中,将背景显示出来,将多选框选中状态
                 * 否则隐藏背景,多选框未选中状态
                 */
                if (checked) {
                    holder.mask.setVisibility(View.VISIBLE);
                    holder.cbCheck.setChecked(true);
                } else {
                    holder.mask.setVisibility(View.GONE);
                    holder.cbCheck.setChecked(false);
                }
            } else {
                //单选的话直接隐藏多选框
                holder.cbCheck.setVisibility(View.GONE);
            }

            if (filePicker.isSelect(fileItem)) {
                holder.mask.setVisibility(View.VISIBLE);
                holder.cbCheck.setChecked(true);
            }
            holder.tv_title.setText(filePicker.timeParse(fileItem.size));
            if (fileItem.mimeType.contains("flac")) {
                holder.iv_type.setImageResource(R.mipmap.icon_flac);
            } else if (fileItem.mimeType.contains("wav")) {
                holder.iv_type.setImageResource(R.mipmap.icon_wav);
            } else if (fileItem.mimeType.contains("mpeg")) {
                holder.iv_type.setImageResource(R.mipmap.icon_mp3);
            } else {
                holder.iv_type.setImageResource(R.mipmap.icon_noaudio);
            }
        }
        return convertView;
    }

    private class ViewHolder {
        public View rootView;
        public ImageView ivThumb;
        public View mask;
        public SuperCheckBox cbCheck;
        public TextView tv_timelong;
        public TextView tv_title;
        public ImageView iv_type;

        public ViewHolder(View view) {
            rootView = view;
            ivThumb = (ImageView) view.findViewById(R.id.iv_thumb);
            iv_type = (ImageView) view.findViewById(R.id.iv_type);
            mask = view.findViewById(R.id.mask);
            cbCheck = (SuperCheckBox) view.findViewById(R.id.cb_check);
            tv_timelong = (TextView) view.findViewById(R.id.tv_timelong);
            tv_title = (TextView) view.findViewById(R.id.tv_title);
        }
    }

    public void setOnFileItemClickListener(OnFileItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 定义一个监听视频被点击的接口
     */
    public interface OnFileItemClickListener {
        void onFileItemClick(View view, FileItem fileItem, int position);
    }

    /**
     * 根据歌曲路径获得专辑封面
     *
     * @param filePath 文件路径，like XXX/XXX/XX.mp3
     * @return 专辑封面bitmap
     * @Description 获取专辑封面
     */
    public static Bitmap createAlbumArt(final String filePath) {
        Bitmap bitmap = null;
        //能够获取多媒体文件元数据的类
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath); //设置数据源
            byte[] embedPic = retriever.getEmbeddedPicture(); //得到字节型数据
            bitmap = BitmapFactory.decodeByteArray(embedPic, 0, embedPic.length); //转换为图片
            //要优化后再加载
//            bitmap=BitmapUtil.decodeBitmapByByteArray(embedPic,80,80);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return bitmap;
    }

    public static byte[] createAlbumArtByte(String filePath) {
        Bitmap bitmap = null;
        //能够获取多媒体文件元数据的类
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        byte[] embedPic = null;
        try {
            retriever.setDataSource(filePath); //设置数据源
            embedPic = retriever.getEmbeddedPicture(); //得到字节型数据
//            bitmap = BitmapFactory.decodeByteArray(embedPic, 0, embedPic.length); //转换为图片
            //要优化后再加载
//            bitmap=BitmapUtil.decodeBitmapByByteArray(embedPic,80,80);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return embedPic;
    }
}
package me.zuichu.picker.adapter.file;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import me.zuichu.picker.FilePicker;
import me.zuichu.picker.R;
import me.zuichu.picker.Utils;
import me.zuichu.picker.bean.FileItem;

/**
 * 谭东增加扩充
 * QQ 852041173
 */
public class FilePageAdapter extends PagerAdapter {

    private int screenWidth;
    private int screenHeight;
    private FilePicker filePicker;
    private ArrayList<FileItem> files = new ArrayList<FileItem>();
    private Activity mActivity;
    public PhotoViewClickListener listener;

    /**
     * 构造函数
     *
     * @param activity
     * @param files    音频数据源
     */
    public FilePageAdapter(Activity activity, ArrayList<FileItem> files) {
        this.mActivity = activity;
        this.files = files;

        DisplayMetrics dm = Utils.getScreenPix(activity);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        filePicker = FilePicker.getInstance();
    }

    public void setData(ArrayList<FileItem> files) {
        this.files = files;
    }

    public void setPhotoViewClickListener(PhotoViewClickListener listener) {
        this.listener = listener;
    }

    /**
     * 实例化一个页卡
     *
     * @param container
     * @param position
     * @return
     */
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View view = mActivity.getLayoutInflater().inflate(R.layout.viewpager_audio_item, null);
        ImageView imageview = (ImageView) view.findViewById(R.id.imageview);
        ImageView iv_play = (ImageView) view.findViewById(R.id.iv_play);
        final FileItem fileItem = files.get(position);
//        Glide.with(mActivity)
//                .load(audioItem.path)
//                .placeholder(R.mipmap.icon_audio)
//                .into(imageview);
        /**
         * 点击播放播放视频
         */
        iv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("file://" + fileItem.path);
                //调用系统自带的播放器
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "audio/*");
                mActivity.startActivity(intent);
            }
        });
        imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.OnPhotoTapListener(view);
            }
        });
        container.addView(view);
        return view;
    }

    /**
     * 返回页卡的数量
     *
     * @return
     */
    @Override
    public int getCount() {
        return files.size();
    }

    /**
     * view是否来自于对象
     *
     * @param view
     * @param object
     * @return
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /**
     * 销毁一个页卡
     *
     * @param container
     * @param position
     * @param object
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public interface PhotoViewClickListener {
        void OnPhotoTapListener(View view);
    }
}

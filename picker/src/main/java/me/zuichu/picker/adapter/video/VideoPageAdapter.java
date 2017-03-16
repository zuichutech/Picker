package me.zuichu.picker.adapter.video;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;

import me.zuichu.picker.R;
import me.zuichu.picker.Utils;
import me.zuichu.picker.VideoPicker;
import me.zuichu.picker.bean.VideoItem;

public class VideoPageAdapter extends PagerAdapter {

    private int screenWidth;
    private int screenHeight;
    private VideoPicker videoPicker;
    private ArrayList<VideoItem> videos = new ArrayList<>();
    private Activity mActivity;
    public PhotoViewClickListener listener;

    /**
     * 构造函数
     *
     * @param activity
     * @param videos   视频数据源
     */
    public VideoPageAdapter(Activity activity, ArrayList<VideoItem> videos) {
        this.mActivity = activity;
        this.videos = videos;

        DisplayMetrics dm = Utils.getScreenPix(activity);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        videoPicker = VideoPicker.getInstance();
    }

    public void setData(ArrayList<VideoItem> videos) {
        this.videos = videos;
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
        View view = mActivity.getLayoutInflater().inflate(R.layout.viewpager_video_item, null);
        ImageView imageview = (ImageView) view.findViewById(R.id.imageview);
        ImageButton btnPlay = (ImageButton) view.findViewById(R.id.btn_play);
        final VideoItem videoItem = videos.get(position);
        videoPicker.getImageLoader().displayImage(mActivity, videoItem.path, imageview, screenWidth, screenHeight);
        /**
         * 点击播放播放视频
         */
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(videoItem.path);
                //调用系统自带的播放器
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "video/mp4");
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
        return videos.size();
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

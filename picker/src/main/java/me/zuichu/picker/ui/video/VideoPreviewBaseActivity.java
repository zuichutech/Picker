package me.zuichu.picker.ui.video;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import me.zuichu.picker.R;
import me.zuichu.picker.Utils;
import me.zuichu.picker.VideoPicker;
import me.zuichu.picker.adapter.video.VideoPageAdapter;
import me.zuichu.picker.bean.VideoItem;
import me.zuichu.picker.ui.image.ImageBaseActivity;
import me.zuichu.picker.view.ViewPagerFixed;

public abstract class VideoPreviewBaseActivity extends ImageBaseActivity {

    protected VideoPicker videoPicker;
    protected ArrayList<VideoItem> mVideoItems;      //跳转进VideoPreviewFragment的视频文件夹
    protected int mCurrentPosition = 0;              //跳转进VideoPreviewFragment时的序号，第几个视频
    protected TextView mTitleCount;                  //显示当前视频的位置  例如  5/31
    protected ArrayList<VideoItem> selectedVideos;   //所有已经选中的视频
    protected View content;
    protected View topBar;
    protected ViewPagerFixed mViewPager;
    protected VideoPageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);

        mCurrentPosition = getIntent().getIntExtra(VideoPicker.EXTRA_SELECTED_VIDEO_POSITION, 0);
        mVideoItems = (ArrayList<VideoItem>) getIntent().getSerializableExtra(VideoPicker.EXTRA_VIDEO_ITEMS);
        videoPicker = VideoPicker.getInstance();
        selectedVideos = videoPicker.getSelectedVideos();

        //初始化控件
        content = findViewById(R.id.content);

        //因为状态栏透明后，布局整体会上移，所以给头部加上状态栏的margin值，保证头部不会被覆盖
        topBar = findViewById(R.id.top_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) topBar.getLayoutParams();
            params.topMargin = Utils.getStatusHeight(this);
            topBar.setLayoutParams(params);
        }
        topBar.findViewById(R.id.btn_ok).setVisibility(View.GONE);
        topBar.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTitleCount = (TextView) findViewById(R.id.tv_des);

        mViewPager = (ViewPagerFixed) findViewById(R.id.viewpager);
        mAdapter = new VideoPageAdapter(this, mVideoItems);
        mAdapter.setPhotoViewClickListener(new VideoPageAdapter.PhotoViewClickListener() {
            @Override
            public void OnPhotoTapListener(View view) {
                onVideoSingleTap();
            }
        });
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mCurrentPosition, false);

        //初始化当前页面的状态
        mTitleCount.setText(getString(R.string.preview_image_count, mCurrentPosition + 1, mVideoItems.size()));
    }

    /** 单击时，隐藏头和尾 */
    public abstract void onVideoSingleTap();
}
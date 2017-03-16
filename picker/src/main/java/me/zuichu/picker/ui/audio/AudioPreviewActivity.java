package me.zuichu.picker.ui.audio;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.format.Formatter;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import me.zuichu.picker.AudioPicker;
import me.zuichu.picker.R;
import me.zuichu.picker.bean.AudioItem;
import me.zuichu.picker.view.SuperCheckBox;

/**
 * 音频预览界面
 */
public class AudioPreviewActivity extends AudioPreviewBaseActivity implements AudioPicker.OnAudioSelectedListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    /**
     * 是否选中了原图的常量
     */
    public static final String ISORIGIN = "isOrigin";

    private boolean isOrigin;                      //是否选中原图
    private SuperCheckBox mCbCheck;                //是否选中当前音频的CheckBox
    private SuperCheckBox mCbOrigin;               //原图
    private Button mBtnOk;                         //确认音频的选择
    private View bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isOrigin = getIntent().getBooleanExtra(AudioPreviewActivity.ISORIGIN, false);
        audioPicker.addOnAudioSelectedListener(this);

        mBtnOk = (Button) topBar.findViewById(R.id.btn_ok);
        mBtnOk.setVisibility(View.VISIBLE);
        mBtnOk.setOnClickListener(this);

        bottomBar = findViewById(R.id.bottom_bar);
        bottomBar.setVisibility(View.VISIBLE);

        mCbCheck = (SuperCheckBox) findViewById(R.id.cb_check);
        mCbOrigin = (SuperCheckBox) findViewById(R.id.cb_origin);
        mCbOrigin.setText(getString(R.string.origin));
        mCbOrigin.setOnCheckedChangeListener(this);
        mCbOrigin.setChecked(isOrigin);

        //初始化当前页面的状态
        onAudioSelected(0, null, false);
        AudioItem item = mAudioItems.get(mCurrentPosition);
        boolean isSelected = audioPicker.isSelect(item);
        mTitleCount.setText(getString(R.string.preview_image_count, mCurrentPosition + 1, mAudioItems.size()));
        mCbCheck.setChecked(isSelected);
        //滑动ViewPager的时候，根据外界的数据改变当前的选中状态和当前的视频的位置描述文本
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                AudioItem item = mAudioItems.get(mCurrentPosition);
                boolean isSelected = audioPicker.isSelect(item);
                mCbCheck.setChecked(isSelected);
                mTitleCount.setText(getString(R.string.preview_image_count, mCurrentPosition + 1, mAudioItems.size()));
            }
        });
        //当点击当前选中按钮的时候，需要根据当前的选中状态添加和移除视频
        mCbCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioItem audioItem = mAudioItems.get(mCurrentPosition);
                int selectLimit = audioPicker.getSelectLimit();
                if (mCbCheck.isChecked() && selectedAudios.size() >= selectLimit) {
                    Toast.makeText(AudioPreviewActivity.this, AudioPreviewActivity.this.getString(R.string.select_limit, selectLimit), Toast.LENGTH_SHORT).show();
                    mCbCheck.setChecked(false);
                } else {
                    audioPicker.addSelectedAudioItem(mCurrentPosition, audioItem, mCbCheck.isChecked());
                }
            }
        });
    }

    /**
     * 视频添加成功后，修改当前视频的选中数量
     * 当调用 addSelectedAudioItem 或 deleteSelectedAudioItem 都会触发当前回调
     */
    @Override
    public void onAudioSelected(int position, AudioItem item, boolean isAdd) {
        if (audioPicker.getSelectAudioCount() > 0) {
            mBtnOk.setText(getString(R.string.select_complete, audioPicker.getSelectAudioCount(), audioPicker.getSelectLimit()));
            mBtnOk.setEnabled(true);
        } else {
            mBtnOk.setText(getString(R.string.complete));
            mBtnOk.setEnabled(false);
        }
        //选中原图时获取视频的大小
        if (mCbOrigin.isChecked()) {
            long size = 0;
            for (AudioItem audioItem : selectedAudios)
                size += audioItem.size;
            String fileSize = Formatter.formatFileSize(this, size);
            mCbOrigin.setText(getString(R.string.origin_size, fileSize));
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_ok) {
            Intent intent = new Intent();
            intent.putExtra(AudioPicker.EXTRA_RESULT_AUDIO_ITEMS, audioPicker.getSelectedAudios());
            setResult(AudioPicker.RESULT_AUDIO_ITEMS, intent);
            finish();
        } else if (id == R.id.btn_back) {
            Intent intent = new Intent();
            //携带参数书否选中原图
            intent.putExtra(AudioPreviewActivity.ISORIGIN, isOrigin);
            setResult(AudioPicker.RESULT_AUDIO_BACK, intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(AudioPreviewActivity.ISORIGIN, isOrigin);
        setResult(AudioPicker.RESULT_AUDIO_BACK, intent);
        finish();
        super.onBackPressed();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.cb_origin) {
            if (isChecked) {
                long size = 0;
                for (AudioItem item : selectedAudios)
                    size += item.size;
                String fileSize = Formatter.formatFileSize(this, size);
                isOrigin = true;
                mCbOrigin.setText(getString(R.string.origin_size, fileSize));
            } else {
                isOrigin = false;
                mCbOrigin.setText(getString(R.string.origin));
            }
        }
    }

    @Override
    protected void onDestroy() {
        audioPicker.removeOnAudioSelectedListener(this);
        super.onDestroy();
    }

    /** 单击时，隐藏头和尾 */
    @Override
    public void onAudioSingleTap() {
        if (topBar.getVisibility() == View.VISIBLE) {
            topBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.top_out));
            bottomBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));
            topBar.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
            tintManager.setStatusBarTintResource(R.color.transparent);//通知栏所需颜色
            //给最外层布局加上这个属性表示，Activity全屏显示，且状态栏被隐藏覆盖掉。
            if (Build.VERSION.SDK_INT >= 16) content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            topBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.top_in));
            bottomBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
            topBar.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);
            tintManager.setStatusBarTintResource(R.color.status_bar);//通知栏所需颜色
            //Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住
            if (Build.VERSION.SDK_INT >= 16) content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }
}

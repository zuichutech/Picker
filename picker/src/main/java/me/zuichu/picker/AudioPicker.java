package me.zuichu.picker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.zuichu.picker.bean.AudioFolder;
import me.zuichu.picker.bean.AudioItem;
import me.zuichu.picker.loader.ImageLoader;

/**
 * 谭东增加扩充
 * QQ 852041173
 */
public class AudioPicker {

    public static final String TAG = AudioPicker.class.getSimpleName();
    /**
     * 录音的结果请求
     */
    public static final int REQUEST_AUDIO_TAKE = 1006;
    /**
     * 请求音频预览
     */
    public static final int REQUEST_AUDIO_PREVIEW = 1007;
    /**
     * 返回音频数据的结果
     */
    public static final int RESULT_AUDIO_ITEMS = 1008;
    /**
     * 从预览界面返回
     */
    public static final int RESULT_AUDIO_BACK = 1009;

    /**
     * 选中的视频项
     */
    public static final String EXTRA_RESULT_AUDIO_ITEMS = "extra_result_items";
    /**
     * 已选择的视频项
     */
    public static final String EXTRA_SELECTED_AUDIO_POSITION = "selected_audio_position";
    /**
     * 已选中的所有音频文件夹项
     */
    public static final String EXTRA_AUDIO_ITEMS = "extra_audio_items";

    private boolean multiMode = true;    //图片选择模式
    private int selectLimit = 9;         //最大选择图片数量
    private boolean showCamera = true;   //显示相机
    private ImageLoader imageLoader;     //图片加载器
    private File takeAudioFile;

    private ArrayList<AudioItem> mSelectedAudios = new ArrayList<>();   //选中的图片集合
    private List<AudioFolder> mAudioFolders;      //所有的图片文件夹
    private int mCurrentAudioFolderPosition = 0;  //当前选中的文件夹位置 0表示所有图片
    private List<OnAudioSelectedListener> mAudioSelectedListeners;          // 图片选中的监听回调

    private static AudioPicker mInstance;

    private AudioPicker() {
    }

    public static AudioPicker getInstance() {
        if (mInstance == null) {
            synchronized (AudioPicker.class) {
                if (mInstance == null) {
                    mInstance = new AudioPicker();
                }
            }
        }
        return mInstance;
    }

    public boolean isMultiMode() {
        return multiMode;
    }

    public void setMultiMode(boolean multiMode) {
        this.multiMode = multiMode;
    }

    public int getSelectLimit() {
        return selectLimit;
    }

    public void setSelectLimit(int selectLimit) {
        this.selectLimit = selectLimit;
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    public void setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
    }

    public File getTakeAudioFile() {
        return takeAudioFile;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public List<AudioFolder> getAudioFolders() {
        return mAudioFolders;
    }

    public void setAudioFolders(List<AudioFolder> audioFolders) {
        mAudioFolders = audioFolders;
    }

    public int getCurrentAudioFolderPosition() {
        return mCurrentAudioFolderPosition;
    }

    /**
     * 设置当前选中视频文件夹所在的位置
     */
    public void setCurrentAudioFolderPosition(int mCurrentSelectedAudioSetPosition) {
        mCurrentAudioFolderPosition = mCurrentSelectedAudioSetPosition;
    }

    /**
     * 获取当前视频所在的文件夹
     */
    public ArrayList<AudioItem> getCurrentAudioFolderItems() {
        return mAudioFolders.get(mCurrentAudioFolderPosition).audios;
    }

    /**
     * 是否选中该视频 checkbox
     */
    public boolean isSelect(AudioItem item) {
        return mSelectedAudios.contains(item);
    }

    public int getSelectAudioCount() {
        if (mSelectedAudios == null) {
            return 0;
        }
        return mSelectedAudios.size();
    }

    public ArrayList<AudioItem> getSelectedAudios() {
        return mSelectedAudios;
    }

    /**
     * 清除选中的视频
     */
    public void clearSelectedAudios() {
        if (mSelectedAudios != null) mSelectedAudios.clear();
    }

    public void clear() {
        if (mAudioSelectedListeners != null) {
            mAudioSelectedListeners.clear();
            mAudioSelectedListeners = null;
        }
        if (mAudioFolders != null) {
            mAudioFolders.clear();
            mAudioFolders = null;
        }
        if (mSelectedAudios != null) {
            mSelectedAudios.clear();
        }
        mCurrentAudioFolderPosition = 0;
    }

    /**
     * 录像的方法
     */
    public void takeRecord(Activity activity, int requestCode) {
//        Intent takeRecordIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        Intent takeRecordIntent = new Intent(
                MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        /**
         * 当由startActivityForResult函数触发时，任何活动在返回时都将调用onActivityResult方法，
         * 通过将一个独特的常量连同意图一起传入，我们能够在onActivityResult方法中对他们进行区分。
         **/
//        takeRecordIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (takeRecordIntent.resolveActivity(activity.getPackageManager()) != null) {
            //是否存在SD卡
            if (Utils.existSDCard())
                takeAudioFile = new File(Environment.getExternalStorageDirectory(), "/DCIM/movie/");
            else takeAudioFile = Environment.getDataDirectory();
            takeAudioFile = createFile(takeAudioFile, "RIDEO_", ".mp3");
            if (takeAudioFile != null) {
                // 默认情况下，即不需要指定intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                // 照相机有自己默认的存储路径，拍摄的照片将返回一个缩略图。如果想访问原始图片，
                // 可以通过dat extra能够得到原始图片位置。即，如果指定了目标uri，data就没有数据，
                // 如果没有指定uri，则data就返回有数据！
                takeRecordIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(takeAudioFile));
            }
        }
//        takeRecordIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        activity.startActivityForResult(takeRecordIntent, requestCode);
    }

    /**
     * 根据系统时间、前缀、后缀产生一个文件
     */
    public static File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) folder.mkdirs();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }

    /**
     * 扫描图片
     */
    public static void galleryAddPic(Context context, File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * 视频选中的监听
     */
    public interface OnAudioSelectedListener {
        void onAudioSelected(int position, AudioItem item, boolean isAdd);
    }

    /**
     * 添加视频选中的监听
     */
    public void addOnAudioSelectedListener(OnAudioSelectedListener l) {
        if (mAudioSelectedListeners == null) mAudioSelectedListeners = new ArrayList<>();
        mAudioSelectedListeners.add(l);
    }

    public void removeOnAudioSelectedListener(OnAudioSelectedListener l) {
        if (mAudioSelectedListeners == null) return;
        mAudioSelectedListeners.remove(l);
    }

    /**
     * 添加选中某一项视频
     */
    public void addSelectedAudioItem(int position, AudioItem item, boolean isAdd) {
        if (isAdd) mSelectedAudios.add(item);
        else mSelectedAudios.remove(item);
        notifyAudioSelectedChanged(position, item, isAdd);
    }

    private void notifyAudioSelectedChanged(int position, AudioItem item, boolean isAdd) {
        if (mAudioSelectedListeners == null) return;
        for (OnAudioSelectedListener l : mAudioSelectedListeners) {
            l.onAudioSelected(position, item, isAdd);
        }
    }

    /**
     * Android读出的时长为 long 类型以毫秒数为单位，例如：将 234736 转化为分钟和秒应为 03:55 （包含四舍五入）
     *
     * @param duration 时长
     * @return
     */
    public String timeParse(long duration) {
        String time = "";
        long minute = duration / 60000;
        long seconds = duration % 60000;
        long second = Math.round((float) seconds / 1000);
        if (minute < 10) {
            time += "0";
        }
        time += minute + ":";
        if (second < 10) {
            time += "0";
        }
        time += second;
        return time;
    }
}
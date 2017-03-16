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

import me.zuichu.picker.bean.FileFolder;
import me.zuichu.picker.bean.FileItem;
import me.zuichu.picker.loader.ImageLoader;

/**
 * 谭东增加扩充
 * QQ 852041173
 */
public class FilePicker {

    public static final String TAG = FilePicker.class.getSimpleName();
    /**
     * 录音的结果请求
     */
    public static final int REQUEST_FILE_TAKE = 1006;
    /**
     * 请求文件预览
     */
    public static final int REQUEST_FILE_PREVIEW = 1007;
    /**
     * 返回文件数据的结果
     */
    public static final int RESULT_FILE_ITEMS = 1008;
    /**
     * 从预览界面返回
     */
    public static final int RESULT_FILE_BACK = 1009;

    /**
     * 选中的文件项
     */
    public static final String EXTRA_RESULT_FILE_ITEMS = "extra_result_items";
    /**
     * 已选择的文件项
     */
    public static final String EXTRA_SELECTED_FILE_POSITION = "selected_file_position";
    /**
     * 已选中的所有音频文件夹项
     */
    public static final String EXTRA_FILE_ITEMS = "extra_file_items";

    private boolean multiMode = true;    //文件选择模式
    private int selectLimit = 9;         //最大选择文件数量
    private boolean showCamera = true;   //显示相机
    private ImageLoader imageLoader;     //文件加载器
    private File takeFile;

    private ArrayList<FileItem> mSelectedFiles = new ArrayList<FileItem>();   //选中的文件集合
    private List<FileFolder> mFileFolders;      //所有的文件文件夹
    private int mCurrentFileFolderPosition = 0;  //当前选中的文件夹位置 0表示所有文件
    private List<OnFileSelectedListener> mFileSelectedListeners;          // 文件选中的监听回调

    private static FilePicker mInstance;

    private FilePicker() {
    }

    public static FilePicker getInstance() {
        if (mInstance == null) {
            synchronized (FilePicker.class) {
                if (mInstance == null) {
                    mInstance = new FilePicker();
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

    public File getTakeFile() {
        return takeFile;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public List<FileFolder> getFileFolders() {
        return mFileFolders;
    }

    public void setFileFolders(List<FileFolder> fileFolders) {
        mFileFolders = fileFolders;
    }

    public int getCurrentFileFolderPosition() {
        return mCurrentFileFolderPosition;
    }

    /**
     * 设置当前选中视频文件夹所在的位置
     */
    public void setCurrentFileFolderPosition(int mCurrentSelectedFileSetPosition) {
        mCurrentFileFolderPosition = mCurrentSelectedFileSetPosition;
    }

    /**
     * 获取当前视频所在的文件夹
     */
    public ArrayList<FileItem> getCurrentFileFolderItems() {
        return mFileFolders.get(mCurrentFileFolderPosition).files;
    }

    /**
     * 是否选中该视频 checkbox
     */
    public boolean isSelect(FileItem item) {
        return mSelectedFiles.contains(item);
    }

    public int getSelectFileCount() {
        if (mSelectedFiles == null) {
            return 0;
        }
        return mSelectedFiles.size();
    }

    public ArrayList<FileItem> getSelectedFiles() {
        return mSelectedFiles;
    }

    /**
     * 清除选中的视频
     */
    public void clearSelectedFiles() {
        if (mSelectedFiles != null) mSelectedFiles.clear();
    }

    public void clear() {
        if (mFileSelectedListeners != null) {
            mFileSelectedListeners.clear();
            mFileSelectedListeners = null;
        }
        if (mFileFolders != null) {
            mFileFolders.clear();
            mFileFolders = null;
        }
        if (mSelectedFiles != null) {
            mSelectedFiles.clear();
        }
        mCurrentFileFolderPosition = 0;
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
                takeFile = new File(Environment.getExternalStorageDirectory(), "/DCIM/movie/");
            else takeFile = Environment.getDataDirectory();
            takeFile = createFile(takeFile, "RIDEO_", ".mp3");
            if (takeFile != null) {
                // 默认情况下，即不需要指定intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                // 照相机有自己默认的存储路径，拍摄的照片将返回一个缩略图。如果想访问原始图片，
                // 可以通过dat extra能够得到原始图片位置。即，如果指定了目标uri，data就没有数据，
                // 如果没有指定uri，则data就返回有数据！
                takeRecordIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(takeFile));
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
    public interface OnFileSelectedListener {
        void onFileSelected(int position, FileItem item, boolean isAdd);
    }

    /**
     * 添加视频选中的监听
     */
    public void addOnFileSelectedListener(OnFileSelectedListener l) {
        if (mFileSelectedListeners == null) mFileSelectedListeners = new ArrayList<>();
        mFileSelectedListeners.add(l);
    }

    public void removeOnFileSelectedListener(OnFileSelectedListener l) {
        if (mFileSelectedListeners == null) return;
        mFileSelectedListeners.remove(l);
    }

    /**
     * 添加选中某一项视频
     */
    public void addSelectedFileItem(int position, FileItem item, boolean isAdd) {
        if (isAdd) mSelectedFiles.add(item);
        else mSelectedFiles.remove(item);
        notifyFileSelectedChanged(position, item, isAdd);
    }

    private void notifyFileSelectedChanged(int position, FileItem item, boolean isAdd) {
        if (mFileSelectedListeners == null) return;
        for (OnFileSelectedListener l : mFileSelectedListeners) {
            l.onFileSelected(position, item, isAdd);
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
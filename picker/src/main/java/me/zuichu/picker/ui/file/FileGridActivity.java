package me.zuichu.picker.ui.file;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import java.util.List;

import me.zuichu.picker.AudioPicker;
import me.zuichu.picker.FileDataSource;
import me.zuichu.picker.FilePicker;
import me.zuichu.picker.R;
import me.zuichu.picker.adapter.file.FileFolderAdapter;
import me.zuichu.picker.adapter.file.FileGridAdapter;
import me.zuichu.picker.bean.FileFolder;
import me.zuichu.picker.bean.FileItem;
import me.zuichu.picker.ui.audio.AudioPreviewActivity;
import me.zuichu.picker.ui.image.ImageBaseActivity;
import me.zuichu.picker.view.FolderPopUpWindow;

/**
 * 音频的加载界面   注意 音频选择不需要剪裁
 */
public class FileGridActivity extends ImageBaseActivity implements FileDataSource.OnFilesLoadedListener, FileGridAdapter.OnFileItemClickListener, FilePicker.OnFileSelectedListener, View.OnClickListener {

    public static final int REQUEST_PERMISSION_STORAGE = 0x01;
    public static final int REQUEST_PERMISSION_CAMERA = 0x02;

    private FilePicker filePicker;

    private boolean isOrigin = false;  //是否选中原图
    private GridView mGridView;  //视频展示控件
    private View mFooterBar;     //底部栏
    private Button mBtnOk;       //确定按钮
    private Button mBtnDir;      //文件夹切换按钮
    private Button mBtnPre;      //预览按钮
    private FileFolderAdapter mFileFolderAdapter;    //音频文件夹的适配器
    private FolderPopUpWindow mFolderPopupWindow;  //VideoSet的PopupWindow
    private List<FileFolder> mFileFolders;   //所有的音频文件夹
    private FileGridAdapter mFileGridAdapter;  //视频九宫格展示的适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_grid);

        filePicker = FilePicker.getInstance();
        filePicker.clear();
        //音频加载完成是回调该接口
        filePicker.addOnFileSelectedListener(this);

        findViewById(R.id.btn_back).setOnClickListener(this);
        mBtnOk = (Button) findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(this);
        mBtnDir = (Button) findViewById(R.id.btn_dir);
        mBtnDir.setOnClickListener(this);
        mBtnPre = (Button) findViewById(R.id.btn_preview);
        mBtnPre.setOnClickListener(this);
        mGridView = (GridView) findViewById(R.id.gridview);
        mFooterBar = findViewById(R.id.footer_bar);
        if (filePicker.isMultiMode()) {
            mBtnOk.setVisibility(View.VISIBLE);
            mBtnPre.setVisibility(View.VISIBLE);
        } else {
            mBtnOk.setVisibility(View.GONE);
            mBtnPre.setVisibility(View.GONE);
        }

        mFileGridAdapter = new FileGridAdapter(this, null);
        mFileFolderAdapter = new FileFolderAdapter(this, null);

        onFileSelected(0, null, false);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //加载手机中的视频数据
                new FileDataSource(this, null, this);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
            }
        }
    }

    /**
     * 6.0以上权限检查
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new FileDataSource(this, null, this);
            } else {
                showToast("权限被禁止，无法选择本地视频");
            }
        } else if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //录像
                filePicker.takeRecord(this, FilePicker.REQUEST_FILE_TAKE);
            } else {
                showToast("权限被禁止，无法打开相机");
            }
        }
    }

    @Override
    protected void onDestroy() {
        filePicker.removeOnFileSelectedListener(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        //点击完成
        if (id == R.id.btn_ok) {
            Intent intent = new Intent();
            intent.putExtra(AudioPicker.EXTRA_RESULT_AUDIO_ITEMS, filePicker.getSelectedFiles());
            setResult(AudioPicker.RESULT_AUDIO_ITEMS, intent);  //返回数据
            finish();
        } else if (id == R.id.btn_dir) {
            //点击全部音频
            if (mFileFolders == null) {
                Toast.makeText(FileGridActivity.this, "您的手机没有音频", Toast.LENGTH_SHORT).show();
                return;
            }
            //点击文件夹按钮
            createPopupFolderList();
            mFileFolderAdapter.refreshData(mFileFolders);  //刷新数据
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            } else {
                mFolderPopupWindow.showAtLocation(mFooterBar, Gravity.NO_GRAVITY, 0, 0);
                //默认选择当前选择的上一个，当目录很多时，直接定位到已选中的条目
                int index = mFileFolderAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                mFolderPopupWindow.setSelection(index);
            }
        } else if (id == R.id.btn_preview) {
            /**
             * 点击预览的事件处理  在onActivityResult中回调处理
             */
            Intent intent = new Intent(FileGridActivity.this, FilePreviewActivity.class);
            intent.putExtra(FilePicker.EXTRA_SELECTED_FILE_POSITION, 0);
            intent.putExtra(FilePicker.EXTRA_FILE_ITEMS, filePicker.getSelectedFiles());
            intent.putExtra(FilePreviewActivity.ISORIGIN, isOrigin);
            startActivityForResult(intent, FilePicker.REQUEST_FILE_PREVIEW);
        } else if (id == R.id.btn_back) {
            //点击返回按钮
            finish();
        }
    }

    /**
     * 创建弹出的ListView
     */
    private void createPopupFolderList() {
        mFolderPopupWindow = new FolderPopUpWindow(this, mFileFolderAdapter);
        mFolderPopupWindow.setOnItemClickListener(new FolderPopUpWindow.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mFileFolderAdapter.setSelectIndex(position);
                filePicker.setCurrentFileFolderPosition(position);
                mFolderPopupWindow.dismiss();
                FileFolder fileFolder = (FileFolder) adapterView.getAdapter().getItem(position);
                if (null != fileFolder) {
                    mFileGridAdapter.refreshData(fileFolder.files);
                    mBtnDir.setText(fileFolder.name);
                }
                mGridView.smoothScrollToPosition(0);//滑动到顶部
            }
        });
        mFolderPopupWindow.setMargin(mFooterBar.getHeight());
    }

    @Override
    public void onFilesLoaded(List<FileFolder> fileFolders) {
        this.mFileFolders = fileFolders;
        filePicker.setFileFolders(fileFolders);
        if (fileFolders.size() == 0) mFileGridAdapter.refreshData(null);
        else mFileGridAdapter.refreshData(fileFolders.get(0).files);
        //视频点击的监听事件
        mFileGridAdapter.setOnFileItemClickListener(this);
        mGridView.setAdapter(mFileGridAdapter);
        mFileFolderAdapter.refreshData(fileFolders);
    }

    /**
     * 点击grid每一项的事件回调
     *
     * @param view
     * @param fileItem
     * @param position
     */
    @Override
    public void onFileItemClick(View view, FileItem fileItem, int position) {
        //根据是否有相机按钮确定位置
        position = filePicker.isShowCamera() ? position - 1 : position;
        //多选音频
        if (filePicker.isMultiMode()) {
            Intent intent = new Intent(FileGridActivity.this, FilePreviewActivity.class);
            intent.putExtra(FilePicker.EXTRA_SELECTED_FILE_POSITION, position);
            intent.putExtra(FilePicker.EXTRA_FILE_ITEMS, filePicker.getCurrentFileFolderItems());
            intent.putExtra(FilePreviewActivity.ISORIGIN, isOrigin);
            startActivityForResult(intent, FilePicker.REQUEST_FILE_PREVIEW);  //如果是多选，点击音频进入预览界面
        } else {
            //单选音频 直接返回选中的音频
            filePicker.clearSelectedFiles();
            filePicker.addSelectedFileItem(position, filePicker.getCurrentFileFolderItems().get(position), true);
            Intent intent = new Intent();
            intent.putExtra(FilePicker.EXTRA_RESULT_FILE_ITEMS, filePicker.getSelectedFiles());
            setResult(FilePicker.RESULT_FILE_ITEMS, intent);
            finish();
        }
    }

    /**
     * 视频选中的监听
     * 视频添加成功后，修改当前视频的选中数量
     * 当调用 addSelectedVideoItem 或 deleteSelectedVideoItem 都会触发当前回调
     *
     * @param position
     * @param item
     * @param isAdd
     */
    @Override
    public void onFileSelected(int position, FileItem item, boolean isAdd) {
        if (filePicker.getSelectFileCount() > 0) {
            mBtnOk.setText(getString(R.string.select_complete, filePicker.getSelectFileCount(), filePicker.getSelectLimit()));
            mBtnOk.setEnabled(true);
            mBtnPre.setEnabled(true);
        } else {
            mBtnOk.setText(getString(R.string.complete));
            mBtnOk.setEnabled(false);
            mBtnPre.setEnabled(false);
        }
        mBtnPre.setText(getResources().getString(R.string.preview_count, filePicker.getSelectFileCount()));
        mFileGridAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            //如果从预览界面返回  判断是否在预览界面选择了原图
            if (resultCode == FilePicker.RESULT_FILE_BACK) {
                //从预览界面接收是否选择原图
                isOrigin = data.getBooleanExtra(AudioPreviewActivity.ISORIGIN, false);
            } else {
                //从拍照界面返回
                //点击 X , 没有选择照片
                if (data.getSerializableExtra(FilePicker.EXTRA_FILE_ITEMS) == null) {
                    //什么都不做
                } else {
                    //说明是从裁剪页面过来的数据，直接返回就可以  应该是从录像界面返回的视频数据  还未作处理
                    setResult(FilePicker.RESULT_FILE_ITEMS, data);
                    finish();
                }
            }
        }
        //录音的结果返回事件
        if (resultCode == RESULT_OK && requestCode == FilePicker.REQUEST_FILE_TAKE) {
            //发送广播通知视频增加了
            FilePicker.galleryAddPic(this, filePicker.getTakeFile());
            FileItem fileItem = new FileItem();
            fileItem.path = filePicker.getTakeFile().getAbsolutePath();
            filePicker.clearSelectedFiles();
            //添加选中这个录像视频
            filePicker.addSelectedFileItem(0, fileItem, true);
            //更新界面设置录像的这个视频为选中状态  在Adapter中使用接口实现
            Intent intent = new Intent();
            intent.putExtra(FilePicker.EXTRA_RESULT_FILE_ITEMS, filePicker.getSelectedFiles());
            setResult(FilePicker.RESULT_FILE_ITEMS, intent);   //单选不需要裁剪，返回数据
            finish();
        }
    }
}
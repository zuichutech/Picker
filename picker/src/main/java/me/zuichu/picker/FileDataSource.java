package me.zuichu.picker;


import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.zuichu.picker.bean.FileFolder;
import me.zuichu.picker.bean.FileItem;

/**
 * 谭东增加扩充
 * QQ 852041173
 */

public class FileDataSource implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_ALL = 0;         //加载所有文件
    public static final int LOADER_CATEGORY = 1;    //分类加载音频
    private final String[] FILE_PROJECTION = {     //查询文件需要的数据列
            MediaStore.Files.FileColumns.TITLE,   //文件的名称  aaa.jpg
            MediaStore.Files.FileColumns.DISPLAY_NAME,  //文件显示名称,string型
            MediaStore.Files.FileColumns.PARENT,           //文件的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
            MediaStore.Files.FileColumns.SIZE,           //文件的大小，long型  132492
            MediaStore.Files.FileColumns.DATA,          //文件真是路径，string型
            MediaStore.Files.FileColumns.DATE_MODIFIED,         //文件修改时间，string型
            MediaStore.Files.FileColumns.MIME_TYPE,      //文件的类型     image/jpeg
            MediaStore.Files.FileColumns.DATE_ADDED};   //文件被添加的时间，long型  1450518608
    private FragmentActivity activity;
    private OnFilesLoadedListener loadedListener;                     //文件加载完成的回调接口
    private ArrayList<FileFolder> fileFolders = new ArrayList<FileFolder>();   //所有的文件文件夹

    /**
     * @param activity       用于初始化LoaderManager，需要兼容到2.3
     * @param path           指定扫描的文件夹目录，可以为 null，表示扫描所有音频
     * @param loadedListener 音频加载完成的监听
     */
    public FileDataSource(FragmentActivity activity, String path, OnFilesLoadedListener loadedListener) {
        this.activity = activity;
        this.loadedListener = loadedListener;

        //得到LoaderManager对象
        LoaderManager loaderManager = activity.getSupportLoaderManager();
        //初始化loader
        if (path == null) {
            loaderManager.initLoader(LOADER_ALL, null, this);//加载所有的音频
        } else {
            //加载指定目录的音频
            Bundle bundle = new Bundle();
            bundle.putString("path", path);
            loaderManager.initLoader(LOADER_CATEGORY, bundle, this);
        }
    }

    /**
     * 指定ID不存在 触发该方法返回一个新的loader对象
     *
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;
        //扫描所有音频
        //查询ContentResolver并返回一个Cursor对象
        if (id == LOADER_ALL)
            cursorLoader = new CursorLoader(activity, MediaStore.Files.getContentUri("external"), FILE_PROJECTION, MediaStore.Files.FileColumns.MIME_TYPE + "= ?", new String[]{Conf.TYPE_MP4}, FILE_PROJECTION[7] + " DESC");
        //扫描某个音频文件夹
        if (id == LOADER_CATEGORY)
            cursorLoader = new CursorLoader(activity, MediaStore.Files.getContentUri("external"), FILE_PROJECTION, FILE_PROJECTION[4] + " like '%" + args.getString("path") + "%'", null, FILE_PROJECTION[7] + " DESC");

        return cursorLoader;
    }

    /**
     * 完成对UI界面的更新
     *
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        fileFolders.clear();
        if (data != null) {
            ArrayList<FileItem> allFiles = new ArrayList<FileItem>();   //所有文件的集合,不分文件夹
            while (data.moveToNext()) {
                //查询数据
                String fileName = data.getString(data.getColumnIndexOrThrow(FILE_PROJECTION[0]));
                String displayName = data.getString(data.getColumnIndexOrThrow(FILE_PROJECTION[1]));
                String parent = data.getString(data.getColumnIndexOrThrow(FILE_PROJECTION[2]));
                long fileSize = data.getLong(data.getColumnIndexOrThrow(FILE_PROJECTION[3]));
                String dataPath = data.getString(data.getColumnIndexOrThrow(FILE_PROJECTION[4]));
                long fileModifiedTime = data.getLong(data.getColumnIndexOrThrow(FILE_PROJECTION[5]));
                String fileMimeType = data.getString(data.getColumnIndexOrThrow(FILE_PROJECTION[6]));
                long fileAddTime = data.getLong(data.getColumnIndexOrThrow(FILE_PROJECTION[7]));
                //封装实体
                FileItem fileItem = new FileItem();
                fileItem.name = fileName;
                fileItem.displayName = displayName;
                fileItem.path = parent;
                fileItem.size = fileSize;
                fileItem.dataPath = dataPath;
                fileItem.modifiTime = fileModifiedTime;
                fileItem.mimeType = fileMimeType;
                fileItem.addTime = fileAddTime;
                allFiles.add(fileItem);
                //根据父路径分类存放音频
                //根据音频的路径获取到音频所在文件夹的路径和名称
                File file = new File(dataPath);
                File fileParentFile = file.getParentFile();
                FileFolder fileFolder = new FileFolder();
                fileFolder.name = fileParentFile.getName();
                fileFolder.path = fileParentFile.getAbsolutePath();
                //判断这个文件夹是否已经存在  如果存在直接添加音频进去  否则将文件夹添加到文件夹的集合中
                if (!fileFolders.contains(fileFolder)) {
                    ArrayList<FileItem> files = new ArrayList<FileItem>();
                    files.add(fileItem);
                    //缩略图
                    fileFolder.cover = fileItem;
                    fileFolder.files = files;
                    fileFolders.add(fileFolder);
                } else {
                    fileFolders.get(fileFolders.indexOf(fileFolder)).files.add(fileItem);
                }
            }
            //防止没有音频报异常
            if (data.getCount() > 0) {
                //构造所有音频的集合
                FileFolder allFilesFolder = new FileFolder();
                allFilesFolder.name = activity.getResources().getString(R.string.all_files);
                allFilesFolder.path = "/";
                //把第一张设置缩略图
                allFilesFolder.cover = allFiles.get(0);
                allFilesFolder.files = allFiles;
                fileFolders.add(0, allFilesFolder);  //确保第一条是所有图片
            }
        }

        //回调接口，通知文件数据准备完成
        FilePicker.getInstance().

                setFileFolders(fileFolders);

        loadedListener.onFilesLoaded(fileFolders);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        System.out.println("--------");
    }

    /**
     * 所有文件加载完成的回调接口
     */
    public interface OnFilesLoadedListener {
        void onFilesLoaded(List<FileFolder> fileFolders);
    }
}

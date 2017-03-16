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

import me.zuichu.picker.bean.AudioFolder;
import me.zuichu.picker.bean.AudioItem;
/**
 * 谭东增加扩充
 * QQ 852041173
 */

public class AudioDataSource implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_ALL = 0;         //加载所有音频
    public static final int LOADER_CATEGORY = 1;    //分类加载音频
    private final String[] AUDIO_PROJECTION = {     //查询音频需要的数据列
            MediaStore.Audio.Media.TITLE,   //音频的名称  aaa.jpg
            MediaStore.Audio.Media.DISPLAY_NAME,  //音频的文件名称,string型
            MediaStore.Audio.Media.DATA,           //音频的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
            MediaStore.Audio.Media.SIZE,           //音频的大小，long型  132492
            MediaStore.Audio.Media.ALBUM,          //歌曲专辑名，string型
            MediaStore.Audio.Media.ARTIST,         //音频的演唱者，string型
            MediaStore.Audio.Media.MIME_TYPE,      //音频的类型     image/jpeg
            MediaStore.Audio.Media.DATE_ADDED       //音频被添加的时间，long型  1450518608
            , MediaStore.Audio.Media.DURATION};    //音频的时长
    private FragmentActivity activity;
    private OnAudiosLoadedListener loadedListener;                     //音频加载完成的回调接口
    private ArrayList<AudioFolder> audioFolders = new ArrayList<>();   //所有的音频文件夹

    /**
     * @param activity       用于初始化LoaderManager，需要兼容到2.3
     * @param path           指定扫描的文件夹目录，可以为 null，表示扫描所有音频
     * @param loadedListener 音频加载完成的监听
     */
    public AudioDataSource(FragmentActivity activity, String path, OnAudiosLoadedListener loadedListener) {
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
            cursorLoader = new CursorLoader(activity, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, AUDIO_PROJECTION, null, null, AUDIO_PROJECTION[8] + " DESC");
        //扫描某个音频文件夹
        if (id == LOADER_CATEGORY)
            cursorLoader = new CursorLoader(activity, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, AUDIO_PROJECTION, AUDIO_PROJECTION[2] + " like '%" + args.getString("path") + "%'", null, AUDIO_PROJECTION[8] + " DESC");

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
        audioFolders.clear();
        if (data != null) {
            ArrayList<AudioItem> allAudios = new ArrayList<>();   //所有音频的集合,不分文件夹
            while (data.moveToNext()) {
                //查询数据
                String audioName = data.getString(data.getColumnIndexOrThrow(AUDIO_PROJECTION[0]));
                String audioFileName = data.getString(data.getColumnIndexOrThrow(AUDIO_PROJECTION[1]));
                String audioPath = data.getString(data.getColumnIndexOrThrow(AUDIO_PROJECTION[2]));
                long audioSize = data.getLong(data.getColumnIndexOrThrow(AUDIO_PROJECTION[3]));
                String audioAlbum = data.getString(data.getColumnIndexOrThrow(AUDIO_PROJECTION[4]));
                String audioArtist = data.getString(data.getColumnIndexOrThrow(AUDIO_PROJECTION[5]));
                String audioMimeType = data.getString(data.getColumnIndexOrThrow(AUDIO_PROJECTION[6]));
                long audioAddTime = data.getLong(data.getColumnIndexOrThrow(AUDIO_PROJECTION[7]));
                long audioTimeLong = data.getLong(data.getColumnIndexOrThrow(AUDIO_PROJECTION[8]));
                //封装实体
                AudioItem audioItem = new AudioItem();
                audioItem.name = audioName;
                audioItem.fileName = audioFileName;
                audioItem.path = audioPath;
                audioItem.size = audioSize;
                audioItem.album = audioAlbum;
                audioItem.artist = audioArtist;
                audioItem.mimeType = audioMimeType;
                audioItem.addTime = audioAddTime;
                audioItem.timeLong = audioTimeLong;
                allAudios.add(audioItem);
                //根据父路径分类存放音频
                //根据音频的路径获取到音频所在文件夹的路径和名称
                File audioFile = new File(audioPath);
                File audioParentFile = audioFile.getParentFile();
                AudioFolder audioFolder = new AudioFolder();
                audioFolder.name = audioParentFile.getName();
                audioFolder.path = audioParentFile.getAbsolutePath();
                //判断这个文件夹是否已经存在  如果存在直接添加音频进去  否则将文件夹添加到文件夹的集合中
                if (!audioFolders.contains(audioFolder)) {
                    ArrayList<AudioItem> images = new ArrayList<>();
                    images.add(audioItem);
                    //缩略图
                    audioFolder.cover = audioItem;
                    audioFolder.audios = images;
                    audioFolders.add(audioFolder);
                } else {
                    audioFolders.get(audioFolders.indexOf(audioFolder)).audios.add(audioItem);
                }
            }
            //防止没有音频报异常
            if (data.getCount() > 0) {
                //构造所有音频的集合
                AudioFolder allAudiosFolder = new AudioFolder();
                allAudiosFolder.name = activity.getResources().getString(R.string.all_audios);
                allAudiosFolder.path = "/";
                //把第一张设置缩略图
                allAudiosFolder.cover = allAudios.get(0);
                allAudiosFolder.audios = allAudios;
                audioFolders.add(0, allAudiosFolder);  //确保第一条是所有图片
            }
        }

        //回调接口，通知音频数据准备完成
        AudioPicker.getInstance().
                setAudioFolders(audioFolders);
        loadedListener.onAudiosLoaded(audioFolders);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        System.out.println("--------");
    }

    /**
     * 所有音频加载完成的回调接口
     */
    public interface OnAudiosLoadedListener {
        void onAudiosLoaded(List<AudioFolder> audioFolders);
    }
}

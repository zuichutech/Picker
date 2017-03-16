package me.zuichu.pickerexample.imageloader;


import android.app.Activity;
import android.net.Uri;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import me.zuichu.picker.loader.ImageLoader;

public class UILImageLoader implements ImageLoader {

    @Override
    public void displayImage(Activity activity, String path, ImageView imageView, int width, int height) {
        ImageSize size = new ImageSize(width, height);
        try {
            com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(URLDecoder.decode(Uri.fromFile(new File(path)) + "","utf-8"), imageView, size);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearMemoryCache() {

    }
}

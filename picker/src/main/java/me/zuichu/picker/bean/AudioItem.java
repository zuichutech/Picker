package me.zuichu.picker.bean;

import java.io.Serializable;

public class AudioItem implements Serializable {
    public String name;       //音频的名字
    public String fileName;  //音频的文件名称
    public String path;       //音频的路径
    public long size;         //音频的大小
    public String album;         //音频的专辑名
    public String artist;        //音频的演唱者
    public String mimeType;   //音频的类型
    public long addTime;      //音频的创建时间
    public long timeLong;      //音频的时长

    /**
     * 音频的路径和创建时间相同就认为是同一个音频
     */
    @Override
    public boolean equals(Object o) {
        try {
            AudioItem other = (AudioItem) o;
            return this.path.equalsIgnoreCase(other.path) && this.addTime == other.addTime;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }
}

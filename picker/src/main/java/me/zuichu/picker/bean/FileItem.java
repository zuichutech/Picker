package me.zuichu.picker.bean;

import java.io.Serializable;

public class FileItem implements Serializable {
    public String name;       //文件的名字
    public String path;       //文件的路径
    public String dataPath;//文件的真实路径
    public String displayName;//文件显示名称
    public long size;         //文件的大小
    public String mimeType;   //文件的类型
    public long addTime;      //文件的创建时间
    public long modifiTime;      //文件修改时间

    /**
     * 文件的路径和创建时间相同就认为是同一个文件
     */
    @Override
    public boolean equals(Object o) {
        try {
            FileItem other = (FileItem) o;
            return this.path.equalsIgnoreCase(other.path) && this.addTime == other.addTime;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }
}

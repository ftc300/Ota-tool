package inshow.carl.com.csd.entity;

import com.orm.SugarRecord;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/4/4
 * @ 描述:
 */


public class FileEntity  extends SugarRecord{
    public String filePath;
    public String fileName;

    public FileEntity() {
    }

    public FileEntity(String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
    }
}

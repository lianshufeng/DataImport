package com.github.data.core.helper;

import com.github.data.core.conf.DataConf;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.RandomAccessFile;

@Component
public class ExportTextHelper {

    @Autowired
    private DataConf dataConf;


    /**
     * 写出一行
     */
    @SneakyThrows
    public synchronized void writeLine(String path, String fileName, String extName, String lineText) {
        final String text = lineText + this.dataConf.getExportNewLineChar();
        File outFile = getFile(path, fileName, extName);
        //文件不存在则创建父类目录
        if (!outFile.exists()) {
            outFile.getParentFile().mkdirs();
        }
        @Cleanup RandomAccessFile randomAccessFile = new RandomAccessFile(outFile, "rw");
        randomAccessFile.seek(randomAccessFile.length());
        randomAccessFile.write(text.getBytes(this.dataConf.getExportTextCharset()));
    }

    /**
     * 获取可以写的文件
     *
     * @param fileName
     * @param extName
     * @return
     */
    private File getFile(String path, String fileName, String extName) {
        for (int i = 0; i < 99999; i++) {
            File file = new File(path + "/" + fileName + i + "." + extName);
            if (!file.exists()) {
                return file;
            }
            if (file.length() < this.dataConf.getExportWriteSize()) {
                return file;
            }
        }
        return null;
    }


}

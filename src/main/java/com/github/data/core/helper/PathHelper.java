package com.github.data.core.helper;

import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class PathHelper {


    /**
     * 获取输入路径下的所有文件
     *
     * @return
     */
    public File[] readInputFiles() {
        File file = getInputPath();
        if (file.exists()) {
            return file.listFiles();
        }
        return new File[0];
    }


    /**
     * 获取输入路径
     *
     * @return
     */
    public File getInputPath() {
        return new File(new ApplicationHome().getDir().getAbsolutePath() + "/store/import");
    }


    /**
     * 获取导出路径
     *
     * @return
     */
    public File getExportPath() {
        return new File(new ApplicationHome().getDir().getAbsolutePath() + "/store/export");
    }

}

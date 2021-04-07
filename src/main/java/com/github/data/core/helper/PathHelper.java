package com.github.data.core.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class PathHelper {


    @Autowired
    private void initStore(ApplicationContext applicationContext) {
        getDataInputPath().mkdirs();
        getDataExportPath().mkdirs();
        getTransformImportPath().mkdirs();
        getTransformExportPath().mkdirs();
        getPhoneLibPath().mkdirs();
    }

    private File getStore() {
        return new File(new ApplicationHome().getDir().getAbsolutePath() + "/store/");
    }


    /**
     * 获取输入路径
     *
     * @return
     */
    public File getDataInputPath() {
        return new File(getStore().getAbsolutePath() + "/data/import");
    }


    /**
     * 获取导出路径
     *
     * @return
     */
    public File getDataExportPath() {
        return new File(getStore().getAbsolutePath() + "/data/export");
    }


    /**
     * 获取转换目录
     *
     * @return
     */
    public File getTransformImportPath() {
        return new File(getStore().getAbsolutePath() + "/transform/import");
    }


    /**
     * 获取转换目录
     *
     * @return
     */
    public File getTransformExportPath() {
        return new File(getStore().getAbsolutePath() + "/transform/export");
    }


    /**
     * 获取手机号码归属地的库导入
     *
     * @return
     */
    public File getPhoneLibPath() {
        return new File(getStore().getAbsolutePath() + "/phone/import");
    }


}

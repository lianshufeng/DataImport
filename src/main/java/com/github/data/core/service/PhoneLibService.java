package com.github.data.core.service;

import com.github.data.core.conf.DataConf;
import com.github.data.core.dao.PhoneLibDao;
import com.github.data.core.domain.DataTable;
import com.github.data.core.domain.PhoneLib;
import com.github.data.core.util.ImeiUtil;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Service
public class PhoneLibService {

    //线程池数量
    private ExecutorService executorService;

    @Autowired
    private DataConf dataConf;

    @Autowired
    private PhoneLibDao phoneLibDao;

    @Autowired
    private void initThreadPool() {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }

        //线程池
        executorService = Executors.newFixedThreadPool(dataConf.getMaxThreadCount());


        //shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdownNow();
        }));
    }


    /**
     * 导入数据
     */
    public void importData(File... files) {
        for (File file : files) {
            executorService.execute(() -> {
                readAndSaveData(file);
            });
        }
    }


    @SneakyThrows
    private void readAndSaveData(File file) {
        log.info("读取文件 : {}", file.getName());

        @Cleanup FileReader fileReader = new FileReader(file);
        @Cleanup BufferedReader reader = new BufferedReader(fileReader);
        String line = null;
        while ((line = reader.readLine()) != null) {
            final String lineText = line;
            try {
                executorService.execute(() -> {
                    saveData(lineText);
                });
            } catch (Exception e) {
                e.printStackTrace();
                log.error("e : {}", e);
            }
        }
    }

    /**
     * 保存数据
     *
     * @param line
     */
    private void saveData(String line) {
        String[] items = line.split(",");
        PhoneLib phoneLib = new PhoneLib();
        phoneLib.setPhone(items[0]);
        phoneLib.setArea(items[1]);
        phoneLib.setProvince(items[2]);
        phoneLib.setCity(items[3]);
        phoneLib.setCatName(items[4]);
        log.info("update : {}", phoneLib);
        phoneLibDao.updateData(phoneLib);

    }

}

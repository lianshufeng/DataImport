package com.github.data.core.service;

import com.github.data.core.dao.DataTableDao;
import com.github.data.core.domain.DataTable;
import com.github.data.core.util.*;
import com.github.data.core.conf.DataConf;
import com.github.data.other.mongo.helper.DBHelper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class DataService {

    @Autowired
    private DataConf dataConf;

    @Autowired
    private DataTableDao dataTableDao;

    @Autowired
    private MongoTemplate mongoTemplate;


    //线程池数量
    private ExecutorService executorService;

    @Autowired
    private DBHelper dbHelper;


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


    /**
     * 导出数据
     */
    @SneakyThrows
    public void exportData() {

        //查询所有数据
        MongoCollection<Document> mongoCollection = this.mongoTemplate.getCollection(this.mongoTemplate.getCollectionName(DataTable.class));
        MongoCursor<Document> mongoCursor = mongoCollection.find().cursor();
        while (mongoCursor.hasNext()) {
            final Document document = mongoCursor.next();
            executorService.execute(() -> {
                writeDataTable(document);
            });
        }
    }


    /**
     * 写出到磁盘上
     */
    @SneakyThrows
    private synchronized void writeDataTable(Document document) {
        final DataTable dataTable = JsonUtil.toObject(dbHelper.toJson(document), DataTable.class);


//        RandomAccessFile randomAccessFile = new RandomAccessFile();
//        System.out.println(dataTable);
    }


    /**
     * 读取并入库
     */
    @SneakyThrows
    private void readAndSaveData(File file) {
        log.info("读取文件 : {}", file.getName());

        @Cleanup FileReader fileReader = new FileReader(file);
        @Cleanup BufferedReader reader = new BufferedReader(fileReader);
        String line = null;
        while ((line = reader.readLine()) != null) {
            final String lineText = line;
            this.executorService.execute(() -> {
                saveData(lineText);
            });
        }


    }


    /**
     * 保存数据
     *
     * @param line
     */
    private void saveData(String line) {
        if (line.indexOf(",") == -1) {
            return;
        }
        String[] items = line.split(",");
        if (items.length < 2) {
            return;
        }
        String phone = items[0];
        if (phone.length() != 11) {
            return;
        }
        String imei = items[1];
        if (imei.length() != 15) {
            return;
        }
        imei = ImeiUtil.build(imei);
        log.info(" {} -> {}", phone, imei);


        DataTable dataTable = new DataTable();

        setPhone(dataTable, phone);
        setImei(dataTable, imei);


        this.dataTableDao.replaceFromImei(dataTable);

    }

    @SneakyThrows
    private void setPhone(DataTable dataTable, String phone) {
        dataTable.setPhone(phone);
        dataTable.setPhoneHash(CRC32Util.update2(phone));

        //查询号码归属地
        String url = String.format("https://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel=%s", phone);
        String info = new String(new HttpClient().get(url), "GBK");
        dataTable.setProvince(TextUtil.subText(info, "province:'", "'", 0));
        dataTable.setCatName(TextUtil.subText(info, "catName:'", "'", 0));

    }


    /**
     * 设置Imei
     *
     * @param dataTable
     * @param imei
     */
    private void setImei(DataTable dataTable, String imei) {
        dataTable.setImei(imei);
        dataTable.setImeiHash(DigestUtils.md5DigestAsHex(imei.getBytes(StandardCharsets.UTF_8)).toUpperCase());
    }


}

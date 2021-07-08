package com.github.data.core.service;

import com.github.data.core.conf.DataConf;
import com.github.data.core.dao.DataTableDao;
import com.github.data.core.dao.PhoneLibDao;
import com.github.data.core.domain.DataTable;
import com.github.data.core.helper.ExportTextHelper;
import com.github.data.core.helper.PathHelper;
import com.github.data.core.util.CRC32Util;
import com.github.data.core.util.ImeiUtil;
import com.github.data.core.util.JsonUtil;
import com.github.data.core.util.SpringELUtil;
import com.github.data.other.mongo.helper.DBHelper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.bson.Document;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

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
    private ExportTextHelper exportTextHelper;

    @Autowired
    private PathHelper pathHelper;

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

    /**
     * 转换数据
     */
    @SneakyThrows
    public void transformData(File... files) {
        //线程安全对象
//        repeat
        final Vector<String> phoneHashSet = new Vector<>();

        //清空存在的导出文件
        Arrays.stream(this.pathHelper.getTransformExportPath().listFiles()).forEach((file) -> {
            file.delete();
        });
        for (final File file : files) {
            executorService.execute(() -> {
                transformData(phoneHashSet, file);
            });
        }
    }


    /**
     * 转换数据
     *
     * @param file
     */
    @SneakyThrows
    private void transformData(final Vector<String> phoneHashSet, File file) {
        log.info("转换文件 : {}", file.getName());

        @Cleanup FileReader fileReader = new FileReader(file);
        @Cleanup BufferedReader reader = new BufferedReader(fileReader);
        String line = null;
        while ((line = reader.readLine()) != null) {
            final String lineText = line;
            try {
                int at = lineText.indexOf("|");
                if (at > -1) {
                    final String phoneHash = lineText.substring(0, at);
                    final List<DataTable> dataTables = this.dataTableDao.findByPhoneHash(phoneHash);
                    log.info("save : {}", dataTables);
                    if (dataTables != null && dataTables.size() > 0) {
                        dataTables.forEach((dataTable) -> {
                            if (phoneHashSet.contains(phoneHash)) {
                                log.info("重复数据：" + dataTable);
                            } else {
                                final String baseName = FilenameUtils.getBaseName(file.getName());
                                final String text = SpringELUtil.parseExpression(dataTable, dataConf.getTransformFormat()) + "|" + lineText;
                                exportTextHelper.writeLine(pathHelper.getTransformExportPath().getAbsolutePath(), baseName, "txt", text);
                                phoneHashSet.add(phoneHash);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }


    /**
     * 导出数据
     */
    @SneakyThrows
    public void exportData() {
        //清空导出目录
        Arrays.stream(this.pathHelper.getDataExportPath().listFiles()).forEach((file) -> {
            file.delete();
        });

        new Thread(() -> {
            //查询所有数据
            MongoCollection<Document> mongoCollection = this.mongoTemplate.getCollection(this.mongoTemplate.getCollectionName(DataTable.class));
            MongoCursor<Document> mongoCursor = mongoCollection.find().cursor();
            while (mongoCursor.hasNext()) {
                writeDataTable(mongoCursor.next());
            }
        }).start();

    }


    /**
     * 写出到磁盘上
     */
    @SneakyThrows
    private void writeDataTable(Document document) {
        try {
            final DataTable dataTable = JsonUtil.toObject(dbHelper.toJson(document), DataTable.class);
            log.info("write : {}", dataTable);
            String outputFileName = StringUtils.hasText(dataTable.getProvince()) ? dataTable.getProvince() + "/" + this.dataConf.getExportFileName() : this.dataConf.getExportFileName();
            exportTextHelper.writeLine(this.pathHelper.getDataExportPath().getAbsolutePath(), outputFileName, "txt", dataTable.getPhoneHash() + "|" + dataTable.getImeiHash());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("error : {}", e);
        }
    }


    /**
     * 读取并入库
     */
    @SneakyThrows
    private void readAndSaveData(File file) {
        log.info("读取文件 : {}", file.getName());

        AtomicLong count = new AtomicLong(0);
        long startTime = System.currentTimeMillis();
        @Cleanup FileReader fileReader = new FileReader(file);
        @Cleanup BufferedReader reader = new BufferedReader(fileReader);
        @Cleanup("cancel") Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long usedTime = (long) ((System.currentTimeMillis() - startTime) / 1000);
                log.info("count : {} , time : {} , rate : {} ", String.valueOf(count.get()), String.valueOf(usedTime), String.valueOf(count.get() / usedTime));
            }
        }, 1000, 1000);


        //任务池
        @Cleanup("shutdownNow") ExecutorService taskPool = Executors.newFixedThreadPool(dataConf.getMaxThreadCount());
        CountDownLatch countDownLatch = new CountDownLatch(dataConf.getMaxThreadCount());
        //启动线程
        for (int i = 0; i < dataConf.getMaxThreadCount(); i++) {
            readTask(taskPool, countDownLatch, reader, count);
        }
        countDownLatch.await();


//        String line = null;
//        while ((line = reader.readLine()) != null) {
//            count.addAndGet(1);
//            try {
//                String lineText = line.trim();
//                saveData(lineText);
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.error("e : {}", e);
//            }
//        }
    }


    private void readTask(ExecutorService taskPool, CountDownLatch countDownLatch, BufferedReader reader, AtomicLong count) {
        String line = readLine(reader, count);
        if (line == null) {
            countDownLatch.countDown();
        }

        //执行任务
        saveData(line.trim());

        //线程继续执行
        taskPool.execute(() -> {
            readTask(taskPool, countDownLatch, reader, count);
        });
    }


    private synchronized String readLine(BufferedReader reader, AtomicLong count) {
        try {
            String line = reader.readLine();
            count.addAndGet(1);
            return line;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

        //根据长度探测手机或者设备号
        String phone = null;
        String imei = null;
        for (String item : items) {
            if (item.length() == 11) {
                phone = item;
            } else if (item.length() == 15) {
                imei = item;
            }
            if (phone != null && imei != null) {
                break;
            }
        }

        if (phone == null || imei == null) {
            log.error("抛弃 : {}", line);
            return;
        }

//        String phone = items[0];
//        if (phone.length() != 11) {
//            return;
//        }
//        String imei = items[1];
//        if (imei.length() != 15) {
//            return;
//        }
        imei = ImeiUtil.build(imei);
//        log.info(" {} -> {}", phone, imei);


        final DataTable dataTable = new DataTable();

        setPhone(dataTable, phone);
        setImei(dataTable, imei);


        this.dataTableDao.replaceFromImei(dataTable);

//        executorService.execute(() -> {
//            this.dataTableDao.replaceFromImei(dataTable);
//        });


    }

    @SneakyThrows
    private void setPhone(DataTable dataTable, String phone) {
        dataTable.setPhone(phone);
        dataTable.setPhoneHash(CRC32Util.update2(phone));

        if (this.dataConf.isImportPhoneProvince() && phone.length() >= 7) {
            Optional.ofNullable(this.phoneLibDao.findByPhone(phone.substring(0, 7))).ifPresent((it) -> {
                BeanUtils.copyProperties(it, dataTable, "phone", "id");
            });
        }


//        //查询号码归属地
//        String url = String.format("https://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel=%s", phone);
//        String info = new String(new HttpClient().get(url), "GBK");
//        dataTable.setProvince(TextUtil.subText(info, "province:'", "'", 0));
//        dataTable.setCatName(TextUtil.subText(info, "catName:'", "'", 0));

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

package com.github.data.core.controller;

import com.github.data.core.helper.PathHelper;
import com.github.data.core.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"data"})
public class DataController {


    @Autowired
    private PathHelper pathHelper;

    @Autowired
    private DataService dataService;


    /**
     * 导入
     *
     * @return
     */
    @RequestMapping({"import"})
    public Object index() {
        File[] files = pathHelper.getDataInputPath().listFiles();
        this.dataService.importData(files);
        return new HashMap<String, Object>() {{
            put("files", Arrays.stream(files).map((it) -> {
                return it.getName();
            }).collect(Collectors.toList()));
            put("tips", "开始执行任务,详情请看控制台");
        }};
    }


    /**
     * 导出
     *
     * @return
     */
    @RequestMapping({"export"})
    public Object export() {
        this.dataService.exportData();
        return new HashMap<String, Object>() {{
            put("tips", "开始导数据,详情请看控制台");
        }};
    }

    /**
     * 提取/转换
     *
     * @return
     */
    @RequestMapping({"transform"})
    public Object transform() {
        File[] files = pathHelper.getTransformImportPath().listFiles();
        this.dataService.transformData(files);
        return new HashMap<String, Object>() {{
            put("files", Arrays.stream(files).map((it) -> {
                return it.getName();
            }).collect(Collectors.toList()));
            put("tips", "开始提取数据,详情请看控制台数据");
            put("数据存放", pathHelper.getTransformExportPath().getAbsolutePath().replaceAll("\\\\", "/"));
        }};
    }

}

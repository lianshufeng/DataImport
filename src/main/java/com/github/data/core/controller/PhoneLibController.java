package com.github.data.core.controller;

import com.github.data.core.helper.PathHelper;
import com.github.data.core.service.DataService;
import com.github.data.core.service.PhoneLibService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"phone/lib/"})
public class PhoneLibController {

    @Autowired
    private PhoneLibService phoneLibService;

    @Autowired
    private PathHelper pathHelper;


    /**
     * 导入
     *
     * @return
     */
    @RequestMapping({"import"})
    public Object index() {
        File[] files = pathHelper.getPhoneLibPath().listFiles();
        this.phoneLibService.importData(files);
        return new HashMap<String, Object>() {{
            put("files", Arrays.stream(files).map((it) -> {
                return it.getName();
            }).collect(Collectors.toList()));
            put("tips", "开始执行任务,详情请看控制台");
        }};
    }

}

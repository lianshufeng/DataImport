package com.github.data.core.conf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "data")
public class DataConf {

    //最大线程数
    private int maxThreadCount = 10;

    //导出文件的名称
    private String exportFileName = "output";

    //导出文件的限制
    private long exportWriteSize = 1024 * 1024 * 400;

    //导出的换行符
    private String exportNewLineChar = "\r\n";

    //导出的编码
    private String exportTextCharset = "GBK";


}

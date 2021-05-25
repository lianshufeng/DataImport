package com.github.data.core.controller;

import com.github.data.core.conf.DataConf;
import com.github.data.core.helper.ExportTextHelper;
import com.github.data.core.helper.PathHelper;
import com.github.data.core.util.TextUtil;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping({"url"})
public class TransformUrlController {
    @Autowired
    private DataConf dataConf;

    @Autowired
    private PathHelper pathHelper;

    @Autowired
    private ExportTextHelper exportTextHelper;

    /**
     * 开始转换
     *
     * @return
     */
    @RequestMapping({"transform"})
    public Object transform() {
        List<File> files = Arrays.stream(pathHelper.getTransformUrlPath().listFiles()).filter((file) -> {
            return FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ROOT).equals("txt");
        }).collect(Collectors.toList());

        files.forEach((file) -> {
            transform(file);
        });

        return new HashMap<String, Object>() {{
            put("tips", "转换完成");
            put("files", files.stream().map((file) -> {
                return FilenameUtils.getBaseName(file.getName());
            }).collect(Collectors.toList()));
        }};
    }

    /**
     * 转换文件
     */
    @SneakyThrows
    private void transform(File file) {
        log.info("读取文件 : {}", file.getName());

        String fileName = file.getAbsolutePath();
        final String outputPath = file.getParentFile().getAbsolutePath();
        final String outputFileName = FilenameUtils.getBaseName(fileName);
        final String extName = "csv";
        final File outFile = new File(outputPath + "/" + outputFileName + "." + extName);
        if (outFile.exists()) {
            outFile.delete();
        }
        writeLine(outFile, "url_host,is_host_fuzzy,url_parse_regex,comments");
        @Cleanup FileReader fileReader = new FileReader(file);
        @Cleanup BufferedReader reader = new BufferedReader(fileReader);
        String line = null;
        while ((line = reader.readLine()) != null) {
            try {
                transformAndSaveData(outFile, line);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("e : {}", e);
            }
        }
    }

    /**
     * 写出数据
     */
    private void transformAndSaveData(File outFile, String line) {
        if (!StringUtils.hasText(line)) {
            return;
        }
        String[] items = line.split("\\|");
        if (items.length < 1) {
            return;
        }
        // url
        String url = items[0].trim();
        // 备注
        String comments = "";
        if (items.length > 1) {
            comments = items[1].trim();
        }

        //提取 url_host
        String url_host = null;
        if (url.indexOf("://") > -1) {
            url_host = TextUtil.subText(url, "://", "/", 0);
        } else {
            url_host = url.substring(0, url.indexOf("/"));
        }

        if (url_host.indexOf(":") > -1) {
            url_host = url_host.substring(0, url_host.indexOf(":"));
        }


        //过滤请求的参数
        String preciseUrl = url;
        if (preciseUrl.indexOf("?") > -1) {
            preciseUrl = preciseUrl.substring(0, preciseUrl.indexOf("?"));
        }

        String is_host_fuzzy = "0";
        String url_parse_regex = "";

        //过滤host
        preciseUrl = preciseUrl.substring(preciseUrl.indexOf(url_host) + url_host.length(), preciseUrl.length());
        //过滤参数
        if (StringUtils.hasText(preciseUrl)) {
            String[] layers = preciseUrl.split("/");
            //使用第N层开始模糊匹配
            int layerCounter = 0;
            for (int i = 0; i < layers.length; i++) {
                layerCounter = layers.length - i - 1;
                String lastLayer = layers[layerCounter];
                if (lastLayer.indexOf("index") > -1 && lastLayer.indexOf(".html") > -1) {
                    continue;
                } else {
                    break;
                }
            }

            StringBuilder dimText = new StringBuilder();
            for (int i = 0; i < layerCounter; i++) {
                dimText.append(layers[i] + "/");
            }
            dimText.append(".*");
            url_parse_regex = url_host + "/" + dimText;
        } else {
            url_parse_regex = url_host + "/.*";
        }

        //格式化URL
        url_parse_regex = formatUrl(url_parse_regex);

        String writeLine = url_host + "," + is_host_fuzzy + "," + url_parse_regex + "," + comments;
        writeLine(outFile, writeLine);
    }


    /**
     * 格式化URL
     *
     * @return
     */
    private static String formatUrl(final String url) {
        String path = url;
        while (path.indexOf("\\") > -1) {
            path = path.replaceAll("\\\\", "/");
        }
        while (path.indexOf("//") > -1) {
            path = path.replaceAll("//", "/");
        }
        return path;
    }


    @SneakyThrows
    private synchronized void writeLine(File outFile, String lineText) {
        final String text = lineText + this.dataConf.getExportNewLineChar();
        @Cleanup RandomAccessFile randomAccessFile = new RandomAccessFile(outFile, "rw");
        randomAccessFile.seek(randomAccessFile.length());
        randomAccessFile.write(text.getBytes("GBK"));
    }

}

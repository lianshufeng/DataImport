package com.github.data.imp.core.domain;

import com.github.data.imp.other.mongo.domain.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataTable extends SuperEntity {

    //手机号码
    @Indexed(unique = true)
    private String phone;

    //crc32
    @Indexed(unique = true)
    private String phoneHash;


    //手机号码归属地
    @Indexed
    private String province;

    //手机运营商
    @Indexed
    private String catName;


    //设备号
    @Indexed(unique = true)
    private String imei;


    //设备号的MD5
    @Indexed(unique = true)
    private String imeiHash;


}

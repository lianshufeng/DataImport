package com.github.data.core.domain;

import com.github.data.other.mongo.domain.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhoneLib extends SuperEntity {


    //手机号码,前7位
    @Indexed
    private String phone;


    //区域号
    @Indexed
    private String area;


    //省份
    @Indexed
    private String province;

    //城市
    @Indexed
    private String city;


    //手机运营商
    @Indexed
    private String catName;

}

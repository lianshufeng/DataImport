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

    @Indexed(unique = true)
    private String phone;

    @Indexed(unique = true)
    private String imei;





}

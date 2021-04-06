package com.github.data.core.dao.impl;

import com.github.data.core.dao.extend.DataTableDaoExtend;
import com.github.data.core.domain.DataTable;
import com.github.data.other.mongo.helper.DBHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;


public class DataTableDaoImpl implements DataTableDaoExtend {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private DBHelper dbHelper;

    @Override
    public String replaceFromImei(DataTable dataTable) {
        //删除存在的数据
        this.mongoTemplate.remove(Query.query(Criteria.where("imei").is(dataTable.getImei())), DataTable.class);

        dbHelper.saveTime(dataTable);
        this.mongoTemplate.save(dataTable);
        return dataTable.getId();
    }
}

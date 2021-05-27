package com.github.data.core.dao.impl;

import com.github.data.core.dao.extend.DataTableDaoExtend;
import com.github.data.core.domain.DataTable;
import com.github.data.other.mongo.helper.DBHelper;
import com.github.data.other.mongo.util.EntityObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;


@Slf4j
public class DataTableDaoImpl implements DataTableDaoExtend {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private DBHelper dbHelper;

    @Override
    public void replaceFromImei(DataTable dataTable) {
        Query query = Query.query(Criteria.where("imei").is(dataTable.getImei()));
        Update update = new Update();
        EntityObjectUtil.entity2Update(dataTable, update);
        this.dbHelper.saveTime(update);
        try {
            this.mongoTemplate.upsert(query, update, DataTable.class);
        } catch (DuplicateKeyException e) {
            log.error("重复数据: {}", dataTable);
        }
    }
}

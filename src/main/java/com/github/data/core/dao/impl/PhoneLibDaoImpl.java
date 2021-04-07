package com.github.data.core.dao.impl;

import com.github.data.core.dao.extend.PhoneLibDaoExtend;
import com.github.data.core.domain.PhoneLib;
import com.github.data.other.mongo.util.EntityObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;


public class PhoneLibDaoImpl implements PhoneLibDaoExtend {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public void updateData(PhoneLib phoneLib) {
        Query query = Query.query(Criteria.where("phone").is(phoneLib.getPhone()));
        Update update = new Update();
        EntityObjectUtil.entity2Update(phoneLib, update);
        this.mongoTemplate.upsert(query, update, PhoneLib.class);
    }
}

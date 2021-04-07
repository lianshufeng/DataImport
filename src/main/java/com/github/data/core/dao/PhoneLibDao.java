package com.github.data.core.dao;

import com.github.data.core.dao.extend.PhoneLibDaoExtend;
import com.github.data.core.domain.PhoneLib;
import com.github.data.other.mongo.dao.MongoDao;

public interface PhoneLibDao extends MongoDao<PhoneLib>, PhoneLibDaoExtend {
}

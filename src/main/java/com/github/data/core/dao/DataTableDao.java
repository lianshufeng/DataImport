package com.github.data.core.dao;

import com.github.data.core.domain.DataTable;
import com.github.data.core.dao.extend.DataTableDaoExtend;
import com.github.data.other.mongo.dao.MongoDao;

public interface DataTableDao extends MongoDao<DataTable>, DataTableDaoExtend {

    /**
     * 通过phoneHash取出数据
     *
     * @param phoneHash
     * @return
     */
    DataTable findByPhoneHash(String phoneHash);

}

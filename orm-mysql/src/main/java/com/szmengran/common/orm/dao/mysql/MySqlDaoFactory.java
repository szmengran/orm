package com.szmengran.common.orm.dao.mysql;

import com.szmengran.common.orm.dao.AbstractDao;
import com.szmengran.common.orm.dao.DaoFactory;

public class MySqlDaoFactory implements DaoFactory{

	@Override
	public AbstractDao getDao() {
		return MySqlDao.getInstance();
	}

}

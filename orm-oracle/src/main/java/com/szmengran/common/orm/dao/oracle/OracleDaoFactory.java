package com.szmengran.common.orm.dao.oracle;

import com.szmengran.common.orm.dao.AbstractDao;
import com.szmengran.common.orm.dao.DaoFactory;

public class OracleDaoFactory implements DaoFactory{

	@Override
	public AbstractDao getDao() {
		return OracleDao.getInstance();
	}

}

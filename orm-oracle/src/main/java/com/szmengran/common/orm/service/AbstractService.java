package com.szmengran.common.orm.service;

import java.io.IOException;

import com.szmengran.common.orm.dao.AbstractDao;
import com.szmengran.common.orm.dao.oracle.OracleDao;
import com.szmengran.common.service.BaseService;

public abstract class AbstractService extends BaseService{
	
	@Override
	public AbstractDao getDao() throws IOException {
		return OracleDao.getInstance();
	}
}
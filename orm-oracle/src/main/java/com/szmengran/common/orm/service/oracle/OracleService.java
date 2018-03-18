/**
 * Project Name:orm-oracle 
 * File Name:OracleService.java 
 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
 * Copyright (c) 2018, 深圳市梦燃科技有限公司 All Rights Reserved. 
 * @createTime 2018年3月18日下午4:06:48
 */
package com.szmengran.common.orm.service.oracle;

import java.io.IOException;

import com.szmengran.common.orm.dao.AbstractDao;
import com.szmengran.common.orm.dao.oracle.OracleDaoFactory;
import com.szmengran.common.service.AbstractService;

public class OracleService extends AbstractService{
	@Override
	public AbstractDao getDao() throws IOException {
		return new OracleDaoFactory().getDao();
	}

}


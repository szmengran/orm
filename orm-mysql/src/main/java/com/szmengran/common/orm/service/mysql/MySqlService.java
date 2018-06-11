/**
 * Project Name:orm-mysql 
 * File Name:MySqlService.java 
 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
 * Copyright (c) 2018, 深圳市梦燃科技有限公司 All Rights Reserved. 
 * @createTime 2018年3月18日下午3:57:48
 */
package com.szmengran.common.orm.service.mysql;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.szmengran.common.orm.dao.AbstractDao;
import com.szmengran.common.orm.dao.mysql.MySqlDao;
import com.szmengran.common.service.BaseService;

@Service("mysqlService")
public class MySqlService extends BaseService{
	@Override
	public AbstractDao getDao() throws IOException {
		return MySqlDao.getInstance();
	}
}


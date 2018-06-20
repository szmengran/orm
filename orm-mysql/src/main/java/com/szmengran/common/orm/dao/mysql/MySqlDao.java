package com.szmengran.common.orm.dao.mysql;

import org.springframework.stereotype.Service;

import com.szmengran.common.orm.dao.AbstractDao;

/**
 * MySql 数据库操作类
 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
 * @creaetTime 2014-7-30 下午8:54:11
 */
@Service("mySqlDao")
public class MySqlDao extends AbstractDao{
	
	@Override
	public String getPageSql(String strSql, int startRow, int pageSize) {
		StringBuffer sb = new StringBuffer();
		sb.append(strSql+" limit "+startRow+" , "+pageSize);
		return sb.toString();
	}
}

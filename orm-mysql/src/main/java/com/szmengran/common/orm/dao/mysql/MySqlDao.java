package com.szmengran.common.orm.dao.mysql;

import com.szmengran.common.orm.DBManager;
import com.szmengran.common.orm.dao.AbstractDao;

/**
 * MySql 数据库操作类
 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
 * @creaetTime 2014-7-30 下午8:54:11
 */
public class MySqlDao extends AbstractDao{
	public DBManager getDBManager(){
		return new DBManager();
	}
	private MySqlDao(){
		
	}
	private static MySqlDao instance = new MySqlDao();
	public static MySqlDao getInstance(){
		return instance;
	}
	@Override
	public String getPageSql(String strSql, int startRow, int pageSize) {
		StringBuffer sb = new StringBuffer();
		sb.append(strSql+" limit "+startRow+" , "+pageSize);
		return sb.toString();
	}
}

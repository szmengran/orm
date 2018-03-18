package com.szmengran.common.orm.dao.oracle;

import com.szmengran.common.orm.DBManager;
import com.szmengran.common.orm.dao.AbstractDao;

/**
 * MySql 数据库操作类
 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
 * @creaetTime 2014-7-30 下午8:54:11
 */
public class OracleDao extends AbstractDao{
	public DBManager getDBManager(){
		return new DBManager();
	}
	private OracleDao(){
		
	}
	private static OracleDao instance = new OracleDao();
	public static OracleDao getInstance(){
		return instance;
	}
	@Override
	public String getPageSql(String strSql, int startRow, int pageSize) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT * FROM (")
		.append(" SELECT A.*, ROWNUM RN FROM (") 
		.append(strSql)
		.append(" ) A WHERE ROWNUM <= ")
		.append(startRow+pageSize)
		.append(" ) WHERE RN >= ")
		.append(startRow+1);
		return sb.toString();
	}
}

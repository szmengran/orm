package com.szmengran.common.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.druid.pool.DruidDataSource;
import com.szmengran.common.Constants;
import com.szmengran.common.PageInfo;
import com.szmengran.common.orm.DBManager;
import com.szmengran.common.orm.dao.AbstractDao;
import com.szmengran.common.orm.dao.mysql.MySqlDao;
import com.szmengran.common.orm.dao.oracle.OracleDao;

public abstract class AbstractService {
	@Value("${spring.datasource.databasetype}")
    private String databasetype;
	
	@Resource
    private DruidDataSource writeDataSource;
	
	@Resource
	private DruidDataSource readDataSource;
	
	public AbstractDao getDao() throws IOException {
		if (Constants.DATABASE_TYPE_ORACLE.equalsIgnoreCase(databasetype)) {
			return OracleDao.getInstance();
		} else {
			return MySqlDao.getInstance();
		}
	}
	
	public DBManager getDBManager(String datasourceType) throws IOException {
		if (Constants.DATASOURCE_READ.equalsIgnoreCase(datasourceType)) {
			return new DBManager(readDataSource);
		} else {
			return new DBManager(writeDataSource);
		}
	}

	/**
	 * 保存一条记录
	 * @param object
	 * @throws IOException
	 * @throws SQLException
	 * @throws Exception 
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * Copyright (c) 2018, 深圳市梦燃科技有限公司 All Rights Reserved. 
	 * @createTime 2018年3月18日下午10:19:08
	 */
	public void save(Object object) throws IOException, SQLException, Exception{
		save(object, null, null);
	}

	/**
	 * 保存一条记录
	 * @param dbManager
	 * @param object
	 * @throws IOException
	 * @throws SQLException
	 * @throws Exception 
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * Copyright (c) 2018, 深圳市梦燃科技有限公司 All Rights Reserved. 
	 * @createTime 2018年3月18日下午10:19:19
	 */
	public void save(DBManager dbManager, Object object) throws IOException, SQLException, Exception{
		save(dbManager, object, null, null);
	}
	
	/**
	 * 根据主键类型生成主键并保存数据
	 * @param object
	 * @param primaryKeyType
	 * @throws IOException
	 * @throws SQLException
	 * @throws Exception      
	 * @return: void      
	 * @throws   
	 * @author <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public void save(Object object, Integer primaryKeyType) throws IOException, SQLException, Exception{
		save(object, primaryKeyType, null);
	}
	/**
	 * 保存一条记录
	 * @param object
	 * @param primaryKeyType
	 * @param seq_name
	 * @throws IOException
	 * @throws SQLException
	 * @throws Exception 
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * Copyright (c) 2018, 深圳市梦燃科技有限公司 All Rights Reserved. 
	 * @createTime 2018年3月18日下午10:19:13
	 */
	public void save(Object object, Integer primaryKeyType, String seq_name) throws IOException, SQLException, Exception{
		DBManager dbManager = getDBManager(Constants.DATASOURCE_WRITE);
		try {
			dbManager.openConnection();
			dbManager.beginTransaction();
			save(dbManager, object, primaryKeyType, seq_name);
			dbManager.commitTransaction();
		} catch (SQLException e) {
			dbManager.rollbackTransaction();
			throw e;
		} catch (Exception e) {
			dbManager.rollbackTransaction();
			throw e;
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 保存一条记录
	 * @param dbManager
	 * @param object
	 * @param primaryKeyType
	 * @param seq_name
	 * @throws Exception 
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * Copyright (c) 2018, 深圳市梦燃科技有限公司 All Rights Reserved. 
	 * @createTime 2018年3月18日下午10:19:26
	 */
	public void save(DBManager dbManager, Object object, Integer primaryKeyType, String seq_name) throws Exception{
		AbstractDao abstractDao = getDao();
		abstractDao.insert(dbManager, object, primaryKeyType, seq_name);
	}

	/**
	 * 批量保存记录
	 * 
	 * @param list
	 * @throws IOException 
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:40:09 AM
	 */
	public void addBatch(List<?> list)throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException, Exception  {
		DBManager dbManager = getDBManager(Constants.DATASOURCE_WRITE);
		try {
			dbManager.openConnection();
			dbManager.beginTransaction();
			addBatch(dbManager, list);
			dbManager.commitBatch();
		} catch (SQLException e) {
			dbManager.rollbackTransaction();
			throw e;
		} catch (Exception e) {
			dbManager.rollbackTransaction();
			throw e;
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 批量保存记录
	 * @param dbManager
	 * @param list
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws SQLException 
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * Copyright (c) 2018, 深圳市梦燃科技有限公司 All Rights Reserved. 
	 * @createTime 2018年3月18日下午10:13:59
	 */
	public void addBatch(DBManager dbManager, List<?> list) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException {
		addBatch(dbManager, list, null, null);
	}

	/**
	 * 批量保存记录
	 * @param dbManager
	 * @param list
	 * @param primaryKeyType
	 * @param seq_name
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws SQLException 
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * Copyright (c) 2018, 深圳市梦燃科技有限公司 All Rights Reserved. 
	 * @createTime 2018年3月18日下午10:13:52
	 */
	public void addBatch(DBManager dbManager, List<?> list, Integer primaryKeyType, String seq_name) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException {
		AbstractDao abstractDao = getDao();
		abstractDao.addBatch(dbManager, list, primaryKeyType, seq_name);
	}

	/**
	 * 删除一条记录
	 * 
	 * @param object
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:41:15 AM
	 */
	public void delete(Object object) throws IOException, SQLException, Exception{
		DBManager dbManager = getDBManager(Constants.DATASOURCE_WRITE);
		try {
			dbManager.openConnection();
			dbManager.beginTransaction();
			delete(dbManager, object);
			dbManager.commitTransaction();
		} catch (SQLException e) {
			dbManager.rollbackTransaction();
			throw e;
		} catch (Exception e) {
			dbManager.rollbackTransaction();
			throw e;
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 删除一条记录
	 * 
	 * @param dbManager
	 * @param object
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:41:51 AM
	 */
	public void delete(DBManager dbManager, Object object) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException {
		AbstractDao abstractDao = getDao();
		abstractDao.delete(dbManager, object);
	}

	/**
	 * 根据条件查找记录
	 * @param object
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws Exception 
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * Copyright (c) 2018, 深圳市梦燃科技有限公司 All Rights Reserved. 
	 * @createTime 2018年3月18日下午4:46:24
	 */
	public <T>List<T> findByConditions(T object, Map<String, Object> params) throws SQLException, Exception {
		return findByConditions(object, params, null, null);
	}

	/**
	 * 根据条件分页查找记录
	 * @param object
	 * @param params
	 * @param startRow
	 * @param pageSize
	 * @return
	 * @throws SQLException
	 * @throws Exception 
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * Copyright (c) 2018, 深圳市梦燃科技有限公司 All Rights Reserved. 
	 * @createTime 2018年3月18日下午4:46:03
	 */
	public <T>List<T> findByConditions(T object, Map<String, Object> params, Integer startRow, Integer pageSize) throws SQLException, Exception
			{
		DBManager dbManager = getDBManager(Constants.DATASOURCE_READ);
		try {
			dbManager.openConnection();
			return findByConditions(dbManager, object, params, null, null);
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 根据条件分页查找记录
	 * @param dbManager
	 * @param object
	 * @param params
	 * @param startRow
	 * @param pageSize
	 * @return
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws SQLException      
	 * @return: List<T>      
	 * @throws   
	 * @author <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public <T>List<T> findByConditions(DBManager dbManager, T object, Map<String, Object> params,
			Integer startRow, Integer pageSize) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, SQLException {
		AbstractDao abstractDao = getDao();
		return abstractDao.findByConditions(dbManager, object, params, startRow, pageSize);
	}

	/**
	 * 根据条件查找记录
	 * @param object
	 * @param conditions
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws Exception 
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * Copyright (c) 2018, 深圳市梦燃科技有限公司 All Rights Reserved. 
	 * @createTime 2018年3月18日下午4:45:04
	 */
	public <T>List<T> findByConditions(T object, StringBuffer conditions, Object[] params) throws SQLException, Exception {
		return findByConditions(object, conditions, params, null, null).getList();
	}

	/**
	 * 根据条件分页查找记录
	 * @param object
	 * @param conditions
	 * @param params
	 * @param strPage
	 * @param strPageSize
	 * @return
	 * @throws SQLException
	 * @throws Exception 
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * Copyright (c) 2018, 深圳市梦燃科技有限公司 All Rights Reserved. 
	 * @createTime 2018年3月18日下午4:44:43
	 */
	public <T>PageInfo<T> findByConditions(T object, StringBuffer conditions, Object[] params, String strPage,
			String strPageSize) throws SQLException, Exception {
		DBManager dbManager = getDBManager(Constants.DATASOURCE_READ);
		try {
			dbManager.openConnection();
			StringBuffer strSql = new StringBuffer();
			strSql.append("SELECT * FROM ").append(object.getClass().getSimpleName().toUpperCase())
					.append(" WHERE 1=1 ");
			if (conditions != null) {
				strSql.append(conditions);
			}
			return findBySql(dbManager, object, strSql.toString(), params, strPage, strPageSize);
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 根据条件分页查找记录
	 * @param object
	 * @param conditions
	 * @param orderby
	 * @param params
	 * @param strPage
	 * @param strPageSize
	 * @return
	 * @throws SQLException
	 * @throws Exception 
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * Copyright (c) 2018, 深圳市梦燃科技有限公司 All Rights Reserved. 
	 * @createTime 2018年3月18日下午4:43:55
	 */
	public <T>PageInfo<T> findByConditions(T object, StringBuffer conditions, String orderby, Object[] params,
			String strPage, String strPageSize) throws SQLException, Exception {
		DBManager dbManager = getDBManager(Constants.DATASOURCE_READ);
		try {
			dbManager.openConnection();
			StringBuffer strSql = new StringBuffer();
			strSql.append("SELECT * FROM ").append(object.getClass().getSimpleName().toUpperCase())
					.append(" WHERE 1=1 ");
			if (conditions != null) {
				strSql.append(conditions);
			}
			return findBySql(dbManager, object, strSql.toString(), orderby, params, strPage, strPageSize);
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 根据SQL查找记录
	 * @param object
	 * @param strSql
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws Exception 
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * Copyright (c) 2018, 深圳市梦燃科技有限公司 All Rights Reserved. 
	 * @createTime 2018年3月18日下午4:43:21
	 */
	public <T>List<T> findBySql(T object, String strSql, Object[] params) throws SQLException, Exception {
		return findBySql(object, strSql, params, null, null).getList();
	}

	/**
	 * 根据SQL分页查找记录
	 * 
	 * @param object
	 * @param strSql
	 * @param params
	 * @param startRow
	 * @param pageSize
	 * @return
	 * @throws SQLException 
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:49:19 AM
	 */
	public <T>PageInfo<T> findBySql(T object, String strSql, Object[] params, String strPage, String strPageSize) throws SQLException, Exception
			{
		DBManager dbManager = getDBManager(Constants.DATASOURCE_READ);
		try {
			dbManager.openConnection();
			return findBySql(dbManager, object, strSql, params, strPage, strPageSize);
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 根据SQL分页查找记录
	 * @param dbManager
	 * @param object
	 * @param strSql
	 * @param params
	 * @param strPage
	 * @param strPageSize
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws InstantiationException 
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * Copyright (c) 2018, 深圳市梦燃科技有限公司 All Rights Reserved. 
	 * @createTime 2018年3月18日下午4:42:23
	 */
	public <T>PageInfo<T> findBySql(DBManager dbManager, T object, String strSql, Object[] params, String strPage,
			String strPageSize) throws IOException, SQLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		AbstractDao abstractDao = getDao();
		PageInfo<T> pageInfo = new PageInfo<T>();
		Integer startRow = null;
		Integer pageSize = null;
		if (strPageSize != null) {
			pageSize = Integer.parseInt(strPageSize);
			Integer page = Integer.parseInt(strPage);
			Integer total = count(dbManager, "SELECT COUNT(*) FROM (" + strSql + ") a", params);
			pageInfo.setPage(page);
			pageInfo.setPageSize(pageSize);
			pageInfo.setTotal(total);
			startRow = (page - 1 <= 0 ? 0 : page - 1) * pageSize;
		}
		List<T> list = abstractDao.findByConditions(dbManager, object, strSql, params, startRow, pageSize);
		pageInfo.setList(list);
		return pageInfo;
	}

	/**
	 * 根据SQL分页查找记录
	 * @param dbManager
	 * @param object
	 * @param strSql
	 * @param orderby
	 * @param params
	 * @param strPage
	 * @param strPageSize
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws InstantiationException 
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * Copyright (c) 2018, 深圳市梦燃科技有限公司 All Rights Reserved. 
	 * @createTime 2018年3月18日下午4:41:38
	 */
	public <T>PageInfo<T> findBySql(DBManager dbManager, T object, String strSql, String orderby, Object[] params,
			String strPage, String strPageSize) throws IOException, SQLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		AbstractDao abstractDao = getDao();
		PageInfo<T> pageInfo = new PageInfo<T>();
		Integer startRow = null;
		Integer pageSize = null;
		if (strPageSize != null) {
			pageSize = Integer.parseInt(strPageSize);
			Integer page = Integer.parseInt(strPage);
			Integer total = count(dbManager, "SELECT COUNT(*) FROM (" + strSql + ") a", params);
			pageInfo.setPage(page);
			pageInfo.setPageSize(pageSize);
			pageInfo.setTotal(total);
			startRow = (page - 1 <= 0 ? 0 : page - 1) * pageSize;
		}
		List<T> list = abstractDao.findByConditions(dbManager, object, strSql + orderby, params, startRow,
				pageSize);
		pageInfo.setList(list);
		return pageInfo;
	}

	/**
	 * 根据SQL查找记录数
	 * 
	 * @param strSql
	 * @param params
	 * @return
	 * @throws SQLException 
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:52:36 AM
	 */
	public int count(String strSql, Object[] params) throws SQLException, Exception{
		DBManager dbManager = getDBManager(Constants.DATASOURCE_READ);
		try {
			dbManager.openConnection();
			return count(dbManager, strSql, params);
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 根据SQL查找记录数
	 * 
	 * @param dbManager
	 * @param strSql
	 * @param params
	 * @return
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:53:11 AM
	 */
	public int count(DBManager dbManager, String strSql, Object[] params) throws IOException, SQLException{
		AbstractDao abstractDao = getDao();
		return abstractDao.count(dbManager, strSql, params);
	}

	public int count(DBManager dbManager, Object object, StringBuffer conditions, Object[] params) throws IOException, SQLException{
		AbstractDao abstractDao = getDao();
		return abstractDao.count(dbManager, object, conditions, params);
	}

	/**
	 * 更新一条记录
	 * 
	 * @param object
	 * @throws IOException 
	 * @throws SQLException, Exception 
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:53:36 AM
	 */
	public void update(Object object) throws IOException, SQLException, Exception{
		DBManager dbManager = getDBManager(Constants.DATASOURCE_WRITE);
		try {
			dbManager.openConnection();
			dbManager.beginTransaction();
			update(dbManager, object);
			dbManager.commitTransaction();
		} catch (Exception e) {
			dbManager.rollbackTransaction();
			throw e;
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 更新一条记录
	 * 
	 * @param dbManager
	 * @param object
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:53:47 AM
	 */
	public void update(DBManager dbManager, Object object) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException{
		AbstractDao abstractDao = getDao();
		abstractDao.update(dbManager, object);
	}

	/**
	 * 执行SQL
	 * 
	 * @param strSql
	 * @throws SQLException 
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:54:52 AM
	 */
	public void executeSql(String strSql) throws SQLException, Exception{
		DBManager dbManager = getDBManager(Constants.DATASOURCE_WRITE);
		try {
			dbManager.openConnection();
			dbManager.beginTransaction();
			executeSql(dbManager, strSql);
			dbManager.commitTransaction();
		} catch (SQLException e) {
			dbManager.rollbackTransaction();
			throw e;
		} catch (Exception e) {
			dbManager.rollbackTransaction();
			throw e;
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 执行SQL
	 * 
	 * @param dbManager
	 * @param strSql
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:55:07 AM
	 */
	public void executeSql(DBManager dbManager, String strSql) throws IOException, SQLException {
		executeSql(dbManager, strSql, null);
	}

	/**
	 * 执行SQL并传人参数
	 * 
	 * @param strSql
	 * @param params
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:55:14 AM
	 */
	public void executeSql(String strSql, Object[] params) throws SQLException,Exception {
		DBManager dbManager = getDBManager(Constants.DATASOURCE_WRITE);
		try {
			dbManager.openConnection();
			dbManager.beginTransaction();
			executeSql(dbManager, strSql, params);
			dbManager.commitTransaction();
		} catch (SQLException e) {
			dbManager.rollbackTransaction();
			throw e;
		} catch (Exception e) {
			dbManager.rollbackTransaction();
			throw e;
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 执行SQL并传人参数
	 * 
	 * @param dbManager
	 * @param strSql
	 * @param params
	 * @throws IOException
	 * @throws SQLException
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:55:38 AM
	 */
	public void executeSql(DBManager dbManager, String strSql, Object[] params) throws IOException, SQLException {
		AbstractDao abstractDao = getDao();
		abstractDao.executeSql(dbManager, strSql, params);
	}

	/**
	 * 根据主键查找某条记录
	 * 
	 * @param object
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:56:11 AM
	 */
	public <T> T findByPrimaryKey(T object) throws SQLException, Exception {
		DBManager dbManager = getDBManager(Constants.DATASOURCE_READ);
		try {
			dbManager.openConnection();
			return findByPrimaryKey(dbManager, object);
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 根据主键查找某条记录
	 * 
	 * @param dbManager
	 * @param object
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:57:00 AM
	 */
	public <T> T findByPrimaryKey(DBManager dbManager, T object)
			throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, SQLException {
		AbstractDao abstractDao = getDao();
		return abstractDao.findByPrimaryKeys(dbManager, object);
	}

	/**
	 * 生成一个主键
	 * 
	 * @return
	 * @throws IOException
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:57:31 AM
	 */
	public String generatePrimaryKey() throws IOException {
		AbstractDao abstractDao = getDao();
		return abstractDao.generatePrimaryKey();
	}

	/**
	 * 根据当前时间生成一个主键
	 * 
	 * @return
	 */
	protected synchronized String generatePrimaryKeyByTime() {
		String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
		return date + (System.nanoTime() + "").substring(6);
	}

	/**
	 * 根据条件获取一个二进制数据
	 * 
	 * @param strSql
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:58:08 AM
	 */
	public byte[] findByteFromBlob(String strSql, Object[] params) throws SQLException, Exception {
		DBManager dbManager = getDBManager(Constants.DATASOURCE_READ);
		try {
			dbManager.openConnection();
			return findByteFromBlob(dbManager, strSql, params);
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 根据条件获取一个二进制数据
	 * 
	 * @param dbManager
	 * @param strSql
	 * @param params
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 8:58:39 AM
	 */
	public byte[] findByteFromBlob(DBManager dbManager, String strSql, Object[] params)
			throws IOException, SQLException {
		AbstractDao abstractDao = getDao();
		return abstractDao.findByteFromBlob(dbManager, strSql, params);
	}
}
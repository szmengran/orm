package com.szmengran.common.orm.dao;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import com.alibaba.druid.pool.DruidDataSource;
import com.szmengran.common.PageInfo;
import com.szmengran.common.orm.DBManager;
import com.szmengran.common.orm.DbPrimaryKeyType;
import com.szmengran.common.reflect.ReflectHandler;

/**
 * 公共的数据库操作类型 Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
 * DateTime： Jan 19, 2017 4:58:51 PM
 *
 */
public abstract class AbstractDao {

	@Resource
	private DruidDataSource writeDataSource;

	@Resource
	private DruidDataSource readDataSource;

	public DruidDataSource getWriteDataSource() {
		return writeDataSource;
	}

	public DruidDataSource getReadDataSource() {
		return readDataSource;
	}

	/**
	 * 保存一条记录
	 * 
	 * @param object
	 * @throws IOException
	 * @throws SQLException
	 * @throws Exception
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a> Copyright (c) 2018,
	 *         深圳市梦燃科技有限公司 All Rights Reserved.
	 * @createTime 2018年3月18日下午10:19:08
	 */
	public void insert(Object object) throws IOException, SQLException, Exception {
		insert(object, null, null);
	}

	/**
	 * 保存一条记录
	 * 
	 * @param dbManager
	 * @param object
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 * @throws Exception
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a> Copyright (c) 2018,
	 *         深圳市梦燃科技有限公司 All Rights Reserved.
	 * @createTime 2018年3月18日下午10:19:19
	 */
	public void insert(DBManager dbManager, Object object) throws IOException, SQLException, Exception {
		insert(dbManager, object, null, null);
	}

	/**
	 * 根据主键类型生成主键并保存数据
	 * 
	 * @param object
	 * @param primaryKeyType
	 * @throws IOException
	 * @throws SQLException
	 * @throws Exception
	 * @throws @author
	 *             <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public void insert(Object object, DbPrimaryKeyType primaryKeyType) throws IOException, SQLException, Exception {
		insert(object, primaryKeyType, null);
	}

	/**
	 * 保存一条记录
	 * 
	 * @param object
	 * @param primaryKeyType
	 * @param seq_name
	 * @throws IOException
	 * @throws SQLException
	 * @throws Exception
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a> Copyright (c) 2018,
	 *         深圳市梦燃科技有限公司 All Rights Reserved.
	 * @createTime 2018年3月18日下午10:19:13
	 */
	public void insert(Object object, DbPrimaryKeyType primaryKeyType, String seq_name)
			throws IOException, SQLException, Exception {
		DBManager dbManager = new DBManager(writeDataSource);
		try {
			dbManager.openConnection();
			dbManager.beginTransaction();
			insert(dbManager, object, primaryKeyType, seq_name);
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
	 * 将数据插入到数据表中，返回插入的成功条数
	 * 
	 * @param dbManager
	 * @param object
	 * @param primaryKeyType
	 * @param seq_name
	 * @return
	 * @throws Exception
	 * @return
	 * @throws @author
	 *             <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public void insert(DBManager dbManager, Object object, DbPrimaryKeyType primaryKeyType, String seq_name) throws Exception {
		Map<String, Method> map = ReflectHandler.getFieldAndGetMethodFromObject(object);
		Set<String> set = map.keySet();
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO " + object.getClass().getSimpleName().toUpperCase() + " (");
		StringBuffer sbFileds = new StringBuffer();
		StringBuffer sbValues = new StringBuffer();
		List<String> primaryKeys = dbManager.getPrimaryKeys(object);
		if (primaryKeys != null && primaryKeys.size() > 1 && primaryKeyType != null) {
			throw new Exception("表【" + object.getClass().getSimpleName().toUpperCase() + "】为组合主键，不能自动生成！");
		}
		for (String field : set) {
			if (DbPrimaryKeyType.AUTO_INCREMENT == primaryKeyType && isPrimaryKey(primaryKeys, field)) { // 主键采用数据库自增长方法
				continue;
			} else if (DbPrimaryKeyType.SEQ == primaryKeyType && isPrimaryKey(primaryKeys, field)) { // 主键采用数据库自增长方法
				sbFileds.append("," + field);
				sbValues.append("," + seq_name + ".NEXTVAL");
				continue;
			}
			sbFileds.append("," + field);
			sbValues.append(",?");
		}
		sb.append(sbFileds.substring(1) + ") VALUES (" + sbValues.substring(1) + ")");
		dbManager.prepareStatement(sb.toString());
		int index = 1;
		for (String field : set) {
			if ((DbPrimaryKeyType.SEQ == primaryKeyType || DbPrimaryKeyType.AUTO_INCREMENT == primaryKeyType)
					&& isPrimaryKey(primaryKeys, field)) {
				continue;
			} else if (DbPrimaryKeyType.UUID == primaryKeyType && isPrimaryKey(primaryKeys, field)) {
				String value = generatePrimaryKey(); // 生产一个UUID作为主键
				dbManager.setPrepareParameters(index++, value);
			} else {
				Method method = map.get(field);
				Object value = method.invoke(object);
				dbManager.setPrepareParameters(index++, value);
			}
		}
		dbManager.executePrepare();
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
	public void addBatch(List<?> list) throws IOException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException, Exception {
		addBatch(list, null, null);
	}

	/**
	 * 批量保存记录
	 * 
	 * @param dbManager
	 * @param list
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws SQLException
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a> Copyright (c) 2018,
	 *         深圳市梦燃科技有限公司 All Rights Reserved.
	 * @createTime 2018年3月18日下午10:13:59
	 */
	public void addBatch(DBManager dbManager, List<?> list)
			throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, SQLException {
		addBatch(dbManager, list, null, null);
	}

	/**
	 * 批量保存记录
	 * 
	 * @param list
	 * @param primaryKeyType
	 * @param seq_name
	 * @throws Exception
	 * @return: void
	 * @throws @author
	 *             <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public void addBatch(List<?> list, DbPrimaryKeyType primaryKeyType, String seq_name) throws Exception {
		DBManager dbManager = new DBManager(writeDataSource);
		try {
			dbManager.openConnection();
			dbManager.beginTransaction();
			addBatch(dbManager, list, primaryKeyType, seq_name);
			dbManager.commitBatch();
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
	 * 将数据批量插入到数据表中,主键采用数据库自动生成
	 * 
	 * @param dbManager
	 * @param list
	 * @param primaryKeyType
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws SQLException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Jan 19, 2017 4:58:19 PM
	 */
	public void addBatch(DBManager dbManager, List<?> list, DbPrimaryKeyType primaryKeyType, String seq_name)
			throws NoSuchMethodException, SecurityException, SQLException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Object object = null;
		if (list.size() > 0) {
			object = list.get(0);
			Map<String, Method> map = ReflectHandler.getFieldAndGetMethodFromObject(object);
			Set<String> set = map.keySet();
			StringBuffer sb = new StringBuffer();
			sb.append("INSERT INTO " + object.getClass().getSimpleName().toUpperCase() + " (");
			StringBuffer sbFileds = new StringBuffer();
			StringBuffer sbValues = new StringBuffer();
			List<String> primaryKeys = dbManager.getPrimaryKeys(object);
			for (String field : set) {
				if (DbPrimaryKeyType.AUTO_INCREMENT == primaryKeyType && isPrimaryKey(primaryKeys, field)) { // 主键采用数据库自增长方法
					continue;
				} else if (DbPrimaryKeyType.SEQ == primaryKeyType && isPrimaryKey(primaryKeys, field)) { // 主键采用数据库自增长方法
					sbFileds.append("," + field);
					sbValues.append("," + seq_name + ".NEXTVAL");
					continue;
				}
				sbFileds.append("," + field);
				sbValues.append(",?");
			}
			sb.append(sbFileds.substring(1) + ") VALUES (" + sbValues.substring(1) + ")");
			dbManager.prepareStatement(sb.toString());
			for (int k = 0; k < list.size(); k++) {
				object = list.get(k);
				int index = 1;
				for (String field : set) {
					if ((DbPrimaryKeyType.AUTO_INCREMENT == primaryKeyType || DbPrimaryKeyType.SEQ == primaryKeyType)
							&& isPrimaryKey(primaryKeys, field)) {
						continue;
					} else if (DbPrimaryKeyType.UUID == primaryKeyType && isPrimaryKey(primaryKeys, field)) {
						String value = generatePrimaryKey(); // 生产一个UUID作为主键
						dbManager.setPrepareParameters(index++, value);
					} else {
						Method method = map.get(field);
						Object value = method.invoke(object);
						dbManager.setPrepareParameters(index++, value);
					}
				}
				dbManager.addBatch();
			}
		}
	}

	/**
	 * 批量插入事物提交
	 * 
	 * @param dbManager
	 * @return
	 * @throws SQLException
	 * @return
	 * @throws @author
	 *             <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public void commitBatch(DBManager dbManager) throws SQLException {
		dbManager.commitBatch();
	}

	/**
	 * 删除一条记录并返回删除的结果条数
	 * 
	 * @param object
	 * @throws IOException
	 * @throws SQLException
	 * @throws Exception
	 * @return: void
	 * @throws @author
	 *             <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public void delete(Object object) throws IOException, SQLException, Exception {
		DBManager dbManager = new DBManager(writeDataSource);
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
	 * 根据主键删除一条记录并返回删除的结果条数
	 * 
	 * @param dbManager
	 * @param object
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws SQLException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @return
	 * @throws @author
	 *             <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public void delete(DBManager dbManager, Object object) throws NoSuchMethodException, SecurityException,
			SQLException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Map<String, Method> map = ReflectHandler.getFieldAndGetMethodFromObject(object);
		StringBuffer sb = new StringBuffer();
		sb.append("DELETE FROM " + object.getClass().getSimpleName().toUpperCase() + " WHERE");
		List<String> fields = dbManager.getPrimaryKeys(object);
		for (int i = 0; i < fields.size(); i++) {
			String field = fields.get(i).toUpperCase();
			if (i == 0) {
				sb.append("  " + field + " = ?");
			} else {
				sb.append(" AND " + field + " = ?");
			}
		}
		dbManager.prepareStatement(sb.toString());
		int index = 1;
		for (int i = 0; i < fields.size(); i++) {
			Method method = map.get(fields.get(i).toUpperCase());
			Object value = method.invoke(object);
			dbManager.setPrepareParameters(index++, value);
		}
		dbManager.executePrepare();
	}

	/**
	 * 根据SQL查找记录数
	 * 
	 * @param strSql
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 * @author <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public int count(String strSql, Object[] params) throws SQLException, Exception {
		DBManager dbManager = new DBManager(readDataSource);
		try {
			dbManager.openConnection();
			return count(dbManager, strSql, params);
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 根据条件查询记录数
	 * 
	 * @param dbManager
	 * @param clazz
	 * @param params
	 * @return
	 * @throws SQLException
	 * @author <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public <T> int count(DBManager dbManager, Class<T> clazz, Map<String, Object> params) throws SQLException {
		StringBuffer conditions = new StringBuffer();
		Object[] values = null;
		if (params != null) {
			Set<String> set = params.keySet();
			int index = 0;
			values = new Object[params.size()];
			for (String key : set) {
				values[index++] = params.get(key);
				conditions.append(" AND ").append(key).append(" = ?");
			}
		}
		return count(dbManager, clazz, conditions, values);
	}

	/**
	 * 根据条件查询数据的记录数
	 * 
	 * @param dbManager
	 * @param clazz
	 * @param conditions
	 * @param params
	 * @return
	 * @throws SQLException
	 * @author <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public <T> int count(DBManager dbManager, Class<T> clazz, StringBuffer conditions, Object[] params)
			throws SQLException {
		StringBuffer strSql = new StringBuffer();
		strSql.append("SELECT COUNT(*) FROM ").append(clazz.getSimpleName().toUpperCase()).append(" WHERE")
				.append(" 1 = 1 ");
		if (conditions != null) {
			strSql.append(conditions);
		}
		return count(dbManager, strSql.toString(), params);
	}

	/**
	 * 根据SQL查询数据的记录数
	 * 
	 * @param dbManager
	 * @param object
	 * @param strSql
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 2:14:00 PM
	 */
	public int count(DBManager dbManager, String strSql, Object[] params) throws SQLException {
		int count = 0;
		dbManager.prepareStatement(strSql);
		if (params != null) {
			int index = 1;
			for (Object value : params) {
				dbManager.setPrepareParameters(index++, value);
			}
		}
		dbManager.executePrepareQuery();
		if (dbManager.next()) {
			count = dbManager.getInt(1);
		}
		return count;
	}

	/**
	 * 根据条件查找记录
	 * 
	 * @param object
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a> Copyright (c) 2018,
	 *         深圳市梦燃科技有限公司 All Rights Reserved.
	 * @createTime 2018年3月18日下午4:46:24
	 */
	public <T> List<T> findByConditions(Class<T> clazz, Map<String, Object> params) throws SQLException, Exception {
		return findByConditions(clazz, params, null, null);
	}

	/**
	 * 根据条件分页查找记录
	 * 
	 * @param object
	 * @param params
	 * @param startRow
	 * @param pageSize
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a> Copyright (c) 2018,
	 *         深圳市梦燃科技有限公司 All Rights Reserved.
	 * @createTime 2018年3月18日下午4:46:03
	 */
	public <T> List<T> findByConditions(Class<T> clazz, Map<String, Object> params, Integer startRow, Integer pageSize)
			throws SQLException, Exception {
		DBManager dbManager = new DBManager(readDataSource);
		try {
			dbManager.openConnection();
			return findByConditions(dbManager, clazz, params, null, null);
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 根据条件查找记录
	 * 
	 * @param object
	 * @param conditions
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a> Copyright (c) 2018,
	 *         深圳市梦燃科技有限公司 All Rights Reserved.
	 * @createTime 2018年3月18日下午4:45:04
	 */
	public <T> List<T> findByConditions(Class<T> clazz, StringBuffer conditions, Object[] params)
			throws SQLException, Exception {
		return findByConditions(clazz, conditions, params, null, null).getList();
	}

	/**
	 * 根据条件分页查找记录
	 * 
	 * @param object
	 * @param conditions
	 * @param params
	 * @param strPage
	 * @param strPageSize
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a> Copyright (c) 2018,
	 *         深圳市梦燃科技有限公司 All Rights Reserved.
	 * @createTime 2018年3月18日下午4:44:43
	 */
	public <T> PageInfo<T> findByConditions(Class<T> clazz, StringBuffer conditions, Object[] params, Integer strPage,
			Integer strPageSize) throws SQLException, Exception {
		return findByConditions(clazz, conditions, null, params, strPage, strPageSize);
	}

	/**
	 * 根据条件分页查找记录
	 * 
	 * @param object
	 * @param conditions
	 * @param orderby
	 * @param params
	 * @param strPage
	 * @param strPageSize
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a> Copyright (c) 2018,
	 *         深圳市梦燃科技有限公司 All Rights Reserved.
	 * @createTime 2018年3月18日下午4:43:55
	 */
	public <T> PageInfo<T> findByConditions(Class<T> clazz, StringBuffer conditions, String orderby, Object[] params,
			Integer strPage, Integer strPageSize) throws SQLException, Exception {
		DBManager dbManager = new DBManager(readDataSource);
		try {
			dbManager.openConnection();
			StringBuffer strSql = new StringBuffer();
			strSql.append("SELECT * FROM ").append(clazz.getSimpleName().toUpperCase()).append(" WHERE 1=1 ");
			if (conditions != null) {
				strSql.append(conditions);
			}
			return findBySql(dbManager, clazz, strSql.toString(), orderby, params, strPage, strPageSize);
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 根据条件查询数据
	 * 
	 * @param dbManager
	 * @param object
	 * @param params
	 * @param startRow
	 * @param pageSize
	 * @return
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Jan 19, 2017 4:57:40 PM
	 */
	public <T> List<T> findByConditions(DBManager dbManager, Class<T> clazz, Map<String, Object> params,
			Integer startRow, Integer pageSize) throws SQLException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		StringBuffer conditions = new StringBuffer();
		Object[] values = null;
		if (params != null) {
			values = new Object[params.size()];
			Set<String> set = params.keySet();
			int index = 0;
			for (String key : set) {
				conditions.append(" AND ").append(key).append(" = ?");
				values[index++] = params.get(key);
			}
		}

		return findByConditions(dbManager, clazz, conditions, values, startRow, pageSize);
	}

	/**
	 * 根据条件分页查询数据
	 * 
	 * @param dbManager
	 * @param object
	 * @param conditions
	 * @param params
	 * @param startRow
	 * @param pageSize
	 * @return
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Jan 19, 2017 4:57:23 PM
	 */
	public <T> List<T> findByConditions(DBManager dbManager, Class<T> clazz, StringBuffer conditions, Object[] params,
			Integer startRow, Integer pageSize) throws SQLException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		StringBuffer strSql = new StringBuffer();
		strSql.append("SELECT * FROM ").append(clazz.getSimpleName().toUpperCase()).append(" WHERE").append(" 1 = 1");
		if (conditions != null) {
			strSql.append(conditions);
		}
		return findByConditions(dbManager, clazz, strSql.toString(), params, startRow, pageSize);
	}

	/**
	 * 根据SQL分页查询数据
	 * 
	 * @param dbManager
	 * @param object
	 * @param strSql
	 * @param params
	 * @param startRow
	 * @param pageSize
	 * @return
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Mar 7, 2017 2:05:46 PM
	 */
	public <T> List<T> findByConditions(DBManager dbManager, Class<T> clazz, String strSql, Object[] params,
			Integer startRow, Integer pageSize) throws SQLException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		List<T> list = new ArrayList<T>();
		if (pageSize != null) { // 如果pageSize!=null说明传进来的参数有值，表示需要分页
			dbManager.prepareStatement(getPageSql(strSql, startRow, pageSize));
		} else {
			dbManager.prepareStatement(strSql);
		}
		if (params != null) {
			int index = 1;
			for (Object value : params) {
				dbManager.setPrepareParameters(index++, value);
			}
		}
		dbManager.executePrepareQuery();
		while (dbManager.next()) {
			list.add((T) dbManager.setObjectValueByField(clazz.newInstance()));
		}
		return list;
	}


	/**
	 * 根据SQL查找记录
	 * 
	 * @param object
	 * @param strSql
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a> Copyright (c) 2018,
	 *         深圳市梦燃科技有限公司 All Rights Reserved.
	 * @createTime 2018年3月18日下午4:43:21
	 */
	public <T> List<T> findBySql(Class<T> clazz, String strSql, Object[] params) throws SQLException, Exception {
		return findBySql(clazz, strSql, params, null, null).getList();
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
	public <T> PageInfo<T> findBySql(Class<T> clazz, String strSql, Object[] params, Integer page, Integer pageSize)
			throws SQLException, Exception {
		DBManager dbManager = new DBManager(readDataSource);
		try {
			dbManager.openConnection();
			return findBySql(dbManager, clazz, strSql, params, page, pageSize);
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 根据SQL分页查找记录
	 * 
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
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a> Copyright (c) 2018,
	 *         深圳市梦燃科技有限公司 All Rights Reserved.
	 * @createTime 2018年3月18日下午4:42:23
	 */
	public <T> PageInfo<T> findBySql(DBManager dbManager, Class<T> clazz, String strSql, Object[] params,
			Integer page, Integer pageSize)
			throws IOException, SQLException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException {
		PageInfo<T> pageInfo = new PageInfo<T>();
		Integer startRow = null;
		if (pageSize != null) {
			Integer total = count(dbManager, "SELECT COUNT(*) FROM (" + strSql + ") a", params);
			pageInfo.setPage(page);
			pageInfo.setPageSize(pageSize);
			pageInfo.setTotal(total);
			startRow = (page - 1 <= 0 ? 0 : page - 1) * pageSize;
		}
		List<T> list = findByConditions(dbManager, clazz, strSql, params, startRow, pageSize);
		pageInfo.setList(list);
		return pageInfo;
	}

	/**
	 * 根据SQL分页查找记录
	 * 
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
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a> Copyright (c) 2018,
	 *         深圳市梦燃科技有限公司 All Rights Reserved.
	 * @createTime 2018年3月18日下午4:41:38
	 */
	public <T> PageInfo<T> findBySql(DBManager dbManager, Class<T> clazz, String strSql, String orderby,
			Object[] params, Integer page, Integer pageSize)
			throws IOException, SQLException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException {
		PageInfo<T> pageInfo = new PageInfo<T>();
		Integer startRow = null;
		if (pageSize != null) {
			Integer total = count(dbManager, "SELECT COUNT(*) FROM (" + strSql + ") a", params);
			pageInfo.setPage(page);
			pageInfo.setPageSize(pageSize);
			pageInfo.setTotal(total);
			startRow = (page - 1 <= 0 ? 0 : page - 1) * pageSize;
		}
		List<T> list = findByConditions(dbManager, clazz, strSql + orderby, params, startRow, pageSize);
		pageInfo.setList(list);
		return pageInfo;
	}

	/**
	 * 拼接分页的sql
	 * 
	 * @param strSql
	 * @param startRow
	 * @param pageSize
	 * @return Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a> DateTime：
	 *         Jan 19, 2017 4:56:14 PM
	 */
	public abstract String getPageSql(String strSql, int startRow, int pageSize);

	/**
	 * 更新一条记录
	 * 
	 * @param object
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 * @throws Exception
	 * @throws @author
	 *             <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public void update(Object object) throws IOException, SQLException, Exception {
		DBManager dbManager = new DBManager(writeDataSource);
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
	 * 更新数据信息
	 * 
	 * @param dbManager
	 * @param object
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws SQLException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws @author
	 *             <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public void update(DBManager dbManager, Object object) throws NoSuchMethodException, SecurityException,
			SQLException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Map<String, Method> map = ReflectHandler.getFieldAndGetMethodFromObject(object);
		Set<String> set = map.keySet();
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE " + object.getClass().getSimpleName().toUpperCase() + " SET ");
		StringBuffer fieldPart = new StringBuffer();
		for (String field : set) {
			fieldPart.append(", " + field + " = ?");
		}
		sb.append(fieldPart.substring(1)).append(" WHERE 1=1 ");
		List<String> idFields = dbManager.getPrimaryKeys(object);
		for (int i = 0; i < idFields.size(); i++) {
			String field = idFields.get(i).toUpperCase();
			sb.append(" AND " + field + " = ?");
		}
		dbManager.prepareStatement(sb.toString());
		int index = 1;
		for (String field : set) {
			Method method = map.get(field);
			Object value = method.invoke(object);
			dbManager.setPrepareParameters(index++, value);
		}
		for (int i = 0; i < idFields.size(); i++) {
			Method method = map.get(idFields.get(i).toUpperCase());
			Object value = method.invoke(object);
			dbManager.setPrepareParameters(index++, value);
		}
		dbManager.executePrepare();
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
		DBManager dbManager = new DBManager(writeDataSource);
		try {
			dbManager.openConnection();
			return findByPrimaryKey(dbManager, object);
		} finally {
			dbManager.close();
		}
	}

	/**
	 * 根据主键查询一条数据
	 * 
	 * @param dbManager
	 * @param object
	 * @return
	 * @throws SQLException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Jan 19, 2017 4:56:31 PM
	 */
	public <T> T findByPrimaryKey(DBManager dbManager, T object) throws SQLException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT * FROM " + object.getClass().getSimpleName().toUpperCase() + " WHERE ");
		List<String> idFields = dbManager.getPrimaryKeys(object);
		for (int i = 0; i < idFields.size(); i++) {
			String field = idFields.get(i).toUpperCase();
			if (i == 0) {
				sb.append("  " + field + " = ?");
			} else {
				sb.append(" AND " + field + " = ?");
			}
		}
		dbManager.prepareStatement(sb.toString());
		int index = 1;
		Map<String, Method> mapGet = ReflectHandler.getFieldAndGetMethodFromObject(object);
		for (int i = 0; i < idFields.size(); i++) {
			Method method = mapGet.get(idFields.get(i).toUpperCase());
			Object value = method.invoke(object);
			dbManager.setPrepareParameters(index++, value);
		}
		dbManager.executePrepareQuery();
		if (dbManager.next()) {
			dbManager.setObjectValueByField(object);
		} else {
			return null;
		}
		return (T) object;
	}

	/**
	 * 执行SQL
	 * 
	 * @param strSql
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 * @return: int
	 * @throws @author
	 *             <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public void executeSql(String strSql) throws SQLException, Exception {
		executeSql(strSql, null);
	}

	/**
	 * 执行SQL
	 * 
	 * @param dbManager
	 * @param strSql
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 * @throws @author
	 *             <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public void executeSql(DBManager dbManager, String strSql) throws IOException, SQLException {
		executeSql(dbManager, strSql, null);
	}

	/**
	 * 执行SQL并传人参数
	 * 
	 * @param strSql
	 * @param params
	 * @return: int
	 * @throws SQLException
	 * @throws Exception
	 * @throws @author
	 *             <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public int executeSql(String strSql, Object[] params) throws SQLException, Exception {
		DBManager dbManager = new DBManager(writeDataSource);
		int count = 0;
		try {
			dbManager.openConnection();
			dbManager.beginTransaction();
			count = executeSql(dbManager, strSql, params);
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
		return count;
	}

	/**
	 * 执行更新或删除
	 * 
	 * @param dbManager
	 * @param strSql
	 * @param params
	 * @return: int
	 * @throws SQLException
	 * @throws @author
	 *             <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public int executeSql(DBManager dbManager, String strSql, Object[] params) throws SQLException {
		dbManager.prepareStatement(strSql);
		if (params != null) {
			int index = 1;
			for (Object value : params) {
				dbManager.setPrepareParameters(index++, value);
			}
		}
		return dbManager.executePrepare();
	}

	/**
	 * 获取下一个主键值
	 * 
	 * @param dbManager
	 * @param sql
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Jan 19, 2017 4:56:47 PM
	 */
	public int nextPrimaryKey(DBManager dbManager, String sql) throws SQLException {
		dbManager.prepareStatement(sql);
		dbManager.executePrepareQuery();
		if (dbManager.next()) {
			Integer id = dbManager.getInt(1);
			if (id == null || id == 0) {
				id = 1;
			}
			return id + 1;
		}
		return 1;
	}

	/**
	 * 生产一个UUID
	 * 
	 * @return Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a> DateTime：
	 *         Jan 19, 2017 4:56:55 PM
	 */
	public String generatePrimaryKey() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * 判断字段是否在主键列表中，以此来判断该字段是否为主键
	 * 
	 * @param primaryKeys
	 * @param field
	 * @return Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a> DateTime：
	 *         Jan 19, 2017 5:59:49 PM
	 */
	protected Boolean isPrimaryKey(List<String> primaryKeys, String field) {
		for (int i = 0; i < primaryKeys.size(); i++) {
			if (primaryKeys.get(i).equalsIgnoreCase(field)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 根据条件获取一个二进制数据
	 * 
	 * @param strSql
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 * @author <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public byte[] findByteFromBlob(String strSql, Object[] params) throws SQLException, Exception {
		DBManager dbManager = new DBManager(readDataSource);
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
	 * @throws SQLException
	 * @throws IOException
	 * @author <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public byte[] findByteFromBlob(DBManager dbManager, String strSql, Object[] params)
			throws SQLException, IOException {
		dbManager.prepareStatement(strSql);
		if (params != null) {
			int index = 1;
			for (Object value : params) {
				if (value instanceof Integer) {
					dbManager.setInt(index++, (Integer) value);
				} else if (value instanceof Double) {
					dbManager.setDouble(index++, Double.parseDouble(value.toString()));
				} else if (value instanceof Date) {
					dbManager.setTimestamp(index++, new Timestamp(((Date) value).getTime()));
				} else if (value instanceof Timestamp) {
					dbManager.setTimestamp(index++, (Timestamp) value);
				} else if (value instanceof Long) {
					dbManager.setLong(index++, ((Long) value).longValue());
				} else {
					dbManager.setString(index++, (String) value);
				}
			}
		}
		Blob image = null;
		byte[] imgData = null;
		dbManager.executePrepareQuery();
		if (dbManager.next()) {
			image = dbManager.getBlob(1);
			imgData = image.getBytes(1, (int) image.length());
		}
		return imgData;
	}
}
package com.szmengran.common.orm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.szmengran.common.cache.DataCache;
import com.szmengran.common.reflect.ReflectHandler;

public class DBManager {
	private Logger logger = LoggerFactory.getLogger(DBManager.class);
	protected DataSource dataSource = null;
	protected Connection conn = null;
	protected ResultSet rs = null;
	protected PreparedStatement ps = null;
	protected Statement sm = null;
	protected CallableStatement cStmt = null;
	
	public DBManager(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	/**
	 * 打开数据库连接
	 * 
	 * @param type
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NamingException
	 * @throws SQLException
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Jan 19, 2017 5:11:31 PM
	 */
	public Connection openConnection() throws SQLException, Exception{
		conn = dataSource.getConnection();
		sm = conn.createStatement();
		return conn;
	}
	
	/**
	 * 
	 * @Description: 封装Connection的prepareStatement方法查询
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:45:36
	 * 
	 * @param strSql
	 * @throws SQLException
	 */
	public void prepareStatement(String strSql) throws SQLException {
		try {
			ps = conn.prepareStatement(strSql);
			logger.info(strSql);
		} catch (SQLException e) {
			logger.error(strSql);
			throw e;
		}
	}

	/**
	 * 
	 * @Description: 封装PrppareStatement的executeQuery方法
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:46:09
	 * 
	 * @throws SQLException
	 */
	public void executePrepareQuery() throws SQLException {
		rs = ps.executeQuery();
	}

	/**
	 * 
	 * @Description: 封装PrppareStatement的execute方法
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:46:23
	 * @return: int
	 * @throws SQLException
	 */
	public int executePrepare() throws SQLException {
		ps.execute();
		return ps.getUpdateCount();
	}

	/**
	 * 
	 * @Description: 存储过程调用
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:46:35
	 * 
	 * @param strSql
	 * @throws SQLException
	 */
	public void prepareCall(String strSql) throws SQLException {
		try {
			cStmt = conn.prepareCall(strSql);
			logger.info(strSql);
		} catch (SQLException e) {
			logger.error(strSql);
			throw e;
		}
	}

	/**
	 * 
	 * @Description: 存储过程执行
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:47:04
	 * 
	 * @throws SQLException
	 */
	public void executePrepareCall() throws SQLException {
		cStmt.execute();
	}

	/**
	 * 
	 * @Description: 存储过程参数设置
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:47:24
	 * 
	 * @param index
	 * @param value
	 * @throws SQLException
	 */
	public void setPrepareCallParameters(int index, Object value) throws SQLException {
		logger.info("index:" + index + ",value:" + value);
		if (value instanceof Integer) {
			cStmt.setInt(index, (Integer) value);
		} else if (value instanceof Double) {
			cStmt.setDouble(index, Double.parseDouble(value.toString()));
		} else if (value instanceof Date) {
			cStmt.setTimestamp(index, new java.sql.Timestamp(((Date) value).getTime()));
		} else if (value instanceof Long) {
			cStmt.setLong(index, ((Long) value).longValue());
		} else {
			cStmt.setString(index, (String) value);
		}
	}

	/**
	 * 
	 * @Description: 存储过程输出参数设置
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:47:43
	 * 
	 * @param index
	 * @param type
	 * @throws SQLException
	 */
	public void registerPrepareCallOutParameters(int index, int type) throws SQLException {
		logger.info("index:" + index + ",type:" + type);
		cStmt.registerOutParameter(index, type);
	}

	/**
	 * 
	 * @Description: 获取存储过程返回的数据
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:49:19
	 * 
	 * @param index
	 * @param type
	 * @return
	 * @throws SQLException
	 */
	public Object getPrepareCallOutParameters(int index, int type) throws SQLException {
		logger.info("index:" + index + ",type:" + type);
		if (type == Types.INTEGER) {
			return cStmt.getInt(index);
		} else if (type == Types.DOUBLE) {
			return cStmt.getDouble(index);
		} else if (type == Types.ARRAY) {
			return cStmt.getArray(index);
		} else if (type == Types.BIGINT) {
			return cStmt.getLong(index);
		} else if (type == Types.BOOLEAN) {
			return cStmt.getBoolean(index);
		} else {
			return cStmt.getString(index);
		}
	}

	/**
	 * 
	 * @Description: 根据SQL参数数据
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:51:20
	 * 
	 * @param strSql
	 * @return
	 * @throws SQLException
	 */
	public ResultSet executeQuery(String strSql) throws SQLException {
		try {
			ps = conn.prepareStatement(strSql);
			rs = ps.executeQuery();
			logger.info(strSql);
		} catch (SQLException e) {
			logger.error(strSql);
			throw e;
		}
		return rs;
	}

	/**
	 * 执行更新和删除操作
	 * @param strSql
	 * @return
	 * @throws SQLException      
	 * @return
	 * @throws   
	 * @author <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public void execute(String strSql) throws SQLException {
		try {
			sm.execute(strSql);
			logger.info(strSql);
		} catch (SQLException e) {
			logger.error(strSql);
			throw e;
		}
	}

	/**
	 * 
	 * @Description: 开启批量添加操作
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:52:19
	 * 
	 * @throws SQLException
	 */
	public void addBatch() throws SQLException {
		ps.addBatch();
		
	}

	/**
	 * 
	 * @Description: 开启批量添加操作
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:53:27
	 * 
	 * @param strSql
	 * @throws SQLException
	 */
	public void addBatch(String strSql) throws SQLException {
		try {
			sm.addBatch(strSql);
			logger.info(strSql);
		} catch (SQLException e) {
			logger.error(strSql);
			throw e;
		}
	}

	/**
	 * 批量提交
	 * @return
	 * @throws SQLException      
	 * @return: int 影响多少行   
	 * @throws   
	 * @author <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public void commitBatch() throws SQLException {
		if (ps != null) {
			ps.executeBatch();
		} else {
			sm.executeBatch();
		}
	}

	/**
	 * 
	 * @Description: 开启事物
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:53:58
	 * 
	 * @throws SQLException
	 */
	public void beginTransaction() throws SQLException {
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @Description: 提交事物
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:54:08
	 * 
	 * @throws SQLException
	 */
	public void commitTransaction() throws SQLException {
		try {
			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @Description: 事物数据回滚
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:54:26
	 * 
	 * @throws SQLException
	 */
	public void rollbackTransaction() throws SQLException {
		try {
			conn.rollback();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @Description: 获取主键字段值
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:54:40
	 * 
	 * @param object
	 * @return
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public List<String> getPrimaryKeys(Object object) throws SQLException {
		Object keys = DataCache.get(object.getClass().getName() + "_PK");
		if (keys != null) {
			return (List<String>) keys;
		}

		List<String> primaryKeys = new ArrayList<String>();
		rs = conn.getMetaData().getPrimaryKeys(null, null, object.getClass().getSimpleName().toUpperCase());
		while (rs.next()) {
			primaryKeys.add(rs.getString("COLUMN_NAME"));
		}
		DataCache.put(object.getClass().getName() + "_PK", primaryKeys);
		return primaryKeys;
	}

	/**
	 * 
	 * @Description: 关闭数据库资源
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:54:52
	 * 
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		if (rs != null) {
			rs.close();
			rs = null;
		}
		if (sm != null)
			try {
				sm.close();
				sm = null;
			} catch (SQLException e) {
				throw e;
			}
		if (ps != null)
			try {
				ps.close();
				ps = null;
			} catch (SQLException e) {
				throw e;
			}
		if (cStmt != null) {
			try {
				cStmt.close();
				cStmt = null;
			} catch (SQLException e) {
				throw e;
			}
		}
		if (conn != null) {
			conn.close();
		}
	}

	/**
	 * 
	 * @Description: 封装ResultSet的setString方法
	 * @author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @date 2016年10月26日 下午1:55:07
	 * 
	 * @param index
	 * @param value
	 * @throws SQLException
	 */
	public void setString(int index, String value) throws SQLException {
		value = value.trim();
		logger.info(index + "-" + value);
		ps.setString(index, value);
	}

	/**
	 * 封装ResultSet的setInt方法
	 * 
	 * @param index
	 * @param value
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:19:44
	 */
	public void setInt(int index, int value) throws SQLException {
		logger.info(index + "-" + value);
		ps.setInt(index, value);
	}

	/**
	 * 封装ResultSet的setLong方法
	 * 
	 * @param index
	 * @param value
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:19:35
	 */
	public void setLong(int index, long value) throws SQLException {
		logger.info(index + "-" + value);
		ps.setLong(index, value);
	}

	/**
	 * 封装ResultSet的setDouble方法
	 * 
	 * @param index
	 * @param value
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:19:24
	 */
	public void setDouble(int index, double value) throws SQLException {
		logger.info(index + "-" + value);
		ps.setDouble(index, value);
	}

	/**
	 * 封装ResultSet的setTimestamp方法
	 * 
	 * @param index
	 * @param value
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:19:14
	 */
	public void setTimestamp(int index, Date value) throws SQLException {
		logger.info(index + "-" + value);
		ps.setTimestamp(index, new Timestamp(value.getTime()));
	}

	/**
	 * 封装ResultSet的setBytes方法
	 * 
	 * @param index
	 * @param value
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:19:04
	 */
	public void setBytes(int index, byte value[]) throws SQLException {
		logger.info(index + "-" + value);
		ps.setBytes(index, value);
	}

	/**
	 * 封装ResultSet的getString方法
	 * 
	 * @param index
	 * @return
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:18:54
	 */
	public String getString(int index) throws SQLException {
		return rs.getString(index);
	}

	/**
	 * 封装ResultSet的getInt方法
	 * 
	 * @param index
	 * @return
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:18:44
	 */
	public int getInt(int index) throws SQLException {
		return rs.getInt(index);
	}

	/**
	 * 封装ResultSet的getLong方法
	 * 
	 * @param index
	 * @return
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:18:32
	 */
	public long getLong(int index) throws SQLException {
		return rs.getLong(index);
	}

	/**
	 * 封装ResultSet的getDouble方法
	 * 
	 * @param index
	 * @return
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:18:16
	 */
	public double getDouble(int index) throws SQLException {
		return rs.getDouble(index);
	}

	/**
	 * 封装ResultSet的getTimestamp方法
	 * 
	 * @param index
	 * @return
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:18:04
	 */
	public Timestamp getTimestamp(int index) throws SQLException {
		return rs.getTimestamp(index);
	}

	/**
	 * 封装ResultSet的getBytes方法
	 * 
	 * @param index
	 * @return
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:17:47
	 */
	public byte[] getBytes(int index) throws SQLException {
		return rs.getBytes(index);
	}

	/**
	 * 封装ResultSet的Timestamp方法
	 * 
	 * @param name
	 * @return
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:17:35
	 */
	public Timestamp getTimestamp(String name) throws SQLException {
		return rs.getTimestamp(name);
	}

	/**
	 * 封装ResultSet的getString方法
	 * 
	 * @param name
	 * @return
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:16:48
	 */
	public String getString(String name) throws SQLException {
		return rs.getString(name).trim();
	}

	/**
	 * 封装ResultSet的getInt方法
	 * 
	 * @param name
	 * @return
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:16:30
	 */
	public int getInt(String name) throws SQLException {
		return rs.getInt(name);
	}

	/**
	 * 封装ResultSet的getLong方法
	 * 
	 * @param name
	 * @return
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:14:57
	 */
	public long getLong(String name) throws SQLException {
		return rs.getLong(name);
	}

	/**
	 * 封装ResultSet的getDouble方法
	 * 
	 * @param name
	 * @return
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:14:57
	 */
	public double getDouble(String name) throws SQLException {
		return rs.getDouble(name);
	}

	/**
	 * 封装ResultSet的getBytes方法
	 * 
	 * @param name
	 * @return
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:14:57
	 */
	public byte[] getBytes(String name) throws SQLException {
		return rs.getBytes(name);
	}

	/**
	 * 设置PreparedStatement的参数值
	 * 
	 * @param index
	 * @param value
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:13:31
	 */
	public void setPrepareParameters(int index, Object value) throws SQLException {
		logger.info("index:" + index + ",value:" + value);
		ps.setObject(index, value);
	}

	/**
	 * 将查询结果ResultSet的值赋给object对象
	 * 
	 * @param object
	 * @return
	 * @throws SQLException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:11:13
	 */
	public Object setObjectValues(Object object) throws SQLException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Map<String, Method> map = ReflectHandler.getFieldAndSetMethodFromObject(object);
		Set<Field> set = ReflectHandler.getAllFields(object);
		String filedName = "";
		for (Field field : set) {
			Object objType = field.getType();
			Method method = map.get(field.getName().toUpperCase());

			filedName = field.getName();
			if (objType == Integer.class) {
				int value = rs.getInt(filedName);
				if (rs.wasNull()) {
					continue;
				}
				method.invoke(object, value);
			} else if (objType == Short.class) {
				Short value = rs.getShort(filedName);
				if (rs.wasNull()) {
					continue;
				}
				method.invoke(object, value);
			}else if (objType == Date.class || objType == Timestamp.class) {
				Timestamp value = rs.getTimestamp(filedName);
				if (rs.wasNull()) {
					continue;
				}
				method.invoke(object, value);
			} else if (objType == java.sql.Date.class) {
				Date date = rs.getDate(filedName);
				if (rs.wasNull()) {
					continue;
				}
				method.invoke(object, date);
			} else if (objType == Double.class) {
				Double value = rs.getDouble(filedName);
				if (rs.wasNull()) {
					continue;
				}
				method.invoke(object, value);
			} else if (objType == Long.class) {
				Long value = rs.getLong(filedName);
				if (rs.wasNull()) {
					continue;
				}
				method.invoke(object, value);
			} else if (objType == Blob.class) {
				Blob value = rs.getBlob(filedName);
				if (rs.wasNull()) {
					continue;
				}
				method.invoke(object, value);
			} else if (objType == Float.class) {
				Float value = rs.getFloat(filedName);
				if (rs.wasNull()) {
					continue;
				}
				method.invoke(object, value);
			} else if (objType == Byte.class) {
				Byte value = rs.getByte(filedName);
				if (rs.wasNull()) {
					continue;
				}
				method.invoke(object, value);
			} else if (objType == Boolean.class) {
				Boolean value = rs.getBoolean(filedName);
				if (rs.wasNull()) {
					continue;
				}
				method.invoke(object, value);
			} else { // 字符串
				String value = rs.getString(filedName);
				if (rs.wasNull()) {
					continue;
				}
				method.invoke(object, value);
			}
		}
		return object;
	}

	/**
	 * 将查询结果ResultSet的值赋给object对象，根据查询的字段进行赋值
	 * 
	 * @param object
	 * @return
	 * @throws SQLException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2015-3-14 下午11:07:50
	 */
	public <T> T setObjectValueByField(T object) throws SQLException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String filedName = "";
		ResultSetMetaData rsmd = ps.getMetaData();
		ResultSet rs = ps.getResultSet();
		Map<String, Method> map = ReflectHandler.getFieldAndSetMethodFromObject(object);
		try{
			int columnCount = rsmd.getColumnCount();
			for (int columnIndex = 1; columnIndex <= columnCount; ++columnIndex) {
				filedName = rsmd.getColumnLabel(columnIndex); // getColumnName
				Method method = map.get(filedName.toUpperCase());
				if (method == null) {
					continue;
				}
				
				Class<?>[] type = method.getParameterTypes();
				if (type[0] == Integer.class) {
					int value = rs.getInt(filedName);
					if (rs.wasNull()) {
						continue;
					}
					method.invoke(object, value);
				} else if (type[0] == Short.class) {
					Short value = rs.getShort(filedName);
					if (rs.wasNull()) {
						continue;
					}
					method.invoke(object, value);
				}else if (type[0] == Date.class || type[0] == Timestamp.class) {
					Timestamp value = rs.getTimestamp(filedName);
					if (rs.wasNull()) {
						continue;
					}
					method.invoke(object, value);
				} else if (type[0] == java.sql.Date.class) {
					Date date = rs.getDate(filedName);
					if (rs.wasNull()) {
						continue;
					}
					method.invoke(object, date);
				} else if (type[0] == Double.class) {
					Double value = rs.getDouble(filedName);
					if (rs.wasNull()) {
						continue;
					}
					method.invoke(object, value);
				} else if (type[0] == Long.class) {
					Long value = rs.getLong(filedName);
					if (rs.wasNull()) {
						continue;
					}
					method.invoke(object, value);
				} else if (type[0] == Blob.class) {
					Blob value = rs.getBlob(filedName);
					if (rs.wasNull()) {
						continue;
					}
					method.invoke(object, value);
				} else if (type[0] == Float.class) {
					Float value = rs.getFloat(filedName);
					if (rs.wasNull()) {
						continue;
					}
					method.invoke(object, value);
				} else if (type[0] == Byte.class) {
					Byte value = rs.getByte(filedName);
					if (rs.wasNull()) {
						continue;
					}
					method.invoke(object, value);
				} else if (type[0] == Boolean.class) {
					Boolean value = rs.getBoolean(filedName);
					if (rs.wasNull()) {
						continue;
					}
					method.invoke(object, value);
				} else { // 字符串
					String value = rs.getString(filedName);
					if (rs.wasNull()) {
						continue;
					}
					method.invoke(object, value);
				}
			}
		}catch(Exception e){
			logger.error("实体类"+object.getClass().getSimpleName()+"的字段"+filedName+"类型与查询的SQL语句字段类型不匹配");
			throw e;
		}
		return object;
	}

	/**
	 * 获取二进制数据
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 *             Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 *             DateTime： Jan 19, 2017 4:43:15 PM
	 */
	public String getBlob(InputStream in) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] data = new byte[4096];
		int count = -1;
		while ((count = in.read(data, 0, 4096)) != -1)
			outStream.write(data, 0, count);

		data = null;
		String result = new String(outStream.toByteArray(), "utf-8");
		return result;

	}

	public Blob getBlob(int index) throws IOException, SQLException {
		return rs.getBlob(index);
	}

	/**
	 * 封装ResultSet的next方法
	 * 
	 * @return
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-3 上午11:10:48
	 */
	public boolean next() throws SQLException {
		return rs.next();
	}

	/**
	 * 批量执行
	 * 
	 * @return
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-4 上午1:44:55
	 */
	public int[] executePreparedUpdateBatch() throws SQLException {
		return ps.executeBatch();
	}

	/**
	 * 获取数据库类型
	 * 
	 * @return
	 * @throws SQLException
	 * @Author <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * @ModifyTime 2014-8-4 上午1:33:03
	 */
	public String getDataBaseType() throws SQLException {
		DatabaseMetaData dbmd = conn.getMetaData();
		return dbmd.getDatabaseProductName();
	}
}

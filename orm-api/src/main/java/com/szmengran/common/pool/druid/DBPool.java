package com.szmengran.common.pool.druid;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSourceFactory;

/**
 * @Package com.szmengran.common.pool.druid
 * @Description: TODO
 * @date 2018年1月20日 上午11:41:01
 * @author <a href="mailto:android_li@sina.cn">Joe</a>
 */
public class DBPool {
	/** 使用配置文件构建Dbcp数据源. */
	public static Properties write = null;
	public static Properties read = null;

	static {
		write = new Properties();
		read = new Properties();
		try {
			loadConfig(write, "writedb.properties");
			loadConfig(read, "readdb.properties");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据类型获取数据源
	 * 
	 * @param type
	 *            数据源类型
	 * @return druid或者dbcp数据源
	 * @throws Exception
	 *             the exception
	 */
	public static final DataSource getDataSource(String type) throws Exception{
		DataSource dataSource = null;
		if (type.equalsIgnoreCase("read")) {
			dataSource = DruidDataSourceFactory.createDataSource(read);
		} else {
			dataSource = DruidDataSourceFactory.createDataSource(write);
		}
		return dataSource;
	}

	/**
	 * 加载配置信息
	 * @param pro
	 * @param config
	 * @throws Exception      
	 * @return: void      
	 * @throws IOException 
	 * @throws   
	 * @author <a href="mailto:android_li@sina.cn">Joe</a>
	 */
	public static final void loadConfig(Properties pro, String config) throws IOException{
		InputStream inputStream = null;
		try {
			inputStream = DBPool.class.getClassLoader().getResourceAsStream("conf/" + config);
			if (inputStream == null) {
				inputStream = DBPool.class.getClassLoader().getResourceAsStream(config);
			}
			if (inputStream == null) {
				return ;
			}
			pro.load(inputStream);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

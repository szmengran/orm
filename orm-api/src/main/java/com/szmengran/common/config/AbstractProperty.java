package com.szmengran.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class AbstractProperty {
	/**
	 * 根据文件名和key获取配置文件的值
	 * @param key 配置文件的键
	 * @param fileName 配置文件文件名
	 * @return
	 * @throws IOException
	 * Author： <a href="mailto:android_li@sina.cn">LiMaoYuan</a>
	 * DateTime： Jan 19, 2017 10:52:22 AM
	 */
	public static String getValueByKey(String key,String fileName) throws IOException{
		Properties properties=new Properties();
		InputStream inStream = AbstractProperty.class.getClassLoader().getResourceAsStream("conf/"+fileName);
		if(inStream == null){
			inStream = AbstractProperty.class.getClassLoader().getResourceAsStream(fileName);
		}
		if(inStream == null){
			return "";
		}
		properties.load(inStream);
		return properties.getProperty(key);
	}
}

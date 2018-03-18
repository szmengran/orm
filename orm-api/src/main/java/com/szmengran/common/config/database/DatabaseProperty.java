package com.szmengran.common.config.database;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.szmengran.common.config.AbstractProperty;

public class DatabaseProperty extends AbstractProperty{

	private static Logger logger = LoggerFactory.getLogger(DatabaseProperty.class);
	private static DatabaseProperty instance = new DatabaseProperty();
	public static final String DATASOURCE_READ = "read";
	public static final String DATASOURCE_WRITE = "write";
	private DatabaseProperty(){
		try {
			this.driver = getValueByKey("driverClassName", "druidwrite.properties");
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		
	}
	public static DatabaseProperty getInstance(){
		return instance;
	}
	private String driver;
	public String getDriver() {
		return driver;
	}
}

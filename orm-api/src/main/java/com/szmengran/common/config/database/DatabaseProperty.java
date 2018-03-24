package com.szmengran.common.config.database;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.szmengran.common.Constant;
import com.szmengran.common.config.AbstractProperty;

public class DatabaseProperty extends AbstractProperty{

	private static Logger logger = LoggerFactory.getLogger(DatabaseProperty.class);
	private static DatabaseProperty instance = new DatabaseProperty();
	private DatabaseProperty(){
		try {
			this.driver = getValueByKey("driverClassName", Constant.WRITE_POOL_CONFIG_FILE);
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

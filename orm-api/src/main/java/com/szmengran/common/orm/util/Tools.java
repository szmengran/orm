package com.szmengran.common.orm.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Package com.szmengran.common.orm.util
 * @Description: 工具类
 * @date 2018年6月13日 上午10:00:32
 * @author <a href="mailto:android_li@sina.cn">Joe</a>
 */
public class Tools {
	/**
	 * 根据当前时间生成一个主键
	 * 
	 * @return
	 */
	public final static synchronized String generatePrimaryKeyByTime() {
		String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
		return date + (System.nanoTime() + "").substring(6);
	}
}

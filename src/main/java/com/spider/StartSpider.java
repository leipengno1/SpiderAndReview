/**   
* @Title: StartSpider.java 
* @Package com.spider 
* @Description: TODO(用一句话描述该文件做什么) 
* @author zhuyj   
* @date 2019-08-17 
*/
package com.spider;

import com.config.SpiderProperties;
import com.util.FileUtil;

import us.codecraft.webmagic.Spider;

/**
 * @ClassName: StartSpider
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: zhuyj
 * @date: 2019-08-16
 */
public class StartSpider implements Runnable {
	
	public static boolean isRuning = false;

	private SpiderProperties spiderProperties;

	public StartSpider(SpiderProperties spiderProperties) {
		this.spiderProperties = spiderProperties;
	}

	@SuppressWarnings("resource")
	@Override
	public void run() {
		try {
			FileUtil.init();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		isRuning = true;
		
		Spider.create(new CfdaProcessor())
				.addUrl(spiderProperties.getStarUrl())
				.addPipeline(new MyFilePipeline(spiderProperties.getSavePath()))
				.setDownloader(new CfdaSeleniumDownloader().setSleepTime(Integer.valueOf(spiderProperties.getSleepTime())))
				.thread(Integer.valueOf(spiderProperties.getThreadNum()))
				.run();
		
		try {
			
		} catch (Exception e) {
			// TODO: handle exception
		}finally {
			isRuning = false;
		}
	
	}

}

/**   
* @Title: CfdaSeleniumDownloader.java 
* @Package com.local.WebSpider.cfda 
* @Description: TODO(用一句话描述该文件做什么) 
* @author zhuyj   
* @date 2019-08-16 
*/
package com.spider;

import java.io.Closeable;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.config.SpiderProperties;
import com.model.ItemInfo;
import com.util.SpringBeanUtil;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.selector.PlainText;

/** 
* @ClassName: CfdaSeleniumDownloader 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author: zhuyj
* @date: 2019-08-16 
*/
public class CfdaSeleniumDownloader implements Downloader, Closeable{
	
	private volatile WebDriverPool webDriverPool;

	private static Logger log = LoggerFactory.getLogger(CfdaSeleniumDownloader.class);

	private int sleepTime = 0;

	private int poolSize = 1;
	
	private SpiderProperties spiderProperties = SpringBeanUtil.getBean("spiderProperties");
	
	
	public CfdaSeleniumDownloader setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
		return this;
	}

	@Override
	public Page download(Request request, Task task) {
		// TODO Auto-generated method stub
		checkInit();
		WebDriver webDriver;
		try {
			webDriver = webDriverPool.get();
		} catch (InterruptedException e) {
			log.warn("interrupted", e);
			return null;
		}
		
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("----------------------------------------------------------------");
		log.info("start processing page:" + request.getUrl());
		
		request.putExtra("UUID", task.getUUID());
		String content = "";
		Page page = new Page();
		page.setUrl(new PlainText(request.getUrl()));
		page.setRequest(request);

		webDriver.get(request.getUrl());
		WebDriver.Options manage = webDriver.manage();
		
		Site site = task.getSite();
		if (site.getCookies() != null) {
			for (Map.Entry<String, String> cookieEntry : site.getCookies()
					.entrySet()) {
				Cookie cookie = new Cookie(cookieEntry.getKey(),
						cookieEntry.getValue());
				manage.addCookie(cookie);
				
			}
		}
		
		boolean returnFlag = true;
		
		try{
			String url = request.getUrl();
			
			if(url.matches(".*?/cfda\\?retryTime=(\\d+)$")) {
				int menuSize = webDriver.findElements(By.xpath("//ul[@class='show_lits ylqx']//li")).size();
				
				if(menuSize == 0) {
					retry(page, url);
					returnFlag = false;
				}
				
				for(int i = 1; i <= menuSize; i++) {
					WebElement menuElement = webDriver.findElement(By.xpath("//ul[@class='show_lits ylqx']//li[" + i + "]"));
					menuElement.click();
					
					WebDriverWait wait = new WebDriverWait(webDriver, 20);
					waitDone(wait);
					
					int totalNum = Integer.valueOf(webDriver.findElement(By.xpath("//b[@class='totalPage']")).getText());
					
					page.addTargetRequest(url + "/result?searchTable=" + URLEncoder.encode(menuElement.getText(), "UTF-8") + "&retryTime=1&totalNum=" + totalNum + "&currentPage=1");
				}
			}else if(url.matches(".*?totalNum=(\\d+)&currentPage=(\\d+)$")) {
								
				int result = getInfo(webDriver, page);
				if(result == 0) {
					retry(page, url);
					returnFlag = false;
				}else {
					Pattern pattern_page = Pattern.compile(".*?retryTime=(\\d+)&totalNum=(\\d+)&currentPage=(\\d+)$",Pattern.CASE_INSENSITIVE);
			        Matcher matcher_page = pattern_page.matcher(url);		        
			        matcher_page.find();
			        
			        int retryTime = Integer.valueOf(matcher_page.group(1));
			        int totalNum = Integer.valueOf(matcher_page.group(2));
			        int currentPage = Integer.valueOf(matcher_page.group(3));
			        if((currentPage + 1) < totalNum) {
			        	page.addTargetRequest(url.replace("retryTime=" + retryTime +"&currentPage=" + currentPage, "retryTime=1&currentPage=" + (currentPage + 1)));
			        }
				}		        
			}else {
				List<WebElement> trElements = webDriver.findElements(By.xpath("//div[@class='detail_list']//table/tbody/tr"));
				
				if(trElements == null || trElements.size() == 0) {
					retry(page, url);
					returnFlag = false;
				}else {
					ItemInfo itemInfo = new ItemInfo();
					for(WebElement trElement : trElements) {
						List<WebElement> tds = trElement.findElements(By.xpath("td"));
						if(tds.size() == 2) {
							WebElement fristTd = tds.get(0);
							
							String name = fristTd.getText();
							String value = tds.get(1).getText();
							if(name.equals("批准日期")) {
								itemInfo.setApprovalDate(value.replaceAll("\\.", "-"));
							}else if(name.equals("有效期") || name.equals("有效期至") || name.equals("有效期截止日")) {
								if(itemInfo.getExpiredDate() == null || itemInfo.getExpiredDate().equals("")) {
									itemInfo.setExpiredDate(value.replaceAll("\\.", "-"));
								}
							}else if(name.equals("地址") || name.equals("注册人住所") || name.equals("生产厂地址（中文）")) {
								if(itemInfo.getEnterpriseAddress() == null || itemInfo.getEnterpriseAddress().equals("")) {
									itemInfo.setEnterpriseAddress(value);
								}
							}else if(name.equals("生产单位") || name.equals("注册人名称") || name.equals("生产厂商名称（中文）") || name.equals("生产厂商名称（英文）")) {
								if(itemInfo.getEnterprise() == null || itemInfo.getEnterprise().equals("")) {
									itemInfo.setEnterprise(value);
								}
								
							}else if(name.equals("注册号") || name.equals("注册证编号")) {
								if(itemInfo.getApprovalNum() == null || itemInfo.getApprovalNum().equals("")) {
									itemInfo.setApprovalNum(value);
								}
							}else if(name.equals("产品名称") || name.equals("产品名称（中文）")) {
								if(itemInfo.getProductName() == null || itemInfo.getProductName().equals("")) {
									itemInfo.setProductName(value);
								}
							}
							String[] splitUrl = url.split("/");
							itemInfo.setEsId(splitUrl[splitUrl.length - 1]);
						}

					}
					page.putField("info", JSON.toJSONString(itemInfo));
				}
			}
		
		}catch(Exception e){
			e.printStackTrace();
		}
		
		page.setRawText(content);
		if(returnFlag) {
			webDriverPool.returnToPool(webDriver);
		}else {
			webDriverPool.closeOne(webDriver);
		}
		
		return page;
	}
	
	private void waitDone(WebDriverWait wait) {
		wait.until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				// TODO Auto-generated method stub
				JavascriptExecutor driver_js= ((JavascriptExecutor) driver);
				return (boolean) driver_js.executeScript("jQuery.ajaxPrefilter(function( options ) { " +
						"	options.global = true;" + 
						"}); return jQuery.active == 0;");
			}					
		});
	}
	
	private int getInfo(WebDriver webDriver, Page page) {
		int liSize = webDriver.findElements(By.xpath("//li/a")).size();
		
		for(int j = 1; j <= liSize; j++) {
			WebElement liElement = webDriver.findElement(By.xpath("//li[" + j + "]/a"));
			
			String val = liElement.getAttribute("onclick");
			
			Pattern pattern_page = Pattern.compile(".*?\\((.*?)\\);$",Pattern.CASE_INSENSITIVE);
	        Matcher matcher_page = pattern_page.matcher(val);
	        if(matcher_page.find()) {
	        	page.addTargetRequest(spiderProperties.getDetailPath() + matcher_page.group(1).replaceAll("'", "").replaceAll(",", "/"));
	        	log.info(matcher_page.group(1).replaceAll("'", "").replaceAll(",", "/"));
	        }
		}
		
		return liSize;
	}
	
	private void retry(Page page, String url) {
		Pattern pattern_retry = Pattern.compile("retryTime=(\\d+)",Pattern.CASE_INSENSITIVE);
        Matcher matcher_retry = pattern_retry.matcher(url);		        
        matcher_retry.find();
        int retryTime = Integer.valueOf(matcher_retry.group(1));
        if(retryTime < 10) {
        	page.addTargetRequest(url.replace("retryTime=" + retryTime, "retryTime=" + (retryTime + 1)));
        }
	}


	private void checkInit() {
		if (webDriverPool == null) {
			synchronized (this) {
				webDriverPool = new WebDriverPool(poolSize);
			}
		}
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		webDriverPool.closeAll();
	}

	@Override
	public void setThread(int threadNum) {
		// TODO Auto-generated method stub
		this.poolSize = threadNum;
	}

}

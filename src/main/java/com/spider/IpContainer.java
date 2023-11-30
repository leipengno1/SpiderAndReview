/**   
* @Title: IpContainer.java 
* @Package com.spider 
* @Description: TODO(用一句话描述该文件做什么) 
* @author zhuyj   
* @date 2019-09-19 
*/
package com.spider;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.config.SpiderProperties;
import com.model.ItemIp;
import com.service.ItemIpService;
import com.util.HTTPClientsUtil;

/** 
* @ClassName: IpContainer 
* @Description: 保存爬去可用的IP
* @author: zhuyj
* @date: 2019-09-19 
*/
@Component
public class IpContainer {
	private static Logger log = LoggerFactory.getLogger(IpContainer.class);
	
	private static BlockingDeque<String> innerQueue = new LinkedBlockingDeque<String>();
	@Autowired
	private ItemIpService itemIpService;
	
	@Autowired
	private SpiderProperties spiderProperties;
 	
	public String get() {
		
		String proxy = innerQueue.poll();
		if(proxy == null) {
			init();
			proxy = innerQueue.poll();
		}
		
		return proxy;
	}
	
	@SuppressWarnings("unchecked")
	private void init() {
		log.info("获取可用ip");
		int times = 1;
		String ipStr = "";
		while(times <= 3 && StringUtils.isEmpty(ipStr)) {
			ipStr = HTTPClientsUtil.doGet(spiderProperties.getProxyPool(), "get", null, null, 0);
			times++;
		}
		
		int num = 0;
		if(StringUtils.isNotEmpty(ipStr)) {
			for(String ip : ipStr.split("\n")) {
				Map<String, Object> map = (Map<String, Object>)JSON.parse(ip);
				
				String type = (String) map.get("type");
				String host = (String) map.get("host");
				int port = (int) map.get("port");
				BigDecimal response = (BigDecimal) map.get("response_time");
				
				if("http".equalsIgnoreCase(type) && response.compareTo(BigDecimal.valueOf(3)) == -1) {
					
					ItemIp itemIp = itemIpService.search(host);
					if(itemIp == null || StringUtils.isEmpty(itemIp.getIp())) {
						String returnStr = HTTPClientsUtil.doGet(spiderProperties.getProxyCheck(), "get", null, host, port);
						
						if(StringUtils.isNotEmpty(returnStr)) {
							returnStr = returnStr.replaceAll("\n", "");
							if(host.equalsIgnoreCase(returnStr)) {
								itemIpService.save(host);
								innerQueue.add(host + ":" + port);
								num++;
							}
						}
					}
				}
				if(num > 10) {
					break;
				}
			}
		}
		
	}
	
	public static void main(String[] args) {
		IpContainer ipContainer = new IpContainer();
		ipContainer.init();
	}
}

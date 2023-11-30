/**   
* @Title: ItemIpService.java 
* @Package com.service 
* @Description: TODO(用一句话描述该文件做什么) 
* @author zhuyj   
* @date 2019-09-23 
*/
package com.service;

import com.model.ItemIp;

/** 
* @ClassName: ItemIpService 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author: zhuyj
* @date: 2019-09-23 
*/
public interface ItemIpService {
	public ItemIp search(String ip);
	public void save(String ip);
}

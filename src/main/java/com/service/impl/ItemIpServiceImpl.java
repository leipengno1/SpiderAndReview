/**   
* @Title: ItemIpServiceImpl.java 
* @Package com.service.impl 
* @Description: TODO(用一句话描述该文件做什么) 
* @author zhuyj   
* @date 2019-09-23 
*/
package com.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dao.ItemIpDao;
import com.model.ItemIp;
import com.service.ItemIpService;

/** 
* @ClassName: ItemIpServiceImpl 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author: zhuyj
* @date: 2019-09-23 
*/
@Service
public class ItemIpServiceImpl implements ItemIpService{
	@Autowired
	private ItemIpDao itemIpDao;

	/** 
	* @Title: search 
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param itemIp
	* @return 
	* @see com.service.ItemIpService#search(com.model.ItemIp) 
	*/
	@Override
	public ItemIp search(String ip) {
		// TODO Auto-generated method stub
		return itemIpDao.selectById(ip);
	}

	/** 
	* @Title: save 
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param ip 
	* @see com.service.ItemIpService#save(java.lang.String) 
	*/
	@Override
	public void save(String ip) {
		// TODO Auto-generated method stub
		itemIpDao.insert(new ItemIp(ip));
	}

}

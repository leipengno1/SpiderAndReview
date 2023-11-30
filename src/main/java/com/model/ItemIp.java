/**   
* @Title: ItemIp.java 
* @Package com.model 
* @Description: TODO(用一句话描述该文件做什么) 
* @author zhuyj   
* @date 2019-09-23 
*/
package com.model;

import java.io.Serializable;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName: ItemIp
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: zhuyj
 * @date: 2019-09-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ITEM_IP_BLACKLIST")
@AllArgsConstructor
public class ItemIp extends Model<ItemIp> {
	/**
	 * @Fields serialVersionUID : TODO(用一句话描述这个变量表示什么)
	 */
	private static final long serialVersionUID = 1L;
	
	@TableId
	private String ip;

	/**
	 * @Title: pkVal
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @return
	 * @see com.baomidou.mybatisplus.activerecord.Model#pkVal()
	 */
	@Override
	protected Serializable pkVal() {
		// TODO Auto-generated method stub
		return this.ip;
	}

}

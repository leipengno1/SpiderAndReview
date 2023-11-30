/**   
* @Title: StartApp.java 
* @Package com 
* @Description: TODO(用一句话描述该文件做什么) 
* @author zhuyj   
* @date 2019-08-15 
*/
package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


/** 
* @ClassName: StartApp 
* @Description: 启动类
* @author: zhuyj
* @date: 2019-08-15 
*/
@SpringBootApplication(scanBasePackages = { "com.*" })
@EnableConfigurationProperties
public class StartApp extends SpringBootServletInitializer {

	/** 
	* @Title: main 
	* @Description: 启动类入口
	* @param args void
	*/
	public static void main(String[] args) {
		SpringApplication.run(StartApp.class, args);	
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		// tomcat启动配置
		return application.sources(StartApp.class);
	}
}
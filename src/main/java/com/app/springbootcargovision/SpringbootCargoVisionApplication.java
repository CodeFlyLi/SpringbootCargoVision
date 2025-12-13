package com.app.springbootcargovision;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用入口类
 * 负责启动 Spring Boot 应用
 */
@SpringBootApplication
@MapperScan("com.app.springbootcargovision.mapper")
public class SpringbootCargoVisionApplication {

	/**
	 * 主方法：程序入口
	 * 通过 SpringApplication.run 启动内嵌 Tomcat 与 Spring 环境
	 */
	public static void main(String[] args) {
		SpringApplication.run(SpringbootCargoVisionApplication.class, args);
	}

}

/**
 * 
 */
package com.test.dbstore.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.test.dbstore.dao.TradeDao;

/**
 * @author Jitendra
 *
 */
@Configuration
@EnableTransactionManagement
public class ApplicationConfiguration {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Bean
	public TradeDao tradeDao() {
		return new TradeDao(jdbcTemplate);
	}
}

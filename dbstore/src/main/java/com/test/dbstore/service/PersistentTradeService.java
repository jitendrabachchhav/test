/**
 * 
 */
package com.test.dbstore.service;

import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.test.dbstore.dao.TradeDao;
import com.test.dbstore.dom.Trade;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jitendra
 *
 */
@Slf4j
@Component
public class PersistentTradeService {
	private final TradeDao tradeDao;
	
	public PersistentTradeService(TradeDao tradeDao) {
		this.tradeDao = tradeDao;
	}
	
	@Transactional	
	public void store(Trade trade, LocalDate today) {
		if (valid(trade, today)) {
			trade.setCreatedOn(today);
			log.info("Trade is valid, trying to store {}", trade);
			tradeDao.upsert(trade);
		}
	}

	private boolean valid(Trade trade, LocalDate today) {
		if (trade.getMaturityDate().isBefore(today)) {
			log.warn("Validation failed, maturityDate {} for tradeId {} is before today {}",trade.getMaturityDate(), trade.getId(), today);
			return false;
		}
		return true;
	}

	public Trade getLatest(String tradeId) {
		return tradeDao.getLatest(tradeId);
	}
	
	public void markTradesExpired(LocalDate date) {
		tradeDao.updateExpired(date);
	}
}

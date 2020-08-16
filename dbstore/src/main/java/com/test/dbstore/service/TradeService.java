/**
 * 
 */
package com.test.dbstore.service;

import java.time.LocalDate;

import com.test.dbstore.dom.Trade;
import com.test.dbstore.util.TradeStore;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jitendra
 *
 */
@Slf4j
public class TradeService {
	private final TradeStore store;

	public TradeService(TradeStore store) {
		this.store = store;
	}

	public void store(Trade trade, LocalDate today) {
		if (valid(trade, today)) {
			trade.setCreatedOn(today);
			log.info("Trade is valid, trying to store {}", trade);
			store.put(trade.getId(), trade);
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
		return store.get(tradeId);
	}
}

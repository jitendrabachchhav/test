/**
 * 
 */
package com.test.dbstore.service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.test.dbstore.dom.Trade;
import com.test.dbstore.util.TradeList;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jitendra
 *
 */
@Slf4j
public class TradeService {
	private final Map<LocalDate, TradeList> store;
	private final Map<String, LocalDate> tradeIdToMaturityDateMap;

	public TradeService() {
		this.store = new ConcurrentHashMap<LocalDate, TradeList>();
		this.tradeIdToMaturityDateMap = new ConcurrentHashMap<String, LocalDate>();
	}

	public void store(Trade trade, LocalDate today) {
		if (valid(trade, today)) {
			trade.setCreatedOn(today);
			log.info("Trade is valid, trying to store {}", trade);
			TradeList existingTrades = store.get(trade.getMaturityDate());
			if (existingTrades == null) {
				existingTrades = new TradeList();
			}
			existingTrades.put(trade.getId(), trade);
			store.put(trade.getMaturityDate(), existingTrades);
			tradeIdToMaturityDateMap.put(trade.getId(), trade.getMaturityDate());
		}
	}

	private boolean valid(Trade trade, LocalDate today) {
		if (trade.getMaturityDate().isBefore(today)) {
			log.warn("Validation failed, maturityDate {} for tradeId {} is before today {}", trade.getMaturityDate(),
					trade.getId(), today);
			return false;
		}
		return true;
	}

	public Trade getLatest(String tradeId) {
		LocalDate maturityDate = tradeIdToMaturityDateMap.get(tradeId);
		if (maturityDate != null) {
			TradeList trades = store.get(maturityDate);
			if (trades != null) {
				return trades.get(tradeId);
			}
		}
		return null;
	}

	public void clearStore() {
		this.store.values().parallelStream().forEach(e -> {
			e.clear();
		});
		this.store.clear();
		this.tradeIdToMaturityDateMap.clear();
	}

	public void markTradesExpired(LocalDate date) {
		TradeList trades = store.get(date);
		if (trades != null) {
			trades.values().parallelStream().forEach(t -> {
				t.setExpired(true);
			});
		}
	}
}

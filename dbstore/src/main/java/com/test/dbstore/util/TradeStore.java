/**
 * 
 */
package com.test.dbstore.util;

import java.util.HashMap;
import java.util.Map;

import com.test.dbstore.dom.Trade;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jitendra
 *
 */
@Slf4j
public class TradeStore {
	private final Map<String, Trade> store = new HashMap<String, Trade>();

	public Trade put(String tradeId, Trade newTrade) {
		synchronized (tradeId) {
			Trade currentTrade = store.get(tradeId);
			if (null == currentTrade || currentTrade.getVersion() <= newTrade.getVersion()) {
				return store.put(tradeId, newTrade);
			} else {
				log.error("Incoming trade's version {} is less than the existing trade's version {} for tradeId {}",
						newTrade.getVersion(), currentTrade.getVersion(), newTrade.getId());
				throw new IllegalArgumentException(
						"Incoming trade's version [" + newTrade.getVersion() + "] is less than the existing trade's version ["
								+ currentTrade.getVersion() + "] for tradeId [" + newTrade.getId() + "]");
			}
		}
	}

	public Trade get(String tradeId) {
		return store.get(tradeId);
	}

	public void clear() {
		this.store.clear();
	}
}

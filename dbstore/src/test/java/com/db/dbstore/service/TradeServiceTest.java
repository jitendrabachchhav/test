/**
 * 
 */
package com.db.dbstore.service;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.test.dbstore.dom.Trade;
import com.test.dbstore.service.TradeService;
import com.test.dbstore.util.TradeStore;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jitendra
 *
 */
@Slf4j
public class TradeServiceTest {
	private TradeStore store;
	private TradeService tradeService;

	public TradeServiceTest() {
		this.store = new TradeStore();
		this.tradeService = new TradeService(store);
	}

	@BeforeEach
	public void beforeTest() {
		store.clear();
	}

	@Test
	public void testWhenTradeMaturityIsInPast() {
		log.info("Starting testWhenTradeMaturityIsInPast");
		LocalDate today = LocalDate.of(2020, 8, 15);
		LocalDate maturityDate = LocalDate.of(2020, 8, 14);
		Trade trade1 = Trade.builder().id("T1")
				.counterPartyId("CP1")
				.bookId("B1")
				.expired(false)
				.maturityDate(maturityDate).version(1).build();
		tradeService.store(trade1, today);
		Trade stored = tradeService.getLatest("T1");
		Assertions.assertNull(stored);
	}

	@Test
	public void testWhenTradeIsValid() {
		log.info("Starting testWhenTradeIsValid");
		LocalDate today = LocalDate.of(2020, 8, 15);
		Trade t1 = Trade.builder().id("T1").maturityDate(today).version(1).build();
		tradeService.store(t1, today);
		Trade stored = tradeService.getLatest("T1");
		Assertions.assertEquals(t1, stored);
	}

	@Test
	public void testThatNewerTradeOverrides() {
		log.info("Starting testThatNewerTradeOverrides");
		LocalDate today = LocalDate.of(2020, 8, 15);
		Trade t1 = Trade.builder().id("T1").maturityDate(today).version(1).build();
		tradeService.store(t1, today);
		Trade t2 = Trade.builder().id("T1").maturityDate(today).bookId("B1").version(2).build();
		tradeService.store(t2, today);
		Trade stored = tradeService.getLatest("T1");
		Assertions.assertEquals(t2, stored);
	}

	@Test
	public void testThatOlderVersionThrowsException() {
		log.info("Starting testThatOlderVersionThrowsException");
		LocalDate today = LocalDate.of(2020, 8, 15);
		Trade t1 = Trade.builder().id("T1").maturityDate(today).version(2).build();
		tradeService.store(t1, today);
		Trade t2 = Trade.builder().id("T1").maturityDate(today).bookId("B1").version(1).build();
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			tradeService.store(t2, today);
		});
	}
}

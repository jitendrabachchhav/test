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

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jitendra
 *
 */
@Slf4j
public class TradeServiceTest {
	private TradeService tradeService;

	public TradeServiceTest() {
		this.tradeService = new TradeService();
	}

	@BeforeEach
	public void beforeTest() {
		tradeService.clearStore();
	}

	@Test
	public void testWhenTradeMaturityIsInPast() {
		log.info("Starting testWhenTradeMaturityIsInPast");
		LocalDate today = LocalDate.of(2020, 8, 15);
		LocalDate maturityDate = LocalDate.of(2020, 8, 14);
		Trade trade1 = Trade.builder().id("T1").counterPartyId("CP1").bookId("B1").expired(false)
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

	@Test
	public void testThatTradesAreMarkedExpiredForGivenDateOnly() {
		LocalDate yesterday = LocalDate.of(2020, 8, 14);
		Trade t1 = getTrade("T1", 1, yesterday);
		Trade t2 = getTrade("T2", 1, yesterday);
		Trade t3 = getTrade("T3", 1, yesterday);
		tradeService.store(t1, yesterday);
		tradeService.store(t2, yesterday);
		tradeService.store(t3, yesterday);

		LocalDate today = LocalDate.of(2020, 8, 15);
		Trade t4 = getTrade("T4", 1, today);
		Trade t5 = getTrade("T5", 1, today);
		Trade t6 = getTrade("T6", 1, today);
		tradeService.store(t4, today);
		tradeService.store(t5, today);
		tradeService.store(t6, today);
		
		tradeService.markTradesExpired(yesterday);
		
		Trade storedT1 = tradeService.getLatest("T1");
		Trade storedT2 = tradeService.getLatest("T2");
		Trade storedT3 = tradeService.getLatest("T3");
		Trade storedT4 = tradeService.getLatest("T4");
		Trade storedT5 = tradeService.getLatest("T5");
		Trade storedT6 = tradeService.getLatest("T6");
		
		Assertions.assertTrue(storedT1.isExpired());
		Assertions.assertTrue(storedT2.isExpired());
		Assertions.assertTrue(storedT3.isExpired());
		Assertions.assertFalse(storedT4.isExpired());
		Assertions.assertFalse(storedT5.isExpired());
		Assertions.assertFalse(storedT6.isExpired());
	}

	private Trade getTrade(String tradeId, int version, LocalDate maturityDate) {
		return Trade.builder().id(tradeId).version(version).counterPartyId("CP1").bookId("B1").expired(false)
				.maturityDate(maturityDate).build();
	}
}

/**
 * 
 */
package com.db.dbstore.service.integration;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.test.dbstore.config.ApplicationConfiguration;
import com.test.dbstore.dao.TradeDao;
import com.test.dbstore.dom.Trade;
import com.test.dbstore.service.PersistentTradeService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jitendra
 *
 */
@ContextConfiguration(classes = ApplicationConfiguration.class)
@JdbcTest
@Slf4j
@Transactional
@TestInstance(Lifecycle.PER_CLASS)
public class PersistentTradeServiceIntegrationTest {
	@Autowired
	private TradeDao tradeDao;
	private PersistentTradeService tradeService;

	@BeforeEach
	public void beforeEachTest() {
		tradeDao.clear();
	}

	@BeforeAll
	public void beforeTest() {
		this.tradeService = new PersistentTradeService(tradeDao);
	}

	@Test
	public void testStoreTradeSucceeds() {
		log.info("Starting testStoreTradeSucceeds");
		LocalDate today = LocalDate.of(2020, 8, 15);
		Trade t1 = getTrade("T1", 1, today, today);

		tradeService.store(t1, today);
		Trade dbTrade = tradeService.getLatest("T1");
		Assertions.assertEquals(t1, dbTrade);
	}

	@Test
	public void testStoreTradeSucceedsWhenLaterVersionIsProcessed() {
		log.info("Starting testStoreTradeSucceedsWhenLaterVersionIsProcessed");
		LocalDate today = LocalDate.of(2020, 8, 15);
		Trade t1 = getTrade("T1", 1, today, today);
		Trade t2 = getTrade("T1", 2, today, today);

		tradeService.store(t1, today);
		Trade dbTrade = tradeService.getLatest("T1");
		Assertions.assertEquals(t1, dbTrade);

		tradeService.store(t2, today);
		dbTrade = tradeService.getLatest("T1");
		Assertions.assertEquals(t2, dbTrade);
	}

	@Test
	public void testExceptionThrownWhenOlderVersionIsProcessed() {
		log.info("Starting testExceptionThrownWhenOlderVersionIsProcessed");
		LocalDate today = LocalDate.of(2020, 8, 15);
		Trade t1 = getTrade("T1", 2, today, today);
		Trade t2 = getTrade("T1", 1, today, today);

		tradeService.store(t1, today);
		Trade dbTrade = tradeService.getLatest("T1");
		Assertions.assertEquals(t1, dbTrade);

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			tradeService.store(t2, today);
		});
		dbTrade = tradeService.getLatest("T1");
		Assertions.assertEquals(t1, dbTrade);
	}

	@Test
	public void testStoreSucceedsWhenSameVersionIsProcessed() {
		log.info("Starting testStoreSucceedsWhenSameVersionIsProcessed");
		LocalDate today = LocalDate.of(2020, 8, 15);
		Trade t1 = getTrade("T1", 2, today, today);
		Trade t2 = getTrade("T1", 2, today, today);

		tradeService.store(t1, today);
		Trade dbTrade = tradeService.getLatest("T1");
		Assertions.assertEquals(t1, dbTrade);

		t2.setBookId("B2");
		tradeService.store(t2, today);
		dbTrade = tradeService.getLatest("T1");
		Assertions.assertEquals(t2, dbTrade);
	}

	@Test
	public void testTradeNotStoredWhenMaturityDateInPast() {
		log.info("Starting testTradeNotStoredWhenMaturityDateInPast");
		LocalDate today = LocalDate.of(2020, 8, 15);
		LocalDate maturityDate = LocalDate.of(2020, 8, 14);
		Trade t1 = getTrade("T1", 2, maturityDate, today);

		tradeService.store(t1, today);
		Trade dbTrade = tradeService.getLatest("T1");
		Assertions.assertNull(dbTrade);
	}

	@Test
	public void testWhenExpiredFlagIsUpdated() {
		log.info("Starting testWhenExpiredFlagIsUpdated");
		LocalDate today = LocalDate.of(2020, 8, 15);
		LocalDate yesterday = LocalDate.of(2020, 8, 14);
		Trade t1 = getTrade("T1", 1, yesterday, yesterday);
		Trade t2 = getTrade("T1", 2, today, today);

		tradeService.store(t1, yesterday);
		tradeService.store(t2, today);
		Trade dbTrade = tradeService.getLatest("T1");
		Assertions.assertEquals(t2, dbTrade);
		tradeService.markTradesExpired(yesterday);
		tradeService.markTradesExpired(today);
		dbTrade = tradeService.getLatest("T1");
		Assertions.assertTrue(dbTrade.isExpired());
	}

	private Trade getTrade(String tradeId, int version, LocalDate maturityDate, LocalDate createdOn) {
		return Trade.builder().id(tradeId).version(version).counterPartyId("CP1").bookId("B1").expired(false)
				.maturityDate(maturityDate).createdOn(maturityDate).build();
	}
}

/**
 * 
 */
package com.test.dbstore.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.test.dbstore.dom.Trade;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jitendra
 *
 */
@Slf4j
public final class TradeDao {
	private static final String INSERT_QUERY = "INSERT INTO trade (id, version, counterPartyId, bookId, maturityDate, createdOn, expired) values (?, ?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_QUERY = "UPDATE trade set counterPartyId = ?, bookId = ?, maturityDate = ?, createdOn = ?, expired = ? where id = ? and version = ?";
	private static final String SELECT_MAX_VERSION_QUERY = "SELECT version FROM trade WHERE id=? ORDER BY version desc FOR UPDATE";
	private static final String SELECT_QUERY = "SELECT * FROM trade WHERE id=? ORDER BY version DESC";
	private static final String TRUNCATE_QUERY = "TRUNCATE TABLE trade";
	private static final String UPDATE_EXPIRED_QUERY = "UPDATE trade SET expired=TRUE WHERE createdOn=?";
	private final JdbcTemplate jdbcTemplate;

	public TradeDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void upsert(Trade trade) {
		int maxVersion = jdbcTemplate.query(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(SELECT_MAX_VERSION_QUERY);
				ps.setString(1, trade.getId());
				return ps;
			}
		}, new ResultSetExtractor<Integer>() {
			@Override
			public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
				if (rs.next()) {
					return rs.getInt(1);
				}
				return 0;
			}
		});

		if (maxVersion == 0 || maxVersion < trade.getVersion()) {
			// insert trade when no row exists or lower version exists
			jdbcTemplate.update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
					PreparedStatement ps = con.prepareStatement(INSERT_QUERY);
					ps.setString(1, trade.getId());
					ps.setInt(2, trade.getVersion());
					ps.setString(3, trade.getCounterPartyId());
					ps.setString(4, trade.getBookId());
					ps.setDate(5, Date.valueOf(trade.getMaturityDate()));
					ps.setDate(6, Date.valueOf(trade.getCreatedOn()));
					ps.setBoolean(7, trade.isExpired());
					return ps;
				}
			});
		} else if (maxVersion == trade.getVersion()) {
			// update when same version exists
			jdbcTemplate.update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
					PreparedStatement ps = con.prepareStatement(UPDATE_QUERY);
					ps.setString(1, trade.getCounterPartyId());
					ps.setString(2, trade.getBookId());
					ps.setDate(3, Date.valueOf(trade.getMaturityDate()));
					ps.setDate(4, Date.valueOf(trade.getCreatedOn()));
					ps.setBoolean(5, trade.isExpired());
					ps.setString(6, trade.getId());
					ps.setInt(7, trade.getVersion());
					return ps;
				}
			});
		} else {
			// older version, throw exception
			log.error("Incoming trade's version {} is less than the existing trade's version {} for tradeId {}",
					trade.getVersion(), maxVersion, trade.getId());
			throw new IllegalArgumentException(
					"Incoming trade's version [" + trade.getVersion() + "] is less than the existing trade's version ["
							+ maxVersion + "] for tradeId [" + trade.getId() + "]");
		}
	}

	public Trade getLatest(String tradeId) {
		return jdbcTemplate.query(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(SELECT_QUERY);
				ps.setString(1, tradeId);
				return ps;
			}
		}, new ResultSetExtractor<Trade>() {
			@Override
			public Trade extractData(ResultSet rs) throws SQLException, DataAccessException {
				if (rs.next()) {
					return Trade.builder().id(tradeId).version(rs.getInt("version"))
							.counterPartyId(rs.getString("counterPartyId"))
							.maturityDate(rs.getDate("maturityDate").toLocalDate())
							.createdOn(rs.getDate("createdOn").toLocalDate()).expired(rs.getBoolean("expired"))
							.bookId(rs.getString("bookId")).build();
				}
				return null;
			}
		});
	}

	public void clear() {
		jdbcTemplate.update(TRUNCATE_QUERY);
	}

	public void updateExpired(LocalDate date) {
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(UPDATE_EXPIRED_QUERY);
				ps.setDate(1, Date.valueOf(date));
				return ps;
			}
		});
	}
}

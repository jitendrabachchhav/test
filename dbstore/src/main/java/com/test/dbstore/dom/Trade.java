/**
 * 
 */
package com.test.dbstore.dom;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

/**
 * @author Jitendra
 *
 */
@Data
@Builder
public class Trade {
	private final String id;
	private final int version;
	private String counterPartyId;
	private String bookId;
	private LocalDate maturityDate;
	private LocalDate createdOn;
	private boolean expired;
}

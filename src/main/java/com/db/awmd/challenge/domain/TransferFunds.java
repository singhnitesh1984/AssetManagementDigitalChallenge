package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * @author Nitesh Singh
 * TransferFunds holds the fields getting utilized in Funds Transfer Functionality.
 */
@Data
public class TransferFunds {
	
	@NotNull
	@NotEmpty
	private final String fromAcctId;

	@NotNull
	@NotEmpty
	private final String toAcctId;
	
	@NotNull
	@Min(value = 0, message = "Initial balance must be positive.")
	private BigDecimal transferAmt;

	public BigDecimal getTransferAmt() {
		return transferAmt;
	}

	public void setTransferAmt(BigDecimal transferAmt) {
		this.transferAmt = transferAmt;
	}

	public String getFromAcctId() {
		return fromAcctId;
	}

	public String getToAcctId() {
		return toAcctId;
	}

	public TransferFunds(String fromAccountId, String toAccountId) {
		this.fromAcctId = fromAccountId;
		this.toAcctId = toAccountId;
		this.transferAmt = BigDecimal.ZERO;
	}

	@JsonCreator
	public TransferFunds(@JsonProperty("fromAcctId") String fromAcctId, @JsonProperty("toAcctId") String toAcctId, @JsonProperty("transferAmt") BigDecimal transferAmt) {
		this.fromAcctId = fromAcctId;
		this.toAcctId = toAcctId;
		this.transferAmt = transferAmt;
	}
}

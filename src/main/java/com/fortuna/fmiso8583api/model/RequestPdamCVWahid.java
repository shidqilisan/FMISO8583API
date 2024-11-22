package com.fortuna.fmiso8583api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestPdamCVWahid {

	@JsonProperty("BILLING_ID")
	public String billingId;

	@JsonProperty("AMOUNT")
	public String amount;
	
	@JsonProperty("BIT62")
	public String bit62;
	
	@JsonProperty("BIT47")
	public String bit47;
	
	@JsonProperty("BIT37")
	public String bit37;
	
	public String getBit37() {
		return bit37;
	}

	public void setBit37(String bit37) {
		this.bit37 = bit37;
	}

	@JsonProperty("KD_CA")
	public String kdCA;
	

	public String getBit47() {
		return bit47;
	}

	public void setBit47(String bit47) {
		this.bit47 = bit47;
	}

	public String getKdCA() {
		return kdCA;
	}

	public void setKdCA(String kdCA) {
		this.kdCA = kdCA;
	}

	public String getBillingId() {
		return billingId;
	}

	public void setBillingId(String billingId) {
		this.billingId = billingId;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getBit62() {
		return bit62;
	}

	public void setBit62(String bit62) {
		this.bit62 = bit62;
	}
}

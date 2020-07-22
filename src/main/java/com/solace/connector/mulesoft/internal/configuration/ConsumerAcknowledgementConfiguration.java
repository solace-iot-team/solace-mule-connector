package com.solace.connector.mulesoft.internal.configuration;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import com.solace.connector.mulesoft.api.SolaceAckMode;

public class ConsumerAcknowledgementConfiguration {
	@DisplayName("Message Acknowledgment Mode")
	@Parameter
	@Optional(defaultValue = "AUTOMATIC_IMMEDIATE")
	@Summary("AUTOMATIC_ON_FLOW_COMPLETION applies to guaranteed message listener only. For consume operations AUTOMATIC_ON_FLOW_COMPLETION requires manual ack like in the case of  MANUAL_CLIENT ")
	@Placement(order = 1, tab = SolaceConfiguration.CONSUMER_TAB_NAME)
	private SolaceAckMode ackMode;

	@DisplayName("Window Size")
	@Parameter
	@Optional(defaultValue = "255")
	@Placement(order = 2, tab = SolaceConfiguration.CONSUMER_TAB_NAME)
	private int guaranteedMessageWindowSize;

	@DisplayName("Message Acknowledgment Threshold")
	@Parameter
	@Optional(defaultValue = "60")
	@Placement(order = 3, tab = SolaceConfiguration.CONSUMER_TAB_NAME)
	private int messageAcknowledgmentThreshold;

	@DisplayName("Message Acknowledgment Time (milliseconds)")
	@Parameter
	@Optional(defaultValue = "1000")
	@Placement(order = 4, tab = SolaceConfiguration.CONSUMER_TAB_NAME)
	private int messageAcknowledgmentTime;

	public int getGuaranteedMessageWindowSize() {
		return guaranteedMessageWindowSize;
	}

	public void setGuaranteedMessageWindowSize(int guaranteedMessageWindowSize) {
		this.guaranteedMessageWindowSize = guaranteedMessageWindowSize;
	}

	public int getMessageAcknowledgmentThreshold() {
		return messageAcknowledgmentThreshold;
	}

	public void setMessageAcknowledgmentThreshold(int messageAcknowledgmentThreshold) {
		this.messageAcknowledgmentThreshold = messageAcknowledgmentThreshold;
	}

	public int getMessageAcknowledgmentTime() {
		return messageAcknowledgmentTime;
	}

	public void setMessageAcknowledgmentTime(int messageAcknowledgmentTime) {
		this.messageAcknowledgmentTime = messageAcknowledgmentTime;
	}

	public SolaceAckMode getAckMode() {
		return ackMode;
	}

	public void setAckMode(SolaceAckMode ackMode) {
		this.ackMode = ackMode;
	}

}

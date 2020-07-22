package com.solace.connector.mulesoft.internal.configuration;

import java.util.Map;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import com.solacesystems.jcsmp.JCSMPProperties;

import java.util.HashMap;



public class SessionProperties {

	private static final String SESSION_TAB = "Advanced";
	
	@DisplayName("Publisher ACK Window Size")
	@Parameter
	@Optional(defaultValue = "1")
	@Placement(order = 4, tab = SESSION_TAB)
	private int publisherAckWindowSize;

	@DisplayName("Publisher ACK Time")
	@Parameter
	@Optional(defaultValue = "2000")
	@Placement(order = 5, tab = SESSION_TAB)
	private int publisherAckTime;
	
	@DisplayName("Generate Receive Timestamps")
	@Parameter
	@Optional(defaultValue = "false")
	@Placement(order = 6, tab = SESSION_TAB)
	private boolean generateReceiveTimestamps;

	@DisplayName("Generate Send Timestamps")
	@Parameter
	@Optional(defaultValue = "false")
	@Placement(order = 7, tab = SESSION_TAB)
	private boolean generateSendTimestamps;

	@DisplayName("Generate Sequence Numbers")
	@Parameter
	@Optional(defaultValue = "false")
	@Placement(order = 8, tab = SESSION_TAB)
	private boolean generateGenerateSequenceNumbers;

	public int getPublisherAckWindowSize() {
		return publisherAckWindowSize;
	}
	public void setPublisherAckWindowSize(int publisherAckWindowSize) {
		this.publisherAckWindowSize = publisherAckWindowSize;
	}
	public int getPublisherAckTime() {
		return publisherAckTime;
	}
	public void setPublisherAckTime(int publisherAckTime) {
		this.publisherAckTime = publisherAckTime;
	}
	public boolean isGenerateReceiveTimestamps() {
		return generateReceiveTimestamps;
	}
	public void setGenerateReceiveTimestamps(boolean generateReceiveTimestamps) {
		this.generateReceiveTimestamps = generateReceiveTimestamps;
	}
	public boolean isGenerateSendTimestamps() {
		return generateSendTimestamps;
	}
	public void setGenerateSendTimestamps(boolean generateSendTimestamps) {
		this.generateSendTimestamps = generateSendTimestamps;
	}
	public boolean isGenerateGenerateSequenceNumbers() {
		return generateGenerateSequenceNumbers;
	}
	public void setGenerateGenerateSequenceNumbers(boolean generateGenerateSequenceNumbers) {
		this.generateGenerateSequenceNumbers = generateGenerateSequenceNumbers;
	}
	
	public Map<String, Object> toJCSMPProperties (){
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(JCSMPProperties.GENERATE_RCV_TIMESTAMPS, this.generateReceiveTimestamps);
		props.put(JCSMPProperties.GENERATE_SEND_TIMESTAMPS, this.generateSendTimestamps);
		props.put(JCSMPProperties.GENERATE_SEQUENCE_NUMBERS, this.generateGenerateSequenceNumbers);
				
		props.put(JCSMPProperties.PUB_ACK_TIME, this.publisherAckTime);
		props.put(JCSMPProperties.PUB_ACK_WINDOW_SIZE, this.publisherAckWindowSize);
		
		return props;
	}
}

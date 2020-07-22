package com.solace.connector.mulesoft.internal.configuration;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import com.solacesystems.jcsmp.JCSMPChannelProperties;

public class ClientChannelProperties {
	
	private static final String CLIENT_CHANNEL_TAB = "Advanced";
	
	@DisplayName("Keep Alive Interval (Milliseconds)")
	@Parameter
	@Optional(defaultValue = "3000")
	@Placement(order = 1, tab = CLIENT_CHANNEL_TAB)
	private int keepAliveIntervalInMillis;
	
	@DisplayName("Keep Alive Limit")
	@Parameter
	@Optional(defaultValue = "10")
	@Placement(order = 1, tab = CLIENT_CHANNEL_TAB)
	private int keepAliveLimit;
	
	@DisplayName("Send Buffer")
	@Parameter
	@Optional(defaultValue = "65536")
	@Placement(order = 1, tab = CLIENT_CHANNEL_TAB)
	private int sendBuffer;

	@DisplayName("Receive Buffer")
	@Parameter
	@Optional(defaultValue = "65536")
	@Placement(order = 4, tab = CLIENT_CHANNEL_TAB)
	private int receiveBuffer;

	@DisplayName("TCP NoDelay")
	@Parameter
	@Optional(defaultValue = "true")
	@Placement(order = 5, tab = CLIENT_CHANNEL_TAB)
	private boolean tcpNoDelay;
	
	@DisplayName("Compression Level")
	@Parameter
	@Optional(defaultValue = "0")
	@Placement(order = 6, tab = CLIENT_CHANNEL_TAB)
	private int compressionLevel;
	
	public JCSMPChannelProperties populateJCSMPChannelProperties(JCSMPChannelProperties props) {
		props.setCompressionLevel(compressionLevel);
		props.setTcpNoDelay(tcpNoDelay);
		props.setReceiveBuffer(receiveBuffer);
		props.setSendBuffer(sendBuffer);
		props.setKeepAliveLimit(keepAliveLimit);
		props.setKeepAliveIntervalInMillis(keepAliveIntervalInMillis);
		// rely on Mule (re)connection retry. try initial connection multiple times in case broker is just starting for unit tests
		props.setConnectRetries(2);
		props.setReconnectRetries(0);
		props.setReconnectRetryWaitInMillis(1000);
		return props;
	}
	
}

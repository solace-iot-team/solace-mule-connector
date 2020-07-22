package com.solace.connector.mulesoft.internal.configuration;

import java.util.concurrent.TimeUnit;

import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

public class RetryConfiguration {
	@DisplayName("Reconnect Retry Count")
	@Parameter
	@Optional(defaultValue = "-1")
	@Placement(order = 1, tab = SolaceConfiguration.CONSUMER_TAB_NAME)
	private int reconnectRetryCount;

	@DisplayName("Reconnect Retry Interval")
	@Parameter
	@Optional(defaultValue = "-1")
	@Placement(order = 2, tab = SolaceConfiguration.CONSUMER_TAB_NAME)
	private int reconnectRetryInterval;
	
	/**
	 * the time unit for the timeout value
	 */
	@Parameter
	@DisplayName("Reconnect Retry Time Unit")
	@Alias("timeUnit")
	@Placement(order = 3, tab = SolaceConfiguration.CONSUMER_TAB_NAME)
	@Optional(defaultValue = "MILLISECONDS")
	private TimeUnit timeUnit;

	

	public int getReconnectRetryCount() {
		return reconnectRetryCount;
	}

	public void setReconnectRetryCount(int reconnectRetryCount) {
		this.reconnectRetryCount = reconnectRetryCount;
	}

	public int getReconnectRetryInterval() {
		return reconnectRetryInterval;
	}

	public void setReconnectRetryInterval(int reconnectRetryInterval) {
		this.reconnectRetryInterval = reconnectRetryInterval;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

}

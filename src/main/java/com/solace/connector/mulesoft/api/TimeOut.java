package com.solace.connector.mulesoft.api;

import java.util.concurrent.TimeUnit;

import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
/**
 * Encapsulates the time out configuration for request-reply, consume and similar operations
 * @author swenhelge
 *
 */
public class TimeOut {
	/**
	 * the timeout value
	 */
	@Parameter
	@Alias("timeOut")
	@Optional(defaultValue = "1000")
	private int timeOut;
	
	/**
	 * the time unit for the timeout value
	 */
	@Parameter
	@Alias("timeUnit")
	@Optional(defaultValue = "MILLISECONDS")
	private TimeUnit timeUnit;

	public int getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}
	/**
	 * Calculates the timeout in milliseconds from time out value and time unit provided.
	 * @return time out interval in milliseconds
	 */
	public int getMillis() {
		return (int) this.getTimeUnit().toMillis(this.getTimeOut());
	}

}

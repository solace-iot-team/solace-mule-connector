package com.solace.connector.mulesoft.internal.configuration;

import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import com.solace.connector.mulesoft.api.SolaceAccessType;
import com.solace.connector.mulesoft.internal.SolaceOperations;
import com.solace.connector.mulesoft.internal.connection.SolaceConnectionProvider;
import com.solace.connector.mulesoft.internal.source.SolaceConsumerSource;
import com.solace.connector.mulesoft.internal.source.SolaceListenerSource;

/**
 * Configuration that is applicable across all operations. We use this to
 * provide defaults in case optional parameters are not provided. This class
 * also bootstraps operations and sources. 
 */
@Operations(SolaceOperations.class)
@Sources({ SolaceListenerSource.class, SolaceConsumerSource.class })
@ConnectionProviders(SolaceConnectionProvider.class)
public class SolaceConfiguration {
	public static final String CONSUMER_TAB_NAME = "Endpoint Consumer";

	@ParameterGroup(name = "Consumer Acknowledgement Configuration")
	ConsumerAcknowledgementConfiguration consumerAcknowledgementConfiguration;

	@ParameterGroup(name = "Flow Reconnect Policy")
	RetryConfiguration retryConfiguration;

	@DisplayName("Queue Access Type")
	@Parameter
	@Optional(defaultValue = "EXCLUSIVE")
	@Placement(order = 100, tab = CONSUMER_TAB_NAME)
	@Summary("Access type for automatically provisioned queues")
	private SolaceAccessType accessType;

	public SolaceAccessType getAccessType() {
		return accessType;
	}

	public void setAccessType(SolaceAccessType accessType) {
		this.accessType = accessType;
	}

	public ConsumerAcknowledgementConfiguration getConsumerAcknowledgementConfiguration() {
		return consumerAcknowledgementConfiguration;
	}

	public void setConsumerAcknowledgementConfiguration(
			ConsumerAcknowledgementConfiguration consumerAcknowledgementConfiguration) {
		this.consumerAcknowledgementConfiguration = consumerAcknowledgementConfiguration;
	}

	public RetryConfiguration getRetryConfiguration() {
		return retryConfiguration;
	}

	public void setRetryConfiguration(RetryConfiguration retryConfiguration) {
		this.retryConfiguration = retryConfiguration;
	}

}

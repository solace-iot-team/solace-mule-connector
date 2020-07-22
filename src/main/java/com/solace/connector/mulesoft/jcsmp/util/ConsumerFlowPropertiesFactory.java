package com.solace.connector.mulesoft.jcsmp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.connector.mulesoft.api.SolaceAckMode;
import com.solace.connector.mulesoft.internal.configuration.ConsumerAcknowledgementConfiguration;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.Endpoint;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;

public class ConsumerFlowPropertiesFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerFlowPropertiesFactory.class);

	public static class Builder {

		private Endpoint endpoint;
		private String selector;
		private String subscription;
		private String ackMode;
		private int ackThreshold = -1;
		private int ackTime = -1;
		private int transportWindowSize = -1;

		public Builder(Endpoint endpoint) {
			assert (endpoint != null);
			this.endpoint = endpoint;
		}

		public Builder withSelector(String selector) {
			this.selector = selector;
			return this;
		}

		public Builder withAckMode(String ackMode) {
			this.ackMode = ackMode;
			return this;
		}

		public Builder withAckThreshold(int threshold) {
			this.ackThreshold = threshold;
			return this;
		}

		public Builder withAckTime(int timer) {
			this.ackTime = timer;
			return this;
		}

		public Builder withTransportWindowSize(int windowSize) {
			this.transportWindowSize = windowSize;
			return this;
		}
		
		public Builder withSubscription(String subscription) {
			this.subscription = subscription;
			return this;
		}
		
		public Builder withConsumerAcknowledgementConfiguration(ConsumerAcknowledgementConfiguration consumerAckConfiguration) {
			assert(consumerAckConfiguration!=null);
			this.ackMode = SolaceAckMode.toSolaceConstant(consumerAckConfiguration.getAckMode());
			this.ackThreshold = consumerAckConfiguration.getMessageAcknowledgmentThreshold();
			this.ackTime = consumerAckConfiguration.getMessageAcknowledgmentTime();
			return this;
		}

		public ConsumerFlowProperties build() {
			final ConsumerFlowProperties flowProps = new ConsumerFlowProperties();
			flowProps.setEndpoint(endpoint);
			if (this.ackMode == null || this.ackMode.isEmpty()) {
				flowProps.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_AUTO);
			} else {
				flowProps.setAckMode(this.ackMode);
			}
			if (this.selector != null && !this.selector.isEmpty()) {
				flowProps.setSelector(this.selector);
			}
			if (ackThreshold > -1)
				flowProps.setAckThreshold(ackThreshold);
			if (ackTime > -1)
				flowProps.setAckTimerInMsecs(ackTime);

			if (transportWindowSize > -1)
				flowProps.setTransportWindowSize(transportWindowSize);
			if (this.subscription!= null && !this.subscription.isEmpty()) {
				flowProps.setNewSubscription(JCSMPFactory.onlyInstance().createTopic(this.subscription));
			}

			LOGGER.debug(
					String.format("Built consumer properties for endpoint [%s] with ackmode=[%s] and selector [%s] ",
							endpoint.getName(), ackMode, selector));
			return flowProps;

		}
	}

}

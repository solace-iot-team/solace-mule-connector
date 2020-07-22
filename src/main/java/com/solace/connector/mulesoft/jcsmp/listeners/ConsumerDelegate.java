package com.solace.connector.mulesoft.jcsmp.listeners;

import java.util.List;

import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.connector.mulesoft.api.SolaceConnectorError;
import com.solace.connector.mulesoft.api.SolaceEndpointType;
import com.solace.connector.mulesoft.internal.connection.ConsumerCache;
import com.solace.connector.mulesoft.jcsmp.util.ConsumerFlowPropertiesFactory;
import com.solace.connector.mulesoft.jcsmp.util.EndpointPropertiesFactory;
import com.solace.connector.mulesoft.jcsmp.util.EndpointUtil;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.Endpoint;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.XMLMessageListener;

public class ConsumerDelegate implements ConnectionListenerDelegate {
	private final Logger LOGGER = LoggerFactory.getLogger(ConsumerDelegate.class);

	private ConsumerCache consumerCache;

	private JCSMPSession session;
	private XMLMessageListener listener;

	private ConnectionListenerProperties properties;
	private FlowReceiver flowReceiver;

	private SolaceEndpointType endpointType = SolaceEndpointType.QUEUE;
	private boolean provisionEndpoint = false;
	private String selector;

	private int accessType;
	private String ackMode;

	@Override
	public void listen(List<String> subscriptions) {
		assert (subscriptions.size() == 1);
		String endpointName = subscriptions.get(0);

		final Endpoint endpoint = EndpointUtil.mapToGuaranteedConsumerEndpoint(endpointType, endpointName);
		final ConsumerFlowProperties flowProps = new ConsumerFlowPropertiesFactory.Builder(endpoint)
				.withAckMode(this.ackMode).withSelector(this.selector)
				.withAckThreshold((int) properties.get(ConnectionListenerProperties.ACK_THRESHOLD))
				.withAckTime((int) this.properties.get(ConnectionListenerProperties.ACK_TIMER))
				.withTransportWindowSize((int) this.properties.get(ConnectionListenerProperties.TRANSPORT_WINDOW_SIZE))
				.build();
		final EndpointProperties endpointProps = new EndpointPropertiesFactory.Builder().withAccessType(this.accessType)
				.build();
		try {
			EndpointUtil.prepareEndpoint(session, endpoint, endpointProps, flowProps, this.endpointType,
					(String) properties.get(ConnectionListenerProperties.SUBSCRIPTION),
					provisionEndpoint && !this.consumerCache.hasEndpoint(endpointName));
			this.consumerCache.putEndpoint(endpointName);
		} catch (JCSMPException e) {
			ModuleException generic = new ModuleException(SolaceConnectorError.GENERIC_ERROR, e);
			LOGGER.error(String.format("Error provisioning endpoint [%s]", endpointName));
			throw generic;
		}

		try {
			flowReceiver = session.createFlow(listener, flowProps);
		} catch (JCSMPException e) {
			ModuleException generic = new ModuleException(SolaceConnectorError.GENERIC_ERROR, e);
			LOGGER.error(String.format("Error creating flow on [%s]", endpointName));
			throw generic;
		}
		try {
			flowReceiver.start();
		} catch (JCSMPException e) {
			ModuleException generic = new ModuleException(SolaceConnectorError.GENERIC_ERROR, e);
			LOGGER.error(String.format("Error starting flow on [%s]", endpointName));
			throw generic;
		}
	}

	@Override
	public void stop() {
		if (flowReceiver != null) {
			flowReceiver.stop();
			flowReceiver.close();
		}
	}

	@Override
	public void init(JCSMPSession session, XMLMessageListener listener, ConnectionListenerProperties properties,
			ConsumerCache consumerCache) {
		assert (properties != null);
		this.consumerCache = consumerCache;
		this.session = session;
		this.listener = listener;
		this.properties = properties;
		Object provisionEndpoint = properties.get(ConnectionListenerProperties.PROVISION_ENDPOINT);
		this.provisionEndpoint = (provisionEndpoint != null) ? (boolean) provisionEndpoint : false;
		Object endpointType = properties.get(ConnectionListenerProperties.ENDPOINT_TYPE);
		this.endpointType = (endpointType != null) ? (SolaceEndpointType) endpointType : SolaceEndpointType.QUEUE;
		this.accessType = properties.containsKey(ConnectionListenerProperties.QUEUE_ACCESS_TYPE)
				? (int) properties.get(ConnectionListenerProperties.QUEUE_ACCESS_TYPE)
				: EndpointProperties.ACCESSTYPE_EXCLUSIVE;
		this.ackMode = properties.containsKey(ConnectionListenerProperties.ACK_MODE)
				? (String) properties.get(ConnectionListenerProperties.ACK_MODE)
				: JCSMPProperties.SUPPORTED_MESSAGE_ACK_AUTO;
		this.selector = properties.containsKey(ConnectionListenerProperties.SELECTOR)
				&& !((String) properties.get(ConnectionListenerProperties.SELECTOR)).isEmpty()
						? (String) properties.get(ConnectionListenerProperties.SELECTOR)
						: null;
		LOGGER.debug(String.format("Consumer is configured with these properties %s", properties));
	}

}

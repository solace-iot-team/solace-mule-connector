package com.solace.connector.mulesoft.jcsmp.util;

import java.util.Iterator;
import java.util.Set;

import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.connector.mulesoft.api.SolaceConnectorError;
import com.solace.connector.mulesoft.api.SolaceEndpointType;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.Destination;
import com.solacesystems.jcsmp.Endpoint;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.JCSMPErrorResponseException;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Subscription;
import com.solacesystems.jcsmp.Topic;

public class EndpointUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(EndpointUtil.class);

	public static Endpoint mapToGuaranteedConsumerEndpoint(SolaceEndpointType endpointType, String endpointName) {
		final Endpoint endpoint;
		if (endpointType.equals(SolaceEndpointType.QUEUE)) {
			endpoint = JCSMPFactory.onlyInstance().createQueue(endpointName);
		} else {
			endpoint = JCSMPFactory.onlyInstance().createDurableTopicEndpointEx(endpointName);
		}
		return endpoint;
	}

	public static Destination mapToGuaranteedProducerDestination(SolaceEndpointType endpointType, String endpointName) {
		final Destination destination;
		if (endpointType.equals(SolaceEndpointType.QUEUE)) {
			destination = JCSMPFactory.onlyInstance().createQueue(endpointName);
		} else {
			destination = JCSMPFactory.onlyInstance().createTopic(endpointName);
		}
		return destination;
	}

	public static void provisionQueue(final JCSMPSession session, final Endpoint endpoint,
			final EndpointProperties endpointProps, SolaceEndpointType endpointType) throws JCSMPException {
		prepareEndpoint(session, endpoint, endpointProps, null, endpointType, null, true);
	}

	public static void prepareEndpoint(final JCSMPSession session, final Endpoint endpoint,
			final EndpointProperties endpointProps, final ConsumerFlowProperties flowProps,
			SolaceEndpointType endpointType, String subscription, boolean provision) throws JCSMPException {
		boolean subscriptionIsValid = subscription != null && !subscription.isEmpty();
		if (endpointType.equals(SolaceEndpointType.TOPIC) && !subscriptionIsValid) {
			String msg = String.format("Attempting to provision Topic Endpoint [%s] but missing a subscription",
					endpoint.getName());
			LOGGER.error(msg);
			ModuleException generic = new ModuleException(msg, SolaceConnectorError.GENERIC_ERROR);
			throw generic;
		}
		if (provision)
			session.provision(endpoint, endpointProps, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
		// require a subscription for topic endpoint
		if (subscriptionIsValid) {
			Topic topic = JCSMPFactory.onlyInstance().createTopic(subscription);
			if (endpointType.equals(SolaceEndpointType.TOPIC)) {
				flowProps.setNewSubscription(topic);
			} else if (provision) {
				try {
					session.addSubscription(endpoint, topic, JCSMPSession.WAIT_FOR_CONFIRM);
				} catch (JCSMPErrorResponseException e) {
					if (!e.getMessage().contains("Subscription Already Exists")) {
						throw e;
					} else {
						LOGGER.info(String.format("Subscription [%s] already exists on [%s]", subscription, endpoint.getName()));
					}
				}
			}
		}
	}

}

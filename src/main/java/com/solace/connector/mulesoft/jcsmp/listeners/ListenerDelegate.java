package com.solace.connector.mulesoft.jcsmp.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.connector.mulesoft.api.SolaceConnectorError;
import com.solace.connector.mulesoft.internal.connection.ConsumerCache;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;

public class ListenerDelegate implements ConnectionListenerDelegate {
	
	private final Logger LOGGER = LoggerFactory.getLogger(ListenerDelegate.class);
	
	private JCSMPSession session;
	private XMLMessageListener listener;

	private XMLMessageConsumer messageConsumer;
	private List<Topic> subscriptions = new ArrayList<>();

	@Override
	public void listen(List<String> subscriptions) {
		try {
			messageConsumer = session.getMessageConsumer(listener);
		} catch (JCSMPException e) {
			ModuleException generic = new ModuleException(SolaceConnectorError.GENERIC_ERROR, e);
			LOGGER.error("Error creating message consumer", e);
			throw generic;
		}
		Topic topic = null;
		Iterator<String> subscriptionsIterator = subscriptions.iterator();
		while (subscriptionsIterator.hasNext()) {
			topic = JCSMPFactory.onlyInstance().createTopic(subscriptionsIterator.next());
			this.subscriptions.add(topic);
			try {
				session.addSubscription(topic);
			} catch (JCSMPException e) {
				ModuleException generic = new ModuleException(SolaceConnectorError.GENERIC_ERROR, e);
				LOGGER.error(String.format("Error subscribing to  [%s]", topic));
				throw generic;
			}
		}

		try {
			messageConsumer.start();
		} catch (JCSMPException e) {
			ModuleException generic = new ModuleException(SolaceConnectorError.GENERIC_ERROR, e);
			LOGGER.error("Error starting message consumer", e);
			throw generic;
		}
	}

	@Override
	public void stop() {
		if (session.isClosed())
			return;
		Topic topic = null;
		Iterator<Topic> topics = this.subscriptions.iterator();
		while (topics.hasNext()) {
			try {
				session.removeSubscription(topic);
			} catch (JCSMPException e) {
				ModuleException generic = new ModuleException(SolaceConnectorError.GENERIC_ERROR, e);
				LOGGER.error("Error starting message consumer", e);
				throw generic;
			}
		}
		this.subscriptions.clear();
		if (messageConsumer != null)
			messageConsumer.stop();
	}

	@Override
	public void init(JCSMPSession session, XMLMessageListener listener, ConnectionListenerProperties properties, ConsumerCache consumerCache) {
		this.session = session;
		this.listener = listener;
	}

}

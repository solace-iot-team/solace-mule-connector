package com.solace.connector.mulesoft.jcsmp.listeners;

import java.util.List;

import com.solace.connector.mulesoft.internal.connection.ConsumerCache;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.XMLMessageListener;

public interface ConnectionListenerDelegate {
	
	public void init(JCSMPSession session, XMLMessageListener listener, ConnectionListenerProperties properties, ConsumerCache consumerCache);
	
	public void listen(List<String> subscriptions );
	
	public void stop();
}

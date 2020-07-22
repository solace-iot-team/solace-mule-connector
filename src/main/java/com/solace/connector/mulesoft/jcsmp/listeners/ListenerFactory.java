package com.solace.connector.mulesoft.jcsmp.listeners;

import com.solace.connector.mulesoft.internal.connection.ConsumerCache;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.XMLMessageListener;

public class ListenerFactory {
	private ListenerFactory() {
		
	}
	
	public static ConnectionListenerDelegate create(ListenerType type, JCSMPSession session, XMLMessageListener listener, ConnectionListenerProperties properties, ConsumerCache consumerCache) {
		ConnectionListenerDelegate delegate = null;
		if (type.equals(ListenerType.LISTENER)) {
			delegate = new ListenerDelegate();
		} else if (type.equals(ListenerType.CONSUMER)){
			delegate = new ConsumerDelegate();
		} 
		if (delegate!=null) {
			delegate.init(session, listener, properties, consumerCache);
		}
		return delegate;
	}
}

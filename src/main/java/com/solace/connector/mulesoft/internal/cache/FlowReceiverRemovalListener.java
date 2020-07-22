package com.solace.connector.mulesoft.internal.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.solacesystems.jcsmp.FlowReceiver;

public class FlowReceiverRemovalListener implements RemovalListener<String, FlowReceiver> {
	private final Logger LOGGER = LoggerFactory.getLogger(FlowReceiverRemovalListener.class);
	
	@Override
	public void onRemoval(RemovalNotification<String, FlowReceiver> notification) {
		LOGGER.debug("FlowReceiver removed from cache, closing it");
		notification.getValue().close();
	}

}

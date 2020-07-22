package com.solace.connector.mulesoft.jcsmp;

import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.connector.mulesoft.api.SolaceConnectorError;
import com.solacesystems.jcsmp.SessionEvent;
import com.solacesystems.jcsmp.SessionEventArgs;
import com.solacesystems.jcsmp.SessionEventHandler;

public class SolaceSessionEventHandler implements SessionEventHandler {

	private final Logger LOGGER = LoggerFactory.getLogger(SolaceSessionEventHandler.class);

	@Override
	public void handleEvent(SessionEventArgs sessionEvent) {
		LOGGER.warn(sessionEvent.getEvent().name());
		LOGGER.debug(sessionEvent.getInfo());
		if (sessionEvent.getEvent() == SessionEvent.DOWN_ERROR) {
			ModuleException generic = new ModuleException(SolaceConnectorError.SESSION_DOWN, sessionEvent.getException());
			LOGGER.error("Session down");
			throw generic;
		}

	}

}

package com.solace.connector.mulesoft.jcsmp;

import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.connector.mulesoft.api.SolaceConnectorError;
import com.solacesystems.jcsmp.JCSMPProducerEventHandler;
import com.solacesystems.jcsmp.ProducerEventArgs;

public class SolaceProducerEventHandler implements JCSMPProducerEventHandler {
	private final Logger LOGGER = LoggerFactory.getLogger(SolaceProducerEventHandler.class);
	
	@Override
	public void handleEvent(ProducerEventArgs event) {
		if (event.getException()==null) {
			LOGGER.debug(String.format("Received a producer event [%s], code [%s]", event.getInfo(), event.getResponseCode()));
		} else  {
			Exception e = event.getException();
			LOGGER.error(String.format("Received a producer exception [%s], info: [%s], code: [%s]", e.getMessage(),event.getInfo(), event.getResponseCode()),e );
			ModuleException generic = new ModuleException(SolaceConnectorError.GENERIC_ERROR, e);
			throw generic;
		}

	}

}

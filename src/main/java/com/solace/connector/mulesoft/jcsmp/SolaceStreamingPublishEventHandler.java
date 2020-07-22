package com.solace.connector.mulesoft.jcsmp;

import java.util.HashMap;
import java.util.Map;

import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.connector.mulesoft.api.SolaceConnectorError;
import com.solacesystems.jcsmp.JCSMPErrorResponseException;
import com.solacesystems.jcsmp.JCSMPErrorResponseSubcodeEx;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;

public class SolaceStreamingPublishEventHandler implements JCSMPStreamingPublishCorrelatingEventHandler {
	private final Logger LOGGER = LoggerFactory.getLogger(SolaceStreamingPublishEventHandler.class);

	private Map<Object, CompletionCallback<Void, Void>> callbacks = new HashMap<Object, CompletionCallback<Void, Void>>();

	public void registerCallback(String key, CompletionCallback<Void, Void> callback) {
		this.callbacks.put(key, callback);
	}

	@Override
	public void responseReceived(String messageID) {
		LOGGER.debug("Producer received response for msg: " + messageID);
	}

	@Override
	public void handleError(String messageID, JCSMPException e, long timestamp) {
		LOGGER.error(String.format("Producer received error for msg: %s@%s - %s%n", messageID, timestamp, e), e);
		throw new ModuleException(SolaceConnectorError.GENERIC_ERROR, e);
	}

	@Override
	public void handleErrorEx(Object key, JCSMPException cause, long timestamp) {
		LOGGER.error(String.format("Producer received negative ack for msg: %s@%s - %s%n", key, timestamp, cause),
				cause);
		CompletionCallback<Void, Void> callback = this.callbacks.get(key);
		if (callback == null) {
			LOGGER.error(String.format("No matching callback for message key [%s]", key));
			return;
		}
		try {
			if (cause instanceof JCSMPErrorResponseException) {
				JCSMPErrorResponseException e = (JCSMPErrorResponseException) cause;
				LOGGER.error(String.format("Raised error for [%s]", key));
				if (e.getSubcodeEx() == JCSMPErrorResponseSubcodeEx.QUEUE_NOT_FOUND) {
					ModuleException queueMissing = new ModuleException(SolaceConnectorError.INVALID_ENDPOINT, cause);
					LOGGER.error(String.format("Queue missing error [%s]", key), cause);
					callback.error(queueMissing);
				} else {
					ModuleException allotherErrors = new ModuleException(SolaceConnectorError.ENDPOINT_ERROR, cause);
					LOGGER.error(String.format("Endpoint error [%s]", key), cause);
					callback.error(allotherErrors);
				}
			} else
				callback.error(cause);
		} finally {
			this.callbacks.remove(key);
		}
	}

	@Override
	public void responseReceivedEx(Object key) {
		if (!this.callbacks.containsKey(key)) {
			LOGGER.debug(String.format("No matching callback for message key [%s]", key));
			return;
		}
		LOGGER.debug(String.format("Processing persistent/non-persistent acknowledgement for correlation key [%s] ", key));
		CompletionCallback<Void, Void> callback = this.callbacks.get(key);
		callback.success(Result.<Void, Void>builder().output(null).attributes(null).build());
		this.callbacks.remove(key);
	}
}

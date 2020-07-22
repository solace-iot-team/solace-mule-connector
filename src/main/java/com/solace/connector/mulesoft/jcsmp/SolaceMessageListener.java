package com.solace.connector.mulesoft.jcsmp;

import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.solace.connector.mulesoft.api.SolaceConnectorError;
import com.solace.connector.mulesoft.api.SolaceMessageProperties;
import com.solace.connector.mulesoft.internal.connection.ConsumerCache;
import com.solace.connector.mulesoft.jcsmp.util.MessageConverter;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.XMLMessageListener;

@Component
public class SolaceMessageListener implements XMLMessageListener {
	public static final String RECEIVED_MESSAGE_CONTEXT_KEY = "ReceivedMessage";

	private ConsumerCache consumerCache;

	private final Logger LOGGER = LoggerFactory.getLogger(SolaceMessageListener.class);

	private MessageConverter messageConverter = new MessageConverter();

	private final String contentType;
	
	private SourceCallback<TypedValue<Object>, SolaceMessageProperties> sourceCallback;

	public SolaceMessageListener(SourceCallback<TypedValue<Object>, SolaceMessageProperties> sourceCallback, ConsumerCache consumerCache, String contentType) {
		this.sourceCallback = sourceCallback;
		this.consumerCache = consumerCache;
		this.contentType = contentType;
	}

	@Override
	public void onReceive(BytesXMLMessage msg) {
		Result<TypedValue<Object>, SolaceMessageProperties> result = messageConverter.convertMessageToObject(msg, this.contentType);
		SourceCallbackContext context = sourceCallback.createContext();
		if (msg.getDeliveryMode().equals(DeliveryMode.PERSISTENT) || msg.getDeliveryMode().equals(DeliveryMode.NON_PERSISTENT)) {
			context.addVariable(RECEIVED_MESSAGE_CONTEXT_KEY, msg);
			consumerCache.put(msg);
		}
		sourceCallback.handle(result, context);

	}

	@Override
	public void onException(JCSMPException e) {
		LOGGER.error("Listener error", e);
		ModuleException generic = new ModuleException(SolaceConnectorError.GENERIC_ERROR, e);
		throw generic;
	}

}

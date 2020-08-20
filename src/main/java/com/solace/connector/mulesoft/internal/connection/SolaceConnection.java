package com.solace.connector.mulesoft.internal.connection;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.connector.mulesoft.api.SolaceAccessType;
import com.solace.connector.mulesoft.api.SolaceAckMode;
import com.solace.connector.mulesoft.api.SolaceConnectorError;
import com.solace.connector.mulesoft.api.SolaceEndpointType;
import com.solace.connector.mulesoft.api.SolaceMessageProperties;
import com.solace.connector.mulesoft.internal.configuration.ClientChannelProperties;
import com.solace.connector.mulesoft.internal.configuration.ConsumerAcknowledgementConfiguration;
import com.solace.connector.mulesoft.internal.configuration.SessionProperties;
import com.solace.connector.mulesoft.jcsmp.SolaceSessionEventHandler;
import com.solace.connector.mulesoft.jcsmp.SolaceStreamingPublishEventHandler;
import com.solace.connector.mulesoft.jcsmp.listeners.ConnectionListenerDelegate;
import com.solace.connector.mulesoft.jcsmp.listeners.ConnectionListenerProperties;
import com.solace.connector.mulesoft.jcsmp.listeners.ListenerFactory;
import com.solace.connector.mulesoft.jcsmp.listeners.ListenerType;
import com.solace.connector.mulesoft.jcsmp.util.ConsumerFlowPropertiesFactory;
import com.solace.connector.mulesoft.jcsmp.util.EndpointPropertiesFactory;
import com.solace.connector.mulesoft.jcsmp.util.EndpointUtil;
import com.solace.connector.mulesoft.jcsmp.util.MessageConverter;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.Context;
import com.solacesystems.jcsmp.ContextProperties;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.Destination;
import com.solacesystems.jcsmp.Endpoint;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.InvalidPropertiesException;
import com.solacesystems.jcsmp.JCSMPChannelProperties;
import com.solacesystems.jcsmp.JCSMPErrorResponseException;
import com.solacesystems.jcsmp.JCSMPErrorResponseSubcodeEx;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPRequestTimeoutException;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Message;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.Requestor;
import com.solacesystems.jcsmp.SDTException;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.XMLMessageProducer;

/**
 * This class represents an extension connection just as example (there is no
 * real connection with anything here c:).
 */

public final class SolaceConnection {
	private final Logger LOGGER = LoggerFactory.getLogger(SolaceConnection.class);

	private final String clientUsername;
	private final String clientPassword;
	private final String msgVPN;
	private final String brokerHost;
	private final int brokerPort;
	private final boolean useTLS;
	private final ClientChannelProperties clientChannelProperties;
	private final SessionProperties sessionProperties;
	private String name;

	private MessageConverter messageConverter = new MessageConverter();

	private JCSMPSession session;
	private Context context = null;
	private XMLMessageProducer producer;
	private SolaceStreamingPublishEventHandler streamingPublishEventHandler = new SolaceStreamingPublishEventHandler();

	@Inject
	private ConsumerCache consumerCache;

	public static class Builder {
		private String clientUsername;
		private String clientPassword;
		private String msgVPN;
		private String brokerHost;
		private int brokerPort;
		private ClientChannelProperties clientChannelProperties;
		private SessionProperties sessionProperties;
		private boolean useTLS;

		public Builder(String brokerHost, int brokerPort, String msgVPN) {
			assert (brokerHost != null);
			assert (msgVPN != null);
			this.brokerHost = brokerHost;
			this.brokerPort = brokerPort;
			this.msgVPN = msgVPN;
		}

		public Builder useTLS(boolean useTLS) {
			this.useTLS = useTLS;
			return this;
		}

		public Builder withCredentials(String clientUsername, String clientPassword) {
			this.clientUsername = clientUsername;
			this.clientPassword = clientPassword;
			return this;
		}

		public Builder withClientChannelProperties(ClientChannelProperties clientChannelProperties) {
			this.clientChannelProperties = clientChannelProperties;
			return this;
		}

		public Builder withSessionproperties(SessionProperties sessionProperties) {
			this.sessionProperties = sessionProperties;
			return this;
		}

		public SolaceConnection build() throws ConnectionException {
			return new SolaceConnection(clientUsername, clientPassword, msgVPN, brokerHost, brokerPort, useTLS,
					clientChannelProperties, sessionProperties);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private SolaceConnection(String clientUsername, String clientPassword, String msgVPN, String brokerHost,
			int brokerPort, boolean useTLS, ClientChannelProperties clientChannelProperties,
			SessionProperties sessionProperties) throws ConnectionException {
		this.clientUsername = clientUsername;
		this.clientPassword = clientPassword;
		this.msgVPN = msgVPN;
		this.brokerHost = brokerHost;
		this.brokerPort = brokerPort;
		this.name = String.format("%s:%s", clientUsername, UUID.randomUUID().toString());
		this.clientChannelProperties = clientChannelProperties;
		this.sessionProperties = sessionProperties;
		this.useTLS = useTLS;
		this.init();
	}

	public void invalidate() {
		if (session != null && !session.isClosed())
			session.closeSession();
		if (context != null)
			context.destroy();
	}

	private void init() throws ConnectionException {
		final JCSMPProperties properties = new JCSMPProperties();
		final String sessionName = UUID.randomUUID().toString();
		this.setName(sessionName);
		String host = String.format("%s:%d", brokerHost, brokerPort);
		if (this.brokerPort == 55443 || this.useTLS) {
			host = String.format("tcps://%s:%d", brokerHost, brokerPort);
		}
		properties.setProperty(JCSMPProperties.APPLICATION_DESCRIPTION, "Solace Mulesoft Connector");
		properties.setProperty(JCSMPProperties.SESSION_NAME, sessionName);
		properties.setProperty(JCSMPProperties.HOST, host);
		properties.setProperty(JCSMPProperties.USERNAME, clientUsername);
		properties.setProperty(JCSMPProperties.VPN_NAME, msgVPN);
		properties.setProperty(JCSMPProperties.PASSWORD, clientPassword);
		properties.setProperty(JCSMPProperties.REAPPLY_SUBSCRIPTIONS, true);
		properties.setProperties(sessionProperties.toJCSMPProperties());
		JCSMPChannelProperties cp = (JCSMPChannelProperties) properties
				.getProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES);
		this.clientChannelProperties.populateJCSMPChannelProperties(cp);

		try {
			String contextName = String.format("%s:%s", brokerHost, this.getClass().getName());
			context = JCSMPFactory.onlyInstance().createContext(new ContextProperties().setName(contextName));
			session = JCSMPFactory.onlyInstance().createSession(properties, context, new SolaceSessionEventHandler());

		} catch (InvalidPropertiesException e) {
			String logMessage = String.format("Could not create SMF session to [%s]", host);
			ConnectionException ce = new ConnectionException(logMessage, e);
			LOGGER.error(logMessage);
			throw ce;
		}
		try {
			session.connect();
		} catch (JCSMPException e) {
			String logMessage = String.format("Could not connect SMF session to [%s]", host);
			ConnectionException ce = new ConnectionException(logMessage, e);
			LOGGER.error(logMessage);
			throw ce;
		}
		try {

			producer = session.getMessageProducer(streamingPublishEventHandler);
		} catch (JCSMPException e) {
			String logMessage = String.format("Could not create producer to [%s]", host);
			LOGGER.error(logMessage);
			ConnectionException ce = new ConnectionException(logMessage, e);
			throw ce;
		}

	}

	/**
	 * Check is the underlying connection is valid (i.e. open and connected)
	 * 
	 * @return
	 */
	public boolean isValid() {
		return !session.isClosed();
	}

	public void publish(SolaceEndpointType endpointType, String endpoint, boolean provisionQueue,
			DeliveryMode deliveryMode, String correlationId, boolean markReply, TypedValue<Object> payload,
			String encoding, String contentType, CompletionCallback<Void, Void> callback) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format(
					"endpointType=%s, endpoint=%s, provisionQueue=%s, deliveryMode=%s, correlationId=%s, markReply=%s, encoding=%s, contentType=%s",
					endpointType, endpoint, provisionQueue, deliveryMode, correlationId, markReply, encoding,
					contentType));
		}
		final Destination endpointDestination;
		Message msg = null;
		try {
			msg = messageConverter.convertObjectToMessage(payload, encoding, contentType);
		} catch (SDTException msgException) {
			ModuleException generic = new ModuleException(SolaceConnectorError.GENERIC_ERROR, msgException);
			LOGGER.error(String.format("Error creating message on endpoint [%s]", endpoint));
			throw generic;
		}
		msg.setDeliveryMode(deliveryMode);

		if (endpointType.equals(SolaceEndpointType.QUEUE)) {
			endpointDestination = JCSMPFactory.onlyInstance().createQueue(endpoint);
			msg.setDeliveryMode(DeliveryMode.PERSISTENT);
			if (provisionQueue && !consumerCache.hasEndpoint(endpoint)) {
				EndpointProperties endpointProps = new EndpointProperties();
				endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
				try {
					EndpointUtil.provisionQueue(session, (Endpoint) endpointDestination, endpointProps, endpointType);
					consumerCache.putEndpoint(endpoint);
				} catch (JCSMPException | ModuleException e) {
					callback.error(e);
				}
			}
		} else {
			endpointDestination = JCSMPFactory.onlyInstance().createTopic(endpoint);
		}

		if (correlationId != null && !correlationId.isEmpty()) {
			msg.setCorrelationId(correlationId);
		}
		msg.setAsReplyMessage(markReply);
		if (DeliveryMode.PERSISTENT.equals(deliveryMode)) {
			String persistentMessageId = UUID.randomUUID().toString();
			msg.setCorrelationKey(persistentMessageId);
			this.streamingPublishEventHandler.registerCallback(persistentMessageId, callback);
		}
		try {
			LOGGER.debug("Publishing to " + endpointDestination.getName());
			this.producer.send(msg, endpointDestination);
		} catch (JCSMPException e) {
			LOGGER.error("Error publishing.", e);
			ModuleException generic = new ModuleException(SolaceConnectorError.GENERIC_ERROR, e);
			LOGGER.error(String.format("Error sending message on endpoint [%s]", endpoint));
			throw generic;
		} finally {
			if (DeliveryMode.DIRECT.equals(deliveryMode)) {
				callback.success(Result.<Void, Void>builder().output(null).attributes(null).build());
			}
		}

	}

	public Result<TypedValue<Object>, SolaceMessageProperties> requestReplyDirect(TypedValue<Object> message,
			String encoding, String contentType, String topicName, int timeOutMillis) {
		Message request;
		try {
			request = messageConverter.convertObjectToMessage(message, encoding, contentType);
		} catch (SDTException msgException) {
			ModuleException generic = new ModuleException(SolaceConnectorError.GENERIC_ERROR, msgException);
			LOGGER.error(String.format("Error creating message on topic [%s]", topicName));
			throw generic;
		}
		Requestor requestor;
		try {
			XMLMessageConsumer consumer = session.getMessageConsumer((XMLMessageListener) null);
			consumer.start();
			requestor = session.createRequestor();
			Destination topic = JCSMPFactory.onlyInstance().createTopic(topicName);
			BytesXMLMessage response = requestor.request(request, timeOutMillis, topic);
			consumer.stop();

			return messageConverter.convertMessageToObject(response, contentType);
		} catch (JCSMPException e) {
			handleTimeOutException(e);
			return null;
		} finally {
		}
	}

	public Result<TypedValue<Object>, SolaceMessageProperties> requestReplyPersistent(SolaceEndpointType endpointType,
			String endpoint, boolean provisionQueue, TypedValue<Object> message, String encoding, String contentType,
			int timeOutMillis, ConsumerAcknowledgementConfiguration consumerAcknowledgementConfiguration,
			SolaceAccessType accessType) {
		Destination requestDestination = EndpointUtil.mapToGuaranteedProducerDestination(endpointType, endpoint);

		Queue replyQueue;
		try {

			if (SolaceEndpointType.QUEUE.equals(endpointType) && provisionQueue
					&& !consumerCache.hasEndpoint(endpoint)) {
				final EndpointProperties endpointProps = new EndpointPropertiesFactory.Builder()
						.withAccessType(SolaceAccessType.toSolaceConstant(accessType)).build();
				EndpointUtil.provisionQueue(session, (Endpoint) requestDestination, endpointProps, endpointType);
				consumerCache.putEndpoint(endpoint);
			}
			replyQueue = session.createTemporaryQueue();

			ConsumerFlowProperties flowProps = new ConsumerFlowPropertiesFactory.Builder(replyQueue)
					.withConsumerAcknowledgementConfiguration(consumerAcknowledgementConfiguration).build();
			FlowReceiver flow = null;
			flow = session.createFlow(null, flowProps);
			flow.start();

			Message request = messageConverter.convertObjectToMessage(message, encoding, contentType);
			request.setDeliveryMode(DeliveryMode.PERSISTENT);
			request.setReplyTo(replyQueue);
			String persistentMessageId = UUID.randomUUID().toString();
			request.setCorrelationKey(persistentMessageId);

			this.producer.send(request, requestDestination);

			BytesXMLMessage responseMessage = flow.receive(timeOutMillis);
			flow.close();
			return this.messageConverter.convertMessageToObject(responseMessage, contentType);
		} catch (JCSMPException e) {
			this.handleTimeOutException(e);
			return null;
		}
	}

	private void handleTimeOutException(JCSMPException e) {
		if (e instanceof JCSMPRequestTimeoutException) {
			LOGGER.error("Timeout in request/reply.", e);
			ModuleException generic = new ModuleException(SolaceConnectorError.REQUEST_TIME_OUT, e);
			throw generic;
		} else {
			LOGGER.error("Error in request/reply.", e);
			ModuleException generic = new ModuleException(SolaceConnectorError.GENERIC_ERROR, e);
			throw generic;
		}
	}	
	/**
	 * 
	 * @param endpointType
	 * @param endpointName
	 * @param timeOutMillis
	 * @return
	 */
	public Result<TypedValue<Object>, SolaceMessageProperties> consume(SolaceEndpointType endpointType,
			String endpointName, String encoding, String contentType, int timeOutMillis,
			ConsumerAcknowledgementConfiguration consumerAcknowledgementConfiguration, String subscription,
			String selector) {
		final Endpoint endpoint = EndpointUtil.mapToGuaranteedConsumerEndpoint(endpointType, endpointName);
		final ConsumerFlowProperties flowProps = new ConsumerFlowPropertiesFactory.Builder(endpoint)
				.withConsumerAcknowledgementConfiguration(consumerAcknowledgementConfiguration)
				.withSubscription(subscription).withSelector(selector).build();
		FlowReceiver flowReceiver = null;
		try {
			flowReceiver = session.createFlow(null, flowProps);

			flowReceiver.startSync();
			BytesXMLMessage msg = flowReceiver.receive(timeOutMillis);
			flowReceiver.stop();
			if (JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT.equals(flowProps.getAckMode()) && msg != null) {
				LOGGER.debug(String.format("Caching message for client acks, id [%s]", msg.getMessageId()));
				consumerCache.put(msg, flowReceiver);
			} else {
				flowReceiver.close();
			}
			if (msg == null)
				LOGGER.debug("No message consumed within timeout");
			return messageConverter.convertMessageToObject(msg, contentType);
		} catch (

		JCSMPException cause) {
			if (flowReceiver != null) {
				flowReceiver.stop();
				flowReceiver.close();
			}
			// TODO - handle other errors? anything relevant that would benefit from
			// specific error message?
			if (cause instanceof JCSMPErrorResponseException && ((JCSMPErrorResponseException) cause)
					.getSubcodeEx() == JCSMPErrorResponseSubcodeEx.UNKNOWN_QUEUE_NAME) {
				ModuleException queueMissing = new ModuleException(SolaceConnectorError.INVALID_ENDPOINT, cause);
				LOGGER.error(String.format("Queue missing error [%s]", endpointName));
				throw queueMissing;
			} else {
				ModuleException generic = new ModuleException(SolaceConnectorError.GENERIC_ERROR, cause);
				LOGGER.error(String.format("Endpoint missing error [%s]", endpointName));
				throw generic;
			}
		}
	}

	/**
	 * 
	 * @param type
	 * @param subscriptions
	 * @param listener
	 * @param properties
	 * @return
	 * @throws JCSMPException
	 */
	public ConnectionListenerDelegate listen(ListenerType type, List<String> subscriptions, XMLMessageListener listener,
			ConnectionListenerProperties properties) {
		ConnectionListenerDelegate d = ListenerFactory.create(type, session, listener, properties, this.consumerCache);
		d.listen(subscriptions);
		return d;
	}

	public void stop(ConnectionListenerDelegate delegate) {
		if (delegate != null)
			delegate.stop();
	}

	public void ack(String messageId, String ackMode) {
		LOGGER.debug(String.format("Attempting to ack message  id [%s]", messageId));
		if (messageId == null) {
			LOGGER.warn("Can not ack message as message id provided is null");
		} else {
			BytesXMLMessage msg = consumerCache.getMessage(messageId);
			this.doAckMessage(msg, ackMode);
			FlowReceiver flowReceiver = this.consumerCache.getFlowReceiver(messageId);
			if (flowReceiver != null) {
				LOGGER.debug("Closing flow receiver");
				flowReceiver.close();
			}
			consumerCache.invalidate(messageId);
		}
	}

	/**
	 * Acknowledge a message if the message is not null and the ack mode of the
	 * connection is manual/client.
	 * 
	 * @param msg
	 */
	public void doAckMessage(BytesXMLMessage msg, String ackMode) {
		if (JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT.equals(ackMode) && msg != null) {
			LOGGER.debug("Message ack'd");
			msg.ackMessage();
		}
		if (msg == null) {
			LOGGER.warn("Can not ack message as message is null (could not be found in cache)");
		}

	}
}

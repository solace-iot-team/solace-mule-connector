package com.solace.connector.mulesoft.internal;

import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.MetadataScope;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;

import com.solace.connector.mulesoft.api.SolaceAckMode;
import com.solace.connector.mulesoft.api.SolaceConsumerEndpoint;
import com.solace.connector.mulesoft.api.SolaceDeliveryMode;
import com.solace.connector.mulesoft.api.SolaceEndpoint;
import com.solace.connector.mulesoft.api.SolaceEndpointType;
import com.solace.connector.mulesoft.api.SolaceMessage;
import com.solace.connector.mulesoft.api.SolaceMessageProperties;
import com.solace.connector.mulesoft.api.SolaceTopic;
import com.solace.connector.mulesoft.api.TimeOut;
import com.solace.connector.mulesoft.internal.configuration.ConsumerAcknowledgementConfiguration;
import com.solace.connector.mulesoft.internal.configuration.SolaceConfiguration;
import com.solace.connector.mulesoft.internal.connection.SolaceConnection;

/**
 * Container class for all connector operations
 *
 * 
 */
@MetadataScope(outputResolver = SolaceOutputResolver.class)
public class SolaceOperations {

	/**
	 * operation to publish messages
	 * 
	 * @param configuration
	 * @param connection
	 * @param endpoint
	 * @param provisionQueue
	 * @param message
	 * @param callback
	 */
	@Throws(PublishErrorProvider.class)
	@Alias("publish")
	public void publish(@Config SolaceConfiguration configuration, @Connection SolaceConnection connection,
			@ParameterGroup(name = "Destination") SolaceEndpoint endpoint,
			@Alias("delivery-mode") @Optional(defaultValue = "DIRECT") SolaceDeliveryMode deliveryMode,
			@Alias("provisionQueue") @Expression(ExpressionSupport.NOT_SUPPORTED) @Optional(defaultValue = "false") @Placement(order = 1, tab = "Advanced") boolean provisionQueue,
			@ParameterGroup(showInDsl = true, name = "Message") SolaceMessage message,
			CompletionCallback<Void, Void> callback) {

		connection.publish(endpoint.getEndpointType(), endpoint.getEndpointName(), provisionQueue,
				deliveryMode.toSolaceDeliveryMode(deliveryMode), message.getCorrelationId(), message.isMarkReply(),
				message.getBody(), message.getEncoding(), message.getContentType(), callback);
	}

	/**
	 * Operation to consume a Persistent message
	 * 
	 * @param configuration
	 * @param connection
	 * @param endpoint
	 * @param timeOut
	 * @return
	 */
	@Alias("consume")
	@Throws(OperationsErrorProvider.class)
	public Result<TypedValue<Object>, SolaceMessageProperties> consume(@Config SolaceConfiguration configuration,
			@Connection SolaceConnection connection, @ParameterGroup(name = "Endpoint") SolaceConsumerEndpoint endpoint,
			@ParameterGroup(name = "Time Out") TimeOut timeOut) {
		int timeOutMillis = timeOut.getMillis();
		ConsumerAcknowledgementConfiguration consumerAcknowledgementConfiguration = configuration
				.getConsumerAcknowledgementConfiguration();
		return connection.consume(endpoint.getEndpointType(), endpoint.getEndpointName(), endpoint.getEncoding(),
				endpoint.getContentType(), timeOutMillis, consumerAcknowledgementConfiguration,
				endpoint.getSubscription(), endpoint.getSelector());
	}

	public void ack(@Config SolaceConfiguration configuration, @Connection SolaceConnection connection,
			@Alias("messageId") @Optional String messageId) {
		connection.ack(messageId,
				SolaceAckMode.toSolaceConstant(configuration.getConsumerAcknowledgementConfiguration().getAckMode()));
	}

	/**
	 * 
	 * @param configuration
	 * @param connection
	 * @param endpoint
	 * @param provisionQueue
	 * @param message
	 * @param timeOut
	 * @return
	 */
	@Throws(OperationsErrorProvider.class)
	public Result<TypedValue<Object>, SolaceMessageProperties> requestReply(@Config SolaceConfiguration configuration,
			@Connection SolaceConnection connection, @ParameterGroup(name = "Endpoint") SolaceEndpoint endpoint,
			@Alias("delivery-mode") @Optional(defaultValue = "DIRECT") SolaceDeliveryMode deliveryMode,
			@Alias("provisionQueue") @Expression(ExpressionSupport.NOT_SUPPORTED) @Optional(defaultValue = "false") @Placement(order = 1, tab = "Advanced") boolean provisionQueue,
			@ParameterGroup(showInDsl = true, name = "Message") SolaceMessage message,
			@ParameterGroup(name = "Time Out") TimeOut timeOut) {
		int timeOutMillis = timeOut.getMillis();
		ConsumerAcknowledgementConfiguration consumerAcknowledgementConfiguration = configuration
				.getConsumerAcknowledgementConfiguration();
		Result<TypedValue<Object>, SolaceMessageProperties> response;
		if (SolaceDeliveryMode.PERSISTENT.equals(deliveryMode) || SolaceEndpointType.QUEUE.equals(endpoint.getEndpointType())) {
			response = connection.requestReplyPersistent(endpoint.getEndpointType(), endpoint.getEndpointName(),
					provisionQueue, message.getBody(), message.getEncoding(), message.getContentType(), timeOutMillis,
					consumerAcknowledgementConfiguration, configuration.getAccessType());
		} else {
			response = connection.requestReplyDirect(message.getBody(), message.getEncoding(), message.getContentType(),
					endpoint.getEndpointName(), timeOutMillis);
		}
		return response;
	}

}

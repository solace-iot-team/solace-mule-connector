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
import com.solace.connector.mulesoft.api.SolaceEndpoint;
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
	 * operation to publish Direct messages
	 * 
	 * @param configuration
	 * @param connection
	 * @param topic
	 * @param message
	 * @param encoding
	 * @param correlationId
	 * @param markReply
	 * @param callback
	 */
	@Alias("publishDirect")
	public void publishDirect(@Config SolaceConfiguration configuration, @Connection SolaceConnection connection,
			@ParameterGroup(name = "Topic") SolaceTopic topic,
			@ParameterGroup(showInDsl = true, name = "Message") SolaceMessage message,
			CompletionCallback<Void, Void> callback) {
		connection.publishDirect(topic.getTopic(), message.getBody(), message.getEncoding(), message.getContentType(), message.getCorrelationId(),
				message.isMarkReply());
		callback.success(Result.<Void, Void>builder().output(null).attributes(null).build());
	}

	/**
	 * operation to publish Persistent messages
	 * 
	 * @param configuration
	 * @param connection
	 * @param endpoint
	 * @param provisionQueue
	 * @param message
	 * @param callback
	 */
	@Throws(PublishPersistentErrorProvider.class)
	@Alias("publishGuaranteed")
	public void publishPersistent(@Config SolaceConfiguration configuration, @Connection SolaceConnection connection,
			@ParameterGroup(name = "Endpoint") SolaceEndpoint endpoint,
			@Alias("provisionQueue") @Expression(ExpressionSupport.NOT_SUPPORTED) @Optional(defaultValue = "false") @Placement(order = 1, tab = "Advanced") boolean provisionQueue,
			@ParameterGroup(showInDsl = true, name = "Message") SolaceMessage message,
			CompletionCallback<Void, Void> callback) {
		connection.publishGuaranteed(endpoint.getEndpointType(), endpoint.getEndpointName(), provisionQueue,
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
		return connection.consume(endpoint.getEndpointType(), endpoint.getEndpointName(), endpoint.getEncoding(), endpoint.getContentType(), timeOutMillis,
				consumerAcknowledgementConfiguration, endpoint.getSubscription(), endpoint.getSelector());
	}

	public void ack(@Config SolaceConfiguration configuration, @Connection SolaceConnection connection,
			@Alias("messageId") @Optional String messageId) {
		connection.ack(messageId,
				SolaceAckMode.toSolaceConstant(configuration.getConsumerAcknowledgementConfiguration().getAckMode()));
	}

	/**
	 * Request reply using direct messaging
	 * 
	 * @param configuration
	 * @param connection
	 * @param topic
	 * @param timeOut
	 * @param timeUnit
	 * @param message
	 * @return
	 */
	@Throws(OperationsErrorProvider.class)
	public Result<TypedValue<Object>, SolaceMessageProperties> requestReplyDirect(
			@Config SolaceConfiguration configuration, @Connection SolaceConnection connection,
			@ParameterGroup(name = "Topic") SolaceTopic topic,
			@ParameterGroup(showInDsl = true, name = "Message") SolaceMessage message,
			@ParameterGroup(name = "Time Out") TimeOut timeOut) {
		int timeOutMillis = timeOut.getMillis();
		return connection.requestReplyDirect(message.getBody(), message.getEncoding(), message.getContentType(), topic.getTopic(), timeOutMillis);
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
	public Result<TypedValue<Object>, SolaceMessageProperties> requestReplyGuaranteed(
			@Config SolaceConfiguration configuration, @Connection SolaceConnection connection,
			@ParameterGroup(name = "Endpoint") SolaceEndpoint endpoint,
			@Alias("provisionQueue") @Expression(ExpressionSupport.NOT_SUPPORTED) @Optional(defaultValue = "false") @Placement(order = 1, tab = "Advanced") boolean provisionQueue,
			@ParameterGroup(showInDsl = true, name = "Message") SolaceMessage message,
			@ParameterGroup(name = "Time Out") TimeOut timeOut) {
		int timeOutMillis = timeOut.getMillis();
		ConsumerAcknowledgementConfiguration consumerAcknowledgementConfiguration = configuration
				.getConsumerAcknowledgementConfiguration();
		return connection.requestReplyPersistent(endpoint.getEndpointType(), endpoint.getEndpointName(), provisionQueue,
				message.getBody(), message.getEncoding(), message.getContentType(),timeOutMillis, consumerAcknowledgementConfiguration,
				configuration.getAccessType());
	}

}

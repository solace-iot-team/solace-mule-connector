package com.solace.connector.mulesoft.internal.source;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.LifecycleException;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.execution.OnSuccess;
import org.mule.runtime.extension.api.annotation.execution.OnTerminate;
import org.mule.runtime.extension.api.annotation.metadata.MetadataScope;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.source.ClusterSupport;
import org.mule.runtime.extension.api.annotation.source.EmitsResponse;
import org.mule.runtime.extension.api.annotation.source.SourceClusterSupport;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.connector.mulesoft.api.SolaceAccessType;
import com.solace.connector.mulesoft.api.SolaceAckMode;
import com.solace.connector.mulesoft.api.SolaceConsumerEndpoint;
import com.solace.connector.mulesoft.api.SolaceMessageProperties;
import com.solace.connector.mulesoft.internal.SolaceOutputResolver;
import com.solace.connector.mulesoft.internal.configuration.SolaceConfiguration;
import com.solace.connector.mulesoft.internal.connection.ConsumerCache;
import com.solace.connector.mulesoft.internal.connection.SolaceConnection;
import com.solace.connector.mulesoft.jcsmp.SolaceMessageListener;
import com.solace.connector.mulesoft.jcsmp.listeners.ConnectionListenerDelegate;
import com.solace.connector.mulesoft.jcsmp.listeners.ConnectionListenerProperties;
import com.solace.connector.mulesoft.jcsmp.listeners.ListenerType;
import com.solacesystems.jcsmp.Message;

@DisplayName("OnGuaranteedMessage")
@MediaType(value = ANY, strict = false)
@Alias("consumer-source")
@EmitsResponse
@ClusterSupport(SourceClusterSupport.NOT_SUPPORTED)
@MetadataScope(outputResolver = SolaceOutputResolver.class)
public class SolaceConsumerSource extends Source<TypedValue<Object>, SolaceMessageProperties> {

	private final Logger LOGGER = LoggerFactory.getLogger(SolaceConsumerSource.class);

	@Inject
	private ConsumerCache consumerCache;

	@Connection
	private ConnectionProvider<SolaceConnection> serverProvider;

	@Config 
	SolaceConfiguration configuration;
	
	private SolaceConnection connection;

	private ConnectionListenerDelegate delegate;

	@ParameterGroup(name="Endpoint")
	private SolaceConsumerEndpoint endpoint;
	
	@Parameter
	@Alias("provisionEndpoint")
	@Optional(defaultValue="false")
	@Expression(ExpressionSupport.NOT_SUPPORTED)
	@Placement(order=1, tab="Advanced" )
	boolean provisionEndpoint = false;

	@Override
	public void onStart(SourceCallback<TypedValue<Object>, SolaceMessageProperties> sourceCallback)
			throws MuleException {
		// add subscriptions and start the consumer
		this.connection = serverProvider.connect();
		try {
			List<String> subscriptions = new ArrayList<String>();
			subscriptions.add(endpoint.getEndpointName());
			ConnectionListenerProperties properties = new ConnectionListenerProperties();
			properties.put(ConnectionListenerProperties.PROVISION_ENDPOINT, this.provisionEndpoint);
			properties.put(ConnectionListenerProperties.ENDPOINT_TYPE, this.endpoint.getEndpointType());
			properties.put(ConnectionListenerProperties.ACK_MODE, SolaceAckMode.toSolaceConstant(configuration.getConsumerAcknowledgementConfiguration().getAckMode()));
			properties.put(ConnectionListenerProperties.QUEUE_ACCESS_TYPE, SolaceAccessType.toSolaceConstant(configuration.getAccessType()));
			properties.put(ConnectionListenerProperties.SELECTOR, this.endpoint.getSelector());
			properties.put(ConnectionListenerProperties.ACK_THRESHOLD, configuration.getConsumerAcknowledgementConfiguration().getMessageAcknowledgmentThreshold());
			properties.put(ConnectionListenerProperties.ACK_TIMER, configuration.getConsumerAcknowledgementConfiguration().getMessageAcknowledgmentTime());
			properties.put(ConnectionListenerProperties.TRANSPORT_WINDOW_SIZE, configuration.getConsumerAcknowledgementConfiguration().getGuaranteedMessageWindowSize());
			properties.put(ConnectionListenerProperties.SUBSCRIPTION, this.endpoint.getSubscription());
			properties.put(ConnectionListenerProperties.SELECTOR, this.endpoint.getSelector());
			LOGGER.info(String.format("Starting consumer on endpoint [%s] with properties [%s]",  endpoint, properties.toString()));
			this.delegate = connection.listen(ListenerType.CONSUMER, subscriptions,
					new SolaceMessageListener(sourceCallback, this.consumerCache, this.endpoint.getContentType()), properties);
		} catch (ModuleException e) {
			LifecycleException se = new LifecycleException(e, this);
			throw se;
		}
	}
	
	
	@OnSuccess
	public void onSuccess(SourceCallbackContext callbackContext) {
		java.util.Optional<Object> object = callbackContext.getVariable(SolaceMessageListener.RECEIVED_MESSAGE_CONTEXT_KEY);
		Message  msg = (Message)object.orElse(null);
		if (configuration.getConsumerAcknowledgementConfiguration().getAckMode().equals(SolaceAckMode.AUTOMATIC_ON_FLOW_COMPLETION))
			connection.doAckMessage(msg, SolaceAckMode.toSolaceConstant(configuration.getConsumerAcknowledgementConfiguration().getAckMode()));
	}

	@Override
	public void onStop() {
		LOGGER.info("Stopping consumer source");
		if (connection != null) {
			connection.stop(delegate);
			connection.invalidate();
		}
	}
	
	@OnTerminate
	public void onTerminate() {
		
	}

}

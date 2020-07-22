package com.solace.connector.mulesoft.internal.source;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import java.util.List;

import javax.inject.Inject;

import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.LifecycleException;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.metadata.MetadataScope;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.annotation.source.ClusterSupport;
import org.mule.runtime.extension.api.annotation.source.EmitsResponse;
import org.mule.runtime.extension.api.annotation.source.SourceClusterSupport;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.connector.mulesoft.api.SolaceMessageProperties;
import com.solace.connector.mulesoft.internal.SolaceOutputResolver;
import com.solace.connector.mulesoft.internal.connection.ConsumerCache;
import com.solace.connector.mulesoft.internal.connection.SolaceConnection;
import com.solace.connector.mulesoft.jcsmp.SolaceMessageListener;
import com.solace.connector.mulesoft.jcsmp.listeners.ConnectionListenerDelegate;
import com.solace.connector.mulesoft.jcsmp.listeners.ListenerType;

@DisplayName("OnDirectMessage")
@MediaType(value = ANY, strict = false)
@Alias("listener-source")
@EmitsResponse
@ClusterSupport(SourceClusterSupport.NOT_SUPPORTED)
@MetadataScope(outputResolver = SolaceOutputResolver.class)
public class SolaceListenerSource extends Source<TypedValue<Object>, SolaceMessageProperties> {

	private final Logger LOGGER = LoggerFactory.getLogger(SolaceListenerSource.class);

	@Inject
	private ConsumerCache consumerCache;

	@Connection
	private ConnectionProvider<SolaceConnection> serverProvider;

	private ConnectionListenerDelegate delegate;

	private SolaceConnection connection;

	@Parameter
	@Alias("subscriptions")
	@Placement(order=10, tab= Placement.DEFAULT_TAB)
	List<String> subscriptions;

	@Parameter
	@DisplayName("Content Type")
	@Summary("MIME Content Type, used if the message does not indicate the content type")
	@Alias("contentType")
	@Optional(defaultValue = "")
	String contentType;
	
	@Override
	public void onStart(SourceCallback<TypedValue<Object>, SolaceMessageProperties> sourceCallback)
			throws MuleException {
		// add subscriptions and start the consumer
		connection = serverProvider.connect();
		try {
			delegate = connection.listen(ListenerType.LISTENER, subscriptions,
					new SolaceMessageListener(sourceCallback, this.consumerCache, this.contentType), null);
		} catch (ModuleException e) {
			LifecycleException se = new LifecycleException(e, this);
			throw se;
		}
	}

	@Override
	public void onStop() {
		if (connection != null) {
			connection.stop(delegate);
			connection.invalidate();
		}
	}
}

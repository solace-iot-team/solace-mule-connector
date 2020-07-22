package com.solace.connector.mulesoft.internal.connection;

import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.connector.mulesoft.api.SolaceConnectorError;
import com.solace.connector.mulesoft.internal.configuration.ClientChannelProperties;
import com.solace.connector.mulesoft.internal.configuration.SessionProperties;

/**

 */
public class SolaceConnectionProvider implements CachedConnectionProvider<SolaceConnection> {

	private final Logger LOGGER = LoggerFactory.getLogger(SolaceConnectionProvider.class);

	/**
	 * Client username
	 */
	@DisplayName("Client Username")
	@Parameter
	@Placement(order = 6, tab = Placement.CONNECTION_TAB)
	private String clientUserName;

	/**
	 * Client Parameter
	 */
	@DisplayName("Client Password")
	@Password
	@Parameter
	@Placement(order = 7, tab = Placement.CONNECTION_TAB)
	private String password;

	/**
	 * Message VPN name
	 */
	@DisplayName("Message VPN")
	@Parameter
	@Placement(order = 4, tab = Placement.CONNECTION_TAB)
	private String msgVPN;

	/**
	 * Broker host
	 */
	@DisplayName("Broker Host")
	@Parameter
	@Placement(order = 1, tab = Placement.CONNECTION_TAB)
	private String brokerHost;

	/**
	 * SMF port to be used for the connection
	 */
	@DisplayName("SMF Port")
	@Parameter
	@Optional(defaultValue = "55555")
	@Placement(order = 2, tab = Placement.CONNECTION_TAB)
	private int brokerPort;

	/**
	 * USe TLS?
	 */
	@DisplayName("Use TLS to connect")
	@Parameter
	@Optional(defaultValue = "false")
	@Placement(order = 3, tab = Placement.CONNECTION_TAB)
	private boolean useTLS;

	@ParameterGroup(name = "Client Channel Properties")
	private ClientChannelProperties clientChannelProperties;

	@ParameterGroup(name = "Session Properties")
	private SessionProperties sessionProperties;

	@Override
	public SolaceConnection connect() throws ConnectionException {
		return (new SolaceConnection.Builder(this.brokerHost, this.brokerPort, this.msgVPN)).useTLS(useTLS)
				.withClientChannelProperties(clientChannelProperties).withCredentials(clientUserName, password)
				.withSessionproperties(sessionProperties).build();
	}

	@Override
	public void disconnect(SolaceConnection connection) {
		try {
			connection.invalidate();
		} catch (Exception e) {
			ModuleException generic = new ModuleException(SolaceConnectorError.GENERIC_ERROR, e);
			String logMessage = String.format("Error disconnecting session [%s]", connection.getName());
			LOGGER.error(logMessage);
			throw generic;
		}
	}

	@Override
	public ConnectionValidationResult validate(SolaceConnection connection) {
		if (connection.isValid())
			return ConnectionValidationResult.success();
		else
			return ConnectionValidationResult.failure("Not connected to broker", null);
	}
}

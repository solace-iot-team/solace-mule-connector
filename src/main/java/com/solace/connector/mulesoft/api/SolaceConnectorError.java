package com.solace.connector.mulesoft.api;

import java.util.Optional;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.error.MuleErrors;

/**
 * Enumeration of errors that the connectro may raise to the Mule framework
 * @author swenhelge
 *
 */
public enum SolaceConnectorError implements ErrorTypeDefinition<SolaceConnectorError> {
	/**
	 * Invalid queue name or topic endpoint name provided
	 */
	INVALID_ENDPOINT(MuleErrors.CONNECTIVITY), 
	/**
	 * Invalid queue name or topic endpoint name provided
	 */
	ENDPOINT_ERROR(MuleErrors.CONNECTIVITY), 
	/**
	 * Raised when a session down event is detected
	 */
	SESSION_DOWN(MuleErrors.CONNECTIVITY), 
	/**
	 * Bad / invalid username/password provided for broker
	 */
	BAD_CREDENTIALS(MuleErrors.CONNECTIVITY),
	/**
	 * Request reply operation timed out
	 */
	REQUEST_TIME_OUT(MuleErrors.CONNECTIVITY),
	/**
	 * Wait timeout while conmsuing message
	 */
	WAIT_TIME_OUT(MuleErrors.CONNECTIVITY),
	/**
	 * Generic error, the underlying cause will communicate error detail from Solace SDK
	 */
	GENERIC_ERROR(MuleErrors.CONNECTIVITY);
	

	private ErrorTypeDefinition<? extends Enum<?>> parent;

	SolaceConnectorError(ErrorTypeDefinition<? extends Enum<?>> parent) {
		this.parent = parent;
	}

	SolaceConnectorError() {
	}

	@Override
	public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
		return Optional.ofNullable(parent);
	}
}
package com.solace.connector.mulesoft.internal;

import java.util.HashSet;
import java.util.Set;

import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import com.solace.connector.mulesoft.api.SolaceConnectorError;
/**
 * Generic list of errors that connector operations can raise.
 * @author swenhelge
 *
 */
public class OperationsErrorProvider implements ErrorTypeProvider {
    @SuppressWarnings("rawtypes")
	@Override
    public Set<ErrorTypeDefinition> getErrorTypes() {
        HashSet<ErrorTypeDefinition> errors = new HashSet<>();
        errors.add(SolaceConnectorError.INVALID_ENDPOINT);
        errors.add(SolaceConnectorError.GENERIC_ERROR);
        errors.add(SolaceConnectorError.REQUEST_TIME_OUT);
        errors.add(SolaceConnectorError.WAIT_TIME_OUT);

        return errors;
    }
}
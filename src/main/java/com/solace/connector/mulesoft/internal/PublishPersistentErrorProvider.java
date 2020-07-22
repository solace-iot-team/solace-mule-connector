package com.solace.connector.mulesoft.internal;

import java.util.HashSet;
import java.util.Set;

import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import com.solace.connector.mulesoft.api.SolaceConnectorError;
/**
* Lists the errors that can be raised by operations that involve publishing of guaranteed messages
*/
public class PublishPersistentErrorProvider implements ErrorTypeProvider {
    @SuppressWarnings("rawtypes")
	@Override
    public Set<ErrorTypeDefinition> getErrorTypes() {
        HashSet<ErrorTypeDefinition> errors = new HashSet<>();
        errors.add(SolaceConnectorError.INVALID_ENDPOINT);
        errors.add(SolaceConnectorError.GENERIC_ERROR);

        return errors;
    }
}
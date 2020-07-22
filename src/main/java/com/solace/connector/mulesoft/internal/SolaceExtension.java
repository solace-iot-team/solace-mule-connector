package com.solace.connector.mulesoft.internal;

import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;

import com.solace.connector.mulesoft.api.SolaceConnectorError;
import com.solace.connector.mulesoft.internal.configuration.SolaceConfiguration;


/**
 * A connector for Solace PubSub+ brokers and appliances with native API integration using the JCSMP Java SDK
 */
@Xml(prefix = "solace")
@Extension(name = "Solace-Connector")
@ErrorTypes(SolaceConnectorError.class)
@Configurations(SolaceConfiguration.class)
public class SolaceExtension {
	
}

package com.solace.connector.mulesoft.api;

import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
/**
 * Base Configuration for a Solace Endpoint (Queue, Topic) 
 * @author swenhelge
 *
 */
public class SolaceEndpoint {
	
	@Parameter
	@Alias("address")
	@DisplayName("Name")
	String endpointName;


	@Parameter
	@Alias("endpoint-type")
	@DisplayName("Type")
	@Optional(defaultValue="TOPIC")
	SolaceEndpointType endpointType;


	public String getEndpointName() {
		return endpointName;
	}


	public void setEndpointName(String endpointName) {
		this.endpointName = endpointName;
	}


	public SolaceEndpointType getEndpointType() {
		return endpointType;
	}


	public void setEndpointType(SolaceEndpointType endpointType) {
		this.endpointType = endpointType;
	}
}

package com.solace.connector.mulesoft.api;

import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

/**
 * Configuration for a Solace Endpoint consumer (Queue, Topic Endpoint)
 * 
 * @author swenhelge
 *
 */
public class SolaceConsumerEndpoint extends SolaceEndpoint {

	@Parameter
	@DisplayName("Selector")
	@Alias("selector")
	@Optional(defaultValue = "")
	String selector;

	@Parameter
	@DisplayName("Subscription")
	@Summary("The subscription will be added to the endpoint when it is provisioned, only applicable if provisioning is enabled")
	@Alias("subscription")
	@Optional(defaultValue = "")
	String subscription;

	@Parameter
	@DisplayName("Content Type")
	@Summary("MIME Content Type, used if the message does not indicate the content type")
	@Alias("contentType")
	@Optional(defaultValue = "")
	String contentType;

	@Parameter
	@DisplayName("Encoding")
	@Summary("Character set the message is encoded in")
	@Alias("encoding")
	@Optional(defaultValue = "UTF-8")
	String encoding;

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public String getSubscription() {
		return subscription;
	}

	public void setSubscription(String subscription) {
		this.subscription = subscription;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

}

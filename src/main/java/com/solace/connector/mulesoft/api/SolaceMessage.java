package com.solace.connector.mulesoft.api;

import java.io.Serializable;

import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

/**
 * Representation of a Solace Message payload and metadata required to send a message
 * @author swenhelge
 *
 */
public class SolaceMessage  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The body of the message
	 */
	@Parameter
	@ParameterDsl(allowReferences = false)
	@Content(primary = true)
	@Summary("The body of the Message")
	private TypedValue<Object> body;

	@Parameter
	@DisplayName("Content Type")
	@Summary("MIME Content Type, used if the message does not indicate the content type")
	@Alias("contentType")
	@Optional(defaultValue = "")
	String contentType;

	/**
	 * 
	 */
	@Alias("encoding")
	@Parameter
	@Optional(defaultValue = "UTF-8")
	String encoding;

	@Alias("correlationId")
	@Parameter
	@Optional(defaultValue = "")
	String correlationId;
	
	
	@Alias("markReply")
	@Parameter
	@Optional(defaultValue = "false")
	boolean markReply;

	public TypedValue<Object> getBody() {
		return body;
	}

	public void setBody(TypedValue<Object> body) {
		this.body = body;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public boolean isMarkReply() {
		return markReply;
	}

	public void setMarkReply(boolean markReply) {
		this.markReply = markReply;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}

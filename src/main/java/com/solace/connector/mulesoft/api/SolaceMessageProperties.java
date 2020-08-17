package com.solace.connector.mulesoft.api;

import java.io.Serializable;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.Destination;
import com.solacesystems.jcsmp.Queue;

/**
 * Encapsulates message attributes/properties that are exposed to the Mule flow
 * in expression, mappers etc.
 * 
 * @author swenhelge
 *
 */
public class SolaceMessageProperties implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String messageId;

	/**
	 * The destination the message was received from / should be sent to
	 */
	private String destination;

	/**
	 * the type of the destination
	 * 
	 */
	private SolaceEndpointType endpointType;

	// TODO - deo we need separate producer and consumer properties? Does this make
	// sense for senders?
	/**
	 * The reply to destination provided by the message that was received
	 */
	private String replyTo;

	/**
	 * Endpoint type of the reply to destination
	 */
	private SolaceEndpointType replyToEndpointType;

	/**
	 * The correlation id of the message that was received or the id to be used for
	 * a message to send
	 */
	private String correlationId;

	private Long senderTimestamp;

	private Long receiverTimestamp;

	private Long sequenceNumber;

	
	private String contentType;
	
	private boolean reply;

	/**
	 * Creates message properties for use in flows based on a Solace message.
	 * Extracts the appropriate metadata form the message.
	 * @param msg
	 */
	public SolaceMessageProperties(BytesXMLMessage msg) {
		if (msg == null) {
			return;
		}
		String destination = msg.getDestination().getName();
		this.setMessageId(msg.getMessageId());
		this.setDestination(destination);
		this.setEndpointType(mapToEndpointType(msg.getDestination()));
		Destination replyTo = msg.getReplyTo();
		if (replyTo != null) {
			this.setReplyTo(replyTo.getName());
			this.setReplyToEndpointType(mapToEndpointType(replyTo));
		}
		String correlationId = msg.getCorrelationId();
		if (correlationId != null && !correlationId.isEmpty()) {
			this.setCorrelationId(correlationId);
		}
		this.senderTimestamp = msg.getSenderTimestamp();
		this.receiverTimestamp = msg.getReceiveTimestamp();
		this.sequenceNumber = msg.getSequenceNumber();
		this.reply = msg.isReplyMessage();
		this.contentType = msg.getHTTPContentType();
	}
	
	

	public String getContentType() {
		return contentType;
	}



	public void setContentType(String contentType) {
		this.contentType = contentType;
	}



	public boolean isReply() {
		return reply;
	}

	public void setReply(boolean reply) {
		this.reply = reply;
	}

	public long getSenderTimestamp() {
		return senderTimestamp;
	}

	public void setSenderTimestamp(long senderTimestamp) {
		this.senderTimestamp = senderTimestamp;
	}

	public long getReceiverTimestamp() {
		return receiverTimestamp;
	}

	public void setReceiverTimestamp(long receiverTimestamp) {
		this.receiverTimestamp = receiverTimestamp;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public SolaceEndpointType getReplyToEndpointType() {
		return replyToEndpointType;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public void setReplyToEndpointType(SolaceEndpointType replyToEndpointType) {
		this.replyToEndpointType = replyToEndpointType;
	}

	public SolaceEndpointType getEndpointType() {
		return endpointType;
	}

	public void setEndpointType(SolaceEndpointType endpointType) {
		this.endpointType = endpointType;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public Long getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	private static SolaceEndpointType mapToEndpointType(Destination d) {
		if (d instanceof Queue) {
			return SolaceEndpointType.QUEUE;
		} else {
			return SolaceEndpointType.TOPIC;
		}

	}

}

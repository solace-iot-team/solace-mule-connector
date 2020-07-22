package com.solace.connector.mulesoft.api;

import com.solacesystems.jcsmp.JCSMPProperties;

/**
 * Represents ACK Modes that are supported by the Connector.
 * @author swenhelge
 *
 */
public enum SolaceAckMode {
	
	/**
	 * Guaranteed Messages are acknowledged immediately
	 */
	AUTOMATIC_IMMEDIATE, 
	/**
	 * Guaranteed messages are acknowledged once a flow completes, applies to listener/consumer source flows. 
	 * For flows with consumer or request/reply activities this is equivalent to MANUAL_CLIENT
	 */
	AUTOMATIC_ON_FLOW_COMPLETION, 
	/**
	 * Messages must be acknowledged explicitly using the Ack activity within the flow.
	 */
	MANUAL_CLIENT;

	/**
	 * Maps the ack modes exposed to Mule to the ack modes supported by the JCSMP SDK
	 * @param ackMode 
	 * @return appropriate JCSMP ack mode name
	 */
	public static final String toSolaceConstant(SolaceAckMode ackMode) {
		if (ackMode.equals(SolaceAckMode.AUTOMATIC_IMMEDIATE)) {
			return JCSMPProperties.SUPPORTED_MESSAGE_ACK_AUTO;
		} else {
			return JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT;
		}
	}

}

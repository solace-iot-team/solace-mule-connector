package com.solace.connector.mulesoft.api;

import com.solacesystems.jcsmp.EndpointProperties;

/**
 * Exposes access type to endpoints (Queues, Toipic End points) to Mule.
 * @author swenhelge
 *
 */
public enum SolaceAccessType {
	EXCLUSIVE,
	NON_EXCLUSIVE;
	
	/**
	 * Maps the exposed Access Types in this enum to the internal int constant used within JCSMP
	 * @param requiredAccessType
	 * @return JCSMP Constant ACCESSTYPE constant
	 */
	public static int toSolaceConstant(SolaceAccessType requiredAccessType) {
		if (EXCLUSIVE.equals(requiredAccessType)) {
			return EndpointProperties.ACCESSTYPE_EXCLUSIVE;
		} else {
			return EndpointProperties.ACCESSTYPE_NONEXCLUSIVE;
		}
	}
}

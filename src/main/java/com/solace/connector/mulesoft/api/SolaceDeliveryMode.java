package com.solace.connector.mulesoft.api;

import com.solacesystems.jcsmp.DeliveryMode;

public enum SolaceDeliveryMode {
	PERSISTENT,
	DIRECT;
	
	public DeliveryMode toSolaceDeliveryMode(SolaceDeliveryMode m) {
		
		return SolaceDeliveryMode.PERSISTENT.equals(m)?DeliveryMode.PERSISTENT:DeliveryMode.DIRECT;
	}
	
}

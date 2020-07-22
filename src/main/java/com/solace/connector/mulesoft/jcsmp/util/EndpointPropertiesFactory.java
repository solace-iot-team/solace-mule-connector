package com.solace.connector.mulesoft.jcsmp.util;

import com.solacesystems.jcsmp.EndpointProperties;

public class EndpointPropertiesFactory {
	public static class Builder{
		private int accessType;
		public Builder() {
			
		}
		
		public Builder withAccessType(int accessType) {
			this.accessType = accessType;
			return this;
		}
		
		public EndpointProperties build() {
			EndpointProperties ep = new EndpointProperties();
			ep.setAccessType(accessType);
			return ep;
		}
	}
}

package com.solace.connector.mulesoft;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.mule.functional.api.flow.FlowRunner;
import org.mule.runtime.api.metadata.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientAckTestCase extends SolaceConnectorTestBaseClass {

	private final Logger LOGGER = LoggerFactory.getLogger(ClientAckTestCase.class);

	/**
	 * Specifies the mule config xml with the flows that are going to be executed in
	 * the tests, this file lives in the test resources.
	 */
	@Override
	protected String getConfigFile() {
		return "test-client-ack-config.xml";
	}

	@SuppressWarnings("unchecked")
	@Test
	public void manualAck() throws Exception {
		FlowRunner flowRunner = flowRunner("ackSuccessTestFlow");
		TypedValue<Object> payloadValue = (TypedValue<Object>) flowRunner.run().getMessage().getPayload().getValue();
		assertNotNull(payloadValue);
		assertNotNull(payloadValue.getValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void manualAckConsumeTimeout() throws Exception {
		FlowRunner flowRunner = flowRunner("ackTimeoutTestFlow");
		TypedValue<Object> payloadValue = (TypedValue<Object>) flowRunner.run().getMessage().getPayload().getValue();
		assertNull(payloadValue);
	}

}
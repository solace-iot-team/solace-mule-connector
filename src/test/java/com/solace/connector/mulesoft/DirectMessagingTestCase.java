package com.solace.connector.mulesoft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.mule.functional.api.flow.FlowRunner;
import org.mule.runtime.api.metadata.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DirectMessagingTestCase extends SolaceConnectorTestBaseClass {

	private final Logger LOGGER = LoggerFactory.getLogger(DirectMessagingTestCase.class);
	/**
	 * Specifies the mule config xml with the flows that are going to be executed in
	 * the tests, this file lives in the test resources.
	 */
	@Override
	protected String getConfigFile() {
		return "test-direct-messaging-config.xml";
	}

	@Test
	public void executePublishTextMessageOperation() throws Exception {

		FlowRunner flowRunner = flowRunner("publishTextMsgFlow");
		flowRunner.run();
	}

	@Test
	public void requestReplyDirectTimeout() {
		try {
			FlowRunner flowRunner = flowRunner("requestReplyDirectTimeoutFlow");
			flowRunner.run();
			fail("Expected an exception as responder is not present");
		} catch (Exception e) {
			System.out.println("********: " + e.getMessage());
			assertTrue(e.getMessage().startsWith("Timeout occurred performing request"));
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void requestReplyDirectSuccess() throws Exception{
		FlowRunner flowRunner = flowRunner("requestReplyDirectSuccessFlow");
		TypedValue<Object> payloadValue = (TypedValue<Object>) flowRunner.run().getMessage().getPayload().getValue();
		assertEquals("hello echo", payloadValue.getValue());
	}
}
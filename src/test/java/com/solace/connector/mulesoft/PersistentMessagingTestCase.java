package com.solace.connector.mulesoft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;
import org.mule.functional.api.flow.FlowRunner;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.internal.exception.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentMessagingTestCase extends SolaceConnectorTestBaseClass {

	private final Logger LOGGER = LoggerFactory.getLogger(PersistentMessagingTestCase.class);
	/**
	 * Specifies the mule config xml with the flows that are going to be executed in
	 * the tests, this file lives in the test resources.
	 */
	@Override
	protected String getConfigFile() {
		return "test-persistent-messaging-config.xml";
	}

	@Test
	public void executePublishPersistentQueueMissing() throws Exception {

		try {
			FlowRunner flowRunner = flowRunner("publishPersistentQueueMissingFlow");
			flowRunner.run();
			fail("Expected an exception as queue is missing");
		} catch (Exception e) {
			System.out.println("********:" + e.getMessage());
			assertTrue(e.getMessage().startsWith("400: Queue Not Found"));
		}
	}

	@Test
	public void executePublishPersistentQueueAutoProvision() throws Exception {

		FlowRunner flowRunner = flowRunner("publishPersistentAutoProvisionFlow");
		flowRunner.run();
	}

	@Test
	public void executePublishPersistentTopic() throws Exception {

		FlowRunner flowRunner = flowRunner("publishPersistentTopicFlow");
		flowRunner.run();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void consumeSuccess() throws Exception {
		FlowRunner flowRunner = flowRunner("consumeSuccessFlow");
		TypedValue<Object> payloadValue = (TypedValue<Object>) flowRunner.run().getMessage().getPayload().getValue();

		assertEquals("Invalid consumer message", "hello consume operation", payloadValue.getValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void consumeTimeOut() throws Exception {
		FlowRunner flowRunner = flowRunner("consumeTimeOutFlow");
		TypedValue<Object> payloadValue = (TypedValue<Object>) flowRunner.run().getMessage().getPayload().getValue();

		assertNull("Invalid consumer message", payloadValue);
	}

	@Test
	public void consumeExhaustLimit() throws Exception {
		try {
			FlowRunner flowRunner = flowRunner("consumeEndpointMissingFlow");
			flowRunner.run();
			fail("Expected an exception as queue is missing");
		} catch (Exception e) {
			System.out.println("********:" + e.getMessage());
			assertTrue(e.getMessage().startsWith("503: Unknown Queue"));
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void requestReplyGuaranteedSuccess() throws Exception{
		FlowRunner flowRunner = flowRunner("requestReplyGuaranteedSuccessFlow");
		TypedValue<Object> payloadValue = (TypedValue<Object>) flowRunner.run().getMessage().getPayload().getValue();
		assertEquals("hello echo guaranteed", payloadValue.getValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void consumeTopicEndpointSuccess() throws Exception {
		this.getFlow("consumer-source-test-topic").stop();
		Thread.sleep(1000);
		FlowRunner flowRunner = flowRunner("consumeTopicSuccessFlow");
		TypedValue<Object> payloadValue = (TypedValue<Object>) flowRunner.run().getMessage().getPayload().getValue();

		assertEquals("Invalid consumer message", "hello consume operation", payloadValue.getValue());

	}
	@SuppressWarnings("unchecked")
	@Test
	public void consumeTopicEndpointMissing() {
		try {
			FlowRunner flowRunner = flowRunner("consumeTopicEndpointMissing");
			TypedValue<Object> payloadValue = (TypedValue<Object>) flowRunner.run().getMessage().getPayload().getValue();
			fail("Expected exception as endpoint does not exist");
		} catch (Throwable t) {
		}

	}


}
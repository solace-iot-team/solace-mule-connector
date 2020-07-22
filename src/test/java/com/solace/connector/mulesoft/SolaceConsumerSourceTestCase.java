package com.solace.connector.mulesoft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mule.functional.api.flow.FlowRunner;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.construct.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolaceConsumerSourceTestCase extends SolaceConnectorTestBaseClass {
	private static final int NUM_MSG = 3;

	private final Logger LOGGER = LoggerFactory.getLogger(SolaceConsumerSourceTestCase.class);

	private static CountDownLatch lock = new CountDownLatch(3);

	public static synchronized void countDown() {
		lock.countDown();
	}

	/**
	 * Specifies the mule config xml with the flows that are going to be executed in
	 * the tests, this file lives in the test resources.
	 */
	@Override
	protected String getConfigFile() {
		return "test-source-config.xml";
	}

	@Before
	public void setup() {
		lock = new CountDownLatch(NUM_MSG);
	}

	@Test
	public void testConsumer() throws Exception {
		LOGGER.info("Run test");

		new Thread(new Publisher("publishPersistentToConsumerFlow")).start();
		lock.await(6000, TimeUnit.MILLISECONDS);
		assertEquals(String.format("Did not receive all messages expected [%s], still awaiting [%d] messages", NUM_MSG,
				lock.getCount()), 0, lock.getCount());
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testConsumerNack() throws Exception{
		new Thread(new Publisher("publishPersistentToClientNackFlow")).start();
		LOGGER.info(String.format("Waiting to [%d] receive messages",lock.getCount()));
		lock.await(6000, TimeUnit.MILLISECONDS);
		assertEquals(String.format("Did not receive all messages expected [%s], still awaiting [%d] messages", NUM_MSG,
				lock.getCount()), 0, lock.getCount());
		// since we didn't ack the message let's see if we got messages on the queue
		Thread.sleep(1000);
		this.getFlow("consumer-nack-test").stop();
		FlowRunner flowRunner = flowRunner("consumeNackFlow");
		TypedValue<Object> payloadValue = ((TypedValue) flowRunner.run().getMessage().getPayload().getValue());
		assertNotNull(payloadValue);
		assertNotNull(payloadValue.getValue());

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testConsumerAck() throws Exception{
		new Thread(new Publisher("publishPersistentToClientAckFlow")).start();
		LOGGER.info(String.format("Waiting to [%d] receive messages",lock.getCount()));
		lock.await(6000, TimeUnit.MILLISECONDS);
		assertEquals(String.format("Did not receive all messages expected [%s], still awaiting [%d] messages", NUM_MSG,
				lock.getCount()), 0, lock.getCount());
		Thread.sleep(1000);
		this.getFlow("consumer-ack-test").stop();
		FlowRunner flowRunner = flowRunner("consumeAckFlow");
		TypedValue<Object> payloadValue = ((TypedValue) flowRunner.run().getMessage().getPayload().getValue());
		assertNull(payloadValue);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testCompletionOfFlowAck() throws Exception{
		new Thread(new Publisher("publishPersistentToEndAckFlow")).start();
		LOGGER.info(String.format("Waiting to [%d] receive messages",lock.getCount()));
		lock.await(6000, TimeUnit.MILLISECONDS);
		assertEquals(String.format("Did not receive all messages expected [%s], still awaiting [%d] messages", NUM_MSG,
				lock.getCount()), 0, lock.getCount());
		Thread.sleep(1000);
		this.getFlow("flow-ack-test").stop();
		FlowRunner flowRunner = flowRunner("consumeEndAckFlow");
		TypedValue<Object> payloadValue = ((TypedValue) flowRunner.run().getMessage().getPayload().getValue());
		assertNull(payloadValue);

	}
	
	@Test
	public void testTopicEndpointConsumer() throws Exception {
		LOGGER.info("Run test");

		new Thread(new Publisher("publishToTopicConsumer")).start();
		lock.await(6000, TimeUnit.MILLISECONDS);
		assertEquals(String.format("Did not receive all messages expected [%s], still awaiting [%d] messages", NUM_MSG,
				lock.getCount()), 0, lock.getCount());
		
	}

	@Test
	public void testTopicEndpointConsumerNoprovision() throws Exception {
		//LOGGER.info("Run test");
		this.getFlow("consumer-source-test-noprovision-setup").stop();
		this.getFlow("consumer-source-test-noprovision").start();
		Thread.sleep(1000);

		new Thread(new Publisher("publishToTopicConsumerNoprovision")).start();
		lock.await(6000, TimeUnit.MILLISECONDS);
		assertEquals(String.format("Did not receive all messages expected [%s], still awaiting [%d] messages", NUM_MSG,
				lock.getCount()), 0, lock.getCount());
		
	}

	
	class Publisher implements Runnable {
		
		String flowName;
		
		Publisher(String flowName){
			this.flowName = flowName;
		}
		
		@Override
		public void run() {
			FlowRunner flowRunner = flowRunner(this.flowName);
			try {
				for (int i = 0; i < 3; i++) {
					LOGGER.info("Publish message");
					flowRunner.run();
					Thread.sleep(50);
				}
			} catch (Exception e) {
				LOGGER.error("Problem Publishing message ", e);
			}

		}

	}
	
}

package com.solace.connector.mulesoft;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mule.functional.api.flow.FlowRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolaceListenerSourceTestCase extends SolaceConnectorTestBaseClass {
	private static final int NUM_MSG = 6;

	private final Logger LOGGER = LoggerFactory.getLogger(SolaceListenerSourceTestCase.class);

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
	public void executeListener() throws Exception {
		LOGGER.info("Run test");
		new Thread(new Publisher()).start();
		lock.await(10000, TimeUnit.MILLISECONDS);
		assertEquals(String.format("Did not receive all messages expected [%s]", NUM_MSG), 0, lock.getCount());
	}

	class Publisher implements Runnable {

		@Override
		public void run() {
			FlowRunner flowRunner = flowRunner("publishToListener");
			try {
				for (int i = 0; i < 3; i++) {
					LOGGER.info("Publish message");
					flowRunner.run();
					Thread.sleep(20);
				}
			} catch (Exception e) {
				LOGGER.error("Problem Publishing message ", e);
			}

		}

	}
}

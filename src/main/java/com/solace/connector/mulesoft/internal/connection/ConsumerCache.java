package com.solace.connector.mulesoft.internal.connection;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.solace.connector.mulesoft.internal.cache.FlowReceiverRemovalListener;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.FlowReceiver;

@Singleton
public class ConsumerCache  {
	private final Logger LOGGER = LoggerFactory.getLogger(ConsumerCache.class);
			
	private ScheduledFuture<?> scheduledFuture;
	private Cache<String, BytesXMLMessage> messageCache = CacheBuilder.newBuilder()
			.expireAfterWrite(5000, TimeUnit.MILLISECONDS).build();

	private Cache<String, FlowReceiver> flowReceiverCache = CacheBuilder.newBuilder()
			.expireAfterWrite(5000, TimeUnit.MILLISECONDS).removalListener(new FlowReceiverRemovalListener()).build();

	private Set<String> autoProvisionedEndpoints = new HashSet<String>();
	
	private ScheduledExecutorService cacheCleanUpService;
	
	public ConsumerCache() {
		init();
	}
	
	@PostConstruct
	private void init() {
		// schedule clean ups of caches
		cacheCleanUpService = Executors.newScheduledThreadPool(1);
		scheduledFuture = cacheCleanUpService.scheduleAtFixedRate(new Runnable() {
			
			public void run() {
				messageCache.cleanUp();
				flowReceiverCache.cleanUp();
			}
		}, 5, 5, TimeUnit.SECONDS);

	}
	
	@PreDestroy
	private void shutdown() {
		scheduledFuture.cancel(true);
	}
	
	public void put(BytesXMLMessage msg) {
		if (msg == null) {
			LOGGER.warn("Message is null");
			return;
		}
		messageCache.put(msg.getMessageId(), msg);
	}
	public void put(String msgId, FlowReceiver rcvr) {
		if (msgId == null || msgId.isEmpty() || rcvr == null) {
			LOGGER.warn("Message Id or Flow receiver is null");
			return;
		}
		this.flowReceiverCache.put(msgId, rcvr);
	}
	
	public void put(BytesXMLMessage msg, FlowReceiver flowReceiver) {
		if (msg == null) {
			LOGGER.warn("Message is null");
			return;
		}
		this.put(msg);
		this.put(msg.getMessageId(), flowReceiver);
	}
	
	public BytesXMLMessage getMessage(String msgId) {
		return messageCache.getIfPresent(msgId);
	}
	public FlowReceiver getFlowReceiver(String msgId) {
		return flowReceiverCache.getIfPresent(msgId);
	}
	
	public void invalidateMessage(String msgId) {
		if (msgId == null || msgId.isEmpty()) 
			return;
		this.messageCache.invalidate(msgId);
		
	}
	public void invalidateFlowReceiver(String msgId) {
		if (msgId == null || msgId.isEmpty()) 
			return;
		this.flowReceiverCache.invalidate(msgId);
	}

	public void invalidate(String msgId) {
		if (msgId == null || msgId.isEmpty()) 
			return;
		this.invalidateFlowReceiver(msgId);
		this.invalidateMessage(msgId);
	}
	
	public void putEndpoint(String name) {
		if (name == null || name.isEmpty())
			return;
		this.autoProvisionedEndpoints.add(name);
	}
	
	public boolean hasEndpoint(String name) {
		if (name == null) {
			return false;
		}
		return this.autoProvisionedEndpoints.contains(name);
	}
	
}

# Solace PubSub+ Connector for Mulesoft

## What is the Connector?

Integrate any Solace PubSub+ broker with SaaS and other applications with a native Solace connector, which is based on the JCSMP Java API.

Read through this user guide to understand how to install the connector and use it to interact with the Solace broker.

## Prerequisites

### Software and Hardware

* Mulesoft Runtime and Anypoint Studio, see requirements on the Mulesoft web site.
* Access to a Solace PubSub+ broker

### Compatibility

The connector ...
* requires a Mulesoft Runtime 4.1 (and above), Community or Enterprise Edition and Anypoint Studio 7.1 (and above).
* uses the Solace JCSMP API 10.8.1 and is compatible with Solace appliances and software versions 7.1.1 and above.

## How to Install

### Add Solace Maven Repository

The connector is currently hosted on third party repository. 
Please add the repository to your Mule Project's pom.xml file:

```
	<repositories>
	<!-- keep all the mulesoft repositories that are configured here --->
		<repository>
			<id>myMavenRepo.read</id>
			<url>https://mymavenrepo.com/repo/27AIdW4GdyXFzutegEs5/</url>
		</repository>
	</repositories>
```

### Add Maven Dependency

Add the connector dependency to your project's pom.xml file

```
<dependency>
	<groupId>com.solace.connector</groupId>
	<artifactId>solace-mulesoft-connector</artifactId>
	<version>0.3.2-SNAPSHOT</version>
	<classifier>mule-plugin</classifier>
</dependency>
```

## How to Configure


The connector configuration provides broker connection parameters, connection and producer settings and governs consumer behaviour such as message acknowledgements.

The configuration element maps to an underlying broker connection. If you need a specific behaviour for different operations you need to provide additional configurations in your flow.
The default values in the configuration match the default values used in the JCSMP API and should provide sensible behaviour.

It is recommended to create separate configurations for each listener (source) operation used in one flow.

### General Configuration

This section contains the major connection and session settings.

#### Connection

Configure the connection to the Solace PubSub+ broker via a plain, compressed or TLS encrypted transport. Currently supports basic authentication. 

* Broker Host: the host name or IP address of the broker
* Broker Port: the port to be used, must correspond to the setting for "Use TLS"
* Use TLS to connect: set to true if encryption/TLS shall be used, make sure to provide the correct port (55443 by default)
* Message VPN: the Message VPN to connect to on the broker
* Client Username: username for the connection
* Client password: the corresponding password 

#### Advanced

Set the session and client channel properties (see Resources for more information)

Client Channel Properties:
* Keep Alive Interval: The amount of time (in ms) to wait between sending out keep-alive messages., in milliseconds
* Keep Alive Limit: The maximum number of consecutive keep-alive messages that can be sent without receiving a response before the connection is closed.
* Send Buffer: The size (in bytes) of the send socket buffer.
* Receive Buffer: The size (in bytes) of the receive socket buffer.
* TCP Nodelay: Whether to set the TCP_NODELAY option. When enabled, this option disables the Nagle's algorithm.
* Compression Level: A compressionLevel setting of 1-9 sets the ZLIB compression level to use; a setting of 0 disables compression entirely.

Session Properties:
* Publisher ACK Window Size: The size of the sliding publisher window for Guaranteed messages. The Guaranteed Message Publish Window Size property limits the maximum number of messages that can be published before an acknowledgement from the broker must be received.
* Publisher ACK Time: The duration of the publisher acknowledgement timer (in milliseconds). When a published message is not acknowledged within the time specified for this timer it is retransmitted.
* Generate Receive Timestamps: Indicates whether to generate a receive timestamp on incoming messages.
* Generate Send Timestamps: Indicates whether to generate a send timestamp in outgoing messages.
* Generate Sequence Numbers: Indicates whether to generate a sequence number in outgoing messages.

#### Endpoint Consumer

General:
* Queue Access Type: access type for the endpoint

Consumer Acknowledgement Configuration (applies to guaranteed messaging)
* Acknowledgement Mode: the message consumer flow's acknowledgement mode, one of
    * `AUTOMATIC_IMMEDIATE`: automatically acknowledge messages immediately, maps to SUPPORTED_MESSAGE_ACK_AUTO mode in JCSMP
    * `AUTOMATIC_ON_FLOW_COMPLETION`: acknowledge messages automatically once a flow is completed, **applies to OnGuaranteedMessage source only, otherwise behaves like `MANUAL_CLIENT`**, maps to `SUPPORTED_MESSAGE_ACK_CLIENT` in JCSMP however the connector acknowledges messages automatically.
    * `MANUAL_CLIENT`: requires a manual acknowledgement of a received message using the Ack operation, maps to `SUPPORTED_MESSAGE_ACK_CLIENT` in JCSMP
* Windows Size: The size of the sliding consumer ACK window.
* Acknowledgement Threshold: The threshold for sending an acknowledgement, configured as a percentage.
* Acknowledgement Time: The duration of the subscriber ACK timer (in milliseconds). 

**Note: the Solace Connector provides no explicit negative acknowledgement (nack) operation, your flow must make sure to manually acknowledge messages. Specifically flows using OnGuaranteedMessage source may run into issues as the underlying Solace consumer is kept-alive **

Flow Reconnect Policy (relates to the Solace Message Consumer Flow):
* Retry Count:  number of times to attempt to reconnect to an endpoint after the initial bound flow goes down
* Retry Interval: how much time to wait between each attempt to reconnect to an endpoint
* Retry Interval Unit: unit for interval

## Message Types and MIME Types

The connector converts Solace message types to Java objects or Mulesoft messages:

| Solace Message Type | Payload Type         | Media/MIME Type |
| ------------------- | -------------------- | ---------- |
| TextMessage         | String               | Determines MIME type in this order: [message content type](https://docs.solace.com/API-Developer-Online-Ref-Documentation/java/com/solacesystems/jcsmp/XMLMessage.html#getHTTPContentType()), operation's content type property, text/plain |
| MapMessage          | java.util.Map        | text/plain           |
| StreamMessage       | java.util.List       | text/plain           |
| Other               | byte[]               |  Determines MIME type in this order: [message content type](https://docs.solace.com/API-Developer-Online-Ref-Documentation/java/com/solacesystems/jcsmp/XMLMessage.html#getHTTPContentType()), operation's content type property, text/plain        |

When converting Mulesoft messages to Solace Messages the [message content type](https://docs.solace.com/API-Developer-Online-Ref-Documentation/java/com/solacesystems/jcsmp/XMLMessage.html#getHTTPContentType()) is set on the Solace message.
At first it will be attempted to determine the MIME type will from the Mule message falling back to the content type provided in the operation. 
| Payload Type |  Solace Message Type |
| --- | ---|
| String | TextMessage |
| java.util.Map | MapMessage |
| java.util.Collection | StreamMessage |
| java.util.Iterator | StreamMessage |
| byte[] | BytesMessage |
| OutputHandler | BytesMessage |

## Consumer and Message Cache

The connector caches consumers and messages for manual acknowledgement and cache entries expire after 10 seconds.
Using time outs for operations such as `consume` or flow run durations due to slow  downstream systems that lead to execeeding the 10 second expiry may result in errors or other undesirable behaviour.

## Operations

The connector provides the following operations:
* Publish Direct: publish a direct message to a topic
* Publish Guaranteed: publishes a message to a queue
* Consume: consumes a guaranteed message from an endpoint - a queue or topic endpoint
* Request-Reply Direct: blocking operation sends a direct message to a topic and waits for a response on the reply-to topic specified by the sender
* Request-Reply Guaranteed: blocking operation sends a guaranteed message to a queue and waits for a response on the reply-to temporary queue created by the sender
* Ack: acknowledges receipt of a guaranteed message 


### Common Elements for Operations

Basic Settings - Connector configuration:
* choose an existing or create a new connection. Take the notes in the configuration section into account when deciding which configuration to re-use or when to create a new configuration.

Message - Operations that publish messages use the same Mule elements:
* Body: the payload of the message, can be a String or any other Mule data structure such as JSON, XML, Pojo. See "Message Types and MIME Types" above
* Content Type: the MIME content type of the message, if not present the MIME type will be determined automatically.
* Encoding: the encoding used for serialisation of the payload to a String
* Correlation id: a correlation id when sending a message as a response to an incoming message
* Mark reply: marks the messages as a response to a request message that was sent e.g. via a RequestReplyDirect operation

The metadata of received messages is exposed in message properties:
* correlationId: the correlation id of the message, this is useful 
* destination: topic or queue that the message was sent to
* endpointType: endpoint type - queue or topic that the message was received from.
* messageId: message id
* reply: indicates this message was marked as a reply to a request message
* replyTo: destination (topic, queue) that a response is expected to be sent to in asnwer to a request message
* replyToEndpointType: endpoint type of the replyTo destination
* senderTimestamp: time stamp of message set by sender
* sequenceNumber: sequence number of message

### Publish Direct

Sends a direct message to a Solace topic.

General
* Topic: specify the topic
* Message:
    * See common elements section


### Publish Guaranteed

Sends a guaranteed message to a Solace broker.

General
* Endpoint: specify the endpoint
    * Name: of the queue or topic
    * Type: queue or topic
* Message:
    * See common elements section

Advanced
* General:
    * Provision queue: indicates if the queue shall be automatically created, will attempt to create if not present. Not applicable to topic

### Consume

Blocking operation that consumes a message from a queue or topic endpoint with a time out. If the time out is exceeded the processing resumes with an undefined/null messages.
The flow needs to handle this case accordingly.

General:
* Endpoint:
    * Name: Name of queue or topic endpoint
    * Type: queue or topic endpoint
    * Selector: enable client applications to specify which messages they are interested in receiving, as determined by the messages’ header field and property values, [Message Selectors](https://solace.com/samples/solace-samples-java/feature_message-selectors/)
    * Subscription: a subscription to add to the endpoint, required for topic endpoints, uses SMF topic subscription syntax.
    * Content Type: the MIME content type of the message, if not present the MIME type will be determined automatically
    * Encoding: the encoding used for serialisation of the payload to a String
* Time Out:
    * Time out: time to wait for a message
    * Time out interval: interval time unit

### Request-Reply Direct

Blocking operation sends a direct message to a topic and waits for a response on the reply-to topic automatically specified by the sender.
Throws a SOLACE.REQUEST_TIME_OUT exception if no response was received.

General
* Topic: specify the topic to send the request to
* Message:
    * See common elements section


### Request-Reply Guaranteed

Blocking operation sends a guaranteed message to a queue or topic and waits for a response on the reply-to temporary queue automatically created by the sender.
Throws a SOLACE.REQUEST_TIME_OUT exception if no response was received.

General:
* Endpoint:
    * Name: name of the queue or topic
    * Type: queue or topic 
* Message:
    * See common elements section


### Ack

Acknowledges receipt of a guaranteed message, this operation has no effect if the acknowledgement mode is  `AUTOMATIC_IMMEDIATE`.

General
* Message Id: the id of the message to acknowledge, use a message id returned in the Message Properties of a received message, e.g. the output of a Consume operation. 

## Sources

The connector provides two listeners that trigger flows on receipt of a message:
* OnDirectMessage: triggered when a direct message is received on a set of topic subscriptions.
* OnGuaranteedMessage: triggered when a guaranteed message is received on a queue or topic endpoint.

### OnDirectMessage

Triggered when a direct message is received on a set of topic subscriptions.

General:
* Content Type: MIME type of the message, this is used if the MIME type can't be determined from the message. See Message Types and MIME Types above.
* Subscriptions: a list of topic subscriptions that will be applied to the source, uses SMF topic subscription syntax.

### OnGuaranteedMessage

Triggered when a guaranteed message is received on a queue or topic endpoint.

General:
* Endpoint:
    * Name: Name of queue or topic endpoint
    * Type: queue or topic endpoint
    * Selector: enable client applications to specify which messages they are interested in receiving, as determined by the messages’ header field and property values,  [Message Selectors](https://solace.com/samples/solace-samples-java/feature_message-selectors/)
    * Subscription: a subscription to add to the endpoint, required for topic endpoints, uses SMF topic subscription syntax.
    * Content Type: the MIME content type of the message, if not present the MIME type will be determined automatically
    * Encoding: the encoding used for serialisation of the payload to a String    
Advanced
* General:
    * Provision queue: indicates if the queue shall be automatically created, will attempt to create if not present. Not applicable to topic
     
## Examples

You can find some examples that you can import into Anypoint Studio at [solace-mule-connector-examples](https://github.com/solace-iot-team/solace-mule-connector-examples):

* Listen for and send messages  - direct and guaranteed
* Using consume operation
* Request-reply with a responder service
* Manual Message Acknowledgement
* Complex message - JSON payload

## Resources

### Solace PubSub+ Getting Started

You can set up a Solace broker to try this connector on  
* [Solace Cloud](https://docs.solace.com/Solace-Cloud/cloud-getting-started.htm)
* [Using docker](https://docs.solace.com/Solace-SW-Broker-Set-Up/Docker-Containers/Set-Up-Docker-Container-Image.htm)

### Configuration Parameter Documentation

The parameters exposed by configuration, operations and listeners represent parameters of the underlying Java API.

These are documented here:
* [JCSMPProperties](https://docs.solace.com/API-Developer-Online-Ref-Documentation/java/com/solacesystems/jcsmp/JCSMPProperties.html)
* [JCSMPChannelProperties](https://docs.solace.com/API-Developer-Online-Ref-Documentation/java/com/solacesystems/jcsmp/JCSMPChannelProperties.html)
* [ConsumerFlowProperties](https://docs.solace.com/API-Developer-Online-Ref-Documentation/java/com/solacesystems/jcsmp/ConsumerFlowProperties.html)

For more background on individual properties consult [docs.solace.com](https://docs.solace.com)



### JCSMP Java API Documentation

[docs.solace.com/API-Developer-Online-Ref-Documentation/java/index.html](https://docs.solace.com/API-Developer-Online-Ref-Documentation/java/index.html)
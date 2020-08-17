package com.solace.connector.mulesoft.jcsmp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.MediaTypeUtils;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.message.OutputHandler;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.solace.connector.mulesoft.api.SolaceMessageProperties;
import com.solacesystems.jcsmp.BytesMessage;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.MapMessage;
import com.solacesystems.jcsmp.Message;
import com.solacesystems.jcsmp.SDTException;
import com.solacesystems.jcsmp.SDTMap;
import com.solacesystems.jcsmp.SDTStream;
import com.solacesystems.jcsmp.StreamMessage;
import com.solacesystems.jcsmp.TextMessage;

@Component
public class MessageConverter {
	private final Logger LOGGER = LoggerFactory.getLogger(MessageConverter.class);

	public MessageConverter() {

	}

	public Result<TypedValue<Object>, SolaceMessageProperties> convertMessageToObject(BytesXMLMessage msg,
			String contentType) {
		TypedValue<Object> payload = null;
		DataType dt = null;

		if (msg == null) {
			SolaceMessageProperties props = new SolaceMessageProperties(msg);
			Result<TypedValue<Object>, SolaceMessageProperties> result = Result
					.<TypedValue<Object>, SolaceMessageProperties>builder().output(payload).attributes(props).build();
			return result;
		}

		LOGGER.debug(String.format("Processing solace message %s", msg.getClass().getCanonicalName()));
		if (msg instanceof TextMessage) {
			payload = convertFromTextMessage(msg, contentType);
		} else if (msg instanceof MapMessage) {
			payload = convertFromMapMessage(msg);
		} else if (msg instanceof StreamMessage) {
			payload = convertFromStreamMessage(msg);

		} else {
			payload = convertFromBytesMessage(msg, contentType);

		}
		SolaceMessageProperties props = new SolaceMessageProperties(msg);
		Result<TypedValue<Object>, SolaceMessageProperties> result = Result
				.<TypedValue<Object>, SolaceMessageProperties>builder().output(payload).attributes(props).build();
		return result;
	}

	private TypedValue<Object> convertFromStreamMessage(BytesXMLMessage msg) {
		TypedValue<Object> payload;
		DataType dt;
		dt = DataType.builder().asCollectionTypeBuilder().itemMediaType("text/plain;charset=UTF-8")
				.itemType(Object.class).mediaType("text/plain;charset=UTF-8").build();
		StreamMessage streamMsg = (StreamMessage) msg;
		List<Object> listPayload = new ArrayList<Object>();
		SDTStream stream = streamMsg.getStream();
		Object o;
		try {
			while ((o = stream.read()) != null) {
				listPayload.add(o);
			}
		} catch (SDTException e) {
			LOGGER.error("Error processing stream message", e);
		}
		payload = new TypedValue<Object>(listPayload, dt);
		return payload;
	}

	private TypedValue<Object> convertFromMapMessage(BytesXMLMessage msg) {
		TypedValue<Object> payload;
		DataType dt;
		dt = DataType.builder().mapType(Map.class).keyType(String.class).keyMediaType("text/plain;charset=UTF-8")
				.valueType(Object.class).valueMediaType("text/plain;charset=UTF-8")
				.mediaType("text/plain;charset=UTF-8").build();
		Map<String, Object> mapPayload = new HashMap<>();
		MapMessage m = (MapMessage) msg;
		SDTMap msgMap = m.getMap();
		msgMap.keySet().forEach(k -> {
			try {
				mapPayload.put(k, msgMap.get(k));
			} catch (SDTException e) {
				LOGGER.error(String.format("Could not map data for key [%s]", k), e);
			}
		});
		payload = new TypedValue<Object>(mapPayload, dt);
		return payload;
	}

	private TypedValue<Object> convertFromTextMessage(BytesXMLMessage msg, String contentType) {
		TypedValue<Object> payload;
		DataType dt;
		String mediaType = msg.getHTTPContentType();
		if (StringUtils.isBlank(mediaType)) {
			// fall back to configured content type or set to plain text
			if (!StringUtils.isBlank(contentType)) {
				mediaType = contentType;
			} else {
				mediaType = MediaType.TEXT.toRfcString();
			}
		}
		String encoding = msg.getHTTPContentEncoding();
		if (StringUtils.isBlank(encoding)) {
			encoding = "utf-8";
		}
		String textPayload = ((TextMessage) msg).getText();
		dt = DataType.builder().type(String.class).mediaType(mediaType).charset(encoding).build();

		payload = new TypedValue<Object>(textPayload, dt);
		return payload;
	}

	private TypedValue<Object> convertFromBytesMessage(BytesXMLMessage msg, String contentType) {
		TypedValue<Object> payload;
		DataType dt;
		String mediaType = msg.getHTTPContentType();
		if (StringUtils.isBlank(mediaType)) {
			// fall back to configured content type or set to plain text
			mediaType = contentType;
		}
		String encoding = msg.getHTTPContentEncoding();
		if (StringUtils.isBlank(encoding)) {
			encoding = "utf-8";
		}
		byte[] bytesPayload = msg.getBytes();
		if (msg instanceof BytesMessage) {
			bytesPayload = ((BytesMessage) msg).getData();
		}
		if (!StringUtils.isBlank(mediaType))
			dt = DataType.builder().type(String.class).mediaType(mediaType).charset(encoding).build();
		else 
			dt = DataType.BYTE_ARRAY;
		payload = new TypedValue<Object>(bytesPayload, dt);
		return payload;
	}

	@SuppressWarnings("rawtypes")
	public Message convertObjectToMessage(TypedValue<Object> typedValueObject, String encoding, String contentType)
			throws SDTException {
		Object object = typedValueObject.getValue();

		Class<? extends Object> typedValueClass = typedValueObject.getValue()!=null?typedValueObject.getValue().getClass():null;
		LOGGER.debug(String.format("Converting to a Solace Message: datatype=[%s], valueClass=[%s] ",
				typedValueObject.getDataType(), typedValueClass));
		if (object instanceof CursorProvider) {
			object = ((CursorProvider) object).openCursor();
		}

		Message msg = null;
		if (object == null) {
			throw new RuntimeException("Message body was 'null', which is not a value of a supported type");
		} else if (object instanceof Message) {
			msg = (Message) object;
		} else if (object instanceof String) {
			msg = stringToMessage((String) object);
		} else if (object instanceof Map && validateMapMessageType((Map) object)) {
			return mapToMessage((Map) object);
		} else if (object instanceof InputStream) {
			msg = MediaTypeUtils.isStringRepresentable(typedValueObject.getDataType().getMediaType())
					? stringToMessage(IOUtils.toString((InputStream) object, getCharset(typedValueObject, encoding)))
					: byteArrayToMessage(IOUtils.toByteArray((InputStream) object));
		} else if (object instanceof Collection<?>) {
			msg = collectionToMessage((Collection<?>) object);
		} else if (object instanceof Iterator) {
			msg = iteratorToMessage((Iterator) object);
		} else if (object instanceof byte[]) {
			msg = byteArrayToMessage((byte[]) ((byte[]) object));
		} else if (object instanceof OutputHandler) {
			msg = outputHandlerToMessage((OutputHandler) object);
		} else {
			throw new RuntimeException(
					"Message body was not of a supported type. Valid types are Message, String, Map, InputStream, List, byte[], Serializable or OutputHandler, but was "
							+ getClassName(object));
		}
		// set content type and encoding
		String mimeType = typedValueObject.getDataType().getMediaType().toRfcString();
		if (StringUtils.isBlank(mimeType)) {
			mimeType = contentType;
		}
		msg.setHTTPContentType(mimeType);
		msg.setHTTPContentEncoding(encoding);

		return msg;
	}

	private Message stringToMessage(String payload) {
		TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
		msg.setText(payload);
		return msg;
	}

	private static boolean validateStreamMessageType(Object candidate) {
		return candidate == null || candidate instanceof Boolean || candidate instanceof Byte
				|| candidate instanceof Short || candidate instanceof Character || candidate instanceof Integer
				|| candidate instanceof Long || candidate instanceof Float || candidate instanceof Double
				|| candidate instanceof String || candidate instanceof byte[];
	}

	private static boolean validateMapMessageType(Map<?, ?> candidate) {
		return candidate.values().stream().allMatch(MessageConverter::validateStreamMessageType);
	}

	private static Message mapToMessage(Map<?, ?> value) throws SDTException {
		MapMessage mMsg = JCSMPFactory.onlyInstance().createMessage(MapMessage.class);
		Iterator<?> entryIterator = value.entrySet().iterator();
		SDTMap map = JCSMPFactory.onlyInstance().createMap();
		while (entryIterator.hasNext()) {
			Entry<?, ?> entry = (Entry<?, ?>) entryIterator.next();
			map.putObject(entry.getKey().toString(), entry.getValue());
		}
		mMsg.setMap(map);
		return mMsg;
	}

	private static Charset getCharset(TypedValue<Object> typedValueObject, String encoding) {
		return (Charset) typedValueObject.getDataType().getMediaType().getCharset()
				.orElse(!StringUtils.isBlank(encoding) ? Charset.forName(encoding) : null);
	}

	private static Message byteArrayToMessage(byte[] value) {
		BytesMessage bMsg = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);
		bMsg.clearContent();
		bMsg.writeBytes(value);
		return bMsg;
	}

	private static Message collectionToMessage(Collection<?> value) {
		return iteratorToMessage(value.iterator());
	}

	private static Message iteratorToMessage(Iterator<?> value) {
		StreamMessage sMsg = JCSMPFactory.onlyInstance().createMessage(StreamMessage.class);

		while (value.hasNext()) {
			Object o = value.next();
			addObjectToStreamMessage(sMsg, o);
		}

		return sMsg;
	}

	private static void addObjectToStreamMessage(StreamMessage sMsg, Object o) {
		if (validateStreamMessageType(o)) {
			sMsg.getStream().writeObject(o);
		} else {
			throw new RuntimeException(String.format(
					"Invalid type passed to StreamMessage: %s . Allowed types are: Boolean, Byte, Short, Character, Integer, Long, Float, Double,String and byte[]",
					getClassName(o)));
		}
	}

	private static String getClassName(Object object) {
		return object == null ? "null" : object.getClass().getName();
	}

	private static Message outputHandlerToMessage(OutputHandler value) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		try {
			value.write((CoreEvent) null, output);
		} catch (IOException e) {
			throw new RuntimeException("Could not serialize OutputHandler.", e);
		}

		BytesMessage bMsg = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);
		bMsg.writeBytes(output.toByteArray());

		return bMsg;
	}

}

package com.noctarius.tengi.serialization.impl;

import com.noctarius.tengi.Identifier;
import com.noctarius.tengi.Message;
import com.noctarius.tengi.SystemException;
import com.noctarius.tengi.buffer.ReadableMemoryBuffer;
import com.noctarius.tengi.buffer.WritableMemoryBuffer;
import com.noctarius.tengi.config.MarshallerConfiguration;
import com.noctarius.tengi.serialization.Protocol;
import com.noctarius.tengi.serialization.TypeId;
import com.noctarius.tengi.serialization.debugger.DebuggableMarshaller;
import com.noctarius.tengi.serialization.debugger.DebuggableProtocol;
import com.noctarius.tengi.serialization.marshaller.Identifiable;
import com.noctarius.tengi.serialization.marshaller.Marshaller;
import com.noctarius.tengi.serialization.marshaller.MarshallerFilter;
import com.noctarius.tengi.utils.ExceptionUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultProtocol
        implements Protocol, DebuggableProtocol, DefaultProtocolConstants {

    private final Map<Short, Class<?>> typeById = new ConcurrentHashMap<>();
    private final Map<Class<?>, Short> reverseTypeId = new ConcurrentHashMap<>();

    private final ConcurrentMap<Class<?>, Marshaller> marshallerCache = new ConcurrentHashMap<>();

    private final Map<MarshallerFilter, Marshaller> marshallers = new HashMap<>();
    private final Map<Short, Marshaller> marshallerById = new ConcurrentHashMap<>();
    private final Map<Marshaller, Short> reverseMarshallerId = new ConcurrentHashMap<>();

    public DefaultProtocol(Collection<MarshallerConfiguration> marshallerConfigurations) {
        this(null, marshallerConfigurations);
    }

    public DefaultProtocol(InputStream is, Collection<MarshallerConfiguration> marshallerConfigurations) {
        ClassLoader classLoader = getClass().getClassLoader();
        registerInternalTypes(classLoader);
        typesInitializer(classLoader.getResourceAsStream(TYPE_MANIFEST_FILENAME));
        if (is != null) {
            typesInitializer(is);
        }
        registerInternalMarshallers();
        registerMarshallers(marshallerConfigurations);
    }

    private void registerInternalTypes(ClassLoader classLoader) {
        try {
            Enumeration<URL> resources = classLoader.getResources(TYPE_DEFAULT_MANIFEST_FILENAME);
            while (resources.hasMoreElements()) {
                typesInitializer(resources.nextElement().openStream());
            }

        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    @Override
    public String getMimeType() {
        return PROTOCOL_MIME_TYPE;
    }

    @Override
    public void writeTypeId(Object value, WritableMemoryBuffer memoryBuffer) {
        Class<?> type;
        if (value instanceof Class) {
            type = (Class<?>) value;
        } else {
            type = value.getClass();
        }
        Short typeId = reverseTypeId.get(type);

        if (typeId == null) {
            throw new SystemException("TypeId for type '" + type.getName() + "' not found. Not registered?");
        }
        memoryBuffer.writeShort(typeId);
    }

    @Override
    public <T> Class<T> readTypeId(ReadableMemoryBuffer memoryBuffer) {
        short typeId = memoryBuffer.readShort();
        return (Class<T>) typeById.get(typeId);
    }

    @Override
    public Object readTypeObject(ReadableMemoryBuffer memoryBuffer) {
        try {
            Class<?> clazz = readTypeId(memoryBuffer);
            return clazz.newInstance();
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    @Override
    public Class<?> findType(ReadableMemoryBuffer memoryBuffer) {
        int readerIndex = memoryBuffer.readerIndex();
        try {
            short typeId = memoryBuffer.readShort();
            Class<?> clazz = typeById.get(typeId);
            if (clazz != null) {
                return clazz;
            }
            Marshaller<?> marshaller = marshallerById.get(typeId);
            if (marshaller != null && marshaller instanceof DebuggableMarshaller) {
                return ((DebuggableMarshaller<?>) marshaller).findType(memoryBuffer, this);
            }
            return null;
        } finally {
            memoryBuffer.readerIndex(readerIndex);
        }
    }

    @Override
    public <O> O readObject(ReadableMemoryBuffer memoryBuffer)
            throws Exception {

        short typeId = memoryBuffer.readShort();
        Marshaller marshaller = marshallerById.get(typeId);
        return (O) marshaller.unmarshall(memoryBuffer, this);
    }

    @Override
    public <O> void writeObject(O object, WritableMemoryBuffer memoryBuffer)
            throws Exception {

        Marshaller marshaller = computeMarshaller(object);
        memoryBuffer.writeShort(findMarshallerId(marshaller));
        marshaller.marshall(object, memoryBuffer, this);
    }

    private void registerInternalMarshallers() {
        // External types
        registerMarshaller(PacketMarshallerFilter.INSTANCE, PacketMarshaller.INSTANCE);
        registerMarshaller(MarshallableMarshallerFilter.INSTANCE, MarshallableMarshaller.INSTANCE);

        // Internal types
        registerMarshaller(Message.class, CommonMarshaller.MessageMarshaller.INSTANCE);
        registerMarshaller(Identifier.class, CommonMarshaller.IdentifierMarshaller.INSTANCE);
        registerMarshaller(Byte.class, CommonMarshaller.ByteMarshaller.INSTANCE);
        registerMarshaller(byte.class, CommonMarshaller.ByteMarshaller.INSTANCE);
        registerMarshaller(Short.class, CommonMarshaller.ShortMarshaller.INSTANCE);
        registerMarshaller(short.class, CommonMarshaller.ShortMarshaller.INSTANCE);
        registerMarshaller(Integer.class, CommonMarshaller.IntegerMarshaller.INSTANCE);
        registerMarshaller(int.class, CommonMarshaller.IntegerMarshaller.INSTANCE);
        registerMarshaller(Long.class, CommonMarshaller.LongMarshaller.INSTANCE);
        registerMarshaller(long.class, CommonMarshaller.LongMarshaller.INSTANCE);
        registerMarshaller(Float.class, CommonMarshaller.FloatMarshaller.INSTANCE);
        registerMarshaller(float.class, CommonMarshaller.FloatMarshaller.INSTANCE);
        registerMarshaller(Double.class, CommonMarshaller.DoubleMarshaller.INSTANCE);
        registerMarshaller(double.class, CommonMarshaller.DoubleMarshaller.INSTANCE);
        registerMarshaller(String.class, CommonMarshaller.StringMarshaller.INSTANCE);
        registerMarshaller(byte[].class, CommonMarshaller.ByteArrayMarshaller.INSTANCE);
    }

    private void registerMarshallers(Collection<MarshallerConfiguration> marshallerConfigurations) {
        marshallerConfigurations.forEach((config) -> registerMarshaller(config.getMarshallerFilter(), config.getMarshaller()));
    }

    private void registerMarshaller(MarshallerFilter filter, Marshaller marshaller) {
        short marshallerId = findMarshallerId(marshaller);
        marshallers.put(filter, marshaller);
        marshallerById.put(marshallerId, marshaller);
        reverseMarshallerId.put(marshaller, marshallerId);
    }

    private <O> void registerMarshaller(Class<O> clazz, Marshaller marshaller) {
        short marshallerId = findMarshallerId(marshaller);
        marshallerCache.put(clazz, marshaller);
        marshallerById.put(marshallerId, marshaller);
        reverseMarshallerId.put(marshaller, marshallerId);
    }

    private void typesInitializer(InputStream is) {
        if (is == null) {
            return;
        }

        try {
            Reader pipeReader = new InputStreamReader(is, "UTF-8");
            BufferedReader reader = new LineNumberReader(pipeReader);

            String line;
            while ((line = reader.readLine()) != null) {
                registerTypeId(line.trim());
            }

        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    private void registerTypeId(String className)
            throws Exception {

        Class<?> clazz = Class.forName(className);

        TypeId annotation = clazz.getAnnotation(TypeId.class);
        if (annotation == null) {
            throw new SystemException("Registered serialization type is not annotated with @TypeId");
        }

        short typeId = annotation.value();

        reverseTypeId.put(clazz, typeId);
        typeById.put(typeId, clazz);
    }

    private <O> short findMarshallerId(Marshaller<O> marshaller) {
        Short marshallerId = reverseMarshallerId.get(marshaller);
        if (marshallerId != null) {
            return marshallerId;
        }

        if (marshaller instanceof Identifiable) {
            return ((Identifiable<Short>) marshaller).identifier();
        }

        TypeId annotation = marshaller.getClass().getAnnotation(TypeId.class);
        if (annotation == null) {
            throw new SystemException("Registered marshaller type is not annotated with @TypeId");
        }

        return annotation.value();
    }

    private Marshaller computeMarshaller(Object object) {
        Class<?> clazz = object.getClass();
        Marshaller marshaller = marshallerCache.get(clazz);
        if (marshaller != null) {
            return marshaller;
        }

        marshaller = testMarshaller(object, PacketMarshallerFilter.INSTANCE, PacketMarshaller.INSTANCE);
        if (marshaller != null) {
            return marshaller;
        }

        marshaller = testMarshaller(object, MarshallableMarshallerFilter.INSTANCE, MarshallableMarshaller.INSTANCE);
        if (marshaller != null) {
            return marshaller;
        }

        for (Map.Entry<MarshallerFilter, Marshaller> entry : marshallers.entrySet()) {
            marshaller = testMarshaller(object, entry.getKey(), entry.getValue());
            if (marshaller != null) {
                return marshaller;
            }
        }
        throw new SystemException("No suitable marshaller found for type '" + clazz.getName() + "'");
    }

    private Marshaller testMarshaller(Object object, MarshallerFilter filter, Marshaller marshaller) {
        MarshallerFilter.Result result = filter.accept(object);
        if (result == MarshallerFilter.Result.AcceptedAndCache) {
            marshallerCache.putIfAbsent(object.getClass(), marshaller);
            return marshaller;
        } else if (result == MarshallerFilter.Result.Accepted) {
            return marshaller;
        }
        return null;
    }
}
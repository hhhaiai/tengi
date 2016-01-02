/*
 * Copyright (c) 2015-2016, Christoph Engelbert (aka noctarius) and
 * contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.noctarius.tengi.spi.serialization.impl;

import com.noctarius.tengi.core.exception.UnknownTypeException;
import com.noctarius.tengi.core.serialization.Enumerable;
import com.noctarius.tengi.core.serialization.TypeId;
import com.noctarius.tengi.core.serialization.codec.Decoder;
import com.noctarius.tengi.core.serialization.codec.Encoder;
import com.noctarius.tengi.core.serialization.debugger.DebuggableMarshaller;
import com.noctarius.tengi.core.serialization.marshaller.Marshaller;
import com.noctarius.tengi.spi.serialization.Protocol;

@TypeId(DefaultProtocolConstants.SERIALIZED_TYPE_ENUMERABLE)
enum EnumerableMarshaller
        implements Marshaller<Enumerable>, DebuggableMarshaller<Enumerable> {

    INSTANCE;

    @Override
    public Enumerable unmarshall(Decoder decoder, Protocol protocol)
            throws Exception {

        Class<Enumerable> clazz = protocol.readTypeId(decoder);

        int flag = decoder.readInt32();
        Enumerable constant = Enumerable.value(clazz, flag);
        if (constant != null) {
            return constant;
        }
        throw new UnknownTypeException("Enumerable type not found");
    }

    @Override
    public void marshall(Enumerable constant, Encoder encoder, Protocol protocol)
            throws Exception {

        protocol.writeTypeId(constant, encoder);
        encoder.writeInt32("flag", constant.flag());
    }

    @Override
    public Class<?> findType(Decoder decoder, Protocol protocol) {
        return protocol.readTypeId(decoder);
    }

    @Override
    public String debugValue(Object value) {
        return value.toString();
    }
}

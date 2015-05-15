/*
 * Copyright (c) 2015, Christoph Engelbert (aka noctarius) and
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
package com.noctarius.tengi.core.serialization.codec;

import com.noctarius.tengi.core.buffer.ReadableMemoryBuffer;

public interface Decoder {

    int readBytes(byte[] bytes);

    int readBytes(byte[] bytes, int offset, int length);

    boolean readBoolean();

    boolean[] readBitSet();

    byte readByte();

    short readUnsignedByte();

    short readShort();

    char readChar();

    int readInt32();

    int readCompressedInt32();

    long readInt64();

    long readCompressedInt64();

    float readFloat();

    double readDouble();

    String readString();

    <O> O readObject()
            throws Exception;

    <O> O readNullableObject()
            throws Exception;

    ReadableMemoryBuffer getReadableMemoryBuffer();

}
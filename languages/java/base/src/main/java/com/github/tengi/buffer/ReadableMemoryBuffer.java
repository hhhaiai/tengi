package com.github.tengi.buffer;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.github.tengi.UniqueId;

import java.nio.ByteBuffer;

public interface ReadableMemoryBuffer
{

    boolean readable();

    int readableBytes();

    int readBytes( byte[] bytes );

    int readBytes( byte[] bytes, int offset, int length );

    int readBuffer( ByteBuffer byteBuffer );

    int readBuffer( ByteBuffer byteBuffer, int offset, int length );

    int readBuffer( WritableMemoryBuffer memoryBuffer );

    int readBuffer( WritableMemoryBuffer memoryBuffer, int offset, int length );

    boolean readBoolean();

    byte readByte();

    short readUnsignedByte();

    short readShort();

    char readChar();

    int readInt();

    int readCompressedInt();

    long readLong();

    long readCompressedLong();

    float readFloat();

    double readDouble();

    String readString();

    UniqueId readUniqueId();

    int readerIndex();

    void readerIndex( int readerIndex );

}

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
package com.noctarius.tengi.server.impl;

import com.noctarius.tengi.core.connection.Transport;
import com.noctarius.tengi.core.model.Identifier;
import com.noctarius.tengi.core.model.Message;
import com.noctarius.tengi.spi.connection.AbstractConnection;
import com.noctarius.tengi.spi.connection.ConnectionContext;
import com.noctarius.tengi.spi.serialization.Serializer;

public class ClientConnection
        extends AbstractConnection {

    ClientConnection(ConnectionContext connectionContext, Identifier connectionId, //
                     Transport transport, Serializer serializer) {

        super(connectionContext, connectionId, transport, serializer);
    }

    public ConnectionContext getConnectionContext() {
        return super.getConnectionContext();
    }

    void publishMessage(Message message) {
        getMessageListeners().forEach((listener) -> listener.onMessage(this, message));
    }

    @Override
    public void notifyException(Throwable throwable) {
        super.notifyException(throwable);
    }

}

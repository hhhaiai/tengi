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
package com.noctarius.tengi.server.transport;

import com.noctarius.tengi.Transport;
import com.noctarius.tengi.connection.Connection;
import com.noctarius.tengi.server.transport.impl.http.HttpTransport;
import com.noctarius.tengi.server.transport.impl.tcp.TcpTransport;

public enum ServerTransport
        implements Transport {
    TCP_TRANSPORT(new TcpTransport()),
    HTTP_TRANSPORT(new HttpTransport());

    private final Transport transport;

    private ServerTransport(Transport transport) {
        this.transport = transport;
    }

    @Override
    public String getName() {
        return transport.getName();
    }

    @Override
    public boolean isStreaming() {
        return transport.isStreaming();
    }

    @Override
    public boolean accept(Connection connection) {
        return transport.accept(connection);
    }

}

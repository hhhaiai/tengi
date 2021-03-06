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
package com.noctarius.tengi.spi.connection.impl;

public final class TransportConstants {

    private TransportConstants() {
    }

    public static final String TRANSPORT_NAME_TCP = "tengi::transport::tengi";
    public static final String TRANSPORT_NAME_HTTP = "tengi::transport::http/1.1";
    public static final String TRANSPORT_NAME_HTTP2 = "tengi::transport::http/2.0";
    public static final String TRANSPORT_NAME_WEBSOCKET = "tengi::transport::websocket/binary";
    public static final String TRANSPORT_NAME_UDT = "tengi::transport::udt";
    public static final String TRANSPORT_NAME_UDP = "tengi::transport::udp";
    public static final String TRANSPORT_NAME_RDP = "tengi::transport::rdp";

    public static final int DEFAULT_PORT_TCP = 8080;
    public static final int DEFAULT_PORT_UDP = 9090;

    public static final String WEBSOCKET_RELATIVE_PATH = "/wss";

}

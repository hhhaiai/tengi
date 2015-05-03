package com.noctarius.tengi.server.transport.impl.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2FrameAdapter;
import io.netty.handler.codec.http2.Http2Headers;

public class Http2ConnectionProcessor
        extends Http2ConnectionHandler {

    private static final String HTTP_HEADER_NAME_CONNECTIONID = "XX-tengi-connection-id";

    private static final String HTTP_HEADER_NAME_SUPPORTED_TRANSPORT_TYPES = "XX-tengi-transport-types";

    private static final String HTTP_HEADER_NAME_TRANSPORT_TYPE = "XX-tengi-transport-type";

    public Http2ConnectionProcessor() {
        super(new DefaultHttp2Connection(true), new InternalFrameAdapter());
        ((InternalFrameAdapter) decoder().listener()).encoder(encoder());
    }

    private static class InternalFrameAdapter
            extends Http2FrameAdapter {

        private Http2ConnectionEncoder encoder;

        private void encoder(Http2ConnectionEncoder encoder) {
            this.encoder = encoder;
        }

        @Override
        public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endStream)
                throws Http2Exception {

            super.onHeadersRead(ctx, streamId, headers, padding, endStream);
        }

        @Override
        public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream)
                throws Http2Exception {

            return super.onDataRead(ctx, streamId, data, padding, endOfStream);
        }
    }

}

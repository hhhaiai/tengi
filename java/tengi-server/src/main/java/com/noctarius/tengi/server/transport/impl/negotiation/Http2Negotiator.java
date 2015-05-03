package com.noctarius.tengi.server.transport.impl.negotiation;

import com.noctarius.tengi.server.transport.impl.http2.Http2ConnectionProcessor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;
import java.util.List;

import static io.netty.handler.codec.http2.Http2CodecUtil.TLS_UPGRADE_PROTOCOL_NAME;

public class Http2Negotiator
        extends ByteToMessageDecoder {

    private final int maxHttpContentLength;

    public Http2Negotiator(int maxHttpContentLength) {
        this.maxHttpContentLength = maxHttpContentLength;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
            throws Exception {
        System.out.println(in);
        if (initPipeline(ctx)) {
            // When we reached here we can remove this handler as its now clear
            // what protocol we want to use
            // from this point on. This will also take care of forward all
            // messages.
            ctx.pipeline().remove(this);
        }
    }

    private boolean initPipeline(ChannelHandlerContext ctx) {
        // Get the SslHandler from the ChannelPipeline so we can obtain the
        // SslEngine from it.
        SslHandler handler = ctx.pipeline().get(SslHandler.class);
        if (handler == null) {
            // SSL is necessary to negotiate HTTP2, therefore it can only be HTTP
            switchToHttp(ctx);
            return true;
        }

        SelectedProtocol protocol = getProtocol(handler.engine());
        switch (protocol) {
            case UNKNOWN:
                // Not done with choosing the protocol, so just return here for now,
                //return false;
                switchToHttp(ctx);
                break;
            case HTTP_2:
                switchToHttp2(ctx);
                break;
            case HTTP_1_0:
            case HTTP_1_1:
                switchToHttp(ctx);
                break;
            default:
                throw new IllegalStateException("Unknown SelectedProtocol");
        }
        return true;
    }

    private void switchToHttp2(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.addLast("http2-connection-processor", new Http2ConnectionProcessor());
        pipeline.remove(this);
    }

    private void switchToHttp(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.addLast("httpCodec", new HttpServerCodec());
        pipeline.addLast("httpChunkAggregator", new HttpObjectAggregator(maxHttpContentLength));
        pipeline.addLast("websocketNegotiator", new WebsocketNegotiator());
    }

    private SelectedProtocol getProtocol(SSLEngine engine) {
        String[] protocol = engine.getSession().getProtocol().split(":");
        if (protocol != null && protocol.length > 1) {
            SelectedProtocol selectedProtocol = SelectedProtocol.protocol(protocol[1]);
            System.err.println("Selected Protocol is " + selectedProtocol);
            return selectedProtocol;
        }
        return SelectedProtocol.UNKNOWN;
    }

    private enum SelectedProtocol {
        /**
         * Must be updated to match the HTTP/2 draft number.
         */
        HTTP_2(TLS_UPGRADE_PROTOCOL_NAME),
        HTTP_1_1("http/1.1"),
        HTTP_1_0("http/1.0"),
        UNKNOWN("Unknown");

        private final String name;

        SelectedProtocol(String defaultName) {
            name = defaultName;
        }

        public String protocolName() {
            return name;
        }

        /**
         * Get an instance of this enum based on the protocol name returned by the NPN server provider
         *
         * @param name the protocol name
         * @return the SelectedProtocol instance
         */
        public static SelectedProtocol protocol(String name) {
            for (SelectedProtocol protocol : SelectedProtocol.values()) {
                if (protocol.protocolName().equals(name)) {
                    return protocol;
                }
            }
            return UNKNOWN;
        }
    }

}

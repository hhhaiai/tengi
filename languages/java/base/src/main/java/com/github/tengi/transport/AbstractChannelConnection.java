package com.github.tengi.transport;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import com.github.tengi.Connection;
import com.github.tengi.ConnectionConstants;
import com.github.tengi.Message;
import com.github.tengi.MessageListener;
import com.github.tengi.SerializationFactory;
import com.github.tengi.Streamable;
import com.github.tengi.UniqueId;
import com.github.tengi.buffer.ByteBufMemoryBuffer;
import com.github.tengi.buffer.MemoryBuffer;
import com.github.tengi.buffer.MemoryBufferPool;

public abstract class AbstractChannelConnection
    implements Connection
{

    protected final SerializationFactory serializationFactory;

    protected final MemoryBufferPool memoryBufferPool;

    private final Channel channel;

    private volatile MessageListener messageListener = null;

    protected AbstractChannelConnection( Channel channel, MemoryBufferPool memoryBufferPool,
                                         SerializationFactory serializationFactory )
    {
        this.serializationFactory = serializationFactory;
        this.memoryBufferPool = memoryBufferPool;
        this.channel = channel;

        channel.pipeline().addLast( new TengiMemoryBufferDecoder() );
    }

    public Channel getUnderlyingChannel()
    {
        return channel;
    }

    @Override
    public void setMessageListener( MessageListener messageListener )
    {
        this.messageListener = messageListener;
    }

    @Override
    public void clearMessageListener()
    {
        messageListener = null;
    }

    @Override
    public Message prepareMessage( Streamable body )
    {
        return new Message( serializationFactory, this, body, UniqueId.randomUniqueId(), Message.MESSAGE_TYPE_DEFAULT );
    }

    protected void prepareMessageBuffer( Message message, MemoryBuffer memoryBuffer )
    {
        memoryBuffer.writeByte( ConnectionConstants.DATA_TYPE_MESSAGE );
        Message.write( memoryBuffer, message );
    }

    protected void prepareMessageBuffer( MemoryBuffer rawBuffer, Streamable metadata, MemoryBuffer memoryBuffer )
    {
        memoryBuffer.writeByte( ConnectionConstants.DATA_TYPE_RAW );
        writeNullableObject( metadata, memoryBuffer );
        memoryBuffer.writeInt( rawBuffer.writerIndex() );
        memoryBuffer.writeBuffer( rawBuffer, 0, rawBuffer.writerIndex() );
    }

    protected <S extends Streamable> void writeNullableObject( S streamable, MemoryBuffer memoryBuffer )
    {
        if ( streamable == null )
        {
            memoryBuffer.writeByte( (byte) 0 );
        }
        else
        {
            memoryBuffer.writeByte( (byte) 1 );
            memoryBuffer.writeShort( serializationFactory.getClassIdentifier( streamable ) );
            streamable.writeStream( memoryBuffer );
        }
    }

    @SuppressWarnings( "unchecked" )
    protected <S extends Streamable> S readNullableObject( MemoryBuffer memoryBuffer )
    {
        if ( memoryBuffer.readByte() == 1 )
        {
            S streamable = (S) serializationFactory.instantiate( memoryBuffer.readShort() );
            streamable.readStream( memoryBuffer );
            return streamable;
        }
        return null;
    }

    protected ByteBuf getByteBuf( int initialSize )
    {
        return Unpooled.directBuffer( initialSize );
    }

    private class TengiMemoryBufferDecoder
        extends MessageToMessageDecoder<ByteBuf>
    {

        private final Connection connection = AbstractChannelConnection.this;

        @Override
        protected void decode( ChannelHandlerContext ctx, ByteBuf msg, MessageBuf<Object> out )
            throws Exception
        {
            MemoryBuffer memoryBuffer = memoryBufferPool.pop( msg );
            try
            {
                byte frameType = memoryBuffer.readByte();
                switch ( frameType )
                {
                    case ConnectionConstants.DATA_TYPE_MESSAGE:
                        decodeMessageFrame( memoryBuffer );
                        break;

                    case ConnectionConstants.DATA_TYPE_RAW:
                        decodeRawDataFrame( memoryBuffer );
                        break;

                    default:
                        throw new IllegalStateException( "Illegal frame type: " + frameType );
                }
            }
            finally
            {
                memoryBufferPool.push( memoryBuffer );
            }
        }

        private void decodeMessageFrame( MemoryBuffer memoryBuffer )
        {
            Message message = Message.read( memoryBuffer, serializationFactory, connection );
            if ( messageListener != null )
            {
                messageListener.messageReceived( message, connection );
            }
        }

        private void decodeRawDataFrame( MemoryBuffer memoryBuffer )
        {
            Streamable metadata = readNullableObject( memoryBuffer );
            int length = memoryBuffer.readInt();
            ByteBuf rawByteBuf = getByteBuf( length );
            ByteBufMemoryBuffer rawBuffer = new ByteBufMemoryBufferAdapter().setByteBuffer( rawByteBuf );
            rawBuffer.writeBuffer( memoryBuffer, 0, length );
            if ( messageListener != null )
            {
                messageListener.rawDataReceived( rawBuffer, metadata, connection );
            }
        }
    }

    private static class ByteBufMemoryBufferAdapter
        extends ByteBufMemoryBuffer
    {

        @Override
        protected ByteBuf getByteBuffer()
        {
            return super.getByteBuffer();
        }

        @Override
        protected ByteBufMemoryBuffer setByteBuffer( ByteBuf byteBuffer )
        {
            return super.setByteBuffer( byteBuffer );
        }
    }

}

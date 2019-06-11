import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocketSendWithCrc32 {
    private static Logger logger = LoggerFactory.getLogger(SocketSendWithCrc32.class);

    public static void main(String[] args)
    {
        final String serverIp = "127.0.0.1";
        final String serverPort = "5678";

        ChannelFactory channelFactory = new ChannelFactory();
        channelFactory.initConnect();;
        ChannelFuture f = channelFactory.getChannelFuture(serverIp, serverPort);

        Channel channel = f.channel();

        try
        {
            CrcCalc crcCalc = new CrcCalc();
            long resultLong = crcCalc.getCrcValue();

            byte[] packet = ByteBuffer.allocate(32).order(ByteOrder.BIG_ENDIAN).putLong(resultLong).array();
            ByteBufAllocator alloc = channel.alloc();
            ByteBuf buf = alloc.buffer(packet.length);


            buf = buf.writeBytes(packet);
            channel.writeAndFlush(buf);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }

}
class ChannelFactory
{
    Map<String, ChannelFuture> channelMap = new ConcurrentHashMap<String, ChannelFuture>();

    private Bootstrap b = null;
    private NioEventLoopGroup workerGroup = null;

    public void initConnect()
    {
        if (workerGroup == null || workerGroup.isShutdown())
        {
            workerGroup = new NioEventLoopGroup();

            b = new Bootstrap();
            b.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000).handler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                public void initChannel(SocketChannel ch) throws Exception
                {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
//                    ch.pipeline().addLast(new EventNotifyChannelHandler());
                }
            });
        }
    }

    public ChannelFuture getChannelFuture(String ip, String port)
    {
        // ChannelFuture f = channelMap.get(getKey(ip, port));
        //
        // if(f == null || f.isDone() || !f.channel().isOpen())
        // {
        // try
        // {
        // f = b.connect(ip, Integer.valueOf(port)).sync();
        //
        // addChannelFuture(f, ip, port);
        //
        // return f;
        // }
        // catch (InterruptedException e)
        // {
        // logger.error(e.getMessage(), e);
        // }
        // }
        //
        // return f;

        ChannelFuture f = null;

        f = channelMap.get(getKey(ip, port));

        if (f == null || !f.channel().isOpen())
        {
            if (f != null)
            {
                Channel ch = f.channel();

                if (!ch.isOpen())
                {
                    try
                    {
                        f.sync();
                        channelMap.remove(getKey(ip, port));
                    }
                    catch (InterruptedException e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
                else
                {
                    return f;
                }
            }

            f = channelMap.get(getKey(ip, port));

            if (f == null || f.isDone() || !f.channel().isOpen())
            {
                try
                {
                    f = b.connect(ip, Integer.valueOf(port)).sync();
                    channelMap.put(getKey(ip, port), f);
                }
                catch (Exception e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }

        return f;
    }

    public void addChannelFuture(ChannelFuture f, String ip, String port)
    {
        channelMap.put(getKey(ip, port), f);
    }

    public void removeChannelFuture(String ip, String port)
    {
        channelMap.remove(getKey(ip, port));
    }

    private String getKey(String ip, String port)
    {
        return ip + "." + "port";
    }
}
class EventNotifyChannelHandler extends ChannelInboundHandlerAdapter
{
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        // logger.info("##### Client channelActive #####");

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        // logger.info("##### Client channelInactive #####");

        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        // logger.info("##### Client channelRead ##### : " + ((ByteBuf) msg).toString(Charset.defaultCharset()));

        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
    {
        // logger.info("##### Client channelReadComplete #####");

        super.channelReadComplete(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception
    {
        // logger.info("##### Client channelRegistered #####");

        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception
    {
        // logger.info("##### Client channelUnregistered #####");

        super.channelUnregistered(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception
    {
        // logger.info("##### Client channelWritabilityChanged #####");

        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        // logger.error("##### Client exceptionCaught #####", cause);

        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
        // logger.info("##### Client userEventTriggered #####");

        super.userEventTriggered(ctx, evt);
    }
}
class CrcCalc
{
    private long crcmask;
    private long crchighbit;
    private long [] crctab = new long[256];
    private long crcinit_direct;
    private long crcinit_nondirect;

    private int order = 32;
    private long polynom = 0x4c11db7;
    private int direct = 1;
    private long crcinit = 0xffffffff;
    private long crcxor = 0xffffffff;
    private int refin = 1;
    private int refout = 1;

    public long getCrcValue()
    {
        crcmask = ((((long)1 << (order-1)) - 1) << 1) | 1;
        crchighbit = (long)1 << (order-1);

        System.out.println("crcmask: " + crcmask); // 4294967295
        System.out.println("crchighbit: " + crchighbit); // 2147483648

        generateCrcTable();
//        for (long currentNumber : crctab)
//        {
//            System.out.println(currentNumber);
//        }

        long crc, bit;

        if (direct == 0)
        {
            crcinit_nondirect = crcinit;
            crc = crcinit;
            for (int i = 0; i < order; i ++)
            {
                bit = crc & crchighbit;
                crc <<= 1;
                if (bit != 0)
                {
                    crc ^= polynom;
                }
            }
            crc &= crcmask;
            crcinit_direct = crc;
        }
        else
        {
            crcinit_direct = crcinit;
            crc = crcinit;
            for (int i = 0; i < order; i++)
            {
                bit = crc & 1;
                if (bit != 0)
                {
                    crc^= polynom;
                }
                crc >>= 1;
                if (bit != 0)
                {
                    crc|= crchighbit;
                }
            }
            crcinit_nondirect = crc;
        }
        System.out.println(crcinit_nondirect);
        return crcinit_nondirect;
    }

    private long reflect (long crc, int bitnum)
    {
        // reflects the lower 'bitnum' bits of 'crc'
        long crcout = 0;
        long j = 1;
        for (long i = (long)1 << (bitnum-1); i != 0; i >>= 1)
        {
            if ((crc & i) != 0)
            {
                crcout |= j;
            }
            j <<= 1;
        }
        return crcout;
    }
    private void generateCrcTable()
    {
        // make CRC lookup table used by table algorithms

        long bit, crc;

        for (int i = 0; i < 256; i++)
        {
            crc = (long)i;
            if (refin != 0)
            {
                crc = reflect(crc, 8);
            }
            crc <<= order-8;

            for (int j = 0; j < 8; j++)
            {
                bit = crc & crchighbit;
                crc <<= 1;
                if (bit != 0)
                {
                    crc ^= polynom;
                }

                if (refin != 0)
                {
                    crc = reflect(crc, order);
                }
                crc &= crcmask;
                crctab[i]= crc;
            }
        }
    }
}

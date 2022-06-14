package com.fizzed.mhttpd;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

public class MicroHttpd {

    private int port;
    private HttpRouter router;
    private Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public MicroHttpd() {
        this.port = 18080;
    }

    public int getPort() {
        return port;
    }

    public MicroHttpd setPort(int port) {
        this.port = port;
        return this;
    }

    public HttpRouter getRouter() {
        return router;
    }

    public MicroHttpd setRouter(HttpRouter router) {
        this.router = router;
        return this;
    }

    public MicroHttpd start() throws Exception {
        boolean SSL = false;

        // Configure SSL.
        final SslContext sslCtx;
//        if (SSL) {
//            SelfSignedCertificate ssc = new SelfSignedCertificate();
//            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
//        } else {
            sslCtx = null;
//        }

        // Configure the server.
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.group(bossGroup, workerGroup)
            //.channel(Blocking)
            .channel(NioServerSocketChannel.class)
            // memory leak until we FORCED the number of arenas in use
            .childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(1, 1, 4096, 1))
            //.handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ChannelPipeline p = ch.pipeline();
                    /*if (sslCtx != null) {
                        p.addLast(sslCtx.newHandler(ch.alloc()));
                    }*/
                    p.addLast(new HttpServerCodec());
                    //p.addLast(new HttpServerExpectContinueHandler());
                    p.addLast(new MicroHttpdHandler(MicroHttpd.this.router));
                }
            });

        this.serverChannel = b.bind(this.port).sync().channel();

        return this;
    }

    public MicroHttpd join() throws InterruptedException {
        this.serverChannel.closeFuture().sync();
        return this;
    }

    public MicroHttpd stop() throws InterruptedException {
        this.serverChannel.close().sync();
        this.bossGroup.shutdownGracefully();
        this.workerGroup.shutdownGracefully();
        return this;
    }

}
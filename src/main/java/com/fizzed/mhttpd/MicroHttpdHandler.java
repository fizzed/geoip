package com.fizzed.mhttpd;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import static com.fizzed.crux.util.Maybe.maybe;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;

public class MicroHttpdHandler extends SimpleChannelInboundHandler<HttpObject> {
    static private final Logger log = LoggerFactory.getLogger(MicroHttpdHandler.class);

    private final HttpRouter router;

    public MicroHttpdHandler(HttpRouter router) {
        this.router = router;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
//        log.info("Channel read...");

        if (msg instanceof HttpRequest) {
            //log.info("Http request...{}", msg.getClass().getCanonicalName());

            final String localAddress = maybe(ctx.channel().localAddress())
                .typed(InetSocketAddress.class)
                .map(v -> v.getHostString())
                .orNull();
            final String remoteAddress = maybe(ctx.channel().remoteAddress())
                .typed(InetSocketAddress.class)
                .map(v -> v.getHostString())
                .orNull();
            final HttpRequest httpRequest = (HttpRequest)msg;
            final boolean keepAlive = HttpUtil.isKeepAlive(httpRequest);
            final Request request = new Request(localAddress, remoteAddress, httpRequest);
            Response response;

            // lookup the route
            final HttpRoute route = this.router.resolve(httpRequest.method(), request.getPath());

            if (route != null) {
                try {
                    response = route.getHandler().execute(request);
                }
                catch (HttpException e) {
                    response = new Response()
                        .setStatusCode(e.getStatusCode())
                        .setBody(e.getMessage());
                } catch (Exception e) {
                    log.error("Unable to cleanly handle request", e);
                    response = new Response()
                        .setStatusCode(500)
                        .setBody("Internal Server Error");
                }
            }
            else {
                response = new Response()
                    .setStatusCode(404)
                    .setBody("Route Not Found");
            }


            final FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                httpRequest.protocolVersion(),
                HttpResponseStatus.valueOf(response.getStatusCode()),
                Unpooled.wrappedBuffer(response.getBody()));

            // copy all headers
            if (response.getHeaders() != null) {
                response.getHeaders().forEach((name, value) -> {
                    httpResponse.headers().add(name, value);
                });
            }

            // set defaults if not yet set
            if (httpResponse.headers().get("Content-Type") == null) {
                httpResponse.headers().set("Content-Type", "text/html; charset=utf-8");
            }

            httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());

            if (keepAlive) {
                if (!httpRequest.protocolVersion().isKeepAliveDefault()) {
                    httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderNames.KEEP_ALIVE);
                }
            } else {
                // Tell the client we're going to close the connection.
                httpResponse.headers().set(HttpHeaderNames.CONNECTION, CLOSE);
            }

            ChannelFuture f = ctx.writeAndFlush(httpResponse);

            if (!keepAlive) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
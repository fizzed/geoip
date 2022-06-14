package com.fizzed.mhttpd;

import java.io.IOException;

public interface HttpHandler {

    Response execute(Request request) throws IOException;

}
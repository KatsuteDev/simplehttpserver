/*
 * Copyright (C) 2022 Katsute <https://github.com/Katsute>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package dev.katsute.simplehttpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.Map;

public abstract class SimpleHttpExchange extends HttpExchange {

    SimpleHttpExchange(){ }

    static SimpleHttpExchange create(final HttpExchange exchange){
        return new SimpleHttpExchangeImpl(exchange);
    }

    //

    public abstract HttpServer getHttpServer();

    public abstract HttpExchange getHttpExchange();

    //

    public abstract String getRawGet();

    public abstract Map<String,String> getGetMap();

    public abstract boolean hasGet();

    //

    public abstract String getRawPost();

    public abstract Map<String,?> getPostMap();

    public abstract MultipartFormData getMultipartFormData();

    public abstract boolean hasPost();

    //

    public abstract Map<String,String> getCookies();

    public abstract void setCookie(final String key, final String value);

    public abstract void setCookie(final HttpCookie cookie);

    //

    public abstract void send(final int responseCode) throws IOException;

    public abstract void send(final byte[] bytes) throws IOException;

    public abstract void send(final byte[] bytes, final int responseCode) throws IOException;

    public abstract void send(final byte[] bytes, final boolean gzip) throws IOException;

    public abstract void send(final byte[] bytes, final int responseCode, final boolean gzip) throws IOException;

    public abstract void send(final String string) throws IOException;

    public abstract void send(final String string, final int responseCode) throws IOException;

    public abstract void send(final String string, final boolean gzip) throws IOException;

    public abstract void send(final String string, final int responseCode, final boolean gzip) throws IOException;

    public abstract void send(final File file) throws IOException;

    public abstract void send(final File file, final int responseCode) throws IOException;

    public abstract void send(final File file, final boolean gzip) throws IOException;

    public abstract void send(final File file, final int responseCode, final boolean gzip) throws IOException;

}

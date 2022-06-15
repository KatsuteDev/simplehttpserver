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

/**
 * A {@link HttpExchange} with additional extensions to simplify usage.
 * <h1>Requests</h1>
 * <h2><code>GET</code> Request</h2>
 * If a user sends a <code>GET</code> request to the server, a map of keys and values of that request can be retrieved by using the {@link #getGetMap()} method.
 * <h2><code>POST</code> Request</h2>
 * If a user sends a <code>POST</code> request to the server, a map of keys and values; or a {@link MultipartFormData} can be retrieved by using {@link #getPostMap()} and {@link #getMultipartFormData()}.
 *
 * <h3><code>multipart/form-data</code></h3>
 * For requests that have content type <code>multipart/form-data</code>, data must be retrieved using {@link #getMultipartFormData()}, which returns a {@link MultipartFormData} using {@link Record}s and {@link FileRecord}s. Files sent through here are sent as a {@link Byte} array and not a {@link File}.
 *
 * <h2>Cookies</h2>
 * A clients browser cookies for the site can be retrieved by using the {@link #getCookie(String)} or {@link #getCookies()} method.
 * <br>
 * Cookies can be set by using the {@link #setCookie(HttpCookie)} or {@link #setCookie(String, String)}.
 * <br>
 * An exchange must be sent in order to change on the client.
 *
 * <h2>Session</h2>
 * Normally the only "identifier" that we can retrieve from the user is their address and port provided from an exchange. This however, doesn't work across multiple tabs or when the user refreshes the page; instead we use a session cookie to track a user.
 * <br>
 * A server must have a {@link HttpSessionHandler} set using {@link SimpleHttpServer#setSessionHandler(HttpSessionHandler)} for this to work.
 * <br>
 * The {@link HttpSessionHandler} assigns session IDs to clients and allows the server to retrieve them using {@link SimpleHttpServer#getSession(HttpExchange)}.
 *
 * <h1>Response</h1>
 * To send response headers you must first retrieve then with {@link #getResponseHeaders()}, modify them, then send them using {@link #sendResponseHeaders(int, long)} or any other of the send methods.
 * <br>
 * Data can be sent as a {@link Byte} array, {@link String}, or as a {@link File}. Responses can optionally gziped to compress the data sent.
 * <ul>
 *      <li>{@link #send(int)}</li>
 *      <li>{@link #send(byte[])}</li>
 *      <li>{@link #send(byte[], int)}</li>
 *      <li>{@link #send(byte[], boolean)}</li>
 *      <li>{@link #send(byte[], int, boolean)}</li>
 *      <li>{@link #send(String)}</li>
 *      <li>{@link #send(String, int)}</li>
 *      <li>{@link #send(String, boolean)}</li>
 *      <li>{@link #send(String, int, boolean)}</li>
 *      <li>{@link #send(File)}</li>
 *      <li>{@link #send(File, int)}</li>
 *      <li>{@link #send(File, boolean)}</li>
 *      <li>{@link #send(File, int, boolean)}</li>
 * </ul>
 * <b>Note:</b> An exchange must be sent or closed, otherwise the connection may resend the request until it gets response or times out.
 *
 * @see HttpExchange
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
@SuppressWarnings("SpellCheckingInspection")
public abstract class SimpleHttpExchange extends HttpExchange {

    SimpleHttpExchange(){ }

    static SimpleHttpExchange create(final HttpExchange exchange){
        return new SimpleHttpExchangeImpl(exchange);
    }

    //

    /**
     * Returns the http server associated with an exchange.
     *
     * @return http server
     *
     * @see HttpServer
     * @since 5.0.0
     */
    public abstract HttpServer getHttpServer();

    /**
     * Returns the underlying http exchange.
     *
     * @return http exchange
     *
     * @see HttpExchange
     * @since 5.0.0
     */
    public abstract HttpExchange getHttpExchange();

    //

    /**
     * Returns the raw string representation of a GET query. The GET query is the string of characters located after the <code>?</code> in the URL.
     *
     * @return raw GET
     *
     * @see #getGetMap()
     * @see #hasGet()
     * @since 5.0.0
     */
    public abstract String getRawGet();

    /**
     * Returns GET query as a map of keys and values. The GET query is the string of characters located after the <code>?</code> in the URL.
     *
     * @return GET map
     *
     * @see #getRawGet()
     * @see #hasGet()
     * @since 5.0.0
     */
    public abstract Map<String,String> getGetMap();

    /**
     * Returns if there is a GET query. The GET query is the string of characters located after the <code>?</code> in the URL.
     *
     * @return if GET query exists
     *
     * @see #getRawGet()
     * @see #getGetMap()
     * @since 5.0.0
     */
    public abstract boolean hasGet();

    //

    /**
     * Returns the raw string representation of a POST body.
     *
     * @return raw POST body
     *
     * @see #getPostMap()
     * @see #getMultipartFormData()
     * @see #hasPost()
     * @since 5.0.0
     */
    public abstract String getRawPost();

    /**
     * Returns the POST body as a map of keys and values. For content type <code>multipart/form-data</code> use {@link #getMultipartFormData()}.
     *
     * @return POST map
     *
     * @see #getRawPost()
     * @see #getMultipartFormData()
     * @see #hasPost()
     * @since 5.0.0
     */
    public abstract Map<String,?> getPostMap();

    /**
     * For content type <code>multipart/form-data</code>, returns POST body as a {@link MultipartFormData} object.
     *
     * @return multipart/form-data object
     *
     * @see MultipartFormData
     * @see #getRawPost()
     * @see #getPostMap()
     * @see #hasPost()
     * @since 5.0.0
     */
    public abstract MultipartFormData getMultipartFormData();

    /**
     * Returns if there is a POST body.
     *
     * @return if POST body exists
     *
     * @see #getRawPost()
     * @see #getPostMap()
     * @see #getMultipartFormData()
     * @since 5.0.0
     */
    public abstract boolean hasPost();

    //

    /**
     * Returns the value of a given cookie for a client.
     *
     * @param cookie name of cookie
     * @return value of cookie
     *
     * @see #getCookies()
     * @see #setCookie(HttpCookie)
     * @see #setCookie(String, String)
     * @since 5.0.0
     */
    public abstract String getCookie(final String cookie);

    /**
     * Returns a map of all the cookies for a client.
     *
     * @return map of cookies
     *
     * @see #getCookie(String)
     * @see #setCookie(HttpCookie)
     * @see #setCookie(String, String)
     * @since 5.0.0
     */
    public abstract Map<String,String> getCookies();

    /**
     * Sets a cookie for the client. Response must be sent for cookies to update on the client.
     *
     * @param key cookie key
     * @param value value of cookie
     *
     * @see #setCookie(HttpCookie)
     * @see #getCookie(String)
     * @see #getCookies()
     * @since 5.0.0
     */
    public abstract void setCookie(final String key, final String value);

    /**
     * Sets a cookie for the client. Response must be sent for cookies to update on the client.
     *
     * @param cookie http cookie
     *
     * @see HttpCookie
     * @see #setCookie(String, String)
     * @see #getCookies()
     * @see #getCookie(String)
     */
    public abstract void setCookie(final HttpCookie cookie);

    //

    /**
     * Sends a response code to the client.
     *
     * @param responseCode response code
     * @throws IOException IO exception
     *
     * @see #send(byte[])
     * @see #send(byte[], int)
     * @see #send(byte[], boolean)
     * @see #send(byte[], int, boolean)
     * @see #send(String)
     * @see #send(String, int)
     * @see #send(String, boolean)
     * @see #send(String, int, boolean)
     * @see #send(File)
     * @see #send(File, int)
     * @see #send(File, boolean)
     * @see #send(File, int, boolean)
     * @since 5.0.0
     */
    public abstract void send(final int responseCode) throws IOException;

    /**
     * Sends a byte array to the client.
     *
     * @param bytes byte array
     * @throws IOException IO exception
     *
     * @see #send(byte[], int)
     * @see #send(byte[], boolean)
     * @see #send(byte[], int, boolean)
     * @see #send(int)
     * @see #send(String)
     * @see #send(String, int)
     * @see #send(String, boolean)
     * @see #send(String, int, boolean)
     * @see #send(File)
     * @see #send(File, int)
     * @see #send(File, boolean)
     * @see #send(File, int, boolean)
     * @since 5.0.0
     */
    public abstract void send(final byte[] bytes) throws IOException;

    /**
     * Sends a byte array to the client with a response code.
     *
     * @param bytes byte array
     * @param responseCode response code
     * @throws IOException IO exception
     *
     * @see #send(byte[])
     * @see #send(byte[], boolean)
     * @see #send(byte[], int, boolean)
     * @see #send(int)
     * @see #send(String)
     * @see #send(String, int)
     * @see #send(String, boolean)
     * @see #send(String, int, boolean)
     * @see #send(File)
     * @see #send(File, int)
     * @see #send(File, boolean)
     * @see #send(File, int, boolean)
     * @since 5.0.0
     */
    public abstract void send(final byte[] bytes, final int responseCode) throws IOException;

    /**
     * Sends a byte array to the client that can be gziped.
     *
     * @param bytes byte array
     * @param gzip if the response should be compressed
     * @throws IOException IO exception
     *
     * @see #send(byte[])
     * @see #send(byte[], int)
     * @see #send(byte[], int, boolean)
     * @see #send(int)
     * @see #send(String)
     * @see #send(String, int)
     * @see #send(String, boolean)
     * @see #send(String, int, boolean)
     * @see #send(File)
     * @see #send(File, int)
     * @see #send(File, boolean)
     * @see #send(File, int, boolean)
     * @since 5.0.0
     */
    public abstract void send(final byte[] bytes, final boolean gzip) throws IOException;

    /**
     * Sends a byte array to the client with a response code that can be gziped.
     *
     * @param bytes byte array
     * @param responseCode response code
     * @param gzip if the response should be compressed
     * @throws IOException IO exception
     *
     * @see #send(byte[])
     * @see #send(byte[], int)
     * @see #send(byte[], boolean)
     * @see #send(int)
     * @see #send(String)
     * @see #send(String, int)
     * @see #send(String, boolean)
     * @see #send(String, int, boolean)
     * @see #send(File)
     * @see #send(File, int)
     * @see #send(File, boolean)
     * @see #send(File, int, boolean)
     * @since 5.0.0
     */
    public abstract void send(final byte[] bytes, final int responseCode, final boolean gzip) throws IOException;

    /**
     * Sends a string to the client.
     *
     * @param string string to send
     * @throws IOException IO exception
     *
     * @see #send(String, int)
     * @see #send(String, boolean)
     * @see #send(String, int, boolean)
     * @see #send(int)
     * @see #send(byte[])
     * @see #send(byte[], int)
     * @see #send(byte[], boolean)
     * @see #send(byte[], int, boolean)
     * @see #send(File)
     * @see #send(File, int)
     * @see #send(File, boolean)
     * @see #send(File, int, boolean)
     * @since 5.0.0
     */
    public abstract void send(final String string) throws IOException;

    /**
     * Sends a string to the client with a response code
     *
     * @param string string to send
     * @param responseCode response code
     * @throws IOException IO exception
     *
     * @see #send(String)
     * @see #send(String, boolean)
     * @see #send(String, int, boolean)
     * @see #send(int)
     * @see #send(byte[])
     * @see #send(byte[], int)
     * @see #send(byte[], boolean)
     * @see #send(byte[], int, boolean)
     * @see #send(File)
     * @see #send(File, int)
     * @see #send(File, boolean)
     * @see #send(File, int, boolean)
     * @since 5.0.0
     */
    public abstract void send(final String string, final int responseCode) throws IOException;

    /**
     * Sends a string to the client that can be gziped.
     *
     * @param string string to send
     * @param gzip if the response should be compressed
     * @throws IOException IO exception
     *
     * @see #send(String)
     * @see #send(String, int)
     * @see #send(String, int, boolean)
     * @see #send(int)
     * @see #send(byte[])
     * @see #send(byte[], int)
     * @see #send(byte[], boolean)
     * @see #send(byte[], int, boolean)
     * @see #send(File)
     * @see #send(File, int)
     * @see #send(File, boolean)
     * @see #send(File, int, boolean)
     * @since 5.0.0
     */
    public abstract void send(final String string, final boolean gzip) throws IOException;

    /**
     * Sends a string to the client with a response code that can be gziped.
     *
     * @param string string to send
     * @param responseCode response code
     * @param gzip if the response should be compressed
     * @throws IOException IO exception
     *
     * @see #send(String)
     * @see #send(String, int)
     * @see #send(String, boolean)
     * @see #send(int)
     * @see #send(byte[])
     * @see #send(byte[], int)
     * @see #send(byte[], boolean)
     * @see #send(byte[], int, boolean)
     * @see #send(File)
     * @see #send(File, int)
     * @see #send(File, boolean)
     * @see #send(File, int, boolean)
     * @since 5.0.0
     */
    public abstract void send(final String string, final int responseCode, final boolean gzip) throws IOException;

    /**
     * Sends a file to the client.
     *
     * @param file file
     * @throws IOException IO exception
     *
     * @see #send(File, int)
     * @see #send(File, boolean)
     * @see #send(File, int, boolean)
     * @see #send(int)
     * @see #send(byte[])
     * @see #send(byte[], int)
     * @see #send(byte[], boolean)
     * @see #send(byte[], int, boolean)
     * @see #send(String)
     * @see #send(String, int)
     * @see #send(String, boolean)
     * @see #send(String, int, boolean)
     * @since 5.0.0
     */
    public abstract void send(final File file) throws IOException;

    /**
     * Sends a byte array to the client with a response code.
     *
     * @param file file to send
     * @param responseCode response code
     * @throws IOException IO exception
     *
     * @see #send(File)
     * @see #send(File, boolean)
     * @see #send(File, int, boolean)
     * @see #send(int)
     * @see #send(byte[])
     * @see #send(byte[], int)
     * @see #send(byte[], boolean)
     * @see #send(byte[], int, boolean)
     * @see #send(String)
     * @see #send(String, int)
     * @see #send(String, boolean)
     * @see #send(String, int, boolean)
     * @since 5.0.0
     */
    public abstract void send(final File file, final int responseCode) throws IOException;

    /**
     * Sends a byte array to the client that can be gziped.
     *
     * @param file file to send
     * @param gzip if the response should be compressed
     * @throws IOException IO exception
     *
     * @see #send(File)
     * @see #send(File, int)
     * @see #send(File, int, boolean)
     * @see #send(int)
     * @see #send(byte[])
     * @see #send(byte[], int)
     * @see #send(byte[], boolean)
     * @see #send(byte[], int, boolean)
     * @see #send(String)
     * @see #send(String, int)
     * @see #send(String, boolean)
     * @see #send(String, int, boolean)
     * @since 5.0.0
     */
    public abstract void send(final File file, final boolean gzip) throws IOException;

    /**
     * Sends a file to the client with a response code that can be gziped.
     *
     * @param file file to send
     * @param responseCode response code
     * @param gzip if the response should be compressed
     * @throws IOException IO exception
     *
     * @see #send(File)
     * @see #send(File, int)
     * @see #send(File, boolean)
     * @see #send(int)
     * @see #send(byte[])
     * @see #send(byte[], int)
     * @see #send(byte[], boolean)
     * @see #send(byte[], int, boolean)
     * @see #send(String)
     * @see #send(String, int)
     * @see #send(String, boolean)
     * @see #send(String, int, boolean)
     * @since 5.0.0
     */
    public abstract void send(final File file, final int responseCode, final boolean gzip) throws IOException;

}

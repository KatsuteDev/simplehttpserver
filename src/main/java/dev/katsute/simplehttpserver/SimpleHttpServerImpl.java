/*
 * Copyright (C) 2024 Katsute <https://github.com/Katsute>
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

import com.sun.net.httpserver.*;
import dev.katsute.simplehttpserver.handler.RootHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executor;

final class SimpleHttpServerImpl extends SimpleHttpServer {

    private final HttpServer server = HttpServer.create();

    private HttpSessionHandler sessionHandler;

    private final Map<HttpContext,HttpHandler> contexts = Collections.synchronizedMap(new HashMap<>());

    SimpleHttpServerImpl(final Integer port, final Integer backlog) throws IOException{
        if(port != null)
            server.bind(new InetSocketAddress(port), backlog != null ? backlog : 0);
    }

    private void handle(final HttpExchange exchange){
        if(sessionHandler != null)
            sessionHandler.getSession(exchange).update();
    }

    //

    @Override
    public final HttpServer getHttpServer(){
        return server;
    }

// region copy

    @Override
    public synchronized final InetSocketAddress bind(final int port) throws IOException {
        return bind(port, null);
    }

    @Override
    public synchronized final InetSocketAddress bind(final int port, final int backlog) throws IOException {
        return bind(port, (Integer) backlog);
    }

    private synchronized InetSocketAddress bind(final int port, final Integer backlog) throws IOException {
        final InetSocketAddress address = new InetSocketAddress(port);
        server.bind(address, backlog == null ? 0 : backlog);
        return address;
    }

    public synchronized void bind(final InetSocketAddress address) throws IOException {
        bind(address, null);
    }

    @Override
    public synchronized void bind(final InetSocketAddress address, final int backlog) throws IOException {
        bind(address, (Integer) backlog);
    }

    private synchronized void bind(final InetSocketAddress address, final Integer backlog) throws IOException {
        server.bind(Objects.requireNonNull(address), backlog == null ? 0 : backlog);
    }

    //

    @Override
    public final InetSocketAddress getAddress(){
        return server.getAddress();
    }

    //

    @Override
    public synchronized final Executor getExecutor(){
        return server.getExecutor();
    }

    @Override
    public synchronized final void setExecutor(final Executor executor){
        server.setExecutor(executor);
    }

    //

    @Override
    public synchronized final HttpSessionHandler getSessionHandler(){
        return sessionHandler;
    }

    @Override
    public synchronized final void setSessionHandler(final HttpSessionHandler sessionHandler){
        this.sessionHandler = sessionHandler;
    }

    @Override
    public final HttpSession getSession(final HttpExchange exchange){
        return sessionHandler != null ? sessionHandler.getSession(Objects.requireNonNull(exchange) instanceof SimpleHttpExchange ? ((SimpleHttpExchange) exchange).getHttpExchange() : exchange) : null;
    }

    //

    @Override
    public synchronized final HttpContext createContext(final String context){
        return createContext(context, HttpExchange::close);
    }

    //

    @Override
    public synchronized final HttpContext createContext(final String context, final HttpHandler handler){
        final String ct = ContextUtility.getContext(Objects.requireNonNull(context), true, false);
        if(!ct.equals("/") && Objects.requireNonNull(handler) instanceof RootHandler)
            throw new IllegalArgumentException("RootHandler can only be used at the root '/' context");

        final HttpContext hc = server.createContext(ct);

        final HttpHandler wrapper = exchange -> {
            handle(exchange);
            handler.handle(exchange);
        };

        hc.setHandler(wrapper);

        contexts.put(hc, handler);

        return hc;
    }

    //

    @SuppressWarnings("CaughtExceptionImmediatelyRethrown")
    @Override
    public synchronized final void removeContext(final String context){
        try{
            server.removeContext(ContextUtility.getContext(Objects.requireNonNull(context), true, false));
        }catch(final IllegalArgumentException e){
            throw e;
        }finally{
            for(final HttpContext hc : contexts.keySet()){
                if(hc.getPath().equalsIgnoreCase(ContextUtility.getContext(context, true, false))){
                    contexts.remove(hc);
                    break;
                }
            }
        }
    }

    @Override
    public synchronized final void removeContext(final HttpContext context){
        Objects.requireNonNull(context);
        contexts.remove(context);
        server.removeContext(context);
    }

    //

    @Override
    public final HttpHandler getContextHandler(final String context){
        Objects.requireNonNull(context);
        for(final Map.Entry<HttpContext,HttpHandler> entry : contexts.entrySet())
            if(entry.getKey().getPath().equals(ContextUtility.getContext(context, true, false)))
                return entry.getValue();
        return null;
    }

    @Override
    public final HttpHandler getContextHandler(final HttpContext context){
        return contexts.get(Objects.requireNonNull(context));
    }

    @Override
    public final Map<HttpContext,HttpHandler> getContexts(){
        return new HashMap<>(contexts);
    }

    //

    @Override
    public synchronized final String getRandomContext(){
        return getRandomContext("");
    }

    @Override
    public synchronized final String getRandomContext(final String context){
        String targetContext;

        final String head = Objects.requireNonNull(context).isEmpty() ? "" : ContextUtility.getContext(context, true, false);

        do targetContext = head + ContextUtility.getContext(UUID.randomUUID().toString(), true, false);
            while(getContextHandler(targetContext) != null);

        return targetContext;
    }

    //

    @Override
    public synchronized final void start(){
        server.start();
    }

    @Override
    public synchronized final void stop(){
        stop(0);
    }

    @Override
    public synchronized final void stop(final int delay){
        server.stop(delay);
    }

// endregion

    @Override
    public String toString(){
        return "SimpleHttpServer{" +
               "server=" + server +
               ", sessionHandler=" + sessionHandler +
               ", contexts=" + contexts +
               '}';
    }

}
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

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.net.HttpCookie;
import java.util.*;

public class HttpSessionHandler {

    private final Map<String,HttpSession> sessions = Collections.synchronizedMap(new HashMap<>());

    private final String cookie;

    public HttpSessionHandler(){
        this("__session-id");
    }

    public HttpSessionHandler(final String cookie){
        this.cookie = cookie;
    }

    public synchronized String assignSessionID(final HttpExchange exchange){
        String id;
        do id = UUID.randomUUID().toString(); // assign session ID
        while(sessions.containsKey(id));
        return id;
    }

    private String getSetSession(final Headers headers){ // get session that will be set by cookie
        if(headers.containsKey("Set-Cookie"))
           for(final String value : headers.get("Set-Cookie"))
               if(value.startsWith(cookie + "="))
                   return value.substring(cookie.length() + 1, value.indexOf(";"));
       return null;
    }

    public final HttpSession getSession(final HttpExchange exchange){
        final String sessionID;
        final HttpSession session;

        @SuppressWarnings("SpellCheckingInspection")
        final String rcookies = exchange.getRequestHeaders().getFirst("Cookie");
        final Map<String,String> cookies = new HashMap<>();

        if(rcookies != null && !rcookies.isEmpty()){
            final String[] pairs = rcookies.split("; ");
            for(final String pair : pairs){
                final String[] value = pair.split("=");
                cookies.put(value[0], value[1]);
            }
        }

        final String setSession = getSetSession(exchange.getResponseHeaders());
        sessionID = setSession != null ? setSession : cookies.get(cookie); // use session that will be written or session from cookie

        synchronized(this){
            if(!sessions.containsKey(sessionID)){
                session = new HttpSession() {
                    private final String sessionID;
                    private final long creationTime;
                    private long lastAccessTime;

                    {
                        sessionID = assignSessionID(exchange);
                        creationTime = System.currentTimeMillis();
                        lastAccessTime = creationTime;
                        sessions.put(sessionID, this);
                    }

                    @Override
                    public final String getSessionID(){
                        return sessionID;
                    }

                    //

                    @Override
                    public final long getCreationTime(){
                        return creationTime;
                    }

                    @Override
                    public final long getLastAccessed(){
                        return lastAccessTime;
                    }

                    @Override
                    public synchronized final void update(){
                        lastAccessTime = System.currentTimeMillis();
                    }

                };

                final HttpCookie OUT = new HttpCookie(cookie, session.getSessionID());
                OUT.setPath("/");
                OUT.setHttpOnly(true);

                exchange.getResponseHeaders().add("Set-Cookie", OUT.toString());
                sessions.put(session.getSessionID(), session);
            }else{
                session = sessions.get(sessionID);
            }
        }
        return session;
    }

}

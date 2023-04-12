/*
 * Copyright (C) 2023 Katsute <https://github.com/Katsute>
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

/**
 * The session handler is used to assign sessions to exchanges.
 *
 * @see HttpSession
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public class HttpSessionHandler {

    private final Map<String,HttpSession> sessions = Collections.synchronizedMap(new HashMap<>());

    private final String cookie;

    /**
     * Creates a session handler using the cookie <code>__session-id</code>.
     *
     * @since 5.0.0
     */
    public HttpSessionHandler(){
        this("__session-id");
    }

    /**
     * Creates a session handler using a specified cookie.
     *
     * @param cookie cookie to use for session ID
     *
     * @since 5.0.0
     */
    public HttpSessionHandler(final String cookie){
        this.cookie = Objects.requireNonNull(cookie);
    }

    /**
     * Assigns a unique session ID to an exchange.
     *
     * @param exchange http exchange
     * @return unique session ID
     *
     * @since 5.0.0
     */
    public synchronized String assignSessionID(final HttpExchange exchange){
        String id;
        do id = UUID.randomUUID().toString(); // assign session ID
        while(sessions.containsKey(id));
        return id;
    }

    private String getSetSession(final Headers headers){ // get session that will be set by cookie
        for(final Map.Entry<String,List<String>> entry : headers.entrySet())
            if(entry.getKey().equalsIgnoreCase("Set-Cookie")){
                for(final String value : entry.getValue())
                    if(value.startsWith(cookie + "=\""))
                        return value.substring(cookie.length() + 2, value.length() - 1);
                break;
            }
       return null;
    }

    /**
     * Returns the session associated with a particular exchange.
     *
     * @param exchange http exchange
     * @return session associated with exchange
     *
     * @since 5.0.0
     */
    public final HttpSession getSession(final HttpExchange exchange){
        final String sessionID;
        final HttpSession session;

        final Map<String,String> cookies = new HashMap<>();
        for(final Map.Entry<String,List<String>> entry : Objects.requireNonNull(exchange).getRequestHeaders().entrySet()){
            if(entry.getKey().equalsIgnoreCase("Cookie")){
                for(final String value : entry.getValue()){
                    final String[] pair = value.split("=");
                    cookies.put(pair[0], pair[1]);
                }
                break;
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
                OUT.setHttpOnly(true);

                exchange.getResponseHeaders().add("Set-Cookie", OUT.toString());
                sessions.put(session.getSessionID(), session);
            }else{
                session = sessions.get(sessionID);
            }
        }
        return session;
    }

    //

    @Override
    public String toString(){
        return "HttpSessionHandler{" +
               "sessions=" + sessions +
               ", cookie='" + cookie + '\'' +
               '}';
    }

}

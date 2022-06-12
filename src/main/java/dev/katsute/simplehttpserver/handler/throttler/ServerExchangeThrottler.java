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

package dev.katsute.simplehttpserver.handler.throttler;

import com.sun.net.httpserver.HttpExchange;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerExchangeThrottler extends ConnectionThrottler {

    private final Map<InetAddress,AtomicInteger> connections = new ConcurrentHashMap<>();

    private final AtomicInteger connCount = new AtomicInteger(0);
    private final AtomicInteger maxConn = new AtomicInteger(-1);

    public ServerExchangeThrottler(){ }

    public ServerExchangeThrottler(final int maxConnections){
        maxConn.set(maxConnections);
    }

    @Override
    final boolean addConnection(final HttpExchange exchange){
        final InetAddress address = exchange.getRemoteAddress().getAddress();
        final int clientMaxConn = getMaxConnections(exchange);

        if(!connections.containsKey(address))
            connections.put(address, new AtomicInteger(0));

        final AtomicInteger conn = connections.get(address);
        final boolean exempt = canIgnoreConnectionLimit(exchange);

        if(clientMaxConn < 0){
            if(!exempt){
                synchronized(this){
                    final int maxServerConn = maxConn.get();
                    if(maxServerConn < 0 || connCount.get() < maxServerConn){
                        conn.incrementAndGet();
                        connCount.incrementAndGet();
                        return true;
                    }
                    return false;
                }
            }else{
                conn.incrementAndGet();
                return true;
            }
        }else{
            if(!exempt){
                synchronized(this){
                    final int maxServerConn = maxConn.get();
                    if(conn.get() < clientMaxConn && (maxServerConn < 0 || connCount.get() < maxServerConn)){
                        conn.incrementAndGet();
                        connCount.incrementAndGet();
                        return true;
                    }
                    return false;
                }
            }else{
                final AtomicBoolean added = new AtomicBoolean(false);
                conn.updateAndGet(operand -> {
                   if(operand < clientMaxConn) added.set(true);
                   return operand < clientMaxConn ? operand + 1 : operand;
                });
                return added.get();
            }
        }
    }

    @Override
    final void deleteConnection(final HttpExchange exchange){
        final InetAddress address = exchange.getRemoteAddress().getAddress();
        if(connections.containsKey(address)){
            connections.get(address).decrementAndGet();
            if(!canIgnoreConnectionLimit(exchange))
                connCount.decrementAndGet();
        }
    }

    @Override
    public int getMaxConnections(final HttpExchange exchange){
        return -1;
    }

    //

    @SuppressWarnings("SameReturnValue")
    public boolean canIgnoreConnectionLimit(final HttpExchange exchange){
        return false;
    }

    public synchronized final void setMaxServerConnections(final int connections){
        maxConn.set(connections);
    }

    public synchronized final int getMaxServerConnections(){
        return maxConn.get();
    }

}

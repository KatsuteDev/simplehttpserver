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

package dev.katsute.simplehttpserver.handler.file;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CacheFileAdapter {

    private final long cacheTimeMillis;
    private final AtomicLong closestExpiry = new AtomicLong(0);

    public CacheFileAdapter(final long cacheTimeMillis){
        this.cacheTimeMillis = cacheTimeMillis;
    }

    public CacheFileAdapter(final long cacheTime, final TimeUnit timeUnit){
        cacheTimeMillis = timeUnit.toMillis(cacheTime);
    }

    final long getCacheTimeMillis(){
        return cacheTimeMillis;
    }

    final long getClosestExpiry(){
        return closestExpiry.get();
    }

    synchronized final void updateClosestExpiry(final long expiry){
        final long was = closestExpiry.get();
        if(expiry < was || was < System.currentTimeMillis()) // update expiry if new is lower or if expiry has lapsed
            closestExpiry.set(expiry);
    }

}

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

import dev.katsute.simplehttpserver.handler.file.FileOptions.FileLoadingOption;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicLong;

final class FileEntry {

    private final File file;
    private final FileAdapter adapter;
    private final FileLoadingOption loadingOption;

    private byte[] bytes = null;

    private final AtomicLong lastModified = new AtomicLong();

    private final AtomicLong expiry = new AtomicLong(0); // cache only
    private final long cacheTime; // cache only

    //

    FileEntry(final File file, final FileAdapter fileAdapter, final FileLoadingOption loadingOption){
        if(loadingOption == FileLoadingOption.CACHE && !(fileAdapter instanceof CacheFileAdapter))
            throw new IllegalArgumentException("CACHE option must use a CacheFileAdapter");

        this.file           = file;
        this.adapter        = fileAdapter;
        this.loadingOption  = loadingOption;
        this.cacheTime      = fileAdapter instanceof CacheFileAdapter ? ((CacheFileAdapter) fileAdapter).getCacheTimeMillis() : -1;

        if(loadingOption != FileLoadingOption.LIVE && loadingOption != FileLoadingOption.CACHE){
            try{
                bytes = adapter.getBytes(file, Files.readAllBytes(file.toPath()));
            }catch(final Throwable ignored){
                bytes = null;
            }finally{
                if(loadingOption != FileLoadingOption.PRELOAD)
                    lastModified.set(file.lastModified());
            }
        }
    }

    //

    public final File getFile(){
        return file;
    }

    public synchronized final void reloadBytes(){
        if(loadingOption == FileLoadingOption.PRELOAD || loadingOption == FileLoadingOption.LIVE)
            throw new UnsupportedOperationException();
        lastModified.set(file.lastModified());
        try{
            bytes = adapter.getBytes(file, Files.readAllBytes(file.toPath()));
        }catch(final Throwable ignored){
            bytes = null;
        }
    }

    public synchronized final void clearBytes(){
        if(loadingOption == FileLoadingOption.PRELOAD || loadingOption == FileLoadingOption.LIVE)
            throw new UnsupportedOperationException();
        lastModified.set(0);
        bytes = null;
    }

    public final byte[] getBytes(){
        switch(loadingOption){
            case MODIFY:
            case CACHE:
                final long now = System.currentTimeMillis();
                // update the file if it was modified or now exceeds the expiry time
                if((loadingOption == FileLoadingOption.CACHE && now > expiry.getAndUpdate(was -> now + cacheTime)) || file.lastModified() != lastModified.get())
                    reloadBytes();
            case PRELOAD:
                return bytes;
            default:
            case LIVE:
                try{
                    return adapter.getBytes(file, Files.readAllBytes(file.toPath())); // read and adapt bytes
                }catch(final Throwable ignored){
                    return null;
                }
        }
    }

    public final FileLoadingOption getLoadingOption(){
        return loadingOption;
    }

    final long getExpiry(){
        return expiry.get();
    }

}

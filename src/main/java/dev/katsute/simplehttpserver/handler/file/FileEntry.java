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

package dev.katsute.simplehttpserver.handler.file;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import static dev.katsute.simplehttpserver.handler.file.FileOptions.FileLoadingOption.*;

final class FileEntry {

    private final File file;
    private final Path path;
    private final FileAdapter adapter;
    private final FileOptions options;

    //

    private byte[] bytes = null;

    private final AtomicLong lastModified = new AtomicLong(); // modify only

    private final AtomicLong expiry = new AtomicLong(-1); // cache only

    //

    FileEntry(final File file, final FileAdapter fileAdapter, final FileOptions options){ // <- change to this!
        this.file    = file;
        this.path    = file.toPath();
        this.adapter = fileAdapter;
        this.options = options;

        if(options.loading == PRELOAD || options.loading == MODIFY){
            try{
                bytes = adapter.getBytes(file, Files.readAllBytes(path));
            }catch(final Throwable ignored){
                bytes = null;
            }finally{
                if(options.loading == MODIFY)
                    lastModified.set(file.lastModified());
            }
        }
    }

    //

    final File getFile(){
        return file;
    }

    synchronized final void reloadBytes(){
        switch(options.loading){
            default:
            case PRELOAD:
            case LIVE:
                throw new UnsupportedOperationException("Reload is only supported for CACHE and MODIFY options");
            case MODIFY:
                lastModified.set(file.lastModified());
            case CACHE:
                expiry.getAndUpdate(was -> System.currentTimeMillis() + options.cache); // reset expiry
                try{
                    bytes = adapter.getBytes(file, Files.readAllBytes(path));
                }catch(final Throwable ignored){
                    bytes = null;
                }
        }
    }

    synchronized final void clearBytes(){
        switch(options.loading){
            default:
            case PRELOAD:
            case LIVE:
                throw new UnsupportedOperationException("Clear is only supported for CACHE and MODIFY options");
            case MODIFY:
                lastModified.set(-1); // force getBytes to re-fetch
            case CACHE:
                bytes = null;
        }
    }

    final byte[] getBytes(){
        switch(options.loading){
            case PRELOAD:
                return bytes;
            default:
            case LIVE:
                try{
                    return adapter.getBytes(file, Files.readAllBytes(path)); // read and adapt bytes
                }catch(final Throwable ignored){ }
                return null;
            case MODIFY:
                if(file.lastModified() != lastModified.get()) // reload if modified
                    reloadBytes();
                return bytes;
            case CACHE:
                if(bytes == null || isExpired()) // fetch if no data or re-fetch if expired
                    reloadBytes();
                return bytes;
        }
    }

    final boolean isExpired(){
        return options.loading == CACHE && expiry.get() < System.currentTimeMillis();
    }

    //

    @Override
    public String toString(){
        return "FileEntry{" +
               "file=" + file +
               ", path=" + path +
               ", adapter=" + adapter +
               ", options=" + options +
               ", bytes=" + Arrays.toString(bytes) +
               ", lastModified=" + lastModified +
               ", expiry=" + expiry +
               ", expired=" + isExpired() +
               '}';
    }

}

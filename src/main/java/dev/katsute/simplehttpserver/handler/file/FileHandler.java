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

import com.sun.net.httpserver.HttpExchange;
import dev.katsute.simplehttpserver.SimpleHttpExchange;
import dev.katsute.simplehttpserver.SimpleHttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileHandler implements SimpleHttpHandler {

    private static final FileOptions defaultOptions = new FileOptions();

    private static final FileAdapter defaultAdapter = new FileAdapter() {
            @Override
            public final byte[] getBytes(final File file, final byte[] bytes){
                return bytes;
            }

            @Override
            public final String getName(final File file){
                return FileHandler.getName(file);
            }
    };

    private static String getName(final File file){
        return file.getParentFile() == null ? file.getPath() : file.getName(); // fix issue with drives not having a name
    }

    //

    private final FileAdapter adapter;

    private final Map<String,FileEntry> files = new ConcurrentHashMap<>();
    private final Map<String,DirectoryEntry> directories = new ConcurrentHashMap<>();

    //

    public FileHandler(){
        this(defaultAdapter);
    }

    public FileHandler(final FileAdapter adapter){
        this.adapter = adapter;
    }

    //

    public final void addFile(final File file){
        addFile(file, adapter.getName(file), null);
    }

    public final void addFile(final File file, final FileOptions options){
        addFile(file, adapter.getName(file), options);
    }

    public final void addFile(final File file, final String fileName){
        addFile(file, fileName, null);
    }

    public final void addFile(final File file, final String fileName, final FileOptions options){
        try{
            final FileOptions opts = options == null ? defaultOptions : new FileOptions(options); // dereference to prevent modification
            files.put(
                ContextUtility.joinContexts(true, false, opts.context, fileName == null ? adapter.getName(file) : fileName),
                new FileEntry(file, adapter, opts)
            );
        }catch(final UncheckedIOException ignored){ }
    }

    //

    public final void addFiles(final File[] files){
        addFiles(files, null);
    }

    public final void addFiles(final File[] files, final FileOptions options){
        for(final File file : files)
            addFile(file, options);
    }

    //

    public final void addDirectory(final File directory){
        addDirectory(directory, getName(directory), null);
    }

    public final void addDirectory(final File directory, final FileOptions options){
        addDirectory(directory, getName(directory), options);
    }

    public final void addDirectory(final File directory, final String directoryName){
        addDirectory(directory, directoryName, null);
    }

    public final void addDirectory(final File directory, final String directoryName, final FileOptions options){
        try{
            final FileOptions opts = options == null ? defaultOptions : new FileOptions(options); // dereference to prevent modification
            final String target = ContextUtility.joinContexts(true, false, opts.context, directoryName);
            directories.put(
                target,
                new DirectoryEntry(directory, adapter, opts)
            );
        }catch(final UncheckedIOException ignored){}
    }

    //

    public final void removeFile(final String context){
        files.remove(ContextUtility.getContext(context, true, false));
    }

    public final void removeFile(final File file){
        removeFile(adapter.getName(file));
    }

    public final void removeFile(final File file, final FileOptions options){
        removeFile(ContextUtility.joinContexts(true, false, options.context, adapter.getName(file)));
    }

    public final void removeDirectory(final String context){
        directories.remove(ContextUtility.getContext(context, true, false));
    }

    public final void removeDirectory(final File directory){
        removeDirectory(getName(directory));
    }

    public final void removeDirectory(final String context, final File directory){
       removeDirectory(ContextUtility.joinContexts(true, false, context, getName(directory)));
    }

    //

    @Override
    public final void handle(final SimpleHttpExchange exchange) throws IOException {
        final String context = URLDecoder.decode(ContextUtility.getContext(exchange.getRequestURI().getPath().substring(exchange.getHttpContext().getPath().length()), true, false), "UTF-8");

        boolean refreshExpired = false;

        if(files.containsKey(context)){ // exact file match
            final FileEntry entry = files.get(context);
            handle(exchange, entry.getFile(), entry.getBytes());
        }else{ // leading directory match
            String match = "";
            for(final String key : directories.keySet())
                if(context.startsWith(key) && key.startsWith(match))
                    match = key;

            if(match.isEmpty()){ // no match
                handle(exchange, null, null);
            }else{ // get file from matching directory
                final DirectoryEntry dir = directories.get(match);
                String rel = context.substring(match.length());

                final FileEntry entry = dir.getFileEntry(rel);

                if(entry != null && entry.isExpired()){
                    refreshExpired = true; // check if other files also need clearing
                    entry.clearBytes();
                }

                handle(
                    exchange,
                    entry == null ? dir.getFile(rel) : entry.getFile(),
                    entry == null ? dir.getBytes(rel) : entry.getBytes()
                );

            }
        }
        exchange.close();

        // cache only
        if(refreshExpired){ // clear expired files
            files.values().stream().filter(FileEntry::isExpired).forEach(FileEntry::clearBytes);
            directories.values().forEach(dir -> dir.getFiles().values().stream().filter(FileEntry::isExpired).forEach(FileEntry::clearBytes));
        }
    }

    @Override
    public final void handle(final HttpExchange exchange) throws IOException{
        SimpleHttpHandler.super.handle(exchange);
    }

    public void handle(final SimpleHttpExchange exchange, final File source, final byte[] bytes) throws IOException {
        exchange.send(bytes, HttpURLConnection.HTTP_OK);
    }

}

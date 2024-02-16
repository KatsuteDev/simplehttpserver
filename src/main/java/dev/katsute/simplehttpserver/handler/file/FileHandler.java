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

package dev.katsute.simplehttpserver.handler.file;

import com.sun.net.httpserver.HttpExchange;
import dev.katsute.simplehttpserver.SimpleHttpExchange;
import dev.katsute.simplehttpserver.SimpleHttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A file handler can be used to serve single or multiple files on a server with optional pre/post processing using {@link FileAdapter}s.
 * <br>
 * <h1>{@link FileAdapter}</h1>
 * A {@link FileAdapter} determines where a file can be accessed and what content it will return. By default files would be accessible at the file name (including extension) with the file content.
 *
 * <h1>Adding Files</h1>
 * The name parameters in the add methods supersedes the {@link FileAdapter} and makes a file accessible at whatever name you set.
 *
 * <h1>{@link FileOptions}</h1>
 * File options can be added to influence the behavior of added files.
 * <h2>Context</h2>
 * The {@link FileOptions#context} property determines at where the file will be located with respect to the file handler. By default this is "" and any added files will be accessible directly after the file handler's context.
 * <br>
 * <b>Example:</b> <code>/fileHandlerContext/file.txt</code> by default and <code>/fileHandlerContext/optionsContext/file.txt</code> if a context was set.
 * <h2>Loading Options</h2>
 * The {@link FileOptions#loading} option determines how a file should be loaded when added.
 * <ul>
 *     <li>{@link FileOptions.FileLoadingOption#PRELOAD} - files are read when added</li>
 *     <li>{@link FileOptions.FileLoadingOption#MODIFY} - files are read when added and when modified</li>
 *     <li>{@link FileOptions.FileLoadingOption#CACHE} - files are read when requested and cached for a set time</li>
 *     <li>{@link FileOptions.FileLoadingOption#LIVE} - files are read when requested</li>
 * </ul>
 * <h2>Cache</h2>
 * If the loading option {@link FileOptions.FileLoadingOption#CACHE} is used, the {@link FileOptions#cache} determines how long to cache files for in milliseconds.
 * <h2>Walk</h2>
 * When directories are added, if true, will also include subdirectories; if false, will only include files in the immediate directory.
 *
 * @see FileAdapter
 * @see FileOptions
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
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
        return Objects.requireNonNull(file).getParentFile() == null ? file.getPath() : file.getName(); // fix issue with drives not having a name
    }

    //

    private final FileAdapter adapter;

    private final Map<String,FileEntry> files = new ConcurrentHashMap<>();
    private final Map<String,DirectoryEntry> directories = new ConcurrentHashMap<>();

    //

    /**
     * Creates a file handler.
     *
     * @since 5.0.0
     */
    public FileHandler(){
        this(defaultAdapter);
    }

    /**
     * Creates a file handler, adapting the added files with an {@link FileAdapter}.
     *
     * @param adapter file adapter
     *
     * @see FileAdapter
     * @since 5.0.0
     */
    public FileHandler(final FileAdapter adapter){
        this.adapter = Objects.requireNonNull(adapter);
    }

    //

    /**
     * Adds a file to the handler.
     *
     * @param file file to add
     *
     * @see #addFile(File, FileOptions)
     * @see #addFile(File, String)
     * @see #addFile(File, String, FileOptions)
     * @since 5.0.0
     */
    public final void addFile(final File file){
        addFile(file, adapter.getName(file), null);
    }

    /**
     * Adds a file to the handler from a set of {@link FileOptions}s.
     *
     * @param file file to add
     * @param options file options
     *
     * @see FileOptions
     * @see #addFile(File)
     * @see #addFile(File, String)
     * @see #addFile(File, String, FileOptions)
     * @since 5.0.0
     */
    public final void addFile(final File file, final FileOptions options){
        addFile(file, adapter.getName(file), options);
    }

    /**
     * Adds a file to the handler with a set name. Ignores the {@link FileAdapter} if set.
     *
     * @param file file to add
     * @param fileName file name to use
     *
     * @see #addFile(File)
     * @see #addFile(File, FileOptions)
     * @see #addFile(File, String, FileOptions)
     * @since 5.0.0
     */
    public final void addFile(final File file, final String fileName){
        addFile(file, fileName, null);
    }

    /**
     * Adds a file to the handler with a set name from a set of {@link FileOptions}. Ignores the {@link FileAdapter} if set.
     *
     * @param file file to add
     * @param options file options
     * @param fileName file name to use
     *
     * @see FileOptions
     * @see #addFile(File)
     * @see #addFile(File, FileOptions)
     * @see #addFile(File, String)
     * @since 5.0.0
     */
    public final void addFile(final File file, final String fileName, final FileOptions options){
        Objects.requireNonNull(file);
        try{
            final FileOptions opts = options == null ? defaultOptions : new FileOptions(options); // dereference to prevent modification
            Objects.requireNonNull(opts.loading);
            Objects.requireNonNull(opts.context);
            files.put(
                ContextUtility.joinContexts(true, false, opts.context, fileName == null ? adapter.getName(file) : fileName),
                new FileEntry(file, adapter, opts)
            );
        }catch(final UncheckedIOException ignored){ }
    }

    //

    /**
     * Adds multiple files to the handler.
     *
     * @param files files to add
     *
     * @see #addFiles(File[], FileOptions)
     * @since 5.0.0
     */
    public final void addFiles(final File[] files){
        addFiles(files, null);
    }

    /**
     * Adds multiple files to the handler from a set of {@link FileOptions}.
     *
     * @param files files to add
     * @param options file options
     *
     * @see FileOptions
     * @see #addFiles(File[], FileOptions)
     * @since 5.0.0
     */
    public final void addFiles(final File[] files, final FileOptions options){
        for(final File file : Objects.requireNonNull(files))
            addFile(file, options);
    }

    //

    /**
     * Adds a directory to the handler using the directory name.
     *
     * @param directory directory to add
     *
     * @see #addDirectory(File, FileOptions)
     * @see #addDirectory(File, String)
     * @see #addDirectory(File, String, FileOptions)
     * @since 5.0.0
     */
    public final void addDirectory(final File directory){
        addDirectory(directory, getName(directory), null);
    }

     /**
     * Adds a directory to the handler using the directory name from a set of file options.
     *
     * @param directory directory to add
     * @param options file options
     *
     * @see FileOptions
     * @see #addDirectory(File)
     * @see #addDirectory(File, String)
     * @see #addDirectory(File, String, FileOptions)
     * @since 5.0.0
     */
    public final void addDirectory(final File directory, final FileOptions options){
        addDirectory(directory, getName(directory), options);
    }

     /**
     * Adds a directory to the handler with a set name.
     *
     * @param directory directory to add
     * @param directoryName directory name to use
     *
     * @see #addDirectory(File)
     * @see #addDirectory(File, FileOptions)
     * @see #addDirectory(File, String, FileOptions)
     * @since 5.0.0
     */
    public final void addDirectory(final File directory, final String directoryName){
        addDirectory(directory, directoryName, null);
    }

    /**
     * Adds a directory to the handler using the directory name from a set of file options.
     *
     * @param directory directory to add
     * @param directoryName directory name to use
     * @param options file options
     *
     * @see FileOptions
     * @see #addDirectory(File)
     * @see #addDirectory(File, FileOptions)
     * @see #addDirectory(File, String)
     * @since 5.0.0
     */
    public final void addDirectory(final File directory, final String directoryName, final FileOptions options){
        Objects.requireNonNull(directory);
        try{
            final FileOptions opts = options == null ? defaultOptions : new FileOptions(options); // dereference to prevent modification
            Objects.requireNonNull(opts.loading);
            Objects.requireNonNull(opts.context);
            final String target = ContextUtility.joinContexts(true, false, opts.context, directoryName);
            directories.put(
                target,
                new DirectoryEntry(directory, adapter, opts)
            );
        }catch(final UncheckedIOException ignored){}
    }

    //

    /**
     * Removes a file from the handler at the specified context.
     *
     * @param context context
     *
     * @see #removeFile(File)
     * @see #removeFile(File, FileOptions)
     * @since 5.0.0
     */
    public final void removeFile(final String context){
        files.remove(ContextUtility.getContext(Objects.requireNonNull(context), true, false));
    }

    /**
     * Removes a file from the handler.
     *
     * @param file file to remove
     *
     * @see #removeFile(String)
     * @see #removeFile(File, FileOptions)
     * @since 5.0.0
     */
    public final void removeFile(final File file){
        removeFile(adapter.getName(Objects.requireNonNull(file)));
    }

    /**
     * Removes a file from the handler with file options. Only required if {@link FileOptions#context} was used.
     *
     * @param file file to remove
     * @param options file options
     *
     * @see FileOptions
     * @see #removeFile(String)
     * @see #removeFile(File)
     * @since 5.0.0
     */
    public final void removeFile(final File file, final FileOptions options){
        removeFile(ContextUtility.joinContexts(true, false, options.context, adapter.getName(Objects.requireNonNull(file))));
    }

    /**
     * Removes a directory from the handler at a specified context.
     *
     * @param context context
     *
     * @see #removeDirectory(File)
     * @see #removeDirectory(File, FileOptions)
     * @since 5.0.0
     */
    public final void removeDirectory(final String context){
        directories.remove(ContextUtility.getContext(Objects.requireNonNull(context), true, false));
    }

    /**
     * Removes a directory from the handler.
     *
     * @param directory directory to remove
     *
     * @see #removeDirectory(String)
     * @see #removeDirectory(File, FileOptions)
     * @since 5.0.0
     */
    public final void removeDirectory(final File directory){
        removeDirectory(getName(directory));
    }

    /**
     * Removes a directory from the handler with file options. Only required if {@link FileOptions#context} was used.
     *
     * @param directory directory to remove
     * @param options file options
     *
     * @see FileOptions
     * @see #removeDirectory(String)
     * @see #removeDirectory(File)
     * @since 5.0.0
     */
    public final void removeDirectory(final File directory, final FileOptions options){
        removeDirectory(ContextUtility.joinContexts(true, false, options.context, getName(directory)));
    }

    //

    @Override
    public final void handle(final SimpleHttpExchange exchange) throws IOException {
        final String context = URLDecoder.decode(ContextUtility.getContext(exchange.getRequestURI().getPath().substring(exchange.getHttpContext().getPath().length()), true, false), "UTF-8");

        boolean refreshExpired = false;

        if(files.containsKey(context)){ // exact file match
            final FileEntry entry = files.get(context);

            if(entry.isExpired()){
                refreshExpired = true; // check if other files also need clearing
                entry.clearBytes();
            }

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

    /**
     * Handles a file exchange. The file bytes are the bytes after post processing if {@link FileAdapter} is used.
     *
     * @param exchange http exchange
     * @param source file source
     * @param bytes file bytes
     *
     * @throws IOException IO exception
     *
     * @since 5.0.0
     */
    public void handle(final SimpleHttpExchange exchange, final File source, final byte[] bytes) throws IOException {
        if(source == null)
            exchange.send(HttpURLConnection.HTTP_NOT_FOUND);
        else
            exchange.send(bytes, HttpURLConnection.HTTP_OK);
    }

    //

    @Override
    public String toString(){
        return "FileHandler{" +
               "adapter=" + adapter +
               ", files=" + files +
               ", directories=" + directories +
               '}';
    }

}
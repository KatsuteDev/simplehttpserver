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

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static dev.katsute.simplehttpserver.handler.file.FileOptions.FileLoadingOption.*;

final class DirectoryEntry {

    private final File file;
    private final Path path;
    @SuppressWarnings("SpellCheckingInspection")
    private final String abs, absl;
    private final FileAdapter adapter;
    private final FileOptions options;

    //

    private final Map<String,FileEntry> files = new ConcurrentHashMap<>(); // non LIVE only

    //

    DirectoryEntry(final File directory, final FileAdapter adapter, final FileOptions options){
        this.file     = directory;
        this.path     = directory.toPath();
        this.abs      = directory.getAbsolutePath();
        this.absl     = this.abs.toLowerCase();
        this.adapter  = adapter;
        this.options  = options;

        if(this.options.loading != LIVE){
            if(!options.walk){
                final File[] files = directory.listFiles(File::isFile);
                if(files != null)
                    for(final File f : files)
                        addFile(f);
            }else{
                try{
                    //noinspection Convert2Diamond
                    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                        @Override
                        public final FileVisitResult visitFile(final Path p, final BasicFileAttributes attrs){
                            addDirectoryFile(p.toFile());
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }catch(final IOException e){
                    throw new UncheckedIOException(e);
                }
            }
        }
    }

    // top level files
    private void addFile(final File file){
        files.put(
            ContextUtility.getContext(adapter.getName(file), true, false),
            new FileEntry(file, adapter, options)
        );
    }

    // file in sub directories
    private void addDirectoryFile(final File file){
        files.put(
            ContextUtility.joinContexts(true, false, path.relativize(file.toPath().getParent()).toString(), adapter.getName(file)),
            new FileEntry(file, adapter, options)
        );
    }

    //

    final Map<String,FileEntry> getFiles(){
        return new HashMap<>(files); // dereference
    }

    @SuppressWarnings("SpellCheckingInspection")
    final File getFile(final String path){ // file names are case insensitive
        final String relative = ContextUtility.getContext(path, true, false);
        final File parentFile = new File(abs + relative).getParentFile();
        final String pabs     = parentFile.getAbsolutePath();

        // if not top level directory or if not child of directory folder, then return null file
        if(!pabs.equalsIgnoreCase(abs) && (!options.walk || !pabs.toLowerCase().startsWith(absl))) return null;

        final File targetFile = Paths.get(abs, relative).toFile();
        final String fileName = targetFile.getParentFile() == null ? targetFile.getPath() : targetFile.getName();

        // for each file in parent directory, run adapter to find file that matches adapted name
        final File[] parentFiles = parentFile.listFiles();
        if(parentFiles != null)
            for(final File file : parentFiles)
                if(fileName.equalsIgnoreCase(adapter.getName(file)))
                    return file;
        return null;
    }

    final FileEntry getFileEntry(final String path){
        final String context  = ContextUtility.getContext(path, true, false);
        final FileEntry entry = files.get(context);
        if(entry == null){ // add new entry if not already added and file exists
            final File file = getFile(path);
            return file != null && file.exists()
                ? options.loading != LIVE // only add to files if not LIVE
                    ? files.put(context, new FileEntry(file, adapter, options))
                    : new FileEntry(file, adapter, options)
                : null;
        }else if(!entry.getFile().exists()){ // remove entry if file no longer exists
            files.remove(context);
            return null;
        }else{ // return existing if exists
            return entry;
        }
    }

    final byte[] getBytes(final String path){
        if(options.loading != LIVE ){ // find preloaded bytes
            final FileEntry entry = getFileEntry(path);
            return entry != null ? entry.getBytes() : null;
        }else{
            try{
                final File file = Objects.requireNonNull(getFile(path)); // check if file is allowed
                return file.isFile() ? adapter.getBytes(file, Files.readAllBytes(file.toPath())) : null; // adapt bytes here
            }catch(final Throwable ignored){
                return null;
            }
        }
    }

    //


    @Override
    public String toString(){
        return "DirectoryEntry{" +
               "file=" + file +
               ", path=" + path +
               ", abs='" + abs + '\'' +
               ", absl='" + absl + '\'' +
               ", adapter=" + adapter +
               ", options=" + options +
               ", files=" + files +
               '}';
    }

}

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

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DirectoryEntry {

    private final File directory;
    private final FileAdapter adapter;
    private final FileLoadingOption loadingOption;
    private final boolean isWalkthrough;

    private final Map<String,FileEntry> files = new ConcurrentHashMap<>(); // non LIVE only
    private final Path directoryPath;

    //

    DirectoryEntry(final File directory, final FileAdapter adapter, final FileLoadingOption loadingOption, final boolean isWalkthrough){
        this.directory     = directory;
        this.adapter       = adapter;
        this.loadingOption = loadingOption;
        this.isWalkthrough = isWalkthrough;

        directoryPath      = directory.toPath();

        if(loadingOption != FileLoadingOption.LIVE){
            if(!isWalkthrough){
                final File[] listFiles = Objects.requireNonNullElse(directory.listFiles(File::isFile), new File[0]);
                for(final File file : listFiles)
                    addFile(file);
            }else{
                try{
                    Files.walkFileTree(directoryPath, new SimpleFileVisitor<>() {
                        @Override
                        public final FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs){
                            addDirectoryFile(path.toFile());
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
            new FileEntry(file, adapter, loadingOption)
        );
    }

    // file in sub directories
    private void addDirectoryFile(final File file){
        final String relative = directoryPath.relativize(file.toPath().getParent()).toString(); // attach the relative path (parent) to the adapted file name
        files.put(
            ContextUtility.joinContexts(true, false, relative, adapter.getName(file)),
            new FileEntry(file, adapter, loadingOption)
        );
    }

    //

    public final File getDirectory(){
        return directory;
    }

    public final Map<String,FileEntry> getFiles(){
        return new HashMap<>(files);
    }

    public final File getFile(final String path){
        final String relative   = ContextUtility.getContext(path, true, false);
        final String dabs       = directory.getAbsolutePath();
        final File parentFile   = new File(dabs + relative).getParentFile();
        final String pabs       = parentFile.getAbsolutePath();

        // if not top level directory or if not child of directory folder, then return null file
        if(!pabs.equals(dabs) && (!isWalkthrough || !pabs.startsWith(dabs))) return null;

        final File targetFile = Paths.get(dabs, relative).toFile();
        final String fileName = targetFile.getParentFile() == null ? targetFile.getPath() : targetFile.getName();

        // for each file in parent directory, run adapter to find file that matches adapted name
        for(final File file : Objects.requireNonNullElse(parentFile.listFiles(), new File[0]))
            if(fileName.equals(adapter.getName(file)))
                return file;
        return null;
    }

    public final FileEntry getFileEntry(final String path){
        final String context  = ContextUtility.getContext(path, true, false);
        final FileEntry entry = files.get(context);
        if(entry == null){ // add new entry if not already added and file exists
            final File file = getFile(path);
            return file != null && file.exists()
                ? loadingOption != FileLoadingOption.LIVE // only add to files if not LIVE
                    ? files.put(context, new FileEntry(file, adapter, loadingOption))
                    : new FileEntry(file, adapter, loadingOption)
                : null;
        }else if(!entry.getFile().exists()){ // remove entry if file no longer exists
            files.remove(context);
            return null;
        }else{ // return existing if exists
            return entry;
        }
    }

    public final byte[] getBytes(final String path){
        if(loadingOption != FileLoadingOption.LIVE ){ // find preloaded bytes
            final FileEntry entry = getFileEntry(path);
            return entry != null ? entry.getBytes() : null;
        }else{
            try{
                final File file = Objects.requireNonNull(getFile(path)); // check if file is allowed
                return file.isFile() ? adapter.getBytes(file, Files.readAllBytes(file.toPath())) : null; // adapt bytes here
            }catch(final NullPointerException | IOException ignored){
                return null;
            }
        }
    }

    public final FileLoadingOption getLoadingOption(){
        return loadingOption;
    }

    public final boolean isWalkthrough(){
        return isWalkthrough;
    }

}

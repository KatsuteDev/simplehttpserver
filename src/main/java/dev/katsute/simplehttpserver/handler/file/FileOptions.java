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

/**
 * Represents a set of options for an added file.
 *
 * @see FileHandler
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public class FileOptions {

    /**
     * Creates a new set of file options.
     *
     * @since 5.0.0
     */
    public FileOptions(){ }

    FileOptions(final FileOptions options){
        this.context = options.context;
        this.loading = options.loading;
        this.cache   = options.cache;
        this.walk    = options.walk;
    }

    /**
     * The context that files should be served at relative to the file handler's context.
     *
     * @since 5.0.0
     */
    public String context = "";

    /**
     * The file loading option.
     *
     * @see FileLoadingOption
     * @since 5.0.0
     */
    public FileLoadingOption loading = FileLoadingOption.LIVE;

    /**
     * How long to cache files for. Only used with {@link FileLoadingOption#CACHE}.
     *
     * @see FileLoadingOption#CACHE
     * @since 5.0.0
     */
    public long cache = 0;

    /**
     * When true, also include all subdirectories when adding directories.
     *
     * @since 5.0.0
     */
    public boolean walk = false;

    @Override
    public String toString(){
        return "FileOptions{" +
               "context='" + context + '\'' +
               ", loading=" + loading +
               ", cache=" + cache +
               ", walk=" + walk +
               '}';
    }

    //

    /**
     * How to load files added to the file handler.
     *
     * @since 5.0.0
     * @version 5.0.0
     * @author Katsute
     */
    public enum FileLoadingOption {

        /**
         * File bytes will be read when file is added.
         *
         * @since 5.0.0
         */
        PRELOAD,
        /**
         * File bytes will be read when file is added and any time it is modified.
         *
         * @since 5.0.0
         */
        MODIFY,
        /**
         * File bytes will be read when requested, and cached for {@link #cache} milliseconds.
         *
         * @see FileOptions#cache
         * @since 5.0.0
         */
        CACHE,
        /**
         * File bytes will be read when requested.
         *
         * @since 5.0.0
         */
        LIVE

    }

    /**
     * Builder used to create {@link FileOptions}.
     *
     * @see FileOptions
     * @since 5.0.0
     * @version 5.0.0
     * @author Katsute
     */
    public static class Builder {

        private final FileOptions options = new FileOptions();

        /**
         * Sets the context.
         *
         * @param context context
         * @return builder
         *
         * @see FileOptions#context
         * @since 5.0.0
         */
        public final Builder setContext(final String context){
            options.context = context;
            return this;
        }

        /**
         * Sets the loading option.
         *
         * @param option loading option
         * @return builder
         *
         * @see FileOptions#loading
         * @see FileLoadingOption
         * @since 5.0.0
         */
        public final Builder setLoadingOption(final FileLoadingOption option){
            options.loading = option;
            return this;
        }

        /**
         * Sets the cache time.
         *
         * @param cache cache time
         * @return builder
         *
         * @see FileOptions#cache
         * @since 5.0.0
         */
        public final Builder setCache(final long cache){
            options.cache = cache;
            return this;
        }

        /**
         * Sets the directory walk option.
         *
         * @param walk walk
         * @return builder
         *
         * @see FileOptions#walk
         * @since 5.0.0
         */
        public final Builder setWalk(final boolean walk){
            options.walk = walk;
            return this;
        }

        /**
         * Returns the builder as file options.
         *
         * @return file options
         *
         * @see FileOptions
         * @since 5.0.0
         */
        public final FileOptions build(){
            return new FileOptions(options);
        }

        @Override
        public String toString(){
            return "Builder{" +
                   "options=" + options +
                   '}';
        }

    }

}

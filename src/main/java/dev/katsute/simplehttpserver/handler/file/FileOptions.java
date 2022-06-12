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

public class FileOptions {

    public FileOptions(){ }

    public String context = "";

    public FileLoadingOption loading = FileLoadingOption.LIVE;

    public boolean walk = false;

    //

    public enum FileLoadingOption {

        PRELOAD,
        MODIFY,
        CACHE,
        LIVE

    }

    public static class Builder {

        private final FileOptions options = new FileOptions();

        public final Builder setContext(final String context){
            options.context = context;
            return this;
        }

        public final Builder setLoadingOption(final FileLoadingOption option){
            options.loading = option;
            return this;
        }

        public final Builder setWalk(final boolean walk){
            options.walk = walk;
            return this;
        }

        public final FileOptions build(){
            return options;
        }

    }

}

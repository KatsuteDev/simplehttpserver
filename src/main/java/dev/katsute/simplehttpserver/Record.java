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

package dev.katsute.simplehttpserver;

import java.util.*;

public class Record {

    private final Map<String,Header> headers;
    private final String name, value;

    @SuppressWarnings("unchecked")
    Record(final Map.Entry<String,Map<String,?>> entry){
        name  = Objects.requireNonNull(entry.getKey());
        value = Objects.requireNonNull(Objects.requireNonNull(entry.getValue()).get("value")).toString();

        final Map<String,Header> headers = new HashMap<>();
        for(final Map.Entry<String,Map<String,?>> e : Objects.requireNonNull((Map<String,Map<String,?>>) entry.getValue().get("Headers")).entrySet()){
            headers.put(e.getKey(), new Header(
                Objects.requireNonNull(e.getValue().get("header-name")).toString(),
                Objects.requireNonNull(e.getValue().get("header-value")).toString(),
                (Map<String,String>) Objects.requireNonNull(e.getValue().get("parameters"))
            ));
        }
        this.headers = headers;
    }

    public final String getName(){
        return name;
    }

    public final String getValue(){
        return value;
    }

    public final Header getHeader(final String key){
        return headers.get(key);
    }

    public final Map<String,Header> getHeaders(){
        return new HashMap<String,Header>(headers);
    }

    public final boolean isFile(){
        return this instanceof FileRecord;
    }

    public final FileRecord asFile(){
        return ((FileRecord) this);
    }

    public static class Header {

        private final String name, value;
        private final Map<String,String> params;

        Header(final String name, final String value, final Map<String,String> params){
            this.name = name;
            this.value = value;
            this.params = new HashMap<>(params);
        }

        public final String getName(){
            return name;
        }

        public final String getValue(){
            return value;
        }

        public final String getParameter(final String key){
            return params.get(key);
        }

        public final Map<String,String> getParameters(){
            return params;
        }

    }

}

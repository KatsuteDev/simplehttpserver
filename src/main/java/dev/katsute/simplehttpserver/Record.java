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

/**
 * Represents a record in a multipart/form-data request.
 *
 * @see MultipartFormData
 * @see FileRecord
 *
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
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

    /**
     * Returns the form input name.
     *
     * @return form input name
     *
     * @since 5.0.0
     */
    public final String getName(){
        return name;
    }

    /**
     * Returns the form value.
     *
     * @return form value
     *
     * @since 5.0.0
     */
    public final String getValue(){
        return value;
    }

    /**
     * Returns a specified header.
     *
     * @param key header key
     * @return header
     *
     * @see Header
     * @see #getHeaders()
     * @since 5.0.0
     */
    public final Header getHeader(final String key){
        return headers.get(key);
    }

    /**
     * Returns all the headers.
     *
     * @return headers
     *
     * @see Header
     * @see #getHeader(String)
     * @since 5.0.0
     */
    public final Map<String,Header> getHeaders(){
        return new HashMap<>(headers);
    }

    /**
     * Returns if the record is a {@link FileRecord}.
     *
     * @return if record is a file record
     *
     * @see #asFile()
     * @see FileRecord
     * @since 5.0.0
     */
    public final boolean isFile(){
        return this instanceof FileRecord;
    }

    /**
     * Casts the record to a {@link FileRecord}.
     *
     * @return record as a file record
     *
     * @see #isFile()
     * @see FileRecord
     * @since 5.0.0
     */
    public final FileRecord asFile(){
        return ((FileRecord) this);
    }

    /**
     * Represents a header in a multipart/form-data record.
     *
     * @see Record#getHeader(String)
     * @see Record#getValue()
     * @since 5.0.0
     * @version 5.0.0
     * @author Katsute
     */
    public static class Header {

        private final String name, value;
        private final Map<String,String> params;

        Header(final String name, final String value, final Map<String,String> params){
            this.name = name;
            this.value = value;
            this.params = new HashMap<>(params);
        }

        /**
         * Returns the header name.
         *
         * @return header name
         *
         * @since 5.0.0
         */
        public final String getName(){
            return name;
        }

        /**
         * Returns the header value.
         *
         * @return header value
         *
         * @since 5.0.0
         */
        public final String getValue(){
            return value;
        }

        /**
         * Returns the value for a header parameter.
         *
         * @param key header key
         * @return value of header parameter
         *
         * @see #getParameters()
         * @since 5.0.0
         */
        public final String getParameter(final String key){
            return params.get(Objects.requireNonNull(key));
        }

        /**
         * Returns a map of all parameters for a header.
         *
         * @return header parameters
         *
         * @see #getParameter(String)
         * @since 5.0.0
         */
        public final Map<String,String> getParameters(){
            return new HashMap<>(params);
        }

    }

}

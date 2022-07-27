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
 * Representation of a multipart/form-data body.
 *
 * @see FileRecord
 * @see Record
 * @see SimpleHttpExchange#getMultipartFormData()
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public class MultipartFormData {

    private final Map<String,Record> data;

    MultipartFormData(final Map<String,Record> data){
        this.data = new HashMap<>(data);
    }

    /**
     * Returns the record for a particular key. If the record is supposed to be a file use {@link Record#asFile()}.
     *
     * @param name record key
     * @return record
     *
     * @see Record
     * @see FileRecord
     * @see #getEntries()
     * @since 5.0.0
     */
    public final Record getEntry(final String name){
        return data.get(Objects.requireNonNull(name));
    }

    /**
     * Returns all the records in the response body.
     *
     * @return map of all records
     *
     * @see Record
     * @see FileRecord
     * @see #getEntry(String)
     * @since 5.0.0
     */
    public final Map<String,Record> getEntries(){
        return new HashMap<>(data);
    }

    //

    @Override
    public String toString(){
        return "MultipartFormData{" +
               "data=" + data +
               '}';
    }

}

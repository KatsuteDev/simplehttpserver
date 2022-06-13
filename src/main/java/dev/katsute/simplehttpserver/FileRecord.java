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

import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileRecord extends Record {

    final String fileName, contentType;
    private final byte[] bytes;

    FileRecord(final Map.Entry<String,Map<String,?>> entry){
        super(entry);

        fileName    = Objects.requireNonNull(Objects.requireNonNull(getHeader("Content-Disposition").getParameter("filename")));
        contentType = Objects.requireNonNull(Objects.requireNonNull(getHeader("Content-Type")).getValue());
        bytes       = getValue().getBytes(StandardCharsets.UTF_8);
    }

    public final String getFileName(){
        return fileName;
    }

    public final String getContentType(){
        return contentType;
    }

    public final byte[] getBytes(){
        return Arrays.copyOf(bytes, bytes.length); // dereference
    }

}

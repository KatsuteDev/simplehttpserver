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

import java.util.HashMap;
import java.util.Map;

public class MultipartFormData {

    private final Map<String,Record> data;

    MultipartFormData(final Map<String,Record> data){
        this.data = new HashMap<>(data);
    }

    public final Record getEntry(final String name){
        return data.get(name);
    }

    public final Map<String,Record> getEntries(){
        return new HashMap<>(data);
    }

}

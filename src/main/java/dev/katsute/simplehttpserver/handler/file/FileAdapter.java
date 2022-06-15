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

import java.io.File;
import java.util.Objects;

/**
 * When using a {@link FileHandler}, determines at what name a file should be located at, and what content to return to the client.
 *
 * @see FileHandler
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public interface FileAdapter {

    /**
     * Returns the name that the file should be accessible at. By default this returns the file name.
     * <br>
     * Not used for directories.
     *
     * @param file file
     * @return file name
     *
     * @since 5.0.0
     */
    default String getName(final File file){
        return Objects.requireNonNull(file).getName();
    }

    /**
     * Returns the bytes that the file should return. By default this returns the files content in bytes.
     *
     * @param file file
     * @param bytes file content in bytes
     * @return byte array
     *
     * @since 5.0.0
     */
    default byte[] getBytes(final File file, final byte[] bytes){
        return bytes;
    }

}

/*
 * Copyright (C) 2024 Katsute <https://github.com/Katsute>
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

/**
 * A session keeps track of a single client across multiple exchanges.
 *
 * @see HttpSessionHandler
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public abstract class HttpSession {

    HttpSession(){ }

    /**
     * Returns the session ID.
     *
     * @return session ID
     *
     * @since 5.0.0
     */
    public abstract String getSessionID();

    /**
     * Returns when the session was created as milliseconds since epoch.
     *
     * @return session creation time
     *
     * @since 5.0.0
     */
    public abstract long getCreationTime();

    /**
     * Returns when the session was last accessed as milliseconds since epoch.
     *
     * @return session last accessed time
     *
     * @see #update()
     * @since 5.0.0
     */
    public abstract long getLastAccessed();

    /**
     * Refreshes when the session was last accessed to now.
     *
     * @see #getLastAccessed()
     * @since 5.0.0
     */
    public abstract void update();

}
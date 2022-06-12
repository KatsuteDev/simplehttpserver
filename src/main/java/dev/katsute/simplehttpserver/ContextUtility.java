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

import java.util.regex.Pattern;

abstract class ContextUtility {

    // replace consecutive slashes and back slashes with a single forward slash
    @SuppressWarnings("RegExpRedundantEscape")
    private static final Pattern forwardSlash = Pattern.compile("\\/{2,}|\\\\+");
    // trim start and end slashes as well as whitespace
    @SuppressWarnings("RegExpRedundantEscape")
    private static final Pattern trimSlash = Pattern.compile("^\\s*\\/*|\\/*\\s*$");

    static String getContext(final String context, final boolean leading, final boolean trailing){
        final String linSlash = forwardSlash.matcher(context).replaceAll("/");
        final String strippedSlash = trimSlash.matcher(linSlash).replaceAll("");
        return strippedSlash.length() == 0
            ? leading || trailing ? "/" : ""
            : (leading ? "/" : "") + strippedSlash + (trailing ? "/" : "");
    }

}

/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.config.internal;

import java.awt.Color;
import java.awt.Font;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Arrays;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
final class _Config_Parsers {

    // -- BOOLEAN

    static @Nullable Boolean parseBoolean(@Nullable String input) {
        if(input==null) {
            return null;
        }
        val literal = input.toLowerCase();
        switch (literal) {
        case "false":
        case "0":
        case "no":
            return Boolean.FALSE;

        case "true":
        case "1":
        case "yes":
            return Boolean.TRUE;

        default:
            break;
        }
        return null;
    }

    // -- INTEGER

    static @Nullable Integer parseInteger(@Nullable String input) {
        if(input==null) {
            return null;
        }
        try {
            return Integer.parseInt(input);
        } catch (Exception e) {
            log.warn("failed to parse Integer from input '{}', falling back to <null>", input);
        }
        return null;
    }

    // -- COLOR

    static @Nullable Color parseColor(@Nullable String input) {
        if(input==null) {
            return null;
        }
        try {
            return Color.decode(input);
        } catch (Exception e) {
            log.warn("failed to parse Color from input '{}', falling back to <null>", input);
        }
        return null;
    }

    // -- FONT

    static @Nullable Font parseFont(@Nullable String input) {
        if(input==null) {
            return null;
        }
        try {
            return Font.decode(input);
        } catch (Exception e) {
            log.warn("failed to parse Font from input '{}', falling back to <null>", input);
        }
        return null;
    }

    // -- LIST OF STRINGS

    static @Nullable String[] parseList(@Nullable String input) {
        if(input==null) {
            return null;
        }
        try {
            return _Strings.splitThenStream(input, ",")
                    .map(String::trim)
                    .collect(_Arrays.toArray(String.class));
        } catch (Exception e) {
            log.warn("failed to parse List (of Strings) from input '{}', falling back to <null>", input);
        }
        return null;
    }

}

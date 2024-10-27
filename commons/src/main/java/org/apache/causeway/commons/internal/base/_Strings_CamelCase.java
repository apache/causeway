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
package org.apache.causeway.commons.internal.base;

import java.util.StringTokenizer;
import java.util.function.UnaryOperator;

import org.springframework.lang.Nullable;

import lombok.experimental.UtilityClass;

/**
 * package private utility for {@link _Strings}
 */
@UtilityClass
class _Strings_CamelCase {

    /**
     * Camel case is the practice of writing phrases without spaces or punctuation and with capitalized words.
     * The format indicates the first word starting with EITHER case,
     * then the following words having an initial uppercase letter.
     */
    @Nullable
    String camelCase(final @Nullable String input, final UnaryOperator<String> firstTokenMapper) {

        if(input==null) return null;
        if(input.length()==0) return input;

        var sb = new StringBuffer(input.length());
        var tokenizer = new StringTokenizer(input, " \t\n\r\f._,:;");
        int tokenCount = 0;

        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            ++tokenCount;

            if(tokenCount==1) {
                // convert first token/word using firstTokenMapper
                sb.append(firstTokenMapper.apply(token));
            } else {
                // convert token/word to capitalized
                sb.append(token.substring(0, 1).toUpperCase());
                sb.append(token.substring(1));
            }
        }
        return sb.toString();
    }
}

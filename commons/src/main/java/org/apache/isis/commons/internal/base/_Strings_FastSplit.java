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
package org.apache.isis.commons.internal.base;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.checkerframework.checker.nullness.qual.Nullable;

class _Strings_FastSplit {

    public static void splitThenAccept(
            @Nullable final String input,
            final String separator,
            BiConsumer<String, String> onNonEmptySplit,
            Consumer<String> onNonEmptyLhs,
            Consumer<String> onNonEmptyRhs) {

        if(_Strings.isEmpty(input)) {
            // skip
            return;
        }

        // we have a non-empty string

        final int p = input.indexOf(separator);
        if(p<1){
            if(p==-1) {
                // separator not found
                onNonEmptyLhs.accept(input);
                return;
            }
            if(p==0) {
                // empty lhs in string
                if(input.length()>separator.length()) {
                    onNonEmptyRhs.accept(input);
                }
                return;
            }
        }
        final int q = p + separator.length();
        if(q==input.length()) {
            // empty rhs
            onNonEmptyLhs.accept(input.substring(0, p));
            return;
        }
        onNonEmptySplit.accept(input.substring(0, p), input.substring(q));
    }

}

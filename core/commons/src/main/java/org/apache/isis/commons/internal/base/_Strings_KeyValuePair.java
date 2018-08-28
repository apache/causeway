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

import static org.apache.isis.commons.internal.exceptions._Exceptions.notImplemented;

import org.apache.isis.commons.internal.base._Strings.KeyValuePair;

/**
 *
 * package private mixin for utility class {@link _Strings}
 *
 */
final class _Strings_KeyValuePair implements _Strings.KeyValuePair {

    static KeyValuePair of(String key, String value) {
        return new _Strings_KeyValuePair(key, value);
    }
    
    private final String key;
    private final String value;

    private _Strings_KeyValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String setValue(String value) {
        throw notImplemented();
    }

}

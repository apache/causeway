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
package org.apache.isis.applib.services.urlencoding;

import java.nio.charset.StandardCharsets;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.memento._Mementos.EncoderDecoder;

public interface UrlEncodingService extends EncoderDecoder {

    // -- INHERITED from EncoderDecoder (make explicitly programmatic, in order to avoid meta-model validation)
    @Override
    @Programmatic public String encode(final byte[] bytes);
    @Override
    @Programmatic public byte[] decode(String str);

    // -- EXTENSIONS

    @Programmatic
    public default String encodeString(final String str) {
        return encode(_Strings.toBytes(str, StandardCharsets.UTF_8));
    }

    @Programmatic
    public default String decodeToString(final String str) {
        return _Strings.ofBytes(decode(str), StandardCharsets.UTF_8);
    }

}

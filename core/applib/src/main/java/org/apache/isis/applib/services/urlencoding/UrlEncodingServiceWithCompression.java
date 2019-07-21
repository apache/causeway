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

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Strings;

@DomainService(nature = NatureOfService.DOMAIN)
public class UrlEncodingServiceWithCompression implements UrlEncodingService {

    @Override
    public String encode(final byte[] bytes) {
        return _Strings.ofBytes(_Bytes.asCompressedUrlBase64.apply(bytes), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] decode(final String str) {
        return _Bytes.ofCompressedUrlBase64.apply(_Strings.toBytes(str, StandardCharsets.UTF_8));
    }

    // -- OVERRIDING DEFAULTS FOR STRING UNARY OPERATORS

    //    @Override
    //    public String encodeString(final String str) {
    //    	return _Strings.convert(str, _Bytes.asCompressedUrlBase64, StandardCharsets.UTF_8);
    //    }
    //
    //    @Override
    //    public String decodeToString(final String str) {
    //    	return _Strings.convert(str, _Bytes.ofCompressedUrlBase64, StandardCharsets.UTF_8);
    //    }

    // --


}

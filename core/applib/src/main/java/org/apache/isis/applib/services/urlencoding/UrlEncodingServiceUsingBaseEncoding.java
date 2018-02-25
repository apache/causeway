/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.urlencoding;

import java.nio.charset.StandardCharsets;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.internal.base._Bytes;
import org.apache.isis.applib.internal.base._Strings;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
)
public class UrlEncodingServiceUsingBaseEncoding implements UrlEncodingService {

	@Override @Programmatic
    public String encode(final String str) {
    	return _Strings.convert(str, _Bytes.asUrlBase64, StandardCharsets.UTF_8);
    }

    @Override @Programmatic
    public String decode(final String str) {
    	return _Strings.convert(str, _Bytes.ofUrlBase64, StandardCharsets.UTF_8);
    }

}

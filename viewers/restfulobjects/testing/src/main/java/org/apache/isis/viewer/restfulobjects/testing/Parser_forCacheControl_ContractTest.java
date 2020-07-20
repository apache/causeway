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
package org.apache.isis.viewer.restfulobjects.testing;

import javax.ws.rs.core.CacheControl;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.viewer.restfulobjects.applib.util.Parser;

public abstract class Parser_forCacheControl_ContractTest {


    @Test
    public void forCacheControl() {
        final Parser<CacheControl> parser = Parser.forCacheControl();

        final CacheControl cc1 = createCacheControl();
        cc1.setMaxAge(2000);
        final CacheControl cc2 = createCacheControl();
        cc2.setNoCache(true);
        for (final CacheControl v : new CacheControl[] { cc1, cc2 }) {
            final String asString = parser.asString(v);
            final CacheControl valueOf = parser.valueOf(asString);
            assertThat(v.getMaxAge(), is(equalTo(valueOf.getMaxAge())));
            assertThat(v.isNoCache(), is(equalTo(valueOf.isNoCache())));
        }
    }

    private static CacheControl createCacheControl() {
        final CacheControl cacheControl = new CacheControl();
        cacheControl.getCacheExtension(); // workaround for bug in
        // CacheControl's equals() method
        cacheControl.getNoCacheFields(); // workaround for bug in CacheControl's
        // equals() method
        return cacheControl;
    }


}

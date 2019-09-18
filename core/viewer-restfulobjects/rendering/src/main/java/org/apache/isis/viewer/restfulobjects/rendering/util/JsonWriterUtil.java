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
package org.apache.isis.viewer.restfulobjects.rendering.util;

import java.io.IOException;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.restfulobjects.applib.util.JsonMapper;

public final class JsonWriterUtil {

    private JsonWriterUtil(){}

    public static String jsonFor(final Object object) {
        final JsonMapper.PrettyPrinting prettyPrinting = inferPrettyPrinting();
        try {
            return JsonMapper.instance(prettyPrinting).write(object);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    static IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

    private static JsonMapper.PrettyPrinting inferPrettyPrinting() {
        return _Context.isPrototyping() 
                ? JsonMapper.PrettyPrinting.ENABLE 
                        : JsonMapper.PrettyPrinting.DISABLE;
    }
}

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
package org.apache.causeway.viewer.restfulobjects.rendering.util;

import org.springframework.lang.Nullable;

import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.viewer.restfulobjects.applib.util.JsonMapper;

import lombok.SneakyThrows;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class JsonWriterUtil {

    @SneakyThrows
    public String jsonFor(final Object object, final JsonMapper.PrettyPrinting prettyPrinting) {
        return JsonMapper.instance(prettyPrinting).write(object);
    }

    public String jsonFor(final Object object, @Nullable final CausewaySystemEnvironment systemEnvironment) {
        var prettyPrinting = (systemEnvironment!=null && systemEnvironment.isPrototyping())
                ? JsonMapper.PrettyPrinting.ENABLE
                : JsonMapper.PrettyPrinting.DISABLE;
        return jsonFor(object, prettyPrinting);
    }

}

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
package org.apache.causeway.core.metamodel.objectmanager.memento;

import java.io.Serializable;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.JsonUtils;

/**
 * Provides a summary of the domain object for rendering,
 * having (translated) title and icon.
 *
 * @implSpec works hand in hand with select2 (third-party) java-script
 *  and org.apache.causeway.viewer.wicket.ui.components.widgets.select2.Select2 template configuration.
 */
public record ObjectDisplayDto(
    Class<?> correspondingClass,
    String bookmark,
    String title,
    @Nullable String iconHtml) implements Serializable {

    public static ObjectDisplayDto fromJson(String json) {
        return JsonUtils.tryRead(ObjectDisplayDto.class, json)
            .valueAsNonNullElseFail();
    }

    public static ObjectDisplayDto fromJsonBase64(String base64EncodedJson) {
        return fromJson(_Strings.base64UrlDecode(base64EncodedJson));
    }

    public String toJson() {
        return JsonUtils.toStringUtf8(this);
    }

    public String toJsonBase64() {
        return _Strings.base64UrlEncode(toJson());
    }

}

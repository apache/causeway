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
package org.apache.causeway.applib.layout.resource;

import java.util.Objects;

import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Layout data record with name, format and contents (XML, JSON, etc.) based on format.
 *
 * @since 4.0 {@index}
 */
@Getter @Accessors(fluent = true)
public final class LayoutResource {
	
    private final String resourceName;
    private final CommonMimeType format;
    private final String content;

    public LayoutResource(
    		final String resourceName,
    	    final CommonMimeType format,
    	    final String content) {
    	this.resourceName = Objects.requireNonNull(resourceName);
        this.format = Objects.requireNonNull(format);
        this.content = Objects.requireNonNull(content);
    }

}

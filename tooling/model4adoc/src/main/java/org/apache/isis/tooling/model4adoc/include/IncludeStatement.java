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
package org.apache.isis.tooling.model4adoc.include;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.Builder;
import lombok.Value;

@Value @Builder
public final class IncludeStatement {
    int zeroBasedLineIndex;
    String matchingLine;
    String referencePath;
    String referenceShortName;
    
    String version;
    String component;
    String module;
    String type; // usually 'page'
    String ext;
    String options;
    
    public boolean isLocal() {
        return _Strings.isNullOrEmpty(component);
    }
    
    public String toAdocAsString() {
        //TODO if local might look slightly different 
        if(isLocal()) {
            throw _Exceptions.notImplemented();
        }
        
        return String.format("include::%s%s:%s:%s$%s%s", 
                _Strings.nullToEmpty(version).isEmpty() ? "" : version + "@",
                _Strings.nullToEmpty(component),
                _Strings.nullToEmpty(module),
                type,
                referencePath,
                _Strings.nullToEmpty(options));
    }
    
}

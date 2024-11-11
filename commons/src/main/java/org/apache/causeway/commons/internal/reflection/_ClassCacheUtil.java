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
package org.apache.causeway.commons.internal.reflection;

import java.util.Locale;

import jakarta.inject.Named;

import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Strings;

import lombok.experimental.UtilityClass;

@UtilityClass
class _ClassCacheUtil {
    
    @Nullable String inferName(Class<?> type, MergedAnnotations mergedAnnotations) {
        
        var namedAnnot = mergedAnnotations.get(Named.class);
        
        final String named = namedAnnot.isPresent() 
                ? mergedAnnotations.get(Named.class).getString("value")
                : null;
        
        if(!_Strings.isEmpty(named)) return named;
                
        for(var annot : mergedAnnotations) {
            switch (annot.getType().getName()) {
                case "jakarta.persistence.Table":
                case "javax.jdo.annotations.PersistenceCapable": {
                    var schema = annot.getString("schema");
                    if(_Strings.isEmpty(schema)) continue;
                    
                    var table = annot.getString("table");
                    return "%s.%s".formatted(schema.toLowerCase(Locale.ROOT), _Strings.nonEmpty(table)
                            .orElseGet(type::getSimpleName));
                }
            }
        }
        return null;
    }

    boolean isJdoPersistenceCapable(MergedAnnotations mergedAnnotations) {
        for(var annot : mergedAnnotations) {
            switch (annot.getType().getName()) {
                case "javax.jdo.annotations.PersistenceCapable":
                    return true;
            }
        }
        return false;
    }
    
    boolean isJdoEmbeddedOnly(MergedAnnotations mergedAnnotations) {
        for(var annot : mergedAnnotations) {
            switch (annot.getType().getName()) {
                case "javax.jdo.annotations.PersistenceCapable":
                    if("true".equals(annot.getString("embeddedOnly"))) return true;
                    break;
                case "javax.jdo.annotations.EmbeddedOnly":
                    return true;
            }
        }
        return false;
    }
    
    @Nullable
    private String nameFromPersistenceTable(Class<?> type, @Nullable String schema, @Nullable String table) {
        if(_Strings.isEmpty(schema)) return null;
        return "%s.%s".formatted(schema, _Strings.nonEmpty(table)
                .orElseGet(type::getSimpleName));
    }
    
}

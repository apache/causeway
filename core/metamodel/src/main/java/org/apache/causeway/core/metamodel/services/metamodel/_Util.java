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
package org.apache.causeway.core.metamodel.services.metamodel;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import org.apache.causeway.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.causeway.applib.spec.Specification;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

import lombok.experimental.UtilityClass;

@UtilityClass
class _Util {

    void visitNonNullAttributes(final Facet facet, final BiConsumer<String, String> visitor) {
        facet.visitAttributes((key, attributeObj)->{
            if(attributeObj == null) {
                return;
            }
            String str = asStr(attributeObj);
            visitor.accept(key, str);
        });
    }

    String asStr(final Object attributeObj) {
        String str;
        if(attributeObj instanceof Method) {
            str = asStr((Method) attributeObj);
        } else if(attributeObj instanceof String) {
            str = asStr((String) attributeObj);
        } else if(attributeObj instanceof Enum) {
            str = asStr((Enum<?>) attributeObj);
        } else if(attributeObj instanceof Class) {
            str = asStr((Class<?>) attributeObj);
        } else if(attributeObj instanceof Specification) {
            str = asStr((Specification) attributeObj);
        } else if(attributeObj instanceof Facet) {
            str = asStr((Facet) attributeObj);
        } else if(attributeObj instanceof MetaModelExportSupport) {
            str = asStr((MetaModelExportSupport) attributeObj);
        } else if(attributeObj instanceof Pattern) {
            str = asStr((Pattern) attributeObj);
        } else if(attributeObj instanceof CommandDtoProcessor) {
            str = asStr((CommandDtoProcessor) attributeObj);
        } else if(attributeObj instanceof ObjectSpecification) {
            str = asStr((ObjectSpecification) attributeObj);
        } else if(attributeObj instanceof ObjectMember) {
            str = asStr((ObjectMember) attributeObj);
        } else if(attributeObj instanceof List) {
            str = asStr((List<?>) attributeObj);
        } else if(attributeObj instanceof Object[]) {
            str = asStr((Object[]) attributeObj);
        } else  {
            str = "" + attributeObj;
        }
        return str;
    }

    // -- HELPER

    private String asStr(final String attributeObj) {
        return _Strings.emptyToNull(attributeObj);
    }

    private String asStr(final Specification attributeObj) {
        return attributeObj.getClass().getName();
    }

    private String asStr(final ObjectSpecification attributeObj) {
        return attributeObj.getFullIdentifier();
    }

    private String asStr(final MetaModelExportSupport attributeObj) {
        return attributeObj.toMetamodelString();
    }

    private String asStr(final CommandDtoProcessor attributeObj) {
        return attributeObj.getClass().getName();
    }

    private String asStr(final Pattern attributeObj) {
        return attributeObj.pattern();
    }

    private String asStr(final Facet attributeObj) {
        return attributeObj.getClass().getName();
    }

    private String asStr(final ObjectMember attributeObj) {
        return attributeObj.getId();
    }

    private String asStr(final Class<?> attributeObj) {
        return attributeObj.getCanonicalName();
    }

    private String asStr(final Enum<?> attributeObj) {
        return attributeObj.name();
    }

    private String asStr(final Method attributeObj) {
        return attributeObj.toGenericString();
    }

    private String asStr(final Object[] list) {
        if(list.length == 0) {
            return null; // skip
        }
        List<String> strings = _Lists.newArrayList();
        for (final Object o : list) {
            String s = asStr(o);
            strings.add(s);
        }
        return String.join(";", strings);
    }

    private String asStr(final List<?> list) {
        if(list.isEmpty()) {
            return null; // skip
        }
        List<String> strings = _Lists.newArrayList();
        for (final Object o : list) {
            String s = asStr(o);
            strings.add(s);
        }
        return String.join(";", strings);
    }

}

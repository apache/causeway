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
package demoapp.dom._infra.resources;

import jakarta.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Refs.StringReference;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;

import lombok.RequiredArgsConstructor;

@Service
@Named("demo.AsciiDocReaderService")
@RequiredArgsConstructor
public class AsciiDocReaderService {

    final ResourceReaderService resourceReaderService;
    final CausewayConfiguration configuration;

    public AsciiDoc readFor(final Object anObject) {
        return readFor(anObject.getClass());
    }

    public AsciiDoc readFor(final Object anObject, final String member) {
        return readFor(anObject.getClass(), member);
    }

    public AsciiDoc readFor(final Class<?> aClass) {
        var adocResourceName = String.format("%s.adoc", aClass.getSimpleName());
        var adocResource = readResource(aClass, adocResourceName);
        return toAsciiDoc(adocResource, aClass);
    }

    public AsciiDoc readFor(final Class<?> aClass, final String member) {
        var adocResourceName = String.format("%s-%s.%s", aClass.getSimpleName(), member, "adoc");
        var adocResource = readResource(aClass, adocResourceName);
        return toAsciiDoc(adocResource, aClass);
    }

    // -- HELPER

    private StringReference readResource(final Class<?> aClass, final String adocResourceName) {
        return _Refs.stringRef(resourceReaderService.readResource(aClass, adocResourceName));
    }

    private String replaceVersion(final String adoc) {
        return adoc.replace("{causeway-version}",
                configuration.viewer().common().application().version());
    }

    private AsciiDoc toAsciiDoc(final StringReference adocRef, final Class<?> aClass) {
        return AsciiDoc.valueOf(
                adocRef
                .update(this::replaceVersion)
                .getValue());
    }

}

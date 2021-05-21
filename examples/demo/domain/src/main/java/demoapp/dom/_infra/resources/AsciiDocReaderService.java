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

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

import lombok.val;


@Service
@Named("demo.AsciiDocReaderService")
public class AsciiDocReaderService {

    public AsciiDoc readFor(Object anObject) {
        return readFor(anObject.getClass());
    }

    public AsciiDoc readFor(Object anObject, final String member) {
        return readFor(anObject.getClass(), member);
    }

    public AsciiDoc readFor(Class<?> aClass) {
        val adocResourceName = String.format("%s.adoc", aClass.getSimpleName());
        val asciiDoc = readResourceAndReplaceProperties(aClass, adocResourceName);
        return AsciiDoc.valueOfHtml(asciiDocConverterService.adocToHtml(aClass, asciiDoc));
    }

    public AsciiDoc readFor(Class<?> aClass, final String member) {
        val adocResourceName = String.format("%s-%s.%s", aClass.getSimpleName(), member, "adoc");
        val asciiDoc = readResourceAndReplaceProperties(aClass, adocResourceName);
        return AsciiDoc.valueOfHtml(asciiDocConverterService.adocToHtml(aClass, asciiDoc));
    }

    private String readResourceAndReplaceProperties(Class<?> aClass, String adocResourceName) {
        val adoc = resourceReaderService.readResource(aClass, adocResourceName);
        return adoc.replace("{isis-version}",
                configuration.getViewer().getWicket().getApplication().getVersion());
    }

    @Inject
    AsciiDocConverterService asciiDocConverterService;

    @Inject
    ResourceReaderService resourceReaderService;

    @Inject
    IsisConfiguration configuration;

}

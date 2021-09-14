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

import java.util.stream.Collectors;

import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Refs.StringReference;
import org.apache.isis.commons.internal.base._Text;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

import lombok.RequiredArgsConstructor;
import lombok.val;


@Service
@Named("demo.AsciiDocReaderService")
@RequiredArgsConstructor
public class AsciiDocReaderService {

    final ResourceReaderService resourceReaderService;
    final IsisConfiguration configuration;

    public AsciiDoc readFor(final Object anObject) {
        return readFor(anObject.getClass());
    }

    public AsciiDoc readFor(final Object anObject, final String member) {
        return readFor(anObject.getClass(), member);
    }

    public AsciiDoc readFor(final Class<?> aClass) {
        val adocResourceName = String.format("%s.adoc", aClass.getSimpleName());
        val adocResource = readResource(aClass, adocResourceName);
        return toAsciiDoc(adocResource, aClass);
    }

    public AsciiDoc readFor(final Class<?> aClass, final String member) {
        val adocResourceName = String.format("%s-%s.%s", aClass.getSimpleName(), member, "adoc");
        val adocResource = readResource(aClass, adocResourceName);
        return toAsciiDoc(adocResource, aClass);
    }

    // -- HELPER

    private StringReference readResource(final Class<?> aClass, final String adocResourceName) {
        return _Refs.stringRef(resourceReaderService.readResource(aClass, adocResourceName));
    }

    private String replaceVersion(final String adoc) {
        return adoc.replace("{isis-version}",
                configuration.getViewer().getWicket().getApplication().getVersion());
    }

    private AsciiDoc toAsciiDoc(final StringReference adocRef, final Class<?> aClass) {
        return AsciiDoc.valueOf(
                adocRef
                .update(this::replaceVersion)
                //.update(this::replaceJavaSourceReferences)
                //.update(adoc->prependSource(adoc, aClass))
                .getValue());
    }

    // -- EXPERIMENTAL ... works within IDE, but not when packaged

    private String replaceJavaSourceReferences(final String adoc) {
        return _Text.getLines(adoc)
        .stream()
        .map(line->line.startsWith("include::")
                        && line.contains(".java")
                ? replaceJavaSourceReference(line)
                : line
        )
        .collect(Collectors.joining("\n"));
    }

    //  "include::DemoHomePage.java" -> "include::{sourcedir}/DemoHomePage.java
    private String replaceJavaSourceReference(final String line) {
        val lineRef = _Refs.stringRef(line);
        lineRef.cutAtIndexOfAndDrop("::");
        val classFileSimpleName = lineRef.cutAtIndexOf(".java");
        val remainder = lineRef.getValue();
        return "include::{sourcedir}/" + classFileSimpleName + remainder;
    }

    // setting up the java source root relative to the current directory (application main)
    //XXX dependent on the location of the 'main' class within the file system,
    // if we ever want to improve on that, we should place a marker file on the project root,
    // so we can search up the folder hierarchy on dynamically figure out how many ../
    // actually are required
    private String prependSource(final String adoc, final Class<?> aClass) {
        val packagePath = aClass.getPackage().getName().replace('.', '/');
        return ":sourcedir: ../../domain/src/main/java/" + packagePath + "\n\n" + adoc;
    }


}

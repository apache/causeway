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
package org.apache.isis.extensions.asciidoc;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;

public final class AsciiDocConverter {

	public static String adocToHtml(String adoc) {
        if(asciidoctor==null) {
            asciidoctor = Asciidoctor.Factory.create();
            options = defaultOptions();
        }
        return asciidoctor.convert(adoc, options);
    }

    // -- HELPER

    private static Asciidoctor asciidoctor;
    private static Options options;
    
    private static Options defaultOptions() {
        return OptionsBuilder.options()
                .safe(SafeMode.UNSAFE)
                .toFile(false)
                .attributes(AttributesBuilder.attributes()
                        .sourceHighlighter("coderay")
                        .get())
                .get();
    }
	
}

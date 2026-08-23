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

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.PreprocessorReader;

import org.springframework.stereotype.Service;

@Service
@Named("demo.AsciiDocConverterService")
public class AsciiDocConverterService {

    private final static ThreadLocal<Class<?>> context = new ThreadLocal<>();

    private final ResourceReaderService resourceReaderService;

    private final Asciidoctor asciidoctor;
    private final Options options;

    @Inject
    public AsciiDocConverterService(final ResourceReaderService resourceReaderService) {
        this.resourceReaderService = resourceReaderService;
        this.asciidoctor = createAsciidoctor();
        this.options = Options.builder()
                .safe(SafeMode.UNSAFE)
                .toFile(false)
                .attributes(Attributes.builder()
                        .sourceHighlighter("prism")
                        .icons("font")
                        .build())
                .build();

    }

    private Asciidoctor createAsciidoctor() {

        class LocalIncludeProcessor extends IncludeProcessor {

            @Override
            public boolean handles(final String target) {
                return true;
            }

            @Override
            public void process(final Document document, final PreprocessorReader reader, final String target, final Map<String, Object> attributes) {
                var contextClass = context.get();
                var content = resourceReaderService.readResource(contextClass, target, attributes);
                reader.pushInclude(content, target, target, 1, attributes);
            }
        }

        var asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().includeProcessor(new LocalIncludeProcessor());
        return asciidoctor;
    }

    String adocToHtml(final Class<?> contextClass, final String adoc) {
        try {
            context.set(contextClass);
            return asciidoctor.convert(adoc, options);
        } finally {
            context.remove();
        }
    }

}

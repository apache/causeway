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
package demoapp.dom._infra.samples.markdown;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.causeway.valuetypes.markdown.applib.value.Markdown;

import demoapp.dom._infra.resources.MarkdownReaderService;
import demoapp.dom.types.Samples;

@Service
public class CausewayMarkdownSamples implements Samples<Markdown> {

    @Override
    public Stream<Markdown> stream() {
        return IntStream.rangeClosed(1, 6)
                .mapToObj(x -> markdownReaderService.readFor(getClass(), "sample" + x));
    }

    @Inject
    MarkdownReaderService markdownReaderService;

}

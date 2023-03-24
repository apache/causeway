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

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.causeway.valuetypes.markdown.applib.value.Markdown;

import lombok.val;


@Service
@Named("demo.MarkdownReaderService")
public class MarkdownReaderService {

    public Markdown readFor(Object anObject) {
        return readFor(anObject.getClass());
    }

    public Markdown readFor(Object anObject, final String member) {
        return readFor(anObject.getClass(), member);
    }

    public Markdown readFor(Class<?> aClass) {
        val markdownResourceName = String.format("%s.md", aClass.getSimpleName());
        val markdown = resourceReaderService.readResource(aClass, markdownResourceName);
        return Markdown.valueOf(markdown);
    }

    public Markdown readFor(Class<?> aClass, final String member) {
        val markdownResourceName = String.format("%s-%s.md", aClass.getSimpleName(), member);
        val markdown = resourceReaderService.readResource(aClass, markdownResourceName);
        return Markdown.valueOf(markdown);
    }


    @Inject
    ResourceReaderService resourceReaderService;

}

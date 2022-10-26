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
package org.apache.causeway.extensions.pdfjs.applib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.causeway.extensions.pdfjs.applib.config.Scale;

/**
 * An annotation that can be applied on a <i>Property</i> or a <i>Parameter</i>
 * of type {@link org.apache.causeway.applib.value.Blob}.
 * Such property/parameter will be visualized
 * with <a href="https://github.com/mozilla/pdf.js">PDF.js</a> viewer.
 *
 * @since 2.0 {@index}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.FIELD})
public @interface PdfJsViewer {

    /** The page (number) to render,
     *  when this particular domain object('s property) is rendered the first time.*/
    int initialPageNum() default 1;

    /** The scale to render; defaults to 100%.*/
    Scale initialScale() default Scale._1_00;

    /** The (pixel) height of the panel; defaults to 800px.*/
    int initialHeight() default 800;

}

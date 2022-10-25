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
package org.apache.causeway.viewer.wicket.ui.util;

import java.util.stream.Collectors;

import org.apache.wicket.util.template.PackageTextTemplate;

import org.apache.causeway.commons.internal.base._Text;

import lombok.val;

/**
 * Introduced for optimization purposes.
 */
public abstract class LicensedTextTemplate
extends PackageTextTemplate {

    private static final long serialVersionUID = 1L;

    private final int skipLicenseLines;

    public LicensedTextTemplate(final Class<?> clazz, final String fileName, final int skipLicenseLines) {
        super(clazz, fileName);
        this.skipLicenseLines = skipLicenseLines;
    }

    // super class provides no way of accessing its memoized buffer; so we need to re-memoize here
    protected String scriptTemplate = null;

    @Override
    public String getString() {
        if(scriptTemplate==null) {
            val raw = super.getString();

            // strip first n comment lines
            this.scriptTemplate = _Text.streamLines(raw)
            .skip(skipLicenseLines)
            .collect(Collectors.joining(" "));
        }
        return scriptTemplate;
    }

}

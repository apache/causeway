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

package org.apache.isis.example.metamodel.namefile.facets;

import java.io.IOException;
import java.util.List;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.FacetFactory;

public class NameFileFacetFactory implements FacetFactory {

    private final NameFileParser nameFileParser;

    public NameFileFacetFactory() {
        nameFileParser = new NameFileParser();
        try {
            nameFileParser.parse();
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<FeatureType> getFeatureTypes() {
        return FeatureType.EVERYTHING_BUT_PARAMETERS;
    }

    /**
     * Simply attaches a {@link NameFileFacet}.
     */
    public void process(final ProcessClassContext processClassContaxt) {
        FacetUtil.addFacet(create(processClassContaxt.getCls(), processClassContaxt.getFacetHolder()));
    }

    private NameFileFacet create(final Class<?> cls, final FacetHolder holder) {
        final String memberNameInFile = nameFileParser.getName(cls);
        return memberNameInFile != null ? new NameFileFacet(holder, memberNameInFile) : null;
    }

    /**
     * Simply attaches a {@link NameFileFacet}.
     */
    public void process(final ProcessMethodContext processMethodContext) {
        if (!(processMethodContext.getFacetHolder() instanceof IdentifiedHolder)) {
            return;
        }
        final IdentifiedHolder identifiedHolder = processMethodContext.getFacetHolder();
        final Class<?> declaringClass = processMethodContext.getMethod().getDeclaringClass();
        final String memberName = identifiedHolder.getIdentifier().getMemberName();
        FacetUtil.addFacet(create(declaringClass, memberName, processMethodContext.getFacetHolder()));
    }

    private NameFileFacet create(final Class<?> declaringClass, final String memberName, final FacetHolder holder) {
        final String memberNameInFile = nameFileParser.getMemberName(declaringClass, memberName);
        return memberNameInFile != null ? new NameFileFacet(holder, memberNameInFile) : null;
    }

    public void processParams(final ProcessParameterContext processParameterContext) {
        // nothing to do
    }

}

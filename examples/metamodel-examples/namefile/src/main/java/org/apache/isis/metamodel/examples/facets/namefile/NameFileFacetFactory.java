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


package org.apache.isis.metamodel.examples.facets.namefile;

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
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<FeatureType> getFeatureTypes() {
        return FeatureType.EVERYTHING_BUT_PARAMETERS;
    }

    
    /**
     * Simply attaches a {@link NameFileFacet}.
     */
    @Override
    public void process(ProcessClassContext processClassContaxt) {
        FacetUtil.addFacet(create(processClassContaxt.getCls(), processClassContaxt.getFacetHolder()));
    }

    private NameFileFacet create(final Class<?> cls, FacetHolder holder) {
        String memberNameInFile = nameFileParser.getName(cls);
        return memberNameInFile!=null?new NameFileFacet(holder, memberNameInFile): null;
    }
    
    /**
     * Simply attaches a {@link NameFileFacet}.
     */
    @Override
    public void process(ProcessMethodContext processMethodContext) {
    	if (!(processMethodContext.getFacetHolder() instanceof IdentifiedHolder)) {
    		return;
    	}
		IdentifiedHolder identifiedHolder = processMethodContext.getFacetHolder();
        Class<?> declaringClass = processMethodContext.getMethod().getDeclaringClass();
        String memberName = identifiedHolder.getIdentifier().getMemberName();
        FacetUtil.addFacet(create(declaringClass, memberName, processMethodContext.getFacetHolder()));
    }

    private NameFileFacet create(final Class<?> declaringClass, final String memberName, FacetHolder holder) {
        String memberNameInFile = nameFileParser.getMemberName(declaringClass, memberName);
        return memberNameInFile!=null?new NameFileFacet(holder, memberNameInFile): null;
    }


    @Override
    public void processParams(ProcessParameterContext processParameterContext) {
        // nothing to do
    }


}

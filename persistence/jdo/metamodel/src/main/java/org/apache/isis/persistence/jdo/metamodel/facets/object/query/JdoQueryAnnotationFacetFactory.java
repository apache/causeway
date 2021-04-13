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
package org.apache.isis.persistence.jdo.metamodel.facets.object.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;

import lombok.val;

public class JdoQueryAnnotationFacetFactory extends FacetFactoryAbstract 
implements MetaModelRefiner {

    @Inject private JdoFacetContext jdoFacetContext;
    
    public JdoQueryAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();

        // only applies to JDO entities; ignore any view models
        if(!jdoFacetContext.isPersistenceEnhanced(cls)) {
            return;
        }

        final Queries namedQueriesAnnotation = Annotations.getAnnotation(cls, Queries.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        if (namedQueriesAnnotation != null) {
            super.addFacet(new JdoQueriesFacetAnnotation(
                    namedQueriesAnnotation.value(), facetHolder));
            return;
        }

        final Query namedQueryAnnotation = Annotations.getAnnotation(cls, Query.class);
        if (namedQueryAnnotation != null) {
            super.addFacet(new JdoQueryFacetAnnotation(
                    namedQueryAnnotation, facetHolder));
        }
    }
    
    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        val isValidateFromClause = 
                getConfiguration().getCore().getMetaModel().getValidator().getJdoql().isFromClause();
        if (isValidateFromClause) {
            programmingModel.addValidator(new VisitorForFromClause());
        }

        val isValidateVariablesClause = 
                getConfiguration().getCore().getMetaModel().getValidator().getJdoql().isVariablesClause();
        if (isValidateVariablesClause) {
            programmingModel.addValidator(new VisitorForVariablesClause());
        }
    }

    private static final Pattern fromPattern = Pattern.compile("SELECT.*?FROM[\\s]+([^\\s]+).*", Pattern.CASE_INSENSITIVE);

    static String from(final String query) {
        final Matcher matcher = fromPattern.matcher(query);
        return matcher.matches() ? matcher.group(1) :  null;
    }

    private static final Pattern variablesPattern = Pattern.compile(".*?VARIABLES[\\s]+([^\\s]+).*", Pattern.CASE_INSENSITIVE);
    static String variables(final String query) {
        final Matcher matcher = variablesPattern.matcher(query);
        return matcher.matches() ? matcher.group(1) :  null;
    }



}

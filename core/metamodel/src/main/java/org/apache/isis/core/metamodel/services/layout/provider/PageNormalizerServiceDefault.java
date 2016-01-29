/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.services.layout.provider;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.bootstrap3.BS3Page;
import org.apache.isis.applib.layout.fixedcols.FCPage;
import org.apache.isis.applib.layout.members.v1.Page;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class PageNormalizerServiceDefault implements PageNormalizerService, SpecificationLoaderAware {

    public static final String MEMBERS_TNS = "http://isis.apache.org/schema/applib/layout/members/v1";
    public static final String MEMBERS_SCHEMA_LOCATION = "http://isis.apache.org/schema/applib/layout/members/v1/members.xsd";


    private static final Logger LOG = LoggerFactory.getLogger(PageNormalizerServiceDefault.class);
    private SpecificationLoader specificationLookup;

    private PageNormalizerFC pageNormalizerFC;
    private PageNormalizerBS3 pageNormalizerBS3;

    @PostConstruct
    @Programmatic
    public void init() {
        pageNormalizerFC = new PageNormalizerFC(translationService, specificationLookup);
        pageNormalizerBS3 = new PageNormalizerBS3(translationService, specificationLookup);
    }

    @Programmatic
    @Override
    public List<Class<? extends Page>> pageImplementations() {
        List<Class<? extends Page>> ar = Lists.newArrayList();
        ar.add(FCPage.class);
        ar.add(BS3Page.class);
        return ar;
    }

    @Override
    public void normalize(final Page page, final Class<?> domainClass) {

        if(page instanceof FCPage) {
            final FCPage fcPage = (FCPage) page;
            pageNormalizerFC.normalize(fcPage, domainClass);
        } else
        if(page instanceof BS3Page) {
            final BS3Page bs3Page = (BS3Page) page;
            pageNormalizerBS3.normalize(bs3Page, domainClass);
        }

        page.setNormalized(true);
    }

    @Override
    public String schemaLocationsFor(final Page page) {
        final List<String> parts = Lists.newArrayList();
        parts.add(MEMBERS_TNS);
        parts.add(MEMBERS_SCHEMA_LOCATION);
        if(page instanceof FCPage) {
            parts.add(PageNormalizerFC.TNS);
            parts.add(PageNormalizerFC.SCHEMA_LOCATION);
        }
        if(page instanceof BS3Page) {
            parts.add(PageNormalizerBS3.TNS);
            parts.add(PageNormalizerBS3.SCHEMA_LOCATION);
        }
        return Joiner.on(" ").join(parts);
    }

    @Override
    public void setSpecificationLoader(final SpecificationLoader specificationLookup) {
        this.specificationLookup = specificationLookup;
    }

    @Inject
    TranslationService translationService;

}

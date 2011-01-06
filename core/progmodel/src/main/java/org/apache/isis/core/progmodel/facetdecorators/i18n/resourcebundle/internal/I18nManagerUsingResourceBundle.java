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


package org.apache.isis.core.progmodel.facetdecorators.i18n.resourcebundle.internal;

import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.progmodel.facetdecorators.i18n.I18nManager;

/**
 * REVIEW: why isn't there a type for collections also?
 */
public class I18nManagerUsingResourceBundle implements I18nManager {

    private static final Logger LOG = Logger.getLogger(I18nManagerUsingResourceBundle.class);

    private static final String BASE_FILE_NAME = "i18n";

    private static final String PROPERTY = "property";
    private static final String ACTION = "action";
    private static final String PARAMETER = "parameter";
    
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String HELP = "help";

    private ResourceBundle bundle;

    @SuppressWarnings("unused")
    private IsisConfiguration configuration;

    ////////////////////////////////////////////////////////////////
    // Contructor, init, shutdown
    ////////////////////////////////////////////////////////////////
    
    public I18nManagerUsingResourceBundle(IsisConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void init() {
        try {
            bundle = ResourceBundle.getBundle(BASE_FILE_NAME);
        } catch (final MissingResourceException e) {
            LOG.warn("Missing resource bundle: " + e.getMessage());
        }

    }

    @Override
    public void shutdown() {}

    
    ////////////////////////////////////////////////////////////////
    // Members
    ////////////////////////////////////////////////////////////////

    @Override
    public String getName(final Identifier identifier) {
        return internalizedTextForClassMember(identifier, NAME);
    }

    @Override
    public String getDescription(final Identifier identifier) {
        return internalizedTextForClassMember(identifier, DESCRIPTION);
    }

    @Override
    public String getHelp(final Identifier identifier) {
        return internalizedTextForClassMember(identifier, HELP);
    }

    private String internalizedTextForClassMember(final Identifier identifier, final String textType) {
        if (bundle == null) {
            return null;
        } 
        final String key = buildMemberTypeKey(identifier, textType);
        return lookupTextFromBundle(key);
    }

    private static String buildMemberTypeKey(final Identifier identifier, final String textType) {
        StringBuilder sb = new StringBuilder();
        final String form = identifier.isPropertyOrCollection() ? PROPERTY
            : ACTION;
        sb = sb.append(identifier.getClassName()).append(".").append(form);
        final String memberName = identifier.getMemberName();
        if (!StringUtils.isNullOrEmpty(memberName)) {
            sb.append(".").append(memberName);
        }
        sb.append(".").append(textType);
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////
    // Parameters
    ////////////////////////////////////////////////////////////////

    @Override
    public List<String> getParameterNames(final Identifier identifier) {
        return internalizedTextForParameter(identifier, NAME);
    }

    protected List<String> internalizedTextForParameter(final Identifier identifier, final String textType) {
        if (bundle == null) {
            return null;
        } 
        final List<String> internalizedText = Lists.newArrayList();
        final List<String> memberParameterNames = identifier.getMemberParameterNames();
        int paramNum=0;
        for (@SuppressWarnings("unused") String dummy : memberParameterNames) {
            final String key = buildParameterTypeKey(identifier, textType, paramNum);
            internalizedText.add(lookupTextFromBundle(key));
            paramNum++;
        }
        return internalizedText;
    }

    private static String buildParameterTypeKey(final Identifier identifier, final String textType, int paramNum) {
        return identifier.getClassName() + "." + ACTION + "." + identifier.getMemberName() + "." + PARAMETER
                + (paramNum + 1) + "." + textType;
    }

    
    private String lookupTextFromBundle(final String key) {
        try {
            return bundle.getString(key);
        } catch (final MissingResourceException e) {
            return null;
        }
    }
    
}


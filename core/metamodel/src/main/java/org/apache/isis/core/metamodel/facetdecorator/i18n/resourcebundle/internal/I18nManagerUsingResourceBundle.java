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

package org.apache.isis.core.metamodel.facetdecorator.i18n.resourcebundle.internal;

import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetdecorator.i18n.I18nManager;

/**
 * REVIEW: why isn't there a type for collections also?
 */
public class I18nManagerUsingResourceBundle implements I18nManager {

    private static final Logger LOG = LoggerFactory.getLogger(I18nManagerUsingResourceBundle.class);

    private static final String BASE_FILE_NAME = "i18n";

    private static final String MEMBER_TYPE_PROPERTY = "property";
    private static final String MEMBER_TYPE_COLLECTION = "collection";
    private static final String MEMBER_TYPE_ACTION = "action";
    private static final String MEMBER_TYPE_PARAMETER = "parameter";

    private static final String TEXT_TYPE_NAME = "name";
    private static final String TEXT_TYPE_DESCRIPTION = "description";
    private static final String TEXT_TYPE_HELP = "help";

    private ResourceBundle bundle;

    @SuppressWarnings("unused")
    private final IsisConfiguration configuration;

    // //////////////////////////////////////////////////////////////
    // Contructor, init, shutdown
    // //////////////////////////////////////////////////////////////

    public I18nManagerUsingResourceBundle(final IsisConfiguration configuration) {
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
    public void shutdown() {
    }

    // //////////////////////////////////////////////////////////////
    // Members
    // //////////////////////////////////////////////////////////////

    @Override
    public String getName(final Identifier identifier) {
        return internalizedTextForClassMember(identifier, TEXT_TYPE_NAME);
    }

    @Override
    public String getDescription(final Identifier identifier) {
        return internalizedTextForClassMember(identifier, TEXT_TYPE_DESCRIPTION);
    }

    @Override
    public String getHelp(final Identifier identifier) {
        return internalizedTextForClassMember(identifier, TEXT_TYPE_HELP);
    }

    private String internalizedTextForClassMember(final Identifier identifier, final String textType) {
        if (bundle == null) {
            return null;
        }
        final List<String> key = buildMemberTypeKey(identifier, textType);
        return lookupTextFromBundle(key);
    }

    private static List<String> buildMemberTypeKey(final Identifier identifier, final String textType) {
        final List<String> keys = Lists.newArrayList();

        if (identifier.isPropertyOrCollection()) {
            keys.add(buildMemberTypeKey(identifier, textType, MEMBER_TYPE_PROPERTY));
            keys.add(buildMemberTypeKey(identifier, textType, MEMBER_TYPE_COLLECTION));
        } else {
            keys.add(buildMemberTypeKey(identifier, textType, MEMBER_TYPE_ACTION));
        }
        return keys;
    }

    private static String buildMemberTypeKey(final Identifier identifier, final String textType, final String memberType) {
        final StringBuilder sb = new StringBuilder();
        sb.append(identifier.getClassName()).append(".");
        sb.append(memberType);
        final String memberName = identifier.getMemberName();
        if (!Strings.isNullOrEmpty(memberName)) {
            sb.append(".").append(memberName);
        }
        sb.append(".").append(textType);
        return sb.toString();
    }

    // //////////////////////////////////////////////////////////////
    // Parameters
    // //////////////////////////////////////////////////////////////

    @Override
    public List<String> getParameterNames(final Identifier identifier) {
        return internalizedTextForParameter(identifier, TEXT_TYPE_NAME);
    }

    private List<String> internalizedTextForParameter(final Identifier identifier, final String textType) {
        if (bundle == null) {
            return null;
        }
        final List<String> internalizedText = Lists.newArrayList();
        final List<String> memberParameterNames = identifier.getMemberParameterNames();
        int paramNum = 0;
        for (@SuppressWarnings("unused")
        final String dummy : memberParameterNames) {
            final String key = buildParameterTypeKey(identifier, textType, paramNum);
            internalizedText.add(lookupTextFromBundle(key));
            paramNum++;
        }
        return internalizedText;
    }

    private static String buildParameterTypeKey(final Identifier identifier, final String textType, final int paramNum) {
        return identifier.getClassName() + "." + MEMBER_TYPE_ACTION + "." + identifier.getMemberName() + "." + MEMBER_TYPE_PARAMETER + (paramNum + 1) + "." + textType;
    }

    // //////////////////////////////////////////////////////////////
    // Helpers
    // //////////////////////////////////////////////////////////////

    private String lookupTextFromBundle(final List<String> keys) {
        for (final String key : keys) {
            final String text = lookupTextFromBundle(key);
            if (text != null) {
                return text;
            }
        }
        return null;
    }

    private String lookupTextFromBundle(final String key) {
        try {
            return bundle.getString(key);
        } catch (final MissingResourceException e) {
            return null;
        }
    }

}

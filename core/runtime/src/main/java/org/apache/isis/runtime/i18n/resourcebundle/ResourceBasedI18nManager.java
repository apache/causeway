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


package org.apache.isis.runtime.i18n.resourcebundle;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.apache.isis.applib.Identifier;
import org.apache.isis.metamodel.config.IsisConfiguration;


public class ResourceBasedI18nManager implements I18nManager {

    private static final Logger LOG = Logger.getLogger(ResourceBasedI18nManager.class);

    private static final String BASE_FILE_NAME = "i18n";

    private static final String PARAMETER = "parameter";
    private static final String DESCRIPTION = "description";
    private static final String PROPERTY = "property";
    private static final String NAME = "name";
    private static final String HELP = "help";
    private static final String ACTION = "action";

    private ResourceBundle bundle;

    @SuppressWarnings("unused")
    private IsisConfiguration configuration;

    public ResourceBasedI18nManager(IsisConfiguration configuration) {
        this.configuration = configuration;
    }

    public void init() {
        try {
            bundle = ResourceBundle.getBundle(BASE_FILE_NAME);
        } catch (final MissingResourceException e) {
            LOG.warn("Missing resource bundle: " + e.getMessage());
        }

    }

    public void shutdown() {}

    public String getName(final Identifier identifier) {
        return text(identifier, NAME);
    }

    public String getDescription(final Identifier identifier) {
        return text(identifier, DESCRIPTION);
    }

    public String getHelp(final Identifier identifier) {
        return text(identifier, HELP);
    }

    // TODO allow description and help to be found for parameters
    public String[] getParameterNames(final Identifier identifier) {
        if (bundle == null) {
            return null;
        } else {
            final String[] array = new String[identifier.getMemberParameterNames().length];
            for (int i = 0; i < array.length; i++) {
                final String key = identifier.getClassName() + "." + ACTION + "." + identifier.getMemberName() + "." + PARAMETER
                        + (i + 1) + "." + NAME;
                try {
                    array[i] = bundle.getString(key);
                } catch (final MissingResourceException e) {
                    array[i] = null;
                }
            }
            return array;
        }
    }

    private String text(final Identifier identifier, final String type) {
        if (bundle == null) {
            return null;
        } else {
            final String form = identifier.isPropertyOrCollection() ? PROPERTY : ACTION;
            final String key = identifier.getClassName() + "." + form + "." + identifier.getMemberName() + "." + type;
            try {
                return bundle.getString(key);
            } catch (final MissingResourceException e) {
                return null;
            }
        }
    }

}


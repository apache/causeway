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
package org.apache.isis.viewer.wicket.viewer.integration;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.Localizer;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.runtime.session.IsisSession;
import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.viewer.wicketapp.IsisWicketApplication;

/**
 * Implementation integrates Isis' own i18n support ({@link TranslationService}) with Wicket's equivalent i18n
 * mechanism (the {@link Localizer} singleton).
 */
public class LocalizerForIsis extends Localizer {

    @Inject private IsisSessionFactory isisSessionFactory;
    @Inject private TranslationService translationService;
    
    /**
     * Uses Isis' {@link TranslationService} to translate the key, but falls back to Wicket's own implementation
     * if no translation is available.
     */
    @Override
    public String getString(
            final String key,
            final Component component,
            final IModel<?> model,
            final Locale locale,
            final String style,
            final IModel<String> defaultValue)
                    throws MissingResourceException {

        final String translated = translate(key, component);
        if(!_Strings.isNullOrEmpty(translated) && !translated.equals(key)) {
            return translated;
        }
        return super.getString(key, component, model, locale, style, defaultValue);
    }

    protected String translate(final String key, final Component component) {
        final Class<?> contextClass = determineContextClassElse(component, IsisWicketApplication.class);
        final String context = contextClass.getName();
        if(IsisSession.isInSession()) {
            return translate(key, context);
        } else {
            return isisSessionFactory.doInSession(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return translate(key, context);
                }
            });
        }
    }

    private Class<?> determineContextClassElse(final Component component, final Class<?> fallback) {
        return component != null
                ? determineContextClass(component)
                        : fallback;
    }

    private Class<? > determineContextClass(final Component component) {
        // special case
        if(component instanceof org.wicketstuff.select2.Select2Choice ||
                component instanceof org.wicketstuff.select2.Select2MultiChoice) {
            return component.getClass();
        }
        final Component parentComponent = pageElseSignificantParentOf(component);
        return enclosing(parentComponent.getClass());
    }

    private Component pageElseSignificantParentOf(final Component component) {
        final Component page = pageOf(component);
        if (page != null) {
            return page;
        }
        return parentFormOrPanelOf(component);
    }

    /**
     * Search up this component instance's hierarchy looking for containing page.
     */
    private Component pageOf(final Component component) {
        if(component instanceof Page) {
            return component;
        }
        final MarkupContainer parent = component.getParent();
        if(parent != null) {
            return pageOf(parent);
        }
        return component;
    }

    /**
     * Search up this component instance's hierarchy, and use the first form or panel that is a parent
     * of this component.
     */
    private Component parentFormOrPanelOf(final Component component) {
        if(component instanceof Form || component instanceof Panel) {
            return component;
        }
        final MarkupContainer parent = component.getParent();
        if(parent != null) {
            return parentFormOrPanelOf(parent);
        }
        return parent;
    }

    private Class<?> enclosing(final Class<?> cls) {
        Class<?> enclosingClass = cls.getEnclosingClass();
        return enclosingClass != null? enclosing(enclosingClass): cls;
    }

    private String translate(final String key, final String context) {
        return translationService.translate(context, key);
    }


}

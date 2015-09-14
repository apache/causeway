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

package org.apache.isis.viewer.wicket.model.models;

import java.util.Map;
import com.google.common.collect.Maps;
import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.string.PrependingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.hints.UiHintPathSignificant;

/**
 * Adapter for {@link LoadableDetachableModel}s, providing access to some of the
 * Isis' dependencies.
 */
public abstract class ModelAbstract<T> extends LoadableDetachableModel<T> implements UiHintContainer {

    private static final long serialVersionUID = 1L;

    public ModelAbstract() {
    }

    public ModelAbstract(final T t) {
        super(t);
    }


    // //////////////////////////////////////////////////////////
    // Hint support
    // //////////////////////////////////////////////////////////

    private final Map<String, String> hints = Maps.newTreeMap();

    public String getHint(final Component component, final String key) {
        if(component == null) {
            return null;
        }
        String hintKey = hintKey(component, key);
        return hints.get(hintKey);
    }

    @Override
    public void setHint(Component component, String key, String value) {
        if(component == null) {
            return;
        }
        String hintKey = hintKey(component, key);
        if(value != null) {
            hints.put(hintKey, value);
        } else {
            hints.remove(hintKey);
        }
    }

    @Override
    public void clearHint(Component component, String key) {
        setHint(component, key, null);
    }


    private static String hintKey(Component component, String key) {
        return hintPathFor(component) + "-" + key;
    }

    private static String hintPathFor(Component component)
    {
        return Strings.afterFirstPathComponent(fullHintPathFor(component), Component.PATH_SEPARATOR);
    }

    private static String fullHintPathFor(Component component)
    {
        final PrependingStringBuffer buffer = new PrependingStringBuffer(32);
        for (Component c = component; c != null; c = c.getParent())
        {
            if(c instanceof UiHintPathSignificant) {
                if (buffer.length() > 0)
                {
                    buffer.prepend(Component.PATH_SEPARATOR);
                }
                buffer.prepend(c.getId());
            }
        }
        return buffer.toString();
    }

    protected Map<String, String> getHints() {
        return hints;
    }

    // //////////////////////////////////////////////////////////////
    // Dependencies
    // //////////////////////////////////////////////////////////////

    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

}

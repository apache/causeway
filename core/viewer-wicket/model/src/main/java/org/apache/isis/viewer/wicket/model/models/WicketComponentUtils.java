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

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.renderStrategy.DeepChildFirstVisitor;
import org.apache.wicket.util.visit.IVisit;

import org.apache.isis.commons.internal.base._Casts;

public class WicketComponentUtils {

    public WicketComponentUtils(){}

    /**
     * Locates a component implementing the required class on the same page as the supplied component.
     */
    public static <T> T getFrom(Component component, final Class<T> cls) {
        return getFrom(component.getPage(), cls);
    }

    /**
     * Locates a component implementing the required class on the supplied page.
     */
    public static <T> T getFrom(Page page, final Class<T> cls) {
        final Object[] pComponent = new Object[1];
        page.visitChildren(new DeepChildFirstVisitor() {
            @Override
            public void component(Component component, IVisit<Void> visit) {
                if(cls.isAssignableFrom(component.getClass())) {
                    pComponent[0] =  component;
                    visit.stop();
                }
            }
            @Override
            public boolean preCheck(Component component) {
                return false;
            }
        });

        return _Casts.uncheckedCast(pComponent[0]);
    }

}

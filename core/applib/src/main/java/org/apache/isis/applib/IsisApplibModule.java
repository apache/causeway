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
package org.apache.isis.applib;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "module")
public class IsisApplibModule extends ModuleAbstract {

    // -- UI EVENT CLASSES

    public abstract static class TitleUiEvent<S>
    extends org.apache.isis.applib.events.ui.TitleUiEvent<S> {
        private static final long serialVersionUID = 1L;
    }

    public abstract static class IconUiEvent<S>
    extends org.apache.isis.applib.events.ui.IconUiEvent<S> {
        private static final long serialVersionUID = 1L;
    }

    public abstract static class CssClassUiEvent<S>
    extends org.apache.isis.applib.events.ui.CssClassUiEvent<S> {
        private static final long serialVersionUID = 1L;
    }
    public abstract static class LayoutUiEvent<S>
            extends org.apache.isis.applib.services.eventbus.LayoutUiEvent<S> {
        private static final long serialVersionUID = 1L;
    }

    // -- DOMAIN EVENT CLASSES

    public abstract static class ActionDomainEvent<S>
    extends org.apache.isis.applib.events.domain.ActionDomainEvent<S> {
        private static final long serialVersionUID = 1L;
    }

    public abstract static class CollectionDomainEvent<S,T>
    extends org.apache.isis.applib.events.domain.CollectionDomainEvent<S,T> {
        private static final long serialVersionUID = 1L;
    }

    public abstract static class PropertyDomainEvent<S,T>
    extends org.apache.isis.applib.events.domain.PropertyDomainEvent<S,T> {
        private static final long serialVersionUID = 1L;
    }



}

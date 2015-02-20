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
package org.apache.isis.viewer.wicket.ui.components.widgets.select2;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;

import com.google.common.collect.Lists;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

/**
 * A CSS reference that loads <a href="https://github.com/ivaynberg/select2/">Select2.css</a>
 * and <a href="http://fk.github.io/select2-bootstrap-css/">Select2-Bootstrap3</a>
 * <p>Depends on select2.css.</p>
 */
public class Select2BootstrapCssReference extends CssResourceReference {

    public Select2BootstrapCssReference() {
        super(Select2BootstrapCssReference.class, "select2-bootstrap.css");
    }

    @Override
    public Iterable<? extends HeaderItem> getDependencies() {
        return Lists.newArrayList(CssHeaderItem.forReference(new WebjarsCssResourceReference("/select2/current/select2.css")));
    }
}

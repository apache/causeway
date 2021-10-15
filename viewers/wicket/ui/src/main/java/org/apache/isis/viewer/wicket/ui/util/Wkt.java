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
package org.apache.isis.viewer.wicket.ui.util;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.danekja.java.util.function.serializable.SerializableSupplier;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Wkt {

    public Label label(final String id, final String label) {
        return new Label(id, label);
    }

    public Label label(final String id, final SerializableSupplier<String> labelSupplier) {
        return new Label(id, LambdaModel.<String>of(labelSupplier));
    }

    public Label labelAdd(final MarkupContainer container, final String id, final String label) {
        val component = label(id, label);
        container.addOrReplace(component);
        return component;
    }

    public Label labelAdd(final MarkupContainer container, final String id, final SerializableSupplier<String> labelSupplier) {
        val component = label(id, labelSupplier);
        container.addOrReplace(component);
        return component;
    }

    public Label labelAdd(final WebMarkupContainer container, final String id, final IModel<String> labelModel) {
        val component = new Label(id, labelModel);
        container.addOrReplace(component);
        return component;
    }

}

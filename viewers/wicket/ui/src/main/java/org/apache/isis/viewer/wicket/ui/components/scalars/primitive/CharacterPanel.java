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
package org.apache.isis.viewer.wicket.ui.components.scalars.primitive;

import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.CharacterConverter;
import org.springframework.util.ClassUtils;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldAbstract;

import lombok.val;

/**
 * Panel for rendering scalars of type {@link Character} or <tt>char</tt>.
 */
public class CharacterPanel
extends ScalarPanelTextFieldAbstract<Character> {

    private static final long serialVersionUID = 1L;

    public CharacterPanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel, Character.class);
    }

    @Override
    public <C> IConverter<C> getConverter(final Class<C> _type) {
        val type = ClassUtils.resolvePrimitiveIfNecessary(_type);
        if(Character.class.equals(type)) {
            return (IConverter<C>) CharacterConverter.INSTANCE; //FIXME[ISIS-2882] use value semantics instead
        }
        return super.getConverter(_type);
    }

}

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

package org.apache.isis.viewer.wicket.model.mementos;

import java.util.List;
import org.apache.wicket.util.string.StringValue;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.viewer.wicket.model.util.Strings;

public enum PageParameterNames {

    /**
     * The object's {@link Oid}.
     * 
     * <p>
     * Also encodes the object's spec, and whether the object is persistent or not.
     */
    OBJECT_OID,
    
    /**
     * Owning type of an action.
     * 
     * <p>
     * Whereas {@link #OBJECT_SPEC} is the concrete runtime type of the adapter,
     * the owning type could be some superclass if the action has been
     * inherited.
     */
    ACTION_OWNING_SPEC, 
    
    /**
     * 
     */
    ACTION_TYPE, 
    
    /**
     * 
     */
    ACTION_ID, 
    
    /**
     * 
     */
    ACTION_MODE, 
    
    /**
     * 
     */
    ACTION_SINGLE_RESULTS_MODE,
    
    /**
     * The argument acting as a context for a contributed action, if any.
     * 
     * <p>
     * In the format N=OBJECT_OID, where N is the 0-based action parameter
     * index.
     */
    ACTION_PARAM_CONTEXT,

    /**
     * To render as the page title
     */
    PAGE_TITLE,

    /**
     * Whether this bookmarks an action or entity, etc, as per {@link BookmarkablePageType}.
     */
    PAGE_TYPE,
    
    /**
     * Action argument(s), if known.
     */
    ACTION_ARGS;

    /**
     * Returns the {@link #name()} formatted as
     * {@link Strings#camelCase(String) camel case}.
     * 
     * <p>
     * For example, <tt>ACTION_TYPE</tt> becomes <tt>actionType</tt>.
     */
    @Override
    public String toString() {
        return Strings.toCamelCase(name());
    }

    public String getStringFrom(final PageParameters pageParameters) {
        return getStringFrom(pageParameters, null);
    }

    public String getStringFrom(PageParameters pageParameters, String defaultValue) {
        return pageParameters.get(this.toString()).toString(defaultValue);
    }

    public <T extends Enum<T>> T getEnumFrom(PageParameters pageParameters, Class<T> enumClass) {
        String value = getStringFrom(pageParameters);
        return value != null? Enum.valueOf(enumClass, value): null;
    }

    public List<String> getListFrom(PageParameters pageParameters) {
        return Lists.transform(pageParameters.getValues(this.toString()), new Function<StringValue, String>() {
            @Override
            public String apply(@Nullable StringValue input) {
                return input.toString();
            }
        });
    }


    public void addStringTo(final PageParameters pageParameters, final String value) {
        pageParameters.add(this.toString(), value);
    }

    public void addEnumTo(final PageParameters pageParameters, final Enum<?> someEnum) {
        addStringTo(pageParameters, someEnum.name());
    }



}

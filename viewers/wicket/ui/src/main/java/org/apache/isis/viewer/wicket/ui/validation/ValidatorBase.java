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
package org.apache.isis.viewer.wicket.ui.validation;

import org.apache.wicket.validation.IValidator;

import org.apache.isis.core.webapp.context.IsisAppCommonContext;

import lombok.Getter;

/**
 * Provides the <em>common context</em> for all implementing sub-classes.
 * @since 2.0
 *
 */
public abstract class ValidatorBase<T> implements IValidator<T> {

    private static final long serialVersionUID = 1L;
    
    @Getter protected final transient IsisAppCommonContext commonContext;
    
    protected ValidatorBase(IsisAppCommonContext commonContext) {
        this.commonContext = commonContext;
    }
    
}

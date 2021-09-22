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
package org.apache.isis.viewer.wicket.viewer.services;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;

@Service
@Named("isis.viewer.wicket..WicketViewerSettingsDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class WicketViewerSettingsDefault implements WicketViewerSettings {

    private static final long serialVersionUID = 1L;

    @Inject private transient IsisConfiguration configuration;

    @Override
    public int getMaxTitleLengthInStandaloneTables() {
        return getConfiguration().getViewer().getWicket().getMaxTitleLengthInStandaloneTables();
    }

    @Override
    public int getMaxTitleLengthInParentedTables() {
        return getConfiguration().getViewer().getWicket().getMaxTitleLengthInParentedTables();
    }

    @Override
    public String getDatePattern() {
        return getConfiguration().getViewer().getWicket().getDatePattern();
    }

    @Override
    public String getDateTimePattern() {
        return getConfiguration().getViewer().getWicket().getDateTimePattern();
    }

    @Override
    public boolean isReplaceDisabledTagWithReadonlyTag() {
        return getConfiguration().getViewer().getWicket().isReplaceDisabledTagWithReadonlyTag();
    }

    @Override
    public boolean isPreventDoubleClickForFormSubmit() {
        return getConfiguration().getViewer().getWicket().isPreventDoubleClickForFormSubmit();
    }

    @Override
    public boolean isPreventDoubleClickForNoArgAction() {
        return getConfiguration().getViewer().getWicket().isPreventDoubleClickForNoArgAction();
    }

    @Override
    public boolean isUseIndicatorForFormSubmit() {
        return getConfiguration().getViewer().getWicket().isUseIndicatorForFormSubmit();
    }

    @Override
    public boolean isUseIndicatorForNoArgAction() {
        return getConfiguration().getViewer().getWicket().isUseIndicatorForNoArgAction();
    }

    @Override
    public PromptStyle getPromptStyle() {
        return getConfiguration().getViewer().getWicket().getPromptStyle();
    }

    @Override
    public boolean isRedirectEvenIfSameObject() {
        return getConfiguration().getViewer().getWicket().isRedirectEvenIfSameObject();
    }

    // -- HELPER

    private IsisConfiguration getConfiguration() {
        if(configuration==null) {
            configuration = CommonContextUtils.getCommonContext().getConfiguration();
        }
        return configuration;
    }
}

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

package org.apache.isis.viewer.wicket.model.isis;

import java.io.Serializable;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.core.config.IsisConfiguration;

public interface WicketViewerSettings extends Serializable {

    /**
     * as per {@link IsisConfiguration.Viewer.Wicket#setMaxTitleLengthInStandaloneTables(int)}
     */
    int getMaxTitleLengthInStandaloneTables();

    /**
     * as per {@link IsisConfiguration.Viewer.Wicket#setMaxTitleLengthInParentedTables(int)}
     * @return
     */
    int getMaxTitleLengthInParentedTables();

    /**
     * As per {@link IsisConfiguration.Viewer.Wicket#setDatePattern(String)}
     * @return
     */
    String getDatePattern();

    /**
     * As per {@link IsisConfiguration.Viewer.Wicket#setDateTimePattern(String)}
     * @return
     */
    String getDateTimePattern();

    /**
     * As per {@link IsisConfiguration.Viewer.Wicket#setTimestampPattern(String)}
     *
     * @deprecated - seemingly unused
     */
    @Deprecated
    String getTimestampPattern();

    /**
     * As per {@link IsisConfiguration.Viewer.Wicket#setReplaceDisabledTagWithReadonlyTag(boolean)}
     */
    boolean isReplaceDisabledTagWithReadonlyTag();

    /**
     * As per {@link IsisConfiguration.Viewer.Wicket#setPreventDoubleClickForFormSubmit(boolean)}
     */
    boolean isPreventDoubleClickForFormSubmit();

    /**
     * As per {@link IsisConfiguration.Viewer.Wicket#setPreventDoubleClickForNoArgAction(boolean)}
     */
    boolean isPreventDoubleClickForNoArgAction();

    /**
     * As per {@link IsisConfiguration.Viewer.Wicket#setUseIndicatorForFormSubmit(boolean)}
     */
    boolean isUseIndicatorForFormSubmit();

    /**
     * As per {@link IsisConfiguration.Viewer.Wicket#setUseIndicatorForNoArgAction(boolean)}
     */
    boolean isUseIndicatorForNoArgAction();

    /**
     * As per {@link IsisConfiguration.Viewer.Wicket#setPromptStyle(PromptStyle)}
     */
    PromptStyle getPromptStyle();

    /**
     * As per {@link IsisConfiguration.Viewer.Wicket#setRedirectEvenIfSameObject(boolean)}
     */
    boolean isRedirectEvenIfSameObject();

}

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
package demoapp.webapp.wicket.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.extensions.fullcalendar.wkt.ui.viewer.CausewayModuleExtFullCalendarWicketUi;
import org.apache.causeway.extensions.viewer.wicket.exceldownload.ui.CausewayModuleExtExcelDownloadWicketUi;

import demoapp.webapp.wicket.common.featured.customui.WhereInTheWorldPanelFactory;

/**
 * Featured Wicket specific extensions.
 */
@Configuration
@Import({
    WhereInTheWorldPanelFactory.class,
    CausewayModuleExtFullCalendarWicketUi.class,
    CausewayModuleExtExcelDownloadWicketUi.class, // allows for collection download as excel
})
public class DemoAppWicketCommon {

}

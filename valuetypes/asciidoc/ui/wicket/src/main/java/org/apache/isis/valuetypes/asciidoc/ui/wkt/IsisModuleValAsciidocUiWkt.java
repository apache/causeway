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
package org.apache.isis.valuetypes.asciidoc.ui.wkt;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.valuetypes.asciidoc.applib.IsisModuleValAsciidocApplib;
import org.apache.isis.valuetypes.asciidoc.ui.wkt.components.AsciiDocPanelFactoriesWkt;
import org.apache.isis.valuetypes.asciidoc.ui.wkt.components.schema.chg.v2.ChangesDtoPanelFactoriesWkt;
import org.apache.isis.valuetypes.asciidoc.ui.wkt.components.schema.cmd.v2.CommandDtoPanelFactoriesWkt;
import org.apache.isis.valuetypes.asciidoc.ui.wkt.components.schema.ixn.v2.InteractionDtoPanelFactoriesWkt;

@Configuration
@Import({
    IsisModuleValAsciidocApplib.class,
    AsciiDocPanelFactoriesWkt.Parented.class,
    AsciiDocPanelFactoriesWkt.Standalone.class,
    InteractionDtoPanelFactoriesWkt.Parented.class,
    InteractionDtoPanelFactoriesWkt.Standalone.class,
    ChangesDtoPanelFactoriesWkt.Parented.class,
    ChangesDtoPanelFactoriesWkt.Standalone.class,
    CommandDtoPanelFactoriesWkt.Parented.class,
    CommandDtoPanelFactoriesWkt.Standalone.class,
})
public class IsisModuleValAsciidocUiWkt {
}

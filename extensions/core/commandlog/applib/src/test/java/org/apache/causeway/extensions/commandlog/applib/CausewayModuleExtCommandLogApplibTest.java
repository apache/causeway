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
package org.apache.causeway.extensions.commandlog.applib;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandExportManager;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandExportManager_exportSelected;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandExportManager_excludeCommands;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayableCommand_makeExportable;

class CausewayModuleExtCommandLogApplibTest {

    @Test
    void module_imports_current_command_export_manager_actions_without_obsolete_make_selected_exportable_action() {
        final var imports = Arrays.<Class<?>>asList(
                CausewayModuleExtCommandLogApplib.class.getAnnotation(Import.class).value());

        assertThat(imports)
                .contains(
                        CommandExportManager.class,
                        CommandExportManager_exportSelected.class,
                        CommandExportManager_excludeCommands.class,
                        ReplayableCommand_makeExportable.class);
        assertThat(imports)
                .extracting(Class::getSimpleName)
                .doesNotContain("CommandExportManager_makeSelectedExportable");
    }
}

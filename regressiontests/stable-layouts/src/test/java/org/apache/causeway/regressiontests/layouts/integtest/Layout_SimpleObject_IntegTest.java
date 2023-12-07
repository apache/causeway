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
 *
 */
package org.apache.causeway.regressiontests.layouts.integtest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.regressiontests.layouts.integtest.model.SimpleObject;

import lombok.val;

@SpringBootTest(
        classes = Layout_SimpleObject_IntegTest.AppManifest.class
)
@ActiveProfiles("test")
public class Layout_SimpleObject_IntegTest extends LayoutTestAbstract {

    @Test
    void openRestApi() {
        val tester =
                testerFactory.actionTester(SimpleObject.class, "openRestApi", Where.OBJECT_FORMS);
        tester.assertLayoutPosition(ActionLayout.Position.PANEL_DROPDOWN);
        tester.assertLayoutGroup(LayoutConstants.FieldSetId.METADATA);
        tester.assertLayoutOrder("750.1");
    }

    @Test
    void clearHints() {
        val tester =
                testerFactory.actionTester(SimpleObject.class, "clearHints", Where.OBJECT_FORMS);
        tester.assertLayoutPosition(ActionLayout.Position.PANEL_DROPDOWN);
        tester.assertLayoutGroup(LayoutConstants.FieldSetId.METADATA);
        tester.assertLayoutOrder("400.1");
    }

}

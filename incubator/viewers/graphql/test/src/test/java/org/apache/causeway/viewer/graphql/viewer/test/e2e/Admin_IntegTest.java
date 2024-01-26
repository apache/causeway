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
package org.apache.causeway.viewer.graphql.viewer.test.e2e;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.viewer.graphql.viewer.test.domain.Department;
import org.apache.causeway.viewer.graphql.viewer.test.domain.DeptHead;
import org.apache.causeway.viewer.graphql.viewer.test.domain.StaffMember;

import lombok.val;


//NOT USING @Transactional since we are running server within same transaction otherwise
@ActiveProfiles("test")
public class Admin_IntegTest extends Abstract_IntegTest {

    @Test
    @UseReporter(DiffReporter.class)
    void admin_action() throws Exception {

        // when
        val response = submit();

        // then payload
        Approvals.verify(response, jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void action_with_disabled_param() throws Exception {

        // when
        val response = submit();

        // then payload
        Approvals.verify(response, jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void action_with_hidden_param() throws Exception {

        // when
        val response = submit();

        // then payload
        Approvals.verify(response, jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void other_admin_action() throws Exception {

        // when
        val response = submit();

        // then payload
        Approvals.verify(response, jsonOptions());
    }


}

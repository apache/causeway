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
package org.apache.causeway.viewer.graphql.viewer.test.e2e.special;

import java.util.Optional;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.viewer.graphql.viewer.test.domain.dept.Department;
import org.apache.causeway.viewer.graphql.viewer.test.domain.dept.StaffMember;
import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import static org.assertj.core.api.Assertions.assertThat;

@Order(65)
@ActiveProfiles("test")
public class MemberMetadata_IntegTest extends Abstract_IntegTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void targeted_introspection_exposes_only_the_shared_local_shape() throws Exception {
        var root = objectMapper.readTree(submit());
        var fields = root.at("/data/metadataType/fields");

        assertThat(root.at("/errors").isMissingNode())
                .as(root.toPrettyString())
                .isTrue();
        assertThat(fields).extracting(node -> node.get("name").stringValue())
                .containsExactlyInAnyOrder(
                        "friendlyName",
                        "description",
                        "cssClassFa",
                        "cssClassFaPosition",
                        "maxLength",
                        "pattern",
                        "patternFlags",
                        "multiLine",
                        "labelPosition",
                        "typicalLength")
                .doesNotContain("members", "grid", "menu", "position", "css", "icon", "required");
    }

    @Test
    void known_wrapper_introspection_exposes_the_metadata_field() throws Exception {
        var root = objectMapper.readTree(submit());

        assertThat(root.at("/data/__type/fields"))
                .extracting(node -> node.get("name").stringValue())
                .contains("metadata", "get", "hidden", "disabled", "validate", "datatype");
    }

    @Test
    void breadcrumb_metadata_is_additive_facet_driven_and_shared() throws Exception {
        StaffMember.resetDepartmentReadCount();
        var root = objectMapper.readTree(submit());
        var metadata = root.at("/data/rich/university_dept_Staff/findStaffMemberByName/invoke/results/_meta");

        assertThat(root.at("/errors").isMissingNode())
                .as(root.toPrettyString())
                .isTrue();
        assertThat(root.at("/data/breadcrumbType/fields"))
                .extracting(node -> node.get("name").stringValue())
                .containsExactlyInAnyOrder("logicalTypeName", "id", "title");
        assertThat(metadata.get("title").stringValue()).isEqualTo("Untitled Staff Member");
        assertThat(metadata.get("breadcrumbs"))
                .singleElement()
                .satisfies(node -> {
                    assertThat(node.get("logicalTypeName").stringValue())
                            .isEqualTo("university.dept.Department");
                    assertThat(node.get("id").stringValue()).isNotBlank();
                    assertThat(node.get("title").stringValue()).isEqualTo("Untitled Department");
                });
        assertThat(StaffMember.departmentReadCount())
                .as("selected breadcrumb metadata evaluates the navigable parent")
                .isPositive();
    }

    @Test
    void reads_known_wrappers_and_preserves_resource_metadata_and_redaction() throws Exception {
        StaffMember.resetDepartmentReadCount();
        var bookmark = transactionService.callTransactional(
                Propagation.REQUIRED,
                () -> {
                    Department department = departmentRepository.findByName("Classics");
                    Optional<Bookmark> result = bookmarkService.bookmarkFor(department);
                    return result.orElseThrow();
                }).valueAsNonNullElseFail();

        var response = submit(_Maps.unmodifiable("$departmentId", bookmark.identifier()));
        var root = objectMapper.readTree(response);
        var department = root.at("/data/rich/university_dept_Department");
        var action = department.get("changeName");
        var parameter = action.get("params").get("newName").get("metadata");
        var collection = department.get("staffMembers").get("metadata");
        var staff = root.at("/data/rich/university_dept_Staff/findStaffMemberByName/invoke/results");
        var property = staff.get("name").get("metadata");

        assertThat(action.get("metadata").get("friendlyName").stringValue())
                .isEqualTo("Rename department");
        assertThat(action.get("metadata").get("description").stringValue())
                .isEqualTo("Changes the department display name");
        assertThat(action.get("metadata").get("cssClassFa").stringValue())
                .isEqualTo("pen-to-square");
        assertThat(action.get("metadata").get("cssClassFaPosition").stringValue())
                .isEqualTo("RIGHT");
        assertThat(parameter.get("friendlyName").stringValue()).isEqualTo("Replacement name");
        assertThat(parameter.get("description").stringValue()).isEqualTo("New name for the department");
        assertThat(parameter.get("cssClassFa").isNull()).isTrue();
        assertThat(parameter.get("cssClassFaPosition").isNull()).isTrue();
        assertThat(parameter.get("maxLength").intValue()).isEqualTo(50);
        assertThat(parameter.get("pattern").stringValue()).isEqualTo("[A-Za-z !]+");
        assertThat(parameter.get("patternFlags").intValue()).isEqualTo(2);
        assertThat(parameter.get("multiLine").intValue()).isEqualTo(2);
        assertThat(parameter.get("typicalLength").intValue()).isEqualTo(30);

        assertThat(collection.get("friendlyName").stringValue()).isEqualTo("Department staff");
        assertThat(collection.get("description").stringValue())
                .isEqualTo("Staff assigned to this department");
        assertThat(collection.get("cssClassFa").isNull()).isTrue();
        assertThat(collection.get("cssClassFaPosition").isNull()).isTrue();
        assertThat(collection.get("maxLength").isNull()).isTrue();

        assertThat(property.get("friendlyName").stringValue()).isEqualTo("Staff display name");
        assertThat(property.get("description").stringValue()).isEqualTo("Name shown for the staff member");
        assertThat(property.get("cssClassFa").isNull()).isTrue();
        assertThat(property.get("cssClassFaPosition").isNull()).isTrue();
        assertThat(property.get("maxLength").intValue()).isEqualTo(40);
        assertThat(property.get("pattern").stringValue()).isEqualTo("[A-Za-z .!'-]+");
        assertThat(property.get("patternFlags").intValue()).isEqualTo(2);
        assertThat(property.get("multiLine").intValue()).isEqualTo(2);
        assertThat(property.get("labelPosition").stringValue()).isEqualTo("TOP");
        assertThat(property.get("typicalLength").intValue()).isEqualTo(24);

        assertThat(staff.at("/photo/get/fileAccept").stringValue()).isEqualTo("application/pdf");
        assertThat(staff.at("/hiddenPhoto/metadata/description").isNull()).isTrue();
        assertThat(staff.at("/hiddenPhoto/hidden").booleanValue()).isTrue();
        assertThat(response)
                .doesNotContain("CONFIDENTIAL_RESOURCE_CONTENT")
                .doesNotContain("disabled-reason")
                .doesNotContain("authorization-policy");
        assertThat(StaffMember.departmentReadCount())
                .as("unselected breadcrumb metadata must not evaluate the navigable parent")
                .isZero();
    }
}

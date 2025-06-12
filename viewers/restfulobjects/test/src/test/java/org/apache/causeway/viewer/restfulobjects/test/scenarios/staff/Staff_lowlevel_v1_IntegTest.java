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
package org.apache.causeway.viewer.restfulobjects.test.scenarios.staff;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;

import com.google.gson.GsonBuilder;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.core.metamodel.valuesemantics.BlobValueSemantics;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.viewer.restfulobjects.test.scenarios.Abstract_IntegTest;

import lombok.Getter;
import lombok.SneakyThrows;

@Import({
    Staff_lowlevel_v1_IntegTest.BlobValueSemanticsV1LegacyEncoding.class
})
class Staff_lowlevel_v1_IntegTest extends Abstract_IntegTest {

    @Test
    @UseReporter(DiffReporter.class)
    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    @SneakyThrows
    void createStaffMemberWithPhoto2() {

        // given
        final var staffName = "Fred Smith";

        final var bookmarkBeforeIfAny = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = staffMemberRepository.findByName(staffName);
            return bookmarkService.bookmarkFor(staffMember);
        }).valueAsNonNullElseFail();

        assertThat(bookmarkBeforeIfAny).isEmpty();

        // and given
        final var departmentName = "Classics";
        final var departmentBookmark = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = departmentRepository.findByName(departmentName);
            return bookmarkService.bookmarkFor(staffMember).orElseThrow();
        }).valueAsNonNullElseFail();

        String departmentHref = asRelativeHref(departmentBookmark);
        Invocation.Builder departmentRequest = restfulClient.request(departmentHref);
        Response departmentResponse = departmentRequest.get();
        assertThat(departmentResponse.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);

        // and given
        final var photoEncoded = readFileAndEncodeAsBlob("StaffMember-photo-Bar.pdf");

        // when create request
        final var requestBuilder = restfulClient.request("services/university.dept.Staff/actions/createStaffMemberWithPhoto2/invoke");

        final var body = new Body(staffName, asAbsoluteHref(departmentBookmark), photoEncoded);
        final var bodyJson = new GsonBuilder().create().toJson(body);

        // then
        Approvals.verify(bodyJson, jsonOptions());

        // and when send request
        var response = requestBuilder.post(Entity.entity(bodyJson, "application/json"));

        // then
        assertResponseOK(response);
        var entity = response.readEntity(String.class);
        assertNotNull(entity);

        // and also object is created in database
        final var bookmarkAfterIfAny = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = staffMemberRepository.findByName(staffName);
            return bookmarkService.bookmarkFor(staffMember);
        }).valueAsNonNullElseFail();
        assertThat(bookmarkAfterIfAny).isNotEmpty();
    }

    private String asAbsoluteHref(final Bookmark bookmark) {
        return String.format("%s%s", restfulClient.getConfig().getRestfulBaseUrl(), asRelativeHref(bookmark));
    }

    private String asRelativeHref(final Bookmark bookmark) {
        return String.format("objects/%s/%s", bookmark.logicalTypeName(), bookmark.identifier());
    }

    private String readFileAndEncodeAsBlob(final String fileName) throws IOException, URISyntaxException {
        var bytes = DataSource.ofResource(Abstract_IntegTest.class, fileName)
                .bytes();
        String photoEncoded = encodePdf(fileName, bytes);
        return photoEncoded;
    }

    private String encodePdf(final String fileName, final byte[] pdfBytes) throws URISyntaxException {
        final String pdfBytesEncoded = Base64.getEncoder().encodeToString(pdfBytes);
        final String encodedBlob = String.format("%s:%s:%s", fileName, "application/pdf", pdfBytesEncoded);
        return encodedBlob;
    }

    @Getter
    static class Body {

        private Name name;
        private Department department;
        private Blob photo;

        /**
         * @param nameValue
         * @param departmentHrefValue
         * @param blobValue - is the Blob encoded format: "filename.pdf:application/pdf:pdfBytesBase64Encoded"
         */
        Body(final String nameValue, final String departmentHrefValue, final String blobValue) {
            photo = new Blob(blobValue);
            name = new Name(nameValue);
            department = new Department(new Department.Value(departmentHrefValue));
        }

        record Name(String value) {
        }

        record Department(Value value) {
            record Value(String href) {
            }
        }

        record Blob(String value) {
        }
    }

    @Component
    @Named("causeway.metamodel.value.BlobValueSemanticsV1LegacyEncoding")   // must have different name to original
    static class BlobValueSemanticsV1LegacyEncoding
        extends BlobValueSemantics
        implements Renderer<Blob> {

        public BlobValueSemanticsV1LegacyEncoding() {
        }

        @Override
        public Class<Blob> getCorrespondingClass() {
            return Blob.class;
        }

        @Override
        public ValueType getSchemaValueType() {
            return ValueType.STRING;
        }

        // -- COMPOSER

        @Override
        public ValueDecomposition decompose(final Blob value) {
            return decomposeAsString(value, this::toEncodedString, () -> null);
        }

        @Override
        public Blob compose(final ValueDecomposition decomposition) {
            return composeFromString(decomposition, this::fromEncodedString, ()->null);
        }

        // RENDERER

        @Override
        public String titlePresentation(final ValueSemanticsProvider.Context context, final Blob value) {
            return renderTitle(value, Blob::name);
        }

        @Override
        public String htmlPresentation(final ValueSemanticsProvider.Context context, final Blob value) {
            return renderHtml(value, Blob::name);
        }

        private String toEncodedString(final Blob blob) {
            return blob.name() + ":" + blob.mimeType().getBaseType() + ":" +
            _Strings.ofBytes(_Bytes.encodeToBase64(Base64.getEncoder(), blob.bytes()), StandardCharsets.UTF_8);
        }

        private Blob fromEncodedString(final String data) {
            final int colonIdx = data.indexOf(':');
            final String name  = data.substring(0, colonIdx);
            final int colon2Idx  = data.indexOf(":", colonIdx+1);
            final String mimeTypeBase = data.substring(colonIdx+1, colon2Idx);
            final String payload = data.substring(colon2Idx+1);
            final byte[] bytes = _Bytes.decodeBase64(Base64.getDecoder(), payload.getBytes(StandardCharsets.UTF_8));
            try {
                return new Blob(name, new MimeType(mimeTypeBase), bytes);
            } catch (MimeTypeParseException e) {
                throw new RuntimeException(e);
            }
        }

        // -- EXAMPLES

        @Override
        public Can<Blob> getExamples() {
            return Can.of(
                    Blob.of("a Blob", NamedWithMimeType.CommonMimeType.BIN, new byte[] {1, 2, 3}),
                    Blob.of("another Blob", NamedWithMimeType.CommonMimeType.BIN, new byte[] {3, 4}));
        }

    }

}

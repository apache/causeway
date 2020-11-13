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
package org.apache.isis.testdomain.rest;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.Table;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.applib.client.RepresentationTypeSimplifiedV2;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.extensions.restclient.ActionParameterListBuilder;
import org.apache.isis.extensions.restclient.ResponseDigest;
import org.apache.isis.extensions.restclient.log.ClientConversationFilter;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.rospec.BigComplex;
import org.apache.isis.testdomain.rospec.Configuration_usingRoSpec;
import org.apache.isis.testdomain.rospec.Customer;
import org.apache.isis.testdomain.rospec.RoSpecSampler;
import org.apache.isis.testdomain.util.rest.RestEndpointService;
import org.apache.isis.tooling.model4adoc.AsciiDocFactory;
import org.apache.isis.tooling.model4adoc.AsciiDocWriter;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.IsisModuleViewerRestfulObjectsJaxrsResteasy4;

import static org.apache.isis.testdomain.util.CollectionAssertions.assertComponentWiseEquals;
import static org.apache.isis.testdomain.util.CollectionAssertions.assertComponentWiseNumberEquals;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.cell;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.doc;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.headRow;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.row;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.table;

import lombok.SneakyThrows;
import lombok.val;

@SpringBootTest(
        classes = {
                RestEndpointService.class
        },
        properties = {
                //"logging.level.org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationServiceContentNegotiator=DEBUG",
                //"logging.level.org.apache.isis.extensions.restclient.ResponseDigest=DEBUG"
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(IsisPresets.UseLog4j2Test)
@Import({
    Configuration_headless.class,
    Configuration_usingRoSpec.class,
    IsisModuleViewerRestfulObjectsJaxrsResteasy4.class,
})
@TestMethodOrder(OrderAnnotation.class) // run tests in sequence for reporting
class RestServiceSimpifiedRepresentationTest {

    @LocalServerPort int port; // just for reference (not used)
    @Inject RestEndpointService restService;

    private final RoSpecSampler refSampler = new RoSpecSampler();
    private final Filter4ReprType filter4ReprType = new Filter4ReprType();
    private final Filter4Reporting filter4Reporting = new Filter4Reporting();
    private final Can<ClientConversationFilter> conversationFilters = Can.of(
            filter4ReprType, 
            filter4Reporting);

    
    @AfterAll
    static void tearDown() throws IOException {
        System.out.println("=========================== ASCII DOC ============================");
        System.out.println(Filter4Reporting.print());
        System.out.println("==================================================================");
    }

    // -- VOID

    @Test @Order(1) 
    void voidResult() {
        val digest = digest("voidResult", void.class);
        assertTrue(digest.getEntities().isEmpty());
        filter4ReprType.assertRepresentationType(RepresentationTypeSimplifiedV2.VOID);
    }

    // -- STRING

    @Test @Order(2) 
    void string() {
        val digest = digest("string", String.class);
        val returnValue = digest.getEntities().getSingletonOrFail();
        assertEquals(refSampler.string(), returnValue);
        filter4ReprType.assertRepresentationType(RepresentationTypeSimplifiedV2.VALUE);
    }
    
    @Test @Order(2) 
    void stringUsingGet() {
        val digest = digestUsingGet("stringSafe", String.class);
        val returnValue = digest.getEntities().getSingletonOrFail();
        assertEquals(refSampler.stringSafe(), returnValue);
        filter4ReprType.assertRepresentationType(RepresentationTypeSimplifiedV2.VALUE);
    }

    @Test @Order(3)
    void stringNull() {
        val digest = digest("stringNull", String.class);
        assertTrue(digest.getEntities().isEmpty());
        filter4ReprType.assertHttpReturnCode(404);
        filter4ReprType.assertRepresentationType(null);
    }

    // -- STRING ARRAY

    @Test @Order(4)
    void stringArray() {
        val digest = digestArray("stringArray", String.class, new GenericType<List<String>>(){});
        val returnValue = digest.getEntities();
        assertComponentWiseEquals(refSampler.stringArray(), returnValue);
        filter4ReprType.assertRepresentationType(RepresentationTypeSimplifiedV2.VALUES);
    }

    @Test @Order(5)
    void stringArrayEmpty() {
        val digest = digestArray("stringArrayEmpty", String.class, new GenericType<List<String>>(){});
        assertTrue(digest.getEntities().isEmpty());
        filter4ReprType.assertHttpReturnCode(404);
        filter4ReprType.assertRepresentationType(null);
    }

    @Test @Order(6)
    void stringArrayNull() {
        val digest = digestArray("stringArrayNull", String.class, new GenericType<List<String>>(){});
        assertTrue(digest.getEntities().isEmpty());
        filter4ReprType.assertHttpReturnCode(404);
        filter4ReprType.assertRepresentationType(null);
    }

    // -- STRING LIST

    @Test @Order(7)
    void stringList() {
        val digest = digestList("stringList", String.class, new GenericType<List<String>>(){});
        val returnValues = digest.getEntities();
        assertComponentWiseEquals(refSampler.stringList(), returnValues);
        filter4ReprType.assertRepresentationType(RepresentationTypeSimplifiedV2.VALUES);
    }

    @Test @Order(8)
    void stringListEmpty() {
        val digest = digestList("stringListEmpty", String.class, new GenericType<List<String>>(){});
        assertTrue(digest.getEntities().isEmpty());
        filter4ReprType.assertHttpReturnCode(404);
        filter4ReprType.assertRepresentationType(null);
    }

    @Test @Order(9)
    void stringListNull() {
        val digest = digestList("stringListNull", String.class, new GenericType<List<String>>(){});
        assertTrue(digest.getEntities().isEmpty());
        filter4ReprType.assertHttpReturnCode(404);
        filter4ReprType.assertRepresentationType(null);
    }

    // -- INT

    @Test @Order(10)
    void integer() {
        val digest = digest("integer", Integer.class);
        val returnValue = digest.getEntities().getSingletonOrFail();
        assertEquals(refSampler.integer(), returnValue);
        filter4ReprType.assertRepresentationType(RepresentationTypeSimplifiedV2.VALUE);
    }

    @Test @Order(11)
    void integerNull() {
        val digest = digest("integerNull", Integer.class);
        assertTrue(digest.getEntities().isEmpty());       
        filter4ReprType.assertHttpReturnCode(404);
        filter4ReprType.assertRepresentationType(null);
    }

    @Test @Order(12)
    void integerPrimitive() {
        val digest = digest("integerPrimitive", int.class);
        val returnValue = digest.getEntities().getSingletonOrFail();
        assertEquals(refSampler.integerPrimitive(), returnValue);
        filter4ReprType.assertRepresentationType(RepresentationTypeSimplifiedV2.VALUE);
    }

    // -- BIG INT

    @Test @Order(13)
    void bigInteger() {
        val digest = digest("bigInteger", BigInteger.class);
        val returnValue = digest.getEntities().getSingletonOrFail();
        assertEquals(refSampler.bigInteger(), returnValue);
        filter4ReprType.assertRepresentationType(RepresentationTypeSimplifiedV2.VALUE);
    }

    @Test @Order(14)
    void bigIntegerNull() {
        val digest = digest("bigIntegerNull", BigInteger.class);
        assertTrue(digest.getEntities().isEmpty());
        filter4ReprType.assertHttpReturnCode(404);
        filter4ReprType.assertRepresentationType(null);
    }

    @Test @Order(15)
    void bigIntegerList() {
        val digest = digestList("bigIntegerList", BigInteger.class, new GenericType<List<BigInteger>>(){});
        val returnValue = digest.getEntities();
        assertComponentWiseNumberEquals(refSampler.bigIntegerList(), returnValue);
        filter4ReprType.assertRepresentationType(RepresentationTypeSimplifiedV2.VALUES);
    }

    // -- CUSTOMER

    @Test @Order(16)
    void customer() {
        val digest = digest("customer", Customer.class);
        val returnValue = digest.getEntities().getSingletonOrFail();
        assertEquals(refSampler.customer(), returnValue);
        filter4ReprType.assertRepresentationType(RepresentationTypeSimplifiedV2.OBJECT);
    }

    @Test @Order(17)
    void customerNull() {
        val digest = digest("customerNull", Customer.class);
        assertTrue(digest.getEntities().isEmpty());
        filter4ReprType.assertHttpReturnCode(404);
        filter4ReprType.assertRepresentationType(null);
    }

    @Test @Order(18)
    void customerList() {
        val digest = digestList("customerList", Customer.class, new GenericType<List<Customer>>(){});
        val returnValues = digest.getEntities();
        assertComponentWiseEquals(refSampler.customerList(), returnValues);
        filter4ReprType.assertRepresentationType(RepresentationTypeSimplifiedV2.LIST);
    }

    @Test @Order(19)
    void customerListEmpty() {
        val digest = digestList("customerListEmpty", Customer.class, new GenericType<List<Customer>>(){});
        assertTrue(digest.getEntities().isEmpty());
        filter4ReprType.assertHttpReturnCode(404);
        filter4ReprType.assertRepresentationType(null);
    }

    @Test @Order(20)
    void customerListNull() {
        val digest = digestList("customerListNull", Customer.class, new GenericType<List<Customer>>(){});
        assertTrue(digest.getEntities().isEmpty());
        filter4ReprType.assertHttpReturnCode(404);
        filter4ReprType.assertRepresentationType(null);
    }

    // -- COMPOSITE

    @Test @Order(21)
    void complexList() {
        val digest = digestList("complexList", BigComplex.class, new GenericType<List<BigComplex>>(){});
        val returnValue = digest.getEntities();
        assertComponentWiseEquals(refSampler.complexList(), returnValue);
        // in practice we might be agnostic to the actual representation type here, as long as we can successfully digest
        //filter4ReprType.assertRepresentationType(RepresentationTypeSimplifiedV2.VALUES);
        //filter4ReprType.assertRepresentationType(RepresentationTypeSimplifiedV2.LIST);
    }

    @Test @Order(22)
    void complexAdd() {
        // given 
        val a = BigComplex.of(
                "1.0000000000000000000000000000000000000001", 
                "-2.0000000000000000000000000000000000000002");
        val b = BigComplex.of("3", "4");

        val digest = digest("complexAdd", BigComplex.class, argBuilder->{
            argBuilder.addActionParameter("are", a.getRe().toPlainString());
            argBuilder.addActionParameter("aim", a.getIm().toPlainString());
            argBuilder.addActionParameter("bre", b.getRe().toPlainString());
            argBuilder.addActionParameter("bim", b.getIm().toPlainString());
        });
        val returnValue = digest.getEntities().getSingletonOrFail();
        BigComplex.assertEquals(a.add(b), returnValue);
        // in practice we might be agnostic to the actual representation type here, as long as we can successfully digest
        //filter4ReprType.assertRepresentationType(RepresentationTypeSimplifiedV2.VALUE);
        //filter4ReprType.assertRepresentationType(RepresentationTypeSimplifiedV2.OBJECT);
    }

    // -- HELPER

    <T> ResponseDigest<T> digest(String actionName, Class<T> entityType) {
        return digest(actionName, entityType, argBuilder->{});
    }

    <T> ResponseDigest<T> digest(String actionName, Class<T> entityType, Consumer<ActionParameterListBuilder> onArgs) {

        _Probe.errOut("");
        _Probe.errOut("=== %s", actionName);
        _Probe.errOut("");
        
        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false;
        val client = restService.newClient(useRequestDebugLogging, conversationFilters);

        val request = restService.newInvocationBuilder(client, 
                String.format("services/testdomain.RoSpecSampler/actions/%s/invoke", actionName)); 

        val argBuilder = client.arguments();
        onArgs.accept(argBuilder);

        val args = argBuilder 
                .build();
        
        val argsAsJavaSource = argBuilder.getActionParameterTypes().entrySet().stream()
        .map(entry->String.format("\n    %s %s", entry.getValue().getSimpleName(), entry.getKey()))        
        .collect(Collectors.joining(", "));
        
        filter4Reporting.next(String.format("@Action\n%s %s(%s) {\n    /*...*/\n}", entityType.getSimpleName(), actionName, argsAsJavaSource));

        val response = request.post(args);
        val digest = client.digest(response, entityType);

        if(!digest.isSuccess()) {
            fail(digest.getFailureCause());
        }

        return digest;

    }
    
    <T> ResponseDigest<T> digestUsingGet(String actionName, Class<T> entityType) {

        _Probe.errOut("");
        _Probe.errOut("=== %s", actionName);
        _Probe.errOut("");
        
        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false;
        val client = restService.newClient(useRequestDebugLogging, conversationFilters);

        val request = restService.newInvocationBuilder(client, 
                String.format("services/testdomain.RoSpecSampler/actions/%s/invoke", actionName)); 

        filter4Reporting.next(String.format("@Action\n%s %s {\n    /*...*/\n}", entityType.getSimpleName(), actionName));

        val response = request.get();
        val digest = client.digest(response, entityType);

        if(!digest.isSuccess()) {
            fail(digest.getFailureCause());
        }

        return digest;
    }

    <T> ResponseDigest<T> digestArray(
            String actionName, 
            Class<T> entityType, 
            GenericType<List<T>> genericType) {
        filter4Reporting.next(String.format("@Action\n%s[] %s() {\n    /*...*/\n}", entityType.getSimpleName(), actionName));
        return digestVector(actionName, entityType, genericType);
    }
    
    <T> ResponseDigest<T> digestList(
            String actionName, 
            Class<T> entityType, 
            GenericType<List<T>> genericType) {
        filter4Reporting.next(String.format("@Action\nList<%s> %s() {\n    /*...*/\n}", entityType.getSimpleName(), actionName));
        return digestVector(actionName, entityType, genericType);
    }
    
    <T> ResponseDigest<T> digestVector(
            String actionName, 
            Class<T> entityType, 
            GenericType<List<T>> genericType) {

        _Probe.errOut("");
        _Probe.errOut("=== %s", actionName);
        _Probe.errOut("");
        
        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false;
        val client = restService.newClient(useRequestDebugLogging, conversationFilters);

        val request = restService.newInvocationBuilder(client, 
                String.format("services/testdomain.RoSpecSampler/actions/%s/invoke", actionName)); 

        val args = client.arguments()
                .build();

        val response = request.post(args);
        val digest = client.digestList(response, entityType, genericType);

        if(!digest.isSuccess()) {
            fail(digest.getFailureCause());
        }

        return digest;

    }
    
    // -- FILTER FOR REPRESENTATION TYPE

    static class Filter4ReprType implements ClientConversationFilter {

        private RepresentationTypeSimplifiedV2 reprType;
        private int httpReturnCode;

        @Override
        public void onRequest(String endpoint, String method, String acceptHeaderParsing,
                Map<String, List<String>> headers, String body) {
        }

        @Override
        public void onResponse(int httpReturnCode, Map<String, List<String>> headers, String body) {
            
            this.httpReturnCode = httpReturnCode;
            
            val contentTypeHeaderStrings = Can.ofCollection(headers.get("Content-Type"));
            reprType = RepresentationTypeSimplifiedV2.parseContentTypeHeaderString(
                    contentTypeHeaderStrings.getFirst().orElse(null))
                    .orElse(null);
//            if(reprType==null) {
//                Assertions.fail(String.format(
//                        "Invalid REST response, cannot parse header's Content-Type '%s' for the repr-type to use", 
//                        contentTypeHeaderStrings));
//            } 
        }
        
        void assertRepresentationType(RepresentationTypeSimplifiedV2 expected) {
            assertEquals(expected, reprType);
        }

        void assertHttpReturnCode(int expected) {
            assertEquals(expected, httpReturnCode);
        }
        
    }
    
    // -- FILTER FOR ADOC REPORT
    
    static class Filter4Reporting implements ClientConversationFilter {
        
        private static Document doc = doc();
        private static Table table;
        
        static {
            
            table = table(doc);
            table.setTitle(String.format("Autogenerated by %s", RestServiceSimpifiedRepresentationTest.class.getSimpleName()));
            table.setAttribute("cols", "a,a", true);
            table.setAttribute("header-option", "", true);

            val headRow = headRow(table);

            cell(table, headRow, "Action");
            cell(table, headRow, "Request and Response");
        }
        
        private Row row;
        private StringBuilder requestAndResponseCellContent;

        void next(String javaSource) {
            
            row = row(table);
            cell(table, row, AsciiDocFactory.SourceFactory.java(javaSource, null));
            requestAndResponseCellContent = new StringBuilder();
        }
        
        @Override
        public void onRequest(String endpoint, String method, String acceptHeaderParsing,
                Map<String, List<String>> headers, String body) {
            
            requestAndResponseCellContent.append("==== REQUEST\n\n");
            requestAndResponseCellContent.append(AsciiDocFactory.SourceFactory.json(body, null));
        }

        @Override
        public void onResponse(int httpReturnCode, Map<String, List<String>> headers, String body) {
            val contentTypeHeaderStrings = Can.ofCollection(headers.get("Content-Type"));
            val reprType = RepresentationTypeSimplifiedV2.parseContentTypeHeaderString(
                    contentTypeHeaderStrings.getFirst().orElse(null))
                    .orElse(null);
            
            requestAndResponseCellContent
            .append("\n==== RESPONSE\n\n")
            .append(String.format("- HTTP return code: %d\n", httpReturnCode)) 
            .append(String.format("- Representation Type: %s\n\n", reprType))
            .append(AsciiDocFactory.SourceFactory.json(body, null));
            
            cell(table, row, requestAndResponseCellContent.toString());
        }
        
        @SneakyThrows
        public static String print() {
            return AsciiDocWriter.toString(doc);
        }
        
    }


}

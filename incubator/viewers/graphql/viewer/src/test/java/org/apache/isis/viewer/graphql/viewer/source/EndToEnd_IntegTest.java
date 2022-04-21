package org.apache.isis.viewer.graphql.viewer.source;

import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.viewer.graphql.viewer.source.gqltestdomain.E1;
import org.apache.isis.viewer.graphql.viewer.source.gqltestdomain.E2;
import org.apache.isis.viewer.graphql.viewer.source.gqltestdomain.GQLTestDomainMenu;
import org.apache.isis.viewer.graphql.viewer.source.gqltestdomain.TestEntityRepository;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.core.Scrubber;
import org.approvaltests.reporters.TextWebReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.transaction.annotation.Propagation;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import static org.apache.isis.commons.internal.assertions._Assert.*;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;


//@Transactional NOT USING @Transactional since we are running server within same transaction otherwise
public class EndToEnd_IntegTest extends TestDomainModuleIntegTestAbstract{

    @Inject TransactionService transactionService;
    @Inject IsisSystemEnvironment isisSystemEnvironment;
    @Inject SpecificationLoader specificationLoader;
    @Inject GraphQlSourceForIsis graphQlSourceForIsis;

    @Inject TestEntityRepository testEntityRepository;
    @Inject GQLTestDomainMenu gqlTestDomainMenu;

    private TestInfo testInfo;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        this.testInfo = testInfo;
        assertNotNull(isisSystemEnvironment);
        assertNotNull(specificationLoader);
        assertNotNull(graphQlSourceForIsis);
    }

    @AfterEach
    void afterEach(){
        transactionService.runTransactional(Propagation.REQUIRED, () -> {
            testEntityRepository.removeAll();
        });
    }

    @Test
    @Disabled("Creates schema.gql file for convenience")
    void print_schema_works() throws Exception {

        HttpClient client = HttpClient.newBuilder().build();
        URI uri = URI.create("http://0.0.0.0:" + port + "/graphql/schema");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        File targetFile1 = new File("src/test/resources/testfiles/schema.gql");
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(targetFile1.toPath()));

    }

    @Test
    @UseReporter(TextWebReporter.class)
    void simple_post_request() throws Exception {

        Approvals.verify(submit(), gqlOptions());

    }


    @Test
    @UseReporter(TextWebReporter.class)
    void findAllE1() throws Exception {

        // given
        transactionService.runTransactional(Propagation.REQUIRED, () -> {
            E1 foo = testEntityRepository.createE1("foo", null);
            testEntityRepository.createE2("bar", null);
            transactionService.flushTransaction();
            List<E1> allE1 = testEntityRepository.findAllE1();
            assertTrue(allE1.size()==1);
            List<E2> allE2 = testEntityRepository.findAllE2();
            assertTrue(allE2.size()==1);
        });

        // when, then
        Approvals.verify(submit(), gqlOptions());

    }

    @Test
    @UseReporter(TextWebReporter.class)
    void createE1() throws Exception {

        //File targetFile3 = new File("src/test/resources/testfiles/targetFile3.gql");

        String response1 = transactionService.callTransactional(Propagation.REQUIRED, () -> {

            String submit = submit();
            // just to show we need to query in separate tranasction
            List<E2> list = testEntityRepository.findAllE2();
            assertTrue(list.isEmpty());
            return submit;

        }).ifFailureFail().getValue().get();

        final List<E1> allE1 = new ArrayList<>();
        transactionService.runTransactional(Propagation.REQUIRED, () -> {

            List<E1> all = testEntityRepository.findAllE1();
            allE1.addAll(all);

        });

        assertEquals(1, allE1.size());
        assertEquals("newbee", allE1.get(0).getName());

        Approvals.verify(response1, gqlOptions());

    }

    @Test
    @UseReporter(TextWebReporter.class)
    void changeName() throws Exception {

        List<E2> e2List = new ArrayList<>();

        transactionService.runTransactional(Propagation.REQUIRED, () -> {

            e2List.add(testEntityRepository.createE2("foo", null));

        });

        E2 e2 = e2List.get(0);
        assertEquals("foo", e2.getName());
        assertEquals(e2.getName(), gqlTestDomainMenu.findE2("foo").getName());

        String response = transactionService.callTransactional(Propagation.REQUIRED, () -> {

            return submit();

        }).ifFailureFail().getValue().get();

        e2List.clear();
        transactionService.runTransactional(Propagation.REQUIRED, () -> {

            List<E2> all = testEntityRepository.findAllE2();
            e2List.addAll(all);

        });

        E2 e2Modified = e2List.get(0);

        //TODO: implement ...
//        assertEquals("bar", e2Modified.getName());
//
//        Approvals.verify(response, new Options());

    }


    private String submit() throws Exception{
        val httpRequest = buildRequestWithResource();
        return submitRequest(httpRequest);
    }

    @Data
    static class GqlBody {
        String query;
    }

    private HttpRequest buildRequestWithResource() throws IOException {
        val testMethodName = testInfo.getTestMethod().map(Method::getName).get();
        val resourceName = getClass().getSimpleName() + "." + testMethodName + ".submit.gql";
        val resourceContents = readResource(resourceName);
        val gqlBody = new GqlBody();
        gqlBody.setQuery(resourceContents);
        String gqlBodyStr = objectMapper.writeValueAsString(gqlBody);
        val bodyPublisher = HttpRequest.BodyPublishers.ofString(gqlBodyStr);
        val uri = URI.create("http://0.0.0.0:" + port + "/graphql");
        return HttpRequest.newBuilder().uri(uri).POST(bodyPublisher).setHeader("Content-Type", "application/json").build();
    }

    private String submitRequest(HttpRequest request) throws IOException, InterruptedException {
        val responseBodyHandler = HttpResponse.BodyHandlers.ofString();
        val httpClient = HttpClient.newBuilder().build();
        val httpResponse = httpClient.send(request, responseBodyHandler);
        return httpResponse.body();
    }

    private String readResource(String resourceName) throws IOException {
        URL resource = Resources.getResource(getClass(), resourceName);
        return Resources.toString(resource, StandardCharsets.UTF_8);
    }

    private Options gqlOptions() {
        return new Options().withScrubber(new Scrubber() {
            @SneakyThrows
            @Override
            public String scrub(String s) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(s));
            }
        }).forFile().withExtension(".gql");
    }

}

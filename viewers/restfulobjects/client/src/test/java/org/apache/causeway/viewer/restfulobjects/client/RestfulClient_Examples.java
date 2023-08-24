package org.apache.causeway.viewer.restfulobjects.client;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.apache.causeway.viewer.restfulobjects.client.auth.AuthorizationHeaderFactory;

@SuppressWarnings("unused")
class RestfulClient_Examples {

    void basic_auth() {

        RestfulClient restfulClient = RestfulClient.ofConfig(
                RestfulClientConfig.builder()
                    .restfulBaseUrl("localhost:8080")
                    .authenticationMode(AuthenticationMode.BASIC)
                    .basicAuthUser("sven")
                    .basicAuthPassword("pass")
                    .build());

        Invocation.Builder request = restfulClient.request("services/customers.CustomerApi/actions/findAll/invoke");

        Response response = request.get();
//        Response response = request.put(...);
//        Response response = request.post(...);
//        Response response = request.delete();

    }

    void oauth2_auth() {

        RestfulClient restfulClient = RestfulClient.ofConfig(
                RestfulClientConfig.builder()
                    .restfulBaseUrl("localhost:8080")
                    .authenticationMode(AuthenticationMode.OAUTH2_AZURE)
                    .oauthTenantId("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
                    .oauthClientId("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
                    .oauthClientSecret("xxXXx~XXxxXx-XxXX.XxXX.XxxXX-XxxXX~XxxXX")
                    .build());

        Invocation.Builder request = restfulClient.request("services/customers.CustomerApi/actions/findAll/invoke");

        Response response = request.get();
//        Response response = request.put(...);
//        Response response = request.post(...);
//        Response response = request.delete();

    }

    void auth_other() {

        RestfulClient restfulClient = RestfulClient.ofConfig(
                RestfulClientConfig.builder()
                        .restfulBaseUrl("localhost:8080")
                        .build(),
                        (AuthorizationHeaderFactory) () -> "some authorization header");
    }

    void usage() {

        RestfulClient restfulClient = RestfulClient.ofConfig(
                RestfulClientConfig.builder()
                        .restfulBaseUrl("localhost:8080")
                        //...
                        .build());

        Invocation.Builder request = restfulClient.request("services/customers.CustomerApi/actions/findAll/invoke");

        Response response = request.get();
//        Response response = request.put(...);
//        Response response = request.post(...);
//        Response response = request.delete();

    }

    // javax.ws.rs.client.ClientBuilder
    void refiner() {

        RestfulClient restfulClient = RestfulClient.ofConfig(
                RestfulClientConfig.builder()
                        .restfulBaseUrl("localhost:8080")
                        //...
                        .build(),
                clientBuilder ->
                        clientBuilder.connectTimeout(5, TimeUnit.SECONDS)
                                .readTimeout(2, TimeUnit.SECONDS)
        );
    }



}
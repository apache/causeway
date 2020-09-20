package org.apache.isis.extensions.commandreplay.secondary.fetch;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.extensions.jaxrsclient.applib.client.JaxRsClient;
import org.apache.isis.extensions.jaxrsclient.applib.client.JaxRsResponse;
import org.apache.isis.extensions.jaxrsclient.impl.client.JaxRsClientDefault;
import org.apache.isis.schema.cmd.v2.CommandsDto;

import lombok.val;


public class CommandFetcher_Test {

    @Disabled // intended only for manual verification.
    @Test
    public void testing_the_unmarshalling() {
        val jaxRsClient = new JaxRsClientDefault();
        final UriBuilder uriBuilder = UriBuilder.fromUri(
                        String.format(
                        "%s%s?batchSize=%d",
                        "http://localhost:8080/restful/", CommandFetcher.URL_SUFFIX, 10)
        );
        URI uri = uriBuilder.build();
        JaxRsResponse invoke = jaxRsClient.get(uri, CommandsDto.class, JaxRsClient.ReprType.ACTION_RESULT, "sven", "pass");
        CommandsDto entity = invoke.readEntity(CommandsDto.class);
        System.out.println(new JaxbService.Simple().toXml(entity));
    }
}
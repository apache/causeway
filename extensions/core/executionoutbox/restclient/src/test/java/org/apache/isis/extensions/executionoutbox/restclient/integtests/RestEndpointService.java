package org.apache.isis.extensions.executionoutbox.restclient.integtests;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.core.config.RestEasyConfiguration;
import org.apache.isis.core.config.viewer.web.WebAppContextPath;
import org.apache.isis.extensions.executionoutbox.restclient.api.OutboxClient;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class RestEndpointService {

    private final Environment environment;
    private final RestEasyConfiguration restEasyConfiguration;
    private final WebAppContextPath webAppContextPath;
    private final InteractionService interactionService;

    public OutboxClient newClient(int port, String username, String password) {

        val restRootPath =
                String.format("http://localhost:%d%s/",
                        port,
                        webAppContextPath
                                .prependContextPath(this.restEasyConfiguration.getJaxrs().getDefaultPath())
                );

        return new OutboxClient(restRootPath, username, password);
    }

}

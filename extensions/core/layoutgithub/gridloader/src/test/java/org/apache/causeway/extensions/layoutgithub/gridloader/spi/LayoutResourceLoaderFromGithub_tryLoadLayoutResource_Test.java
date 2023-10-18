package org.apache.causeway.extensions.layoutgithub.gridloader.spi;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.causeway.commons.internal.resources._Resources;

import org.apache.causeway.core.metamodel.services.grid.spi.LayoutResource;

import org.apache.causeway.extensions.layoutgithub.gridloader.menu.LayoutLoaderMenu;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assumptions.assumeThat;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.layoutgithub.gridloader.CausewayModuleExtLayoutGithubLoader;

import lombok.SneakyThrows;
import lombok.val;


class LayoutResourceLoaderFromGithub_tryLoadLayoutResource_Test {


    LayoutResourceLoaderFromGithub loader;

    @BeforeEach
    void preconditions() {
        assumeThat(getApiKey()).isNotNull();
    }

    @BeforeEach
    void setup() {

        val causewayConfiguration = new CausewayConfiguration(null);
        causewayConfiguration.getExtensions().getLayoutGithub().setApiKey(getApiKey());
        causewayConfiguration.getExtensions().getLayoutGithub().setRepository("apache/causeway-app-simpleapp");

        val module = new CausewayModuleExtLayoutGithubLoader();
        val restTemplateForSearch = module.restTemplateForGithubSearch(causewayConfiguration);
        val restTemplateForContent = module.restTemplateForGithubContent(causewayConfiguration);

        val layoutLoaderMenu = new LayoutLoaderMenu();

        loader = new LayoutResourceLoaderFromGithub(restTemplateForSearch, restTemplateForContent, causewayConfiguration, layoutLoaderMenu);
    }

    @Test
    public void happy_case() {

        val layoutResourceIfAny = loader.tryLoadLayoutResource(SimpleObject.class, "SimpleObject.layout.xml");
        assertThat(layoutResourceIfAny).isPresent();

    }

    @Test
    public void sad_case() {

        val layoutResourceIfAny = loader.tryLoadLayoutResource(SimpleObject.class, "Unknown.layout.xml");
        assertThat(layoutResourceIfAny).isEmpty();

    }


    @SneakyThrows
    private String getApiKey() {
        return _Resources.loadAsString(getClass(), "apikey.txt", StandardCharsets.UTF_8);
    }

    // unused
    static class SimpleObject {
    }
}
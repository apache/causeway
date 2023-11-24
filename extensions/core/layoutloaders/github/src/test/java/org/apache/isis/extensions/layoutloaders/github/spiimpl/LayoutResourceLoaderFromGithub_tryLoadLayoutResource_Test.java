package org.apache.isis.extensions.layoutloaders.github.spiimpl;

import java.nio.charset.StandardCharsets;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.extensions.layoutloaders.github.IsisModuleExtLayoutLoadersGithub;
import org.apache.isis.extensions.layoutloaders.github.menu.LayoutLoadersGitHubMenu;

import lombok.SneakyThrows;
import lombok.val;


class LayoutResourceLoaderFromGithub_tryLoadLayoutResource_Test {


    LayoutResourceLoaderFromGithub loader;

    @BeforeEach
    void preconditions() {
        String apiKey = getApiKey();
        assumeThat(apiKey).isNotNull();
    }

    @BeforeEach
    void setup() {

        val isisConfiguration = new IsisConfiguration(null);
        isisConfiguration.getExtensions().getLayoutLoaders().getGithub().setApiKey(getApiKey());
        isisConfiguration.getExtensions().getLayoutLoaders().getGithub().setRepository("apache/isis-app-simpleapp");

        val module = new IsisModuleExtLayoutLoadersGithub();
        val restTemplateForSearch = module.restTemplateForGithubSearch(isisConfiguration);
        val restTemplateForContent = module.restTemplateForGithubContent(isisConfiguration);
        val layoutLoaderMenu = new LayoutLoadersGitHubMenu(isisConfiguration);
        val queryResultsCache = new QueryResultsCache();

        layoutLoaderMenu.new enableDynamicLayoutLoading().act();
        Assertions.assertThat(layoutLoaderMenu.isEnabled()).isTrue();

        loader = new LayoutResourceLoaderFromGithub(restTemplateForSearch, restTemplateForContent, isisConfiguration, layoutLoaderMenu, () -> queryResultsCache);
    }

    @Test
    public void happy_case() {

        val layoutResourceIfAny = loader.lookupLayoutResource(SimpleObject.class, "SimpleObject.layout.xml");
        assertThat(layoutResourceIfAny).isPresent();

    }

    @Test
    public void sad_case() {

        val layoutResourceIfAny = loader.lookupLayoutResource(SimpleObject.class, "Unknown.layout.xml");
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

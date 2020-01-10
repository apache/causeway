package org.apache.isis.core.config.viewer.wicket;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WebAppContextPath_setContextPath_Test {

    private WebAppContextPath webAppContextPath;

    @BeforeEach
    void setup() {
        webAppContextPath = new WebAppContextPath();
    }

    @Test
    void when_null() {
        webAppContextPath.setContextPath(null);

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("");
    }

    @Test
    void when_empty() {
        webAppContextPath.setContextPath("");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("");
    }

    @Test
    void when_no_leading_slash() {
        webAppContextPath.setContextPath("abc");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("abc");
    }

    @Test
    void when_leading_slash() {
        webAppContextPath.setContextPath("/abc");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("abc");
    }

    @Test
    void when_multiple_leading_slashes() {
        webAppContextPath.setContextPath("//abc");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("abc");
    }

    @Test
    void when_no_trailing_slash() {
        webAppContextPath.setContextPath("/abc");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("abc");
    }

    @Test
    void when_trailing_slash() {
        webAppContextPath.setContextPath("/abc/");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("abc");
    }

    @Test
    void when_multiple_trailing_slashes() {
        webAppContextPath.setContextPath("/abc//");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("abc");
    }

    @Test
    void when_multiple_contains_slashes() {
        webAppContextPath.setContextPath("/abc/def/");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("abc/def");
    }

}
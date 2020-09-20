package org.apache.isis.extensions.commandlog.impl.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtils_trimmed_Test {

    @Test
    public void fits() {
        Assertions.assertThat(StringUtils.trimmed("abcde", 5)).isEqualTo("abcde");
    }

    @Test
    public void needs_to_be_trimmed() {
        Assertions.assertThat(StringUtils.trimmed("abcde", 4)).isEqualTo("a...");
    }

    @Test
    public void when_null() {
        Assertions.assertThat(StringUtils.trimmed(null, 4)).isNull();
    }

    @Test
    public void when_empty() {
        Assertions.assertThat(StringUtils.trimmed("", 4)).isEqualTo("");
    }

}
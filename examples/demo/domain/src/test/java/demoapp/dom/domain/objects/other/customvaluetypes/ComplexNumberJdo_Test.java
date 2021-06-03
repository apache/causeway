package demoapp.dom.domain.objects.other.customvaluetypes;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import demoapp.dom.domain.objects.other.embedded.jdo.ComplexNumberJdo;
import lombok.val;

class ComplexNumberJdo_Test {

    @Test
    void title() {
        val cn = ComplexNumberJdo.of(10.0, 5.0);
        Assertions.assertThat(cn.title()).isEqualTo("10.0 + 5.0i");
    }

    @Test
    void parse() {
        val cn = ComplexNumberJdo.parse("10.0 + 5.0i");

        Assertions.assertThat(cn).isPresent();
        Assertions.assertThat(cn.get().title()).isEqualTo("10.0 + 5.0i");
    }
}

package org.apache.causeway.applib.services.user;

import org.apache.causeway.applib.services.sudo.SudoService;
import org.apache.causeway.commons.collections.Can;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class UserMemento_contains_Test {


    @Test
    void when_contains_instance() {

        // given
        UserMemento userMemento = UserMemento.builder().name("foo").roles(Can.ofSingleton(SudoService.ACCESS_ALL_ROLE)).build();

        // when, then
        Assertions.assertThat(userMemento.getRoles()).contains(SudoService.ACCESS_ALL_ROLE);

    }

    @Test
    void when_contains_equivalent() {

        // given
        UserMemento userMemento = UserMemento.builder().name("foo").roles(Can.ofSingleton(SudoService.ACCESS_ALL_ROLE)).build();

        // when, then
        Assertions.assertThat(userMemento.getRoles()).contains(new RoleMemento(SudoService.ACCESS_ALL_ROLE.getName()));
    }

    @Test
    void when_does_not_contain() {

        // given
        UserMemento userMemento = UserMemento.builder().name("foo").build();

        // when, then
        Assertions.assertThat(userMemento.getRoles()).doesNotContain(SudoService.ACCESS_ALL_ROLE);
    }
}

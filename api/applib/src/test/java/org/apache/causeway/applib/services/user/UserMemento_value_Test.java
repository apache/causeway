package org.apache.causeway.applib.services.user;

import lombok.SneakyThrows;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.internaltestsupport.contract.ValueTypeContractTestAbstract;

class UserMemento_value_Test extends ValueTypeContractTestAbstract<UserMemento> {

    private UserMemento.UserMementoBuilder baseline() {
        return
                UserMemento.builder().name("Joe")
                        .authenticationSource(UserMemento.AuthenticationSource.DEFAULT)
                        .authenticationCode("123")
                        .multiTenancyToken("/UK")
                        .roles(Can.of(
                                RoleMemento.builder().name("role-1").build(),
                                RoleMemento.builder().name("role-2").build()))
                        .impersonating(false)
                ;
    }


    @Override
    protected List<UserMemento> getObjectsWithSameValue() {
        return Arrays.asList(
                baseline().build(),
                baseline().realName("Joe Bloggs").build(),
                baseline().realName("Mary Suggs").build(),
                baseline().avatarUrl(newURL("https://joebloggs.net/avatar")).build(),
                baseline().avatarUrl(newURL("https://marysuggs.net/avatar")).build(),
                baseline().languageLocale(Locale.ENGLISH).build(),
                baseline().languageLocale(Locale.FRANCE).build(),
                baseline().timeFormatLocale(Locale.ENGLISH).build(),
                baseline().timeFormatLocale(Locale.FRANCE).build(),
                baseline().numberFormatLocale(Locale.ENGLISH).build(),
                baseline().numberFormatLocale(Locale.FRANCE).build()
        );
    }

    @Override
    protected List<UserMemento> getObjectsWithDifferentValue() {
        return Arrays.asList(
                baseline().name("mary").build(),
                baseline().authenticationSource(UserMemento.AuthenticationSource.EXTERNAL).build(),
                baseline().multiTenancyToken("/FR").build(),
                baseline().authenticationCode("456").build(),
                baseline().roles(Can.ofSingleton(RoleMemento.builder().name("role-3").build())).build()
        );
    }

    @SneakyThrows
    private static URL newURL(String s) {
        return new URL(s);
    }

}

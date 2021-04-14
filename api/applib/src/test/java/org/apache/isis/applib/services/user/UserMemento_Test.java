package org.apache.isis.applib.services.user;

import java.net.MalformedURLException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import lombok.val;

class UserMemento_Test {

    @Nested
    class constructor {

        @Test
        void name_only() {

            // when
            val userMemento = UserMemento.ofName("fredflintstone");

            // then original unchanged
            Assertions.assertThat(userMemento.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento.getRoles()).hasSize(0);
            Assertions.assertThat(userMemento.getRealName()).isNull();
            Assertions.assertThat(userMemento.getAvatarUrl()).isNull();
        }

        @Test
        void name_and_roles() {

            // when
            val userMemento = UserMemento.ofNameAndRoleNames("fredflintstone", "CAVEMAN", "HUSBAND");

            // then
            Assertions.assertThat(userMemento.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento.getRoles()).hasSize(2);
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento.getRealName()).isNull();
            Assertions.assertThat(userMemento.getAvatarUrl()).isNull();
        }
    }

    @Nested
    class withRealName {

        @Test
        void user_and_roles_preserved_and_real_name_set() {
            // copy
            val userMemento = UserMemento.ofNameAndRoleNames("fredflintstone", "CAVEMAN", "HUSBAND");

            // when
            val userMemento2 = userMemento.withRealName("Fred Flintstone");

            // then copy created
            Assertions.assertThat(userMemento2).isNotSameAs(userMemento);

            // then copy correct
            Assertions.assertThat(userMemento2.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento2.getRoles()).hasSize(2);
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento2.getRealName()).isEqualTo("Fred Flintstone");

            // then original unchanged
            Assertions.assertThat(userMemento.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento.getRoles()).hasSize(2);
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento.getRealName()).isNull();
        }

        @Test
        void user_and_roles_and_avatarUrl_preserved_and_real_name_set() throws MalformedURLException {
            // copy
            val userMemento = UserMemento.ofNameAndRoleNames("fredflintstone", "CAVEMAN", "HUSBAND")
                    .withAvatarUrl(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"));

            // when
            val userMemento2 = userMemento.withRealName("Fred Flintstone");

            // then copy created
            Assertions.assertThat(userMemento2).isNotSameAs(userMemento);

            // then copy correct
            Assertions.assertThat(userMemento2.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento2.getRoles()).hasSize(2);
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento2.getAvatarUrl()).isEqualTo(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"));
            Assertions.assertThat(userMemento2.getRealName()).isEqualTo("Fred Flintstone");

            // then original unchanged
            Assertions.assertThat(userMemento.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento.getRoles()).hasSize(2);
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento.getAvatarUrl()).isEqualTo(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"));
            Assertions.assertThat(userMemento.getRealName()).isNull();
        }
    }

    @Nested
    class withAvatarUrl {

        @Test
        void user_and_roles_preserved_and_avatarUrl_set() throws MalformedURLException {

            // copy
            val userMemento = UserMemento.ofNameAndRoleNames("fredflintstone", "CAVEMAN", "HUSBAND");

            // when
            val userMemento2 = userMemento.withAvatarUrl(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"));

            // then copy created
            Assertions.assertThat(userMemento2).isNotSameAs(userMemento);

            // then copy correct
            Assertions.assertThat(userMemento2.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento2.getRoles()).hasSize(2);
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento2.getAvatarUrl()).isEqualTo(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"));

            // then original unchanged
            Assertions.assertThat(userMemento.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento.getRoles()).hasSize(2);
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento.getAvatarUrl()).isNull();
        }

        @Test
        void user_and_roles_and_real_name_preserved_and_avatarUrl_set() throws MalformedURLException {

            // copy
            val userMemento = UserMemento.ofNameAndRoleNames("fredflintstone", "CAVEMAN", "HUSBAND").withRealName("Fred Flintstone");

            // when
            val userMemento2 = userMemento.withAvatarUrl(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"));

            // then copy created
            Assertions.assertThat(userMemento2).isNotSameAs(userMemento);

            // then copy correct
            Assertions.assertThat(userMemento2.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento2.getRoles()).hasSize(2);
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento2.getAvatarUrl()).isEqualTo(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"));
            Assertions.assertThat(userMemento2.getRealName()).isEqualTo("Fred Flintstone");

            // then original unchanged
            Assertions.assertThat(userMemento.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento.getRoles()).hasSize(2);
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento.getRealName()).isEqualTo("Fred Flintstone");
            Assertions.assertThat(userMemento.getAvatarUrl()).isNull();
        }
    }

    @Nested
    class withRole {

        @Test
        void user_and_roles_preserved_and_role_added() throws MalformedURLException {

            // given
            val userMemento = UserMemento.ofNameAndRoleNames("fredflintstone", "CAVEMAN", "HUSBAND");

            // when
            val userMemento2 = userMemento.withRole("WICKET_ROLE");

            // then copy created
            Assertions.assertThat(userMemento2).isNotSameAs(userMemento);

            // then copy correct
            Assertions.assertThat(userMemento2.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento2.getRoles()).hasSize(3);
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("WICKET_ROLE"));

            // then original unchanged
            Assertions.assertThat(userMemento.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento.getRoles()).hasSize(2);
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
        }
    }

    @Nested
    class all_the_withers {

        @Test
        void happy_case() throws MalformedURLException {

            // when
            val userMemento = UserMemento.ofName("fredflintstone")
                    .withRole("CAVEMAN")
                    .withRole("HUSBAND")
                    .withAvatarUrl(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"))
                    .withRealName("Fred Flintstone");

            // then
            Assertions.assertThat(userMemento.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento.getRoles()).hasSize(2);
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento.getAvatarUrl()).isEqualTo(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"));
            Assertions.assertThat(userMemento.getRealName()).isEqualTo("Fred Flintstone");
        }
    }

}

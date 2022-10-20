/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.applib.services.user;

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
            Assertions.assertThat(userMemento.getRoles().size()).isEqualTo(0);
            Assertions.assertThat(userMemento.getRealName()).isNull();
            Assertions.assertThat(userMemento.getAvatarUrl()).isNull();
        }

        @Test
        void name_and_roles() {

            // when
            val userMemento = UserMemento.ofNameAndRoleNames("fredflintstone", "CAVEMAN", "HUSBAND");

            // then
            Assertions.assertThat(userMemento.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento.getRoles().size()).isEqualTo(2);
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
            Assertions.assertThat(userMemento2.getRoles().size()).isEqualTo(2);
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento2.getRealName()).isEqualTo("Fred Flintstone");

            // then original unchanged
            Assertions.assertThat(userMemento.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento.getRoles().size()).isEqualTo(2);
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
            Assertions.assertThat(userMemento2.getRoles().size()).isEqualTo(2);
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento2.getAvatarUrl()).isEqualTo(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"));
            Assertions.assertThat(userMemento2.getRealName()).isEqualTo("Fred Flintstone");

            // then original unchanged
            Assertions.assertThat(userMemento.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento.getRoles().size()).isEqualTo(2);
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
            Assertions.assertThat(userMemento2.getRoles().size()).isEqualTo(2);
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento2.getAvatarUrl()).isEqualTo(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"));

            // then original unchanged
            Assertions.assertThat(userMemento.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento.getRoles().size()).isEqualTo(2);
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
            Assertions.assertThat(userMemento2.getRoles().size()).isEqualTo(2);
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento2.getAvatarUrl()).isEqualTo(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"));
            Assertions.assertThat(userMemento2.getRealName()).isEqualTo("Fred Flintstone");

            // then original unchanged
            Assertions.assertThat(userMemento.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento.getRoles().size()).isEqualTo(2);
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
            val userMemento2 = userMemento.withRoleAdded("WICKET_ROLE");

            // then copy created
            Assertions.assertThat(userMemento2).isNotSameAs(userMemento);

            // then copy correct
            Assertions.assertThat(userMemento2.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento2.getRoles().size()).isEqualTo(3);
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("WICKET_ROLE"));

            // then original unchanged
            Assertions.assertThat(userMemento.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento.getRoles().size()).isEqualTo(2);
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
        }
    }

    @Nested
    class withImpersonating {

        @Test
        void user_and_roles_preserved_and_impersonating_flag_set() throws MalformedURLException {

            // given
            val userMemento = UserMemento.ofNameAndRoleNames("fredflintstone", "CAVEMAN", "HUSBAND");

            // when
            val userMemento2 = userMemento.withImpersonating(true);

            // then copy created
            Assertions.assertThat(userMemento2).isNotSameAs(userMemento);

            // then copy correct
            Assertions.assertThat(userMemento2.isImpersonating()).isTrue();

            // then original unchanged
            Assertions.assertThat(userMemento.isImpersonating()).isFalse();
        }
    }

    @Nested
    class withTenancyToken {

        @Test
        void user_and_roles_preserved_and_impersonating_flag_set() throws MalformedURLException {

            // given
            val userMemento = UserMemento.ofNameAndRoleNames("fredflintstone", "CAVEMAN", "HUSBAND");

            // when
            val userMemento2 = userMemento.withMultiTenancyToken("/ITA");

            // then copy created
            Assertions.assertThat(userMemento2).isNotSameAs(userMemento);

            // then copy correct
            Assertions.assertThat(userMemento2.getMultiTenancyToken()).isEqualTo("/ITA");

            // then original unchanged
            Assertions.assertThat(userMemento.getMultiTenancyToken()).isNull();
        }
    }

    @Nested
    class all_the_withers {

        @Test
        void happy_case() throws MalformedURLException {

            // when
            val userMemento = UserMemento.ofName("fredflintstone")
                    .withRoleAdded("CAVEMAN")
                    .withRoleAdded("HUSBAND")
                    .withAvatarUrl(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"))
                    .withRealName("Fred Flintstone")
                    .withMultiTenancyToken("/USA/Bedrock")
                    ;

            // then
            Assertions.assertThat(userMemento.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento.getRoles().size()).isEqualTo(2);
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento.getAvatarUrl()).isEqualTo(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"));
            Assertions.assertThat(userMemento.getRealName()).isEqualTo("Fred Flintstone");
            Assertions.assertThat(userMemento.isImpersonating()).isFalse();

            // and when
            val userMemento2 = userMemento.withImpersonating(true);

            // then copy created
            Assertions.assertThat(userMemento2).isNotSameAs(userMemento);

            // then copy correct
            Assertions.assertThat(userMemento2.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento2.getRoles().size()).isEqualTo(2);
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento2.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento2.getAvatarUrl()).isEqualTo(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"));
            Assertions.assertThat(userMemento2.getRealName()).isEqualTo("Fred Flintstone");
            Assertions.assertThat(userMemento2.isImpersonating()).isTrue();
            Assertions.assertThat(userMemento2.getMultiTenancyToken()).isEqualTo("/USA/Bedrock");

            // then original unchanged
            Assertions.assertThat(userMemento.getName()).isEqualTo("fredflintstone");
            Assertions.assertThat(userMemento.getRoles().size()).isEqualTo(2);
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("CAVEMAN"));
            Assertions.assertThat(userMemento.streamRoleNames()).anyMatch(x -> x.equals("HUSBAND"));
            Assertions.assertThat(userMemento.getAvatarUrl()).isEqualTo(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"));
            Assertions.assertThat(userMemento.getRealName()).isEqualTo("Fred Flintstone");
            Assertions.assertThat(userMemento.isImpersonating()).isFalse();
            Assertions.assertThat(userMemento.getMultiTenancyToken()).isEqualTo("/USA/Bedrock");

        }
    }

}

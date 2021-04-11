package org.apache.isis.security.spring.authconverters;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.val;

@ExtendWith(MockitoExtension.class)
class AuthenticationConverterOfOAuth2UserPrincipal_Test {

    @Mock Authentication mockAuthentication;

    AuthenticationConverterOfOAuth2UserPrincipal converter;

    @BeforeEach
    void before() {
        converter = new AuthenticationConverterOfOAuth2UserPrincipal();
    }

    @Nested
    class userNameFrom {

        @Test
        void given_login_attr_exists_then_used() {

            // given
            final OAuth2User oAuth2User = new OAuth2User() {
                @Override public String getName() { return "fred"; }
                @Override public Map<String, Object> getAttributes() {
                    return Collections.singletonMap("login", "freddy");
                }
                @Override public Collection<? extends GrantedAuthority> getAuthorities() {
                    return Collections.emptyList();
                }
            };

            // expect
            Mockito.when(mockAuthentication.getPrincipal()).thenReturn(oAuth2User);

            // when
            val userMemento = converter.convert(mockAuthentication);

            // then
            Assertions.assertThat(userMemento).isNotNull();
            Assertions.assertThat(userMemento.getName()).isEqualTo("freddy");

        }

        @Test
        void given_no_login_attr_exists_then_name_instead_is_used() {

            // given
            final OAuth2User oAuth2User = new OAuth2User() {
                @Override public String getName() { return "fred"; }
                @Override public Map<String, Object> getAttributes() {
                    return Collections.emptyMap();
                }
                @Override public Collection<? extends GrantedAuthority> getAuthorities() {
                    return Collections.emptyList();
                }
            };

            // expect
            Mockito.when(mockAuthentication.getPrincipal()).thenReturn(oAuth2User);

            // when
            val userMemento = converter.convert(mockAuthentication);

            // then
            Assertions.assertThat(userMemento).isNotNull();
            Assertions.assertThat(userMemento.getName()).isEqualTo("fred");

        }
    }

    @Nested
    class avatarUrl {

        @Test
        void given_avatarUrl_attr_exists_and_can_be_parsed_then_used() throws MalformedURLException {

            // given
            final OAuth2User oAuth2User = new OAuth2User() {
                @Override public String getName() { return "fred"; }
                @Override public Map<String, Object> getAttributes() {
                    return Collections.singletonMap("avatar_url", "https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png");
                }
                @Override public Collection<? extends GrantedAuthority> getAuthorities() {
                    return Collections.emptyList();
                }
            };

            // expect
            Mockito.when(mockAuthentication.getPrincipal()).thenReturn(oAuth2User);

            // when
            val userMemento = converter.convert(mockAuthentication);

            // then
            Assertions.assertThat(userMemento).isNotNull();
            Assertions.assertThat(userMemento.getAvatarUrl()).isEqualTo(new java.net.URL("https://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png"));

        }

        @Test
        void given_avatarUrl_attr_exists_but_cannot_be_parsed_then_not_used() throws MalformedURLException {

            // given
            final OAuth2User oAuth2User = new OAuth2User() {
                @Override public String getName() { return "fred"; }
                @Override public Map<String, Object> getAttributes() {
                    return Collections.singletonMap("avatar_url", "GARBAGE://upload.wikimedia.org/wikipedia/en/a/ad/Fred_Flintstone.png");
                }
                @Override public Collection<? extends GrantedAuthority> getAuthorities() {
                    return Collections.emptyList();
                }
            };

            // expect
            Mockito.when(mockAuthentication.getPrincipal()).thenReturn(oAuth2User);

            // when
            val userMemento = converter.convert(mockAuthentication);

            // then
            Assertions.assertThat(userMemento).isNotNull();
            Assertions.assertThat(userMemento.getAvatarUrl()).isNull();
        }

        @Test
        void given_avatarUrl_attr_does_not_exists_then_none() throws MalformedURLException {

            // given
            final OAuth2User oAuth2User = new OAuth2User() {
                @Override public String getName() { return "fred"; }
                @Override public Map<String, Object> getAttributes() {
                    return Collections.emptyMap();
                }
                @Override public Collection<? extends GrantedAuthority> getAuthorities() {
                    return Collections.emptyList();
                }
            };

            // expect
            Mockito.when(mockAuthentication.getPrincipal()).thenReturn(oAuth2User);

            // when
            val userMemento = converter.convert(mockAuthentication);

            // then
            Assertions.assertThat(userMemento).isNotNull();
            Assertions.assertThat(userMemento.getAvatarUrl()).isNull();
        }

    }

    @Nested
    class realName {

        @Test
        void given_name_attr_exists_then_used() throws MalformedURLException {

            // given
            final OAuth2User oAuth2User = new OAuth2User() {
                @Override public String getName() { return "fred"; }
                @Override public Map<String, Object> getAttributes() {
                    return Collections.singletonMap("name", "Fred Flintstone");
                }
                @Override public Collection<? extends GrantedAuthority> getAuthorities() {
                    return Collections.emptyList();
                }
            };

            // expect
            Mockito.when(mockAuthentication.getPrincipal()).thenReturn(oAuth2User);

            // when
            val userMemento = converter.convert(mockAuthentication);

            // then
            Assertions.assertThat(userMemento).isNotNull();
            Assertions.assertThat(userMemento.getRealName()).isEqualTo("Fred Flintstone");
        }

        @Test
        void given_name_attr_does_not_exists_then_none() throws MalformedURLException {

            // given
            final OAuth2User oAuth2User = new OAuth2User() {
                @Override public String getName() { return "fred"; }
                @Override public Map<String, Object> getAttributes() {
                    return Collections.emptyMap();
                }
                @Override public Collection<? extends GrantedAuthority> getAuthorities() {
                    return Collections.emptyList();
                }
            };

            // expect
            Mockito.when(mockAuthentication.getPrincipal()).thenReturn(oAuth2User);

            // when
            val userMemento = converter.convert(mockAuthentication);

            // then
            Assertions.assertThat(userMemento).isNotNull();
            Assertions.assertThat(userMemento.getRealName()).isNull();
        }

    }
}

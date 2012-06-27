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
package org.apache.isis.viewer.json.tck;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Objects;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;

import org.apache.isis.viewer.json.applib.HttpMethod;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.JsonRepresentation.LinksToSelf;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.links.LinkRepresentation;
import org.apache.isis.viewer.json.applib.links.Rel;

public class RepresentationMatchers {

    public static <T extends JsonRepresentation> Matcher<T> isMap() {
        return new TypeSafeMatcher<T>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("map");
            }

            @Override
            public boolean matchesSafely(final T item) {
                return item != null && item.isMap();
            }
        };
    }

    public static <T extends JsonRepresentation> Matcher<T> isArray() {
        return new TypeSafeMatcher<T>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("array");
            }

            @Override
            public boolean matchesSafely(final T item) {
                return item != null && item.isArray();
            }
        };
    }

    public static <T extends JsonRepresentation> Matcher<T> isString() {
        return new TypeSafeMatcher<T>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("string");
            }

            @Override
            public boolean matchesSafely(final T item) {
                return item != null && item.isValue() && item.asJsonNode().isTextual();
            }
        };
    }

    public static Matcher<LinkRepresentation> isLink(final HttpMethod httpMethod) {
        return new TypeSafeMatcher<LinkRepresentation>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("link with method " + httpMethod.name());
            }

            @Override
            public boolean matchesSafely(final LinkRepresentation item) {
                return item != null && item.getHttpMethod() == httpMethod;
            }
        };
    }

    public static <T extends JsonRepresentation> Matcher<T> hasFollowableLinkToSelf(final RestfulClient client) {
        return new TypeSafeMatcher<T>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("links to self");
            }

            @Override
            public boolean matchesSafely(final T item) {
                final LinksToSelf initialRepr = (LinksToSelf) item; // no easy
                                                                    // way to do
                                                                    // this with
                                                                    // Hamcrest
                // when
                try {
                    final RestfulResponse<T> followedResp = client.followT(initialRepr.getSelf());

                    // then
                    final T repr2 = followedResp.getEntity();
                    final LinksToSelf repr2AsLinksToSelf = (LinksToSelf) repr2;
                    return initialRepr.getSelf().equals(repr2AsLinksToSelf.getSelf());
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static <T extends JsonRepresentation> void assertThat(final T actual, final AbstractMatcherBuilder<T> matcherBuilder) {
        Assert.assertThat(actual, matcherBuilder.build());
    }

    public static LinkMatcherBuilder isLink(final RestfulClient client) {
        return new LinkMatcherBuilder(client);
    }

    public static LinkMatcherBuilder isLink() {
        return new LinkMatcherBuilder(null);
    }

    public static abstract class AbstractMatcherBuilder<T extends JsonRepresentation> {
        protected RestfulClient client;

        public AbstractMatcherBuilder() {
            this(null);
        }

        public AbstractMatcherBuilder(final RestfulClient client) {
            this.client = client;
        }

        public abstract Matcher<T> build();
    }

    public static class LinkMatcherBuilder extends AbstractMatcherBuilder<JsonRepresentation> {
        private HttpStatusCode statusCode;
        private HttpMethod httpMethod;
        private String rel;
        private String href;
        private Matcher<String> hrefMatcher;
        private Matcher<JsonRepresentation> valueMatcher;
        private Boolean novalue;
        private MediaType mediaType;
        private String typeParameterName;
        private String typeParameterValue;

        private LinkMatcherBuilder(final RestfulClient client) {
            super(client);
        }

        public LinkMatcherBuilder rel(final String rel) {
            this.rel = rel;
            return this;
        }

        public LinkMatcherBuilder rel(final Rel rel) {
            this.rel = rel.getName();
            return this;
        }

        public LinkMatcherBuilder href(final String href) {
            this.href = href;
            return this;
        }

        public LinkMatcherBuilder href(final Matcher<String> methodMatcher) {
            this.hrefMatcher = methodMatcher;
            return this;
        }

        public LinkMatcherBuilder httpMethod(final HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public LinkMatcherBuilder type(final MediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public LinkMatcherBuilder typeParameter(final String typeParameterName, final String typeParameterValue) {
            this.typeParameterName = typeParameterName;
            this.typeParameterValue = typeParameterValue;
            return this;
        }

        public LinkMatcherBuilder novalue() {
            if (valueMatcher != null) {
                throw new IllegalStateException("cannot assert on both there being a value and there not being a value");
            }
            this.novalue = true;
            return this;
        }

        public LinkMatcherBuilder value(final Matcher<JsonRepresentation> valueMatcher) {
            if (this.novalue != null) {
                throw new IllegalStateException("cannot assert on both there being a value and there not being a value");
            }
            this.valueMatcher = valueMatcher;
            return this;
        }

        public LinkMatcherBuilder returning(final HttpStatusCode statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        @Override
        public Matcher<JsonRepresentation> build() {
            return new TypeSafeMatcher<JsonRepresentation>() {

                @Override
                public void describeTo(final Description description) {
                    description.appendText("a link");
                    if (rel != null) {
                        description.appendText(" with rel '").appendText(rel).appendText("'");
                    }
                    if (href != null) {
                        description.appendText(" with href '").appendText(href).appendText("'");
                    }
                    if (hrefMatcher != null) {
                        description.appendText(" with href ");
                        hrefMatcher.describeTo(description);
                    }
                    if (httpMethod != null) {
                        description.appendText(" with method '").appendValue(httpMethod).appendText("'");
                    }
                    if (mediaType != null) {
                        description.appendText(" with type '").appendValue(mediaType).appendText("'");
                    }
                    if (typeParameterName != null) {
                        description.appendText(" with media type parameter '").appendText(typeParameterName).appendText("=").appendText(typeParameterValue).appendText("'");
                    }

                    if (novalue != null && novalue) {
                        description.appendText(" with no value");
                    }
                    if (valueMatcher != null) {
                        description.appendText(" with value ");
                        valueMatcher.describeTo(description);
                    }

                    // trigger link being followed
                    if (statusCode != null) {
                        if (client == null) {
                            throw new IllegalStateException("require client in order to assert on statusCode");
                        }
                        description.appendText(" that when followed returns status " + statusCode);
                    }

                    // assertions on response
                    if (statusCode != null) {
                        description.appendText(" returns ").appendValue(statusCode);
                    }
                }

                @Override
                public boolean matchesSafely(final JsonRepresentation linkRepr) {
                    if (linkRepr == null) {
                        return false;
                    }
                    final LinkRepresentation link = linkRepr.asLink();
                    if (rel != null && !rel.equals(link.getRel())) {
                        return false;
                    }
                    if (href != null && !href.equals(link.getHref())) {
                        return false;
                    }
                    if (hrefMatcher != null && !hrefMatcher.matches(link.getHref())) {
                        return false;
                    }
                    if (httpMethod != null && !httpMethod.equals(link.getHttpMethod())) {
                        return false;
                    }
                    if (mediaType != null && !mediaType.isCompatible(mediaType)) {
                        return false;
                    }
                    if (typeParameterName != null) {
                        final MediaType mediaType = link.getType();
                        final String parameterValue = mediaType.getParameters().get(typeParameterName);
                        if (!typeParameterValue.equals(parameterValue)) {
                            return false;
                        }
                    }
                    if (novalue != null && novalue && link.getValue() != null) {
                        return false;
                    }
                    if (valueMatcher != null && !valueMatcher.matches(link)) {
                        return false;
                    }

                    // follow link if criteria require it
                    RestfulResponse<JsonRepresentation> jsonResp = null;
                    if (statusCode != null) {
                        if (client == null) {
                            return false;
                        }
                        try {
                            jsonResp = client.followT(link);
                        } catch (final Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // assertions based on provided criteria
                    if (statusCode != null) {
                        if (jsonResp.getStatus() != statusCode) {
                            return false;
                        }
                    }

                    return true;
                }
            };
        }

    }

    public static EntryMatcherBuilder entry(final String key) {
        return new EntryMatcherBuilder(key);
    }

    public static class EntryMatcherBuilder extends AbstractMatcherBuilder<JsonRepresentation> {

        private final String key;
        private String value;

        private EntryMatcherBuilder(final String key) {
            this.key = key;
        }

        public EntryMatcherBuilder value(final String value) {
            this.value = value;
            return this;
        }

        @Override
        public Matcher<JsonRepresentation> build() {
            return new TypeSafeMatcher<JsonRepresentation>() {

                @Override
                public void describeTo(final Description description) {
                    description.appendText("map with entry with key: " + key);
                    if (value != null) {
                        description.appendText(", and value: " + value);
                    }
                }

                @Override
                public boolean matchesSafely(final JsonRepresentation item) {
                    if (!item.isMap()) {
                        return false;
                    }
                    final String val = item.getString(key);
                    if (val == null) {
                        return false;
                    }
                    if (value != null && !value.equals(val)) {
                        return false;
                    }
                    return true;
                }
            };
        }

    }

    public static Matcher<MediaType> hasType(final String type) {
        return new TypeSafeMatcher<MediaType>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("has type " + type);
            }

            @Override
            public boolean matchesSafely(final MediaType item) {
                return Objects.equal(type, item.getType());
            }
        };
    }

    public static Matcher<MediaType> hasSubType(final String subtype) {
        return new TypeSafeMatcher<MediaType>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("has subtype " + subtype);
            }

            @Override
            public boolean matchesSafely(final MediaType item) {
                return Objects.equal(subtype, item.getSubtype());
            }
        };
    }

    public static Matcher<MediaType> hasParameter(final String parameterName, final String parameterValue) {
        return new TypeSafeMatcher<MediaType>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText(String.format("has parameter '%s' with value '%s'", parameterName, parameterValue));
            }

            @Override
            public boolean matchesSafely(final MediaType item) {
                final String paramValue = item.getParameters().get(parameterName);
                return Objects.equal(paramValue, parameterValue);
            }
        };
    }

    public static Matcher<CacheControl> hasMaxAge(final int maxAge) {
        return new TypeSafeMatcher<CacheControl>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("has max age of " + maxAge + " secs");
            }

            @Override
            public boolean matchesSafely(final CacheControl item) {
                return maxAge == item.getMaxAge();
            }
        };
    }

}

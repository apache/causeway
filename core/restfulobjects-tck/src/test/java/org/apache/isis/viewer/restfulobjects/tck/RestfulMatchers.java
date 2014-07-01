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
package org.apache.isis.viewer.restfulobjects.tck;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import com.google.common.base.Objects;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation.HasLinkToSelf;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.Header;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;

public class RestfulMatchers {

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

    public static Matcher<LinkRepresentation> isLink(final RestfulHttpMethod httpMethod) {
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

    public static <T extends JsonRepresentation> Matcher<T> isFollowableLinkToSelf(final RestfulClient client) {
        return new TypeSafeMatcher<T>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("links to self");
            }

            @Override
            public boolean matchesSafely(final T item) {
                final HasLinkToSelf initialRepr = (HasLinkToSelf) item; // no easy
                                                                    // way to do
                                                                    // this with
                                                                    // Hamcrest
                // when
                try {
                    final RestfulResponse<T> followedResp = client.followT(initialRepr.getSelf());

                    // then
                    final T repr2 = followedResp.getEntity();
                    final HasLinkToSelf repr2AsLinksToSelf = (HasLinkToSelf) repr2;
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

    public static abstract class AbstractMatcherBuilder<T> {
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
        private RestfulHttpMethod httpMethod;
        private String rel;
        private String href;
        private Matcher<String> relNameMatcher;
        private Matcher<String> hrefMatcher;
        private Matcher<JsonRepresentation> valueMatcher;
        private Boolean novalue;
        private MediaType mediaType;
        private String typeParameterName;
        private String typeParameterValue;
        private String selfHref;
        private JsonRepresentation arguments;
        private String title;
        private Matcher<String> titleMatcher;

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

        public LinkMatcherBuilder rel(final Matcher<String> relNameMatcher) {
            this.relNameMatcher = relNameMatcher;
            return this;
        }

        public LinkMatcherBuilder href(final String href) {
            this.href = href;
            return this;
        }

        public LinkMatcherBuilder href(final Matcher<String> hrefMatcher) {
            this.hrefMatcher = hrefMatcher;
            return this;
        }

        public LinkMatcherBuilder httpMethod(final RestfulHttpMethod httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public LinkMatcherBuilder type(final MediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public LinkMatcherBuilder title(String title) {
            this.title = title;
            return this;
        }

        public LinkMatcherBuilder title(Matcher<String> titleMatcher) {
            this.titleMatcher = titleMatcher;
            return this;
        }
        
        public LinkMatcherBuilder typeParameter(final String typeParameterName, final String typeParameterValue) {
            this.typeParameterName = typeParameterName;
            this.typeParameterValue = typeParameterValue;
            return this;
        }

        public LinkMatcherBuilder arguments(JsonRepresentation arguments) {
            this.arguments = arguments;
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

        public LinkMatcherBuilder responseEntityWithSelfHref(String selfHref) {
            this.selfHref = selfHref;
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
                    if (relNameMatcher != null) {
                        description.appendText(" with rel '");
                        relNameMatcher.describeTo(description);
                    }
                    if (href != null) {
                        description.appendText(" with href '").appendText(href).appendText("'");
                    }
                    if (hrefMatcher != null) {
                        description.appendText(" with href ");
                        hrefMatcher.describeTo(description);
                    }
                    if (title != null) {
                        description.appendText(" with title '").appendText(title).appendText("'");
                    }
                    if (titleMatcher != null) {
                        description.appendText(" with title ");
                        titleMatcher.describeTo(description);
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

                    if (arguments != null) {
                        description.appendText(" with arguments").appendText(arguments.toString());
                    }

                    if (novalue != null && novalue) {
                        description.appendText(" with no value");
                    }
                    if (valueMatcher != null) {
                        description.appendText(" with value ");
                        valueMatcher.describeTo(description);
                    }

                    // trigger link being followed
                    if (statusCode != null || selfHref != null) {
                        if (client == null) {
                            throw new IllegalStateException("require client in order to assert on statusCode");
                        }
                        description.appendText(" that when followed");
                        if (statusCode != null) {
                            description.appendText(" returns status " + statusCode);
                        }
                        if (statusCode != null || selfHref != null) {
                            description.appendText(" and");
                        }
                        if (selfHref != null) {
                            description.appendText(" has a response whose self.href is " + selfHref);
                        }
                    }
                }

                @Override
                public boolean matchesSafely(final JsonRepresentation linkRepr) {
                    if (linkRepr == null) {
                        return false;
                    }
                    final LinkRepresentation link = linkRepr.asLink();
                    if (rel != null && !Rel.parse(rel).matches(link.getRel())) {
                        return false;
                    }
                    if (relNameMatcher != null && !relNameMatcher.matches(link.getRel())) {
                        return false;
                    }
                    if (href != null && !href.equals(link.getHref())) {
                        return false;
                    }
                    if (hrefMatcher != null && !hrefMatcher.matches(link.getHref())) {
                        return false;
                    }
                    if (title != null && !title.equals(link.getTitle())) {
                        return false;
                    }
                    if (titleMatcher != null && !titleMatcher.matches(link.getTitle())) {
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
                    if (arguments != null && !arguments.equals(link.getArguments())) {
                        return false;
                    }
                    if (valueMatcher != null && !valueMatcher.matches(link.getValue())) {
                        return false;
                    }

                    // follow link if criteria require it
                    RestfulResponse<JsonRepresentation> jsonResp = null;
                    if (statusCode != null || selfHref != null) {
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
                    try {
                        if (statusCode != null) {
                            if (jsonResp.getStatus() != statusCode) {
                                return false;
                            }
                        }
                        if (selfHref != null) {
                            JsonRepresentation entity;
                            try {
                                entity = jsonResp.getEntity();
                            } catch (Exception e) {
                                return false;
                            }
                            if(entity == null) {
                                return false;
                            }
                            LinkRepresentation selfLink = entity.getLink("links[rel=self]");
                            if(selfLink == null) {
                                return false;
                            }
                            if (!selfLink.getHref().equals(selfHref)) {
                                return false;
                            }
                        }
                        return true;
                    } finally {
                        // flush any pending response
                        try {
                            jsonResp.getEntity();
                        } catch (Exception e) {
                            // ignore
                        }
                    }
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

    public static Matcher<MediaType> hasMediaType(final String type) {
        return new TypeSafeMatcher<MediaType>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("has type " + type);
            }

            @Override
            public boolean matchesSafely(final MediaType item) {
                final String expectedType = item.getType();
                return expectedType != null && type.contains(expectedType);
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

    public static Matcher<? super JsonRepresentation> mapHas(final String key) {
        return new TypeSafeMatcher<JsonRepresentation>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("is a map with key '" + key + "'");
            }

            @Override
            protected boolean matchesSafely(JsonRepresentation item) {
                return item.mapHas(key);
            }
        };
    }

    public static Matcher<? super MediaType> hasMediaTypeProfile(final String expectedMediaTypeAndProfileStr) {
        final MediaType expectedMediaType = Header.CONTENT_TYPE.parse(expectedMediaTypeAndProfileStr);
        final String expectedProfileIfAny = expectedMediaType.getParameters().get("profile");
        return new TypeSafeMatcher<MediaType>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("is a media type 'application/json' with a profile parameter of '" +  expectedMediaTypeAndProfileStr + '"');
            }

            @Override
            protected boolean matchesSafely(MediaType item) {
                
                if (!item.isCompatible(expectedMediaType)) {
                    return false;
                }
                String actualProfileIfAny = item.getParameters().get("profile");
                return Objects.equal(expectedProfileIfAny, actualProfileIfAny);
            }
        };
    }

    public static Matcher<? super MediaType> hasMediaTypeXRoDomainType(final String expectedXRoDomainType) {
        return new TypeSafeMatcher<MediaType>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("is a media type with an 'x-ro-domain-type' parameter of '" +  expectedXRoDomainType + '"');
            }

            @Override
            protected boolean matchesSafely(MediaType item) {
                String actualXRoDomainTypeIfany = item.getParameters().get("x-ro-domain-type");
                return actualXRoDomainTypeIfany != null && actualXRoDomainTypeIfany.contains(expectedXRoDomainType);
            }
        };
    }

    public static Matcher<? super MediaType> hasMediaTypeXRoElementType(final String expectedXRoElementTypeIfAny) {
        return new TypeSafeMatcher<MediaType>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("is a media type with an 'x-ro-element-type' parameter of '" +  expectedXRoElementTypeIfAny + '"');
            }

            @Override
            protected boolean matchesSafely(MediaType item) {
                String actualXRoElementTypeIfany = item.getParameters().get("x-ro-element-type");
                return actualXRoElementTypeIfany != null && actualXRoElementTypeIfany.contains(expectedXRoElementTypeIfAny);
            }
        };
    }

    public static Matcher<? super RestfulResponse<?>> hasStatus(final HttpStatusCode statusCode) {
        return new TypeSafeMatcher<RestfulResponse<?>>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("has status code of " + statusCode);
            }

            @Override
            protected boolean matchesSafely(RestfulResponse<?> item) {
                return item.getStatus() == statusCode;
            }
        };
    }

    
    public static class CacheControlMatcherBuilder extends AbstractMatcherBuilder<CacheControl> {

        private Boolean noCache;

        @Override
        public Matcher<CacheControl> build() {
            return new TypeSafeMatcher<CacheControl>() {

                @Override
                public void describeTo(Description description) {
                    description.appendText("is a CacheControl header ");
                    if(noCache != null) {
                        description.appendText("with " + (noCache?"no":"") + " cache");
                    }
                }

                @Override
                protected boolean matchesSafely(CacheControl item) {
                    if(noCache != null) {
                        if(item.isNoCache() != noCache) {
                            return false;
                        }
                    }
                    return true;
                }
            };
        }

        public CacheControlMatcherBuilder withNoCache() {
            noCache = true;
            return this;
        }
        
    }
   
}

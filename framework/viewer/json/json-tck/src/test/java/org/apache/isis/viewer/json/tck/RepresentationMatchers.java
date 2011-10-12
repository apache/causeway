package org.apache.isis.viewer.json.tck;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.JsonRepresentation.LinksToSelf;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.apache.isis.viewer.json.tck.RepresentationMatchers.AbstractMatcherBuilder;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;

import com.google.common.base.Objects;


public class RepresentationMatchers {

    public static <T extends JsonRepresentation> Matcher<T> isMap() {
        return new TypeSafeMatcher<T>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("map");
            }

            @Override
            public boolean matchesSafely(T item) {
                return item != null && item.isMap();
            }
        };
    }

    public static <T extends JsonRepresentation> Matcher<T> isArray() {
        return new TypeSafeMatcher<T>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("array");
            }

            @Override
            public boolean matchesSafely(T item) {
                return item != null && item.isArray();
            }
        };
    }

    public static <T extends JsonRepresentation> Matcher<T> isString() {
        return new TypeSafeMatcher<T>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("string");
            }

            @Override
            public boolean matchesSafely(T item) {
                return item != null && item.isValue() && item.asJsonNode().isTextual();
            }
        };
    }

    public static Matcher<Link> isLink(final Method method) {
        return new TypeSafeMatcher<Link>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("link with method " + method.name());
            }

            @Override
            public boolean matchesSafely(Link item) {
                return item != null && item.getMethod() == method;
            }
        };
    }

    public static <T extends JsonRepresentation> Matcher<T> isFollowableLinkToSelf(final RestfulClient client) {
        return new TypeSafeMatcher<T>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("links to self");
            }

            @Override
            public boolean matchesSafely(T item) {
                LinksToSelf initialRepr = (LinksToSelf) item; // no easy way to do this with Hamcrest
                // when
                Response servicesResp;
                try {
                    servicesResp = client.follow(initialRepr.getSelf());
                    RestfulResponse<T> followedResp = RestfulResponse.ofT(servicesResp);
                    
                    // then
                    T repr2 = followedResp.getEntity();
                    LinksToSelf repr2AsLinksToSelf = (LinksToSelf)repr2;
                    return initialRepr.getSelf().equals(repr2AsLinksToSelf.getSelf());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }


    public static <T extends JsonRepresentation> void assertThat(T actual, AbstractMatcherBuilder<T> matcherBuilder) {
        Assert.assertThat(actual, matcherBuilder.build());
    }

    public static LinkMatcherBuilder isLink(RestfulClient client) {
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

        public AbstractMatcherBuilder(RestfulClient client) {
            this.client = client;
        }
        
        public abstract Matcher<T> build();
    }

    public static class LinkMatcherBuilder extends AbstractMatcherBuilder<JsonRepresentation> {
        private HttpStatusCode statusCode;
        private Method method;
        private String rel;
        private String href;
        private Matcher<String> hrefMatcher;
        private Matcher<JsonRepresentation> valueMatcher;
        private Boolean novalue;

        private LinkMatcherBuilder(RestfulClient client) {
            super(client);
        }

        public LinkMatcherBuilder rel(String rel) {
            this.rel = rel;
            return this;
        }

        public LinkMatcherBuilder href(String href) {
            this.href = href;
            return this;
        }

        public LinkMatcherBuilder href(Matcher<String> methodMatcher) {
            this.hrefMatcher = methodMatcher;
            return this;
        }

        public LinkMatcherBuilder method(Method method) {
            this.method = method;
            return this;
        }

        public LinkMatcherBuilder novalue() {
            if(valueMatcher != null) {
                throw new IllegalStateException("cannot assert on both there being a value and there not being a value");
            }
            this.novalue = true;
            return this;
        }

        public LinkMatcherBuilder value(Matcher<JsonRepresentation> valueMatcher) {
            if(this.novalue != null) {
                throw new IllegalStateException("cannot assert on both there being a value and there not being a value");
            }
            this.valueMatcher = valueMatcher;
            return this;
        }

        public LinkMatcherBuilder returning(HttpStatusCode statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        @Override
        public Matcher<JsonRepresentation> build() {
            return new TypeSafeMatcher<JsonRepresentation>() {

                @Override
                public void describeTo(Description description) {
                    description.appendText("a link");
                    if(rel != null) {
                        description.appendText(" with rel ").appendText(rel);
                    }
                    if(href != null) {
                        description.appendText(" with href ").appendText(href);
                    }
                    if(hrefMatcher != null) {
                        description.appendText(" with href ");
                        hrefMatcher.describeTo(description);
                    }
                    if(method != null) {
                        description.appendText(" with method ").appendValue(method);
                    }
                    if(novalue) {
                        description.appendText(" with no value");
                    }
                    if(valueMatcher != null) {
                        description.appendText(" with value ");
                        valueMatcher.describeTo(description);
                    }
                    
                    // trigger link being followed
                    if(statusCode != null) {
                        if(client == null) {
                            throw new IllegalStateException("require client in order to assert on statusCode");
                        }
                        description.appendText(" that when followed returns status " + statusCode);
                    }
                    
                    // assertions on response
                    if(statusCode != null) {
                        description.appendText(" returns ").appendValue(statusCode);
                    }
                }

                @Override
                public boolean matchesSafely(JsonRepresentation linkRepr) {
                    if(linkRepr == null) {
                        return false;
                    }
                    Link link = linkRepr.asLink();
                    if(rel != null && !rel.equals(link.getRel())) {
                        return false;
                    }
                    if(href != null && !href.equals(link.getHref())) {
                        return false;
                    }
                    if(hrefMatcher != null && !hrefMatcher.matches(link.getHref())) {
                        return false;
                    }
                    if(method != null && !method.equals(link.getMethod())) {
                        return false;
                    }
                    if(novalue !=null && novalue && link.getValue() != null) {
                        return false;
                    }
                    if(valueMatcher != null && !valueMatcher.matches(link)) {
                        return false;
                    }

                    // follow link if criteria require it
                    Response linkedResp = null;
                    if(statusCode != null) {
                        if(client == null) {
                            return false;
                        }
                        try {
                            linkedResp = client.follow(link);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // assertions based on provided criteria
                    if(statusCode != null) {
                        RestfulResponse<JsonRepresentation> jsonResp = RestfulResponse.of(linkedResp);
                        if(jsonResp.getStatus() != statusCode) {
                            return false;
                        }
                    }
                    
                    return true;
                }
            };
        }
    }

    public static EntryMatcherBuilder entry(String key) {
        return new EntryMatcherBuilder(key); 
    }

    public static class EntryMatcherBuilder extends AbstractMatcherBuilder<JsonRepresentation>  {

        private final String key;
        private String value;
        private EntryMatcherBuilder(String key) {
            this.key = key;
        }

        public EntryMatcherBuilder value(String value) {
            this.value = value;
            return this;
        }

        @Override
        public Matcher<JsonRepresentation> build() {
            return new TypeSafeMatcher<JsonRepresentation>() {

                @Override
                public void describeTo(Description description) {
                    description.appendText("map with entry with key: " + key);
                    if(value != null) {
                        description.appendText(", and value: " + value);
                    }
                }

                @Override
                public boolean matchesSafely(JsonRepresentation item) {
                    if(!item.isMap()) {
                        return false;
                    }
                    String val = item.getString(key);
                    if(val == null) {
                        return false;
                    }
                    if(value != null && !value.equals(val)) {
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
            public void describeTo(Description description) {
                description.appendText("has type " + type);
            }

            @Override
            public boolean matchesSafely(MediaType item) {
                return Objects.equal(type, item.getType());
            }
        };
    }

    public static Matcher<MediaType> hasSubType(final String subtype) {
        return new TypeSafeMatcher<MediaType>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("has subtype " + subtype);
            }

            @Override
            public boolean matchesSafely(MediaType item) {
                return Objects.equal(subtype, item.getSubtype());
            }
        };
    }

    public static Matcher<MediaType> hasParameter(final String parameterName, final String parameterValue) {
        return new TypeSafeMatcher<MediaType>() {

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("has parameter '%s' with value '%s'", parameterName, parameterValue));
            }

            @Override
            public boolean matchesSafely(MediaType item) {
                final String paramValue = item.getParameters().get(parameterName);
                return Objects.equal(paramValue, parameterValue);
            }
        };
    }

    public static Matcher<CacheControl> hasMaxAge(final int maxAge) {
        return new TypeSafeMatcher<CacheControl>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("has max age of " + maxAge + " secs");
            }

            @Override
            public boolean matchesSafely(CacheControl item) {
                return maxAge == item.getMaxAge();
            }
        };
    }

    
}
    
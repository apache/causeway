package org.apache.isis.viewer.json.tck;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.JsonRepresentation.LinksToSelf;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;


public class RepresentationMatchers {

    public static <T extends JsonRepresentation> T entityOf(Response resp, Class<T> representationType) throws JsonParseException, JsonMappingException, IOException {
        RestfulResponse<T> jsonResp = RestfulResponse.of(resp, representationType);
        return jsonResp.getEntity();
    }


//    public static Matcher<String> matchers(final Method method) {
//        return new TypeSafeMatcher<Link>() {
//
//            @Override
//            public void describeTo(Description description) {
//                description.appendText("link with method " + method.name());
//            }
//
//            @Override
//            public boolean matchesSafely(Link item) {
//                return item != null && item.getMethod() == method;
//            }
//        };
//    }


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
                return item != null && item.isValue() && item.getJsonNode().isTextual();
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
                    RestfulResponse<T> followedResp = RestfulResponse.of(servicesResp, asT(item));
                    
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

    @SuppressWarnings("unchecked")
    private static <T> Class<T> asT(T initialRepr) {
        return (Class<T>) initialRepr.getClass();
    }
    

    public static <T> void assertThat(T actual, AbstractMatcherBuilder<T> matcherBuilder) {
        Assert.assertThat(actual, matcherBuilder.build());
    }

    public static LinkMatcherBuilder isLink(RestfulClient client) {
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

        public AbstractMatcherBuilder(RestfulClient client) {
            this.client = client;
        }
        
        public abstract Matcher<T> build();
    }

    public static class LinkMatcherBuilder extends AbstractMatcherBuilder<Link> {
        private HttpStatusCode statusCode;
        private Method method;
        private String rel;
        private String href;
        private Matcher<String> hrefMatcher;

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


        public LinkMatcherBuilder returning(HttpStatusCode statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        @Override
        public Matcher<Link> build() {
            
            return new TypeSafeMatcher<Link>() {

                @Override
                public void describeTo(Description description) {
                    description.appendText("a link ");
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
                    
                    // trigger link being followed
                    if(statusCode != null) {
                        if(client == null) {
                            description.appendText(" !!! provide client in matcher's constructor !!!");
                        }
                        description.appendText(" that when followed");
                    }
                    
                    // assertions on response
                    if(statusCode != null) {
                        description.appendText(" returns ").appendValue(statusCode);
                    }
                }

                @Override
                public boolean matchesSafely(Link link) {
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
                        RestfulResponse<JsonRepresentation> jsonResp = RestfulResponse.of(linkedResp, JsonRepresentation.class);
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

}
    
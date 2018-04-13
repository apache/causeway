package org.apache.isis.core.metamodel.facets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;

import static org.hamcrest.CoreMatchers.is;

@SuppressWarnings("unused")
public class Annotations_getAnnotations_on_Field_Test {


    @Property(publishing = Publishing.ENABLED)
    @Inherited
    @Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Published {
    }

    @Property(publishing = Publishing.DISABLED)
    @Inherited
    @Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface NotPublished {
    }

    @Published
    @Inherited
    @Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface MetaPublished {
    }

    @Test
    public void direct() throws Exception {

        class SomeDomainObject {
            @Property(publishing = Publishing.ENABLED)
            private String name;

            public String getName() {
                return name;
            }
        }

        Method method = SomeDomainObject.class.getMethod("getName");
        final List<Property> annotations = Annotations.getAnnotations(method, Property.class);

        Assert.assertThat(annotations.size(), is(1));

        Assert.assertThat(annotations.get(0).publishing(), is(Publishing.ENABLED));
    }

    @Test
    public void meta() throws Exception {

        class SomeDomainObject {
            @Published
            private String name;
            public String getName() {
                return name;
            }
        }

        Method method = SomeDomainObject.class.getMethod("getName");
        final List<Property> annotations = Annotations.getAnnotations(method, Property.class);

        Assert.assertThat(annotations.size(), is(1));

        Assert.assertThat(annotations.get(0).publishing(), is(Publishing.ENABLED));
    }

    @Test
    public void metaMeta() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            private String name;
            public String getName() {
                return name;
            }
        }

        Method method = SomeDomainObject.class.getMethod("getName");
        final List<Property> annotations = Annotations.getAnnotations(method, Property.class);

        Assert.assertThat(annotations.size(), is(1));

        Assert.assertThat(annotations.get(0).publishing(), is(Publishing.ENABLED));
    }

    @Test
    public void meta_and_metaMeta() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            @Published
            private String name;
            public String getName() {
                return name;
            }
        }

        Method method = SomeDomainObject.class.getMethod("getName");
        final List<Property> annotations = Annotations.getAnnotations(method, Property.class);

        Assert.assertThat(annotations.size(), is(2));

        Assert.assertThat(annotations.get(0).publishing(), is(Publishing.ENABLED));
        Assert.assertThat(annotations.get(1).publishing(), is(Publishing.ENABLED));
    }

    @Test
    public void meta_overrides_metaMeta() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            @NotPublished
            private String name;
            public String getName() {
                return name;
            }
        }

        Method method = SomeDomainObject.class.getMethod("getName");
        final List<Property> annotations = Annotations.getAnnotations(method, Property.class);

        Assert.assertThat(annotations.size(), is(2));

        Assert.assertThat(annotations.get(0).publishing(), is(Publishing.DISABLED));
        Assert.assertThat(annotations.get(1).publishing(), is(Publishing.ENABLED));
    }

    @Test
    public void direct_overrides_metaMeta() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            @Published
            @Property(publishing = Publishing.DISABLED)
            private String name;
            public String getName() {
                return name;
            }
        }

        Method method = SomeDomainObject.class.getMethod("getName");
        final List<Property> annotations = Annotations.getAnnotations(method, Property.class);

        Assert.assertThat(annotations.size(), is(3));

        Assert.assertThat(annotations.get(0).publishing(), is(Publishing.DISABLED));
        Assert.assertThat(annotations.get(1).publishing(), is(Publishing.ENABLED));
        Assert.assertThat(annotations.get(2).publishing(), is(Publishing.ENABLED));
    }


    @Test
    public void direct_overrides_metaMeta_2() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            @NotPublished
            @Property(publishing = Publishing.ENABLED)
            private String name;
            public String getName() {
                return name;
            }
        }

        Method method = SomeDomainObject.class.getMethod("getName");
        final List<Property> annotations = Annotations.getAnnotations(method, Property.class);

        Assert.assertThat(annotations.size(), is(3));

        Assert.assertThat(annotations.get(0).publishing(), is(Publishing.ENABLED));
        Assert.assertThat(annotations.get(1).publishing(), is(Publishing.DISABLED));
        Assert.assertThat(annotations.get(2).publishing(), is(Publishing.ENABLED));
    }

}
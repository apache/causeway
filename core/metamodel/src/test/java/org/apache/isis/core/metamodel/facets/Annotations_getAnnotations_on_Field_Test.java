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

import org.apache.isis.applib.annotation.Meta;

import static org.hamcrest.CoreMatchers.is;

public class Annotations_getAnnotations_on_Method_Test {


    @Inherited
    @Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DomainObj { // cf @DomainObject
        enum Publishng { // cf Publishing enum
            YES,
            NO,
            NOT_SPECIFIED
        }
        Publishng publishng() default Publishng.NOT_SPECIFIED;
    }

    @Meta
    @DomainObj(publishng = DomainObj.Publishng.YES)
    @Inherited
    @Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Published {
    }

    @Meta
    @DomainObj(publishng = DomainObj.Publishng.NO)
    @Inherited
    @Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface NotPublished {
    }

    @Meta
    @Published
    @Inherited
    @Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface MetaPublished {
    }

    @Test
    public void direct() throws Exception {

        class SomeDomainObject {
            @DomainObj(publishng = DomainObj.Publishng.YES)
            public void updateName(String name) {}
        }

        Method method = SomeDomainObject.class.getMethod("updateName", String.class);
        final List<DomainObj> annotations = Annotations.getAnnotations(method, DomainObj.class);

        Assert.assertThat(annotations.size(), is(1));

        Assert.assertThat(annotations.get(0).publishng(), is(DomainObj.Publishng.YES));
    }

    @Test
    public void meta() throws Exception {

        class SomeDomainObject {
            @Published
            public void updateName(String name) {}
        }

        Method method = SomeDomainObject.class.getMethod("updateName", String.class);
        final List<DomainObj> annotations = Annotations.getAnnotations(method, DomainObj.class);

        Assert.assertThat(annotations.size(), is(1));

        Assert.assertThat(annotations.get(0).publishng(), is(DomainObj.Publishng.YES));
    }

    @Test
    public void metaMeta() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            public void updateName(String name) {}
        }

        Method method = SomeDomainObject.class.getMethod("updateName", String.class);
        final List<DomainObj> annotations = Annotations.getAnnotations(method, DomainObj.class);

        Assert.assertThat(annotations.size(), is(1));

        Assert.assertThat(annotations.get(0).publishng(), is(DomainObj.Publishng.YES));
    }

    @Test
    public void meta_and_metaMeta() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            @Published
            public void updateName(String name) {}
        }

        Method method = SomeDomainObject.class.getMethod("updateName", String.class);
        final List<DomainObj> annotations = Annotations.getAnnotations(method, DomainObj.class);

        Assert.assertThat(annotations.size(), is(2));

        Assert.assertThat(annotations.get(0).publishng(), is(DomainObj.Publishng.YES));
        Assert.assertThat(annotations.get(1).publishng(), is(DomainObj.Publishng.YES));
    }

    @Test
    public void meta_overrides_metaMeta() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            @NotPublished
            public void updateName(String name) {}
        }

        Method method = SomeDomainObject.class.getMethod("updateName", String.class);
        final List<DomainObj> annotations = Annotations.getAnnotations(method, DomainObj.class);

        Assert.assertThat(annotations.size(), is(2));

        Assert.assertThat(annotations.get(0).publishng(), is(DomainObj.Publishng.NO));
        Assert.assertThat(annotations.get(1).publishng(), is(DomainObj.Publishng.YES));
    }

    @Test
    public void direct_overrides_metaMeta() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            @Published
            @DomainObj(publishng = DomainObj.Publishng.NO)
            public void updateName(String name) {}
        }

        Method method = SomeDomainObject.class.getMethod("updateName", String.class);
        final List<DomainObj> annotations = Annotations.getAnnotations(method, DomainObj.class);

        Assert.assertThat(annotations.size(), is(3));

        Assert.assertThat(annotations.get(0).publishng(), is(DomainObj.Publishng.NO));
        Assert.assertThat(annotations.get(1).publishng(), is(DomainObj.Publishng.YES));
        Assert.assertThat(annotations.get(2).publishng(), is(DomainObj.Publishng.YES));
    }


    @Test
    public void direct_overrides_metaMeta_2() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            @NotPublished
            @DomainObj(publishng = DomainObj.Publishng.YES)
            public void updateName(String name) {}
        }

        Method method = SomeDomainObject.class.getMethod("updateName", String.class);
        final List<DomainObj> annotations = Annotations.getAnnotations(method, DomainObj.class);

        Assert.assertThat(annotations.size(), is(3));

        Assert.assertThat(annotations.get(0).publishng(), is(DomainObj.Publishng.YES));
        Assert.assertThat(annotations.get(1).publishng(), is(DomainObj.Publishng.NO));
        Assert.assertThat(annotations.get(2).publishng(), is(DomainObj.Publishng.YES));
    }

}
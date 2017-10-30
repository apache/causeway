package org.apache.isis.core.metamodel.facets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.apache.isis.applib.annotation.Meta;

import static org.hamcrest.CoreMatchers.is;

public class Annotations_getAnnotations_on_Class_Test {


    @Inherited
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
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
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Published {
    }

    @Meta
    @DomainObj(publishng = DomainObj.Publishng.NO)
    @Inherited
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface NotPublished {
    }

    @Meta
    @Published
    @Inherited
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface MetaPublished {
    }

    @Test
    public void direct() throws Exception {

        @DomainObj(publishng = DomainObj.Publishng.YES)
        class SomeDomainObject {}

        final List<DomainObj> annotations = Annotations
                .getAnnotations(SomeDomainObject.class, DomainObj.class);

        Assert.assertThat(annotations.size(), is(1));

        Assert.assertThat(annotations.get(0).publishng(), is(DomainObj.Publishng.YES));
    }

    @Test
    public void meta() throws Exception {

        @Published
        class SomeDomainObject {}

        final List<DomainObj> annotations = Annotations
                .getAnnotations(SomeDomainObject.class, DomainObj.class);

        Assert.assertThat(annotations.size(), is(1));

        Assert.assertThat(annotations.get(0).publishng(), is(DomainObj.Publishng.YES));
    }

    @Test
    public void metaMeta() throws Exception {

        @MetaPublished
        class SomeDomainObject {}

        final List<DomainObj> annotations = Annotations
                .getAnnotations(SomeDomainObject.class, DomainObj.class);

        Assert.assertThat(annotations.size(), is(1));

        Assert.assertThat(annotations.get(0).publishng(), is(DomainObj.Publishng.YES));
    }

    @Test
    public void meta_and_metaMeta() throws Exception {

        @MetaPublished
        @Published
        class SomeDomainObject {}

        final List<DomainObj> annotations = Annotations
                .getAnnotations(SomeDomainObject.class, DomainObj.class);

        Assert.assertThat(annotations.size(), is(2));

        Assert.assertThat(annotations.get(0).publishng(), is(DomainObj.Publishng.YES));
        Assert.assertThat(annotations.get(1).publishng(), is(DomainObj.Publishng.YES));
    }

    @Test
    public void meta_overrides_metaMeta() throws Exception {

        @MetaPublished
        @NotPublished
        class SomeDomainObject {}

        final List<DomainObj> annotations = Annotations
                .getAnnotations(SomeDomainObject.class, DomainObj.class);

        Assert.assertThat(annotations.size(), is(2));

        Assert.assertThat(annotations.get(0).publishng(), is(DomainObj.Publishng.NO));
        Assert.assertThat(annotations.get(1).publishng(), is(DomainObj.Publishng.YES));
    }

    @Test
    public void direct_overrides_metaMeta() throws Exception {

        @MetaPublished
        @Published
        @DomainObj(publishng = DomainObj.Publishng.NO)
        class SomeDomainObject {}

        final List<DomainObj> annotations = Annotations
                .getAnnotations(SomeDomainObject.class, DomainObj.class);

        Assert.assertThat(annotations.size(), is(3));

        Assert.assertThat(annotations.get(0).publishng(), is(DomainObj.Publishng.NO));
        Assert.assertThat(annotations.get(1).publishng(), is(DomainObj.Publishng.YES));
        Assert.assertThat(annotations.get(2).publishng(), is(DomainObj.Publishng.YES));
    }


    @Test
    public void direct_overrides_metaMeta_2() throws Exception {

        @MetaPublished
        @NotPublished
        @DomainObj(publishng = DomainObj.Publishng.YES)
        class SomeDomainObject {}

        final List<DomainObj> annotations = Annotations
                .getAnnotations(SomeDomainObject.class, DomainObj.class);

        Assert.assertThat(annotations.size(), is(3));

        Assert.assertThat(annotations.get(0).publishng(), is(DomainObj.Publishng.YES));
        Assert.assertThat(annotations.get(1).publishng(), is(DomainObj.Publishng.NO));
        Assert.assertThat(annotations.get(2).publishng(), is(DomainObj.Publishng.YES));
    }

}
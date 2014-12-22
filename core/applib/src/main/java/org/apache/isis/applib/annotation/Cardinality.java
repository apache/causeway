package org.apache.isis.applib.annotation;

/**
 * Whether the property or parameter is mandatory or not.
 */
public enum Cardinality {
    /**
     * Default, usually mandatory (and always so for parameters) unless JDO {@link javax.jdo.annotations.Column}
     * has also specified with {@link javax.jdo.annotations.Column#allowsNull()} set to <code>true</code>.
     */
    DEFAULT,
    /**
     * Indicates that the property or parameter is not mandatory.
     */
    OPTIONAL,
    /**
     * Indicates that the property is mandatory (even if the JDO {@link javax.jdo.annotations.Column} annotation
     * says otherwise).
     *
     * <p>
     * When using the JDO/DataNucleus objectstore, it is sometimes necessary to annotate a property as optional
     * (using {@link javax.jdo.annotations.Column#allowsNull()} set to <code>true</code>), even if the property is
     * logically mandatory.  For example, this can occur when the property is in a subtype class that has been
     * "rolled up" to the superclass table using {@link javax.jdo.annotations.Inheritance} with the
     * {@link javax.jdo.annotations.InheritanceStrategy#SUPERCLASS_TABLE superclass}<tt> strategy.
     * </p>
     *
     * <p>
     * This annotation, therefore, is intended to override any objectstore-specific
     * annotation, so that Isis can apply the constraint even though the objectstore
     * is unable to do so.
     * </p>
     */
    MANDATORY
}

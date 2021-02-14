package org.apache.isis.applib.services.swagger;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.RestrictTo;

/**
 * Specifies which elements of the metamodel are included within the generated
 * swagger spec.
 *
 * @since 1.x {@index}
 */
public enum Visibility {

    /**
     * Specification for use by third-party clients, ie public use and not
     * under the control of the authors of the backend Apache Isis application.
     *
     * <p>
     * The generated swagger spec is therefore restricted only to include only
     * view models ({@link DomainObject#nature()} of
     * {@link org.apache.isis.applib.annotation.Nature#VIEW_MODEL})
     * and to REST domain services ({@link DomainService#nature()} of
     * {@link NatureOfService#REST}). Exposing entities also would couple the
     * REST client too deeply to the backend implementation.
     * </p>
     */
    PUBLIC,
    /**
     * Specification for use only by internally-managed clients, ie private
     * internal use.
     *
     * <p>
     * This visibility level removes all constraints and so includes the
     * specifications of domain entities as well as view models. This is
     * perfectly acceptable where the team developing the REST client is the
     * same as the team developing the backend service ... the use of the REST
     * API between the client and server is a private implementation detail of
     * the application.
     * </p>
     */
    PRIVATE,
    /**
     * As {@link #PRIVATE}, also including any prototype actions (where
     * {@link Action#restrictTo()} set to {@link RestrictTo#PROTOTYPING}).
     */
    PRIVATE_WITH_PROTOTYPING;

    public boolean isPublic() {
        return this == PUBLIC;
    }

    public boolean isPrivate() {
        return this == PRIVATE;
    }

    public boolean isPrivateWithPrototyping() {
        return this == PRIVATE_WITH_PROTOTYPING;
    }
}

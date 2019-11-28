package org.apache.isis.extensions.base.dom;

import org.apache.isis.extensions.base.dom.utils.StringUtils;

/**
 * An enum that implements {@link Titled} and moreover its {@link #title()}
 * is derived according to the {@link StringUtils#enumTitle(String)} algorithm.
 */
public interface TitledEnum extends Titled {
    
}

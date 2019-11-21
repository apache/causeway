package org.incode.module.base.dom;

import org.incode.module.base.dom.utils.StringUtils;

/**
 * An enum that implements {@link Titled} and moreover its {@link #title()}
 * is derived according to the {@link StringUtils#enumTitle(String)} algorithm.
 */
public interface TitledEnum extends Titled {
    
}

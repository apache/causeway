package org.apache.isis.subdomains.base.applib;

import org.apache.isis.subdomains.base.applib.utils.StringUtils;

/**
 * An enum that implements {@link Titled} and moreover its {@link #title()}
 * is derived according to the {@link StringUtils#enumTitle(String)} algorithm.
 */
public interface TitledEnum extends Titled {
    
}

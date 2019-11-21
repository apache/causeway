package org.isisaddons.module.excel.dom.util;

/**
 * Created by jodo on 31/03/17.
 */
public enum Mode {
    /**
     * All cells must be well formed and with valid data
     */
    STRICT,
    /**
     * Ignore any cells that cannot be interpreted.
     */
    RELAXED
}

/**
 * 
 */
package org.apache.isis.viewer.restful.viewer.xom;

import nu.xom.Element;


public class DtDd {
    final Element dt;
    final Element dd;

    DtDd(final Element dt, final Element dd) {
        this.dt = dt;
        this.dd = dd;
    }

    public void appendTo(final Element dl) {
        dl.appendChild(dt);
        dl.appendChild(dd);
    }
}

package org.apache.isis.subdomains.base.applib.with;

import java.math.BigInteger;

public interface WithSequence {

    public BigInteger getSequence();
    public void setSequence(BigInteger sequence);
}

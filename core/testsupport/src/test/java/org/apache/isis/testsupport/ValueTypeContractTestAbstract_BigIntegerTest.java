package org.apache.isis.testsupport;

import java.math.BigInteger;

public class ValueTypeContractTestAbstract_BigIntegerTest extends ValueTypeContractTestAbstract {

	@Override
	protected Object getObject() {
		return new BigInteger("1");
	}

	@Override
	protected Object getObjectWithSameValue() {
		return new BigInteger("1");
	}

	@Override
	protected Object getAnotherObjectWithSameValue() {
		return new BigInteger("1");
	}

	@Override
	protected Object getObjectWithDifferentValue() {
		return new BigInteger("2");
	}

}

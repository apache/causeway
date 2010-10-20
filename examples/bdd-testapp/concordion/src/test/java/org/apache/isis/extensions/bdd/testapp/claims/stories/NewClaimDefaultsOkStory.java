package org.apache.isis.extensions.bdd.testapp.claims.stories;

import org.apache.isis.extensions.bdd.concordion.AbstractNakedObjectsConcordionTest;
import org.apache.isis.extensions.bdd.testapp.claims.CustomCssPackage;


public class NewClaimDefaultsOkStory extends AbstractNakedObjectsConcordionTest {


	@Override
	protected Class<?> customCssPackage() {
		return CustomCssPackage.class;
	}
	
}

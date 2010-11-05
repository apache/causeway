package org.apache.isis.support.prototype.stories;

import org.apache.isis.support.prototype.CustomCssPackage;
import org.apache.isis.viewer.bdd.concordion.AbstractIsisConcordionTest;


public class NewClaimDefaultsOkStory extends AbstractIsisConcordionTest {


	@Override
	protected Class<?> customCssPackage() {
		return CustomCssPackage.class;
	}
	
}

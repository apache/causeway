package org.apache.isis.core.metamodel.facets.object.navparent.annotation;

import org.apache.isis.applib.annotation.Navigable;
import org.apache.isis.applib.annotation.PropertyLayout;

class NavigableParentTestSamples {

	// has no navigable parent
	protected static class DomainObjectRoot {

		@Override
		public String toString() {
			return "Root";
		}

	}

	// has navigable parent 'Root' specified via Annotation
	protected static class DomainObjectA {
		
		private final static Object myParent = new DomainObjectRoot();

		@Override
		public String toString() {
			return "A";
		}

		@PropertyLayout(navigable=Navigable.PARENT)
		public Object root() {
			return myParent;
		}
		
	}
	
	// has navigable parent 'A' specified via method
	protected static class DomainObjectB {
		
		private final static Object myParent = new DomainObjectA();

		@Override
		public String toString() {
			return "B";
		}

		public Object parent() {
			return myParent;
		}
		
	}
}

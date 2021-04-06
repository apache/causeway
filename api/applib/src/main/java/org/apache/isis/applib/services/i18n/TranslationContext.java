package org.apache.isis.applib.services.i18n;

import java.lang.reflect.Method;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.exceptions.TranslatableException;
import org.apache.isis.applib.spec.AbstractSpecification;
import org.apache.isis.applib.spec.Specification;

public interface TranslationContext {

	String stringify();
	
	static class DefaultTranslationContext implements TranslationContext {
		final String ctx;
		DefaultTranslationContext(String ctx) {
			this.ctx = ctx;
		}
		@Override
		public String stringify() {
			return ctx;
		}		
	}
	
	static TranslationContext ofSimpleStringIdentifier(String simple) {
		return new DefaultTranslationContext(simple);
	}	
	
	static TranslationContext ofClass(Class<?> contextClass) {
		return new DefaultTranslationContext(contextClass.getName());
	}
	
	static TranslationContext ofClassAndMethodName(Class<?> contextClass, String methodName) {
		return new DefaultTranslationContext(contextClass.getName() + "#" + methodName);
	}	
	
	static TranslationContext ofTrEx(TranslatableException translatableException) {
		return new DefaultTranslationContext(translatableException.getTranslationContext());
	}

	static TranslationContext ofIdentifier(Identifier identifier) {
		return new DefaultTranslationContext(identifier.getTranslationContext());
	}

	static TranslationContext ofIdentifierFullIdentity(Identifier identifier) {
		return new DefaultTranslationContext(identifier.getFullIdentityString());
	}

	static TranslationContext ofTitleMethod(Method titleMethod) {
		return new DefaultTranslationContext(titleMethod.getDeclaringClass().getName() + "#" + titleMethod.getName() + "()");
	}

	static TranslationContext ofEnum(Enum<?> objectAsEnum) {
		return new DefaultTranslationContext(objectAsEnum.getClass().getName() + "#" + objectAsEnum.name());
	}

	static TranslationContext ofDisabledObjectMethod(Method disabledObjectMethod) {
		return new DefaultTranslationContext(disabledObjectMethod.getDeclaringClass().getName() + "#" + disabledObjectMethod.getName() + "()");
	}

	static TranslationContext ofIdentifierForTab(Identifier identifier) {
		return new DefaultTranslationContext(identifier.getTranslationContext() + "~tabName");
	}
	
	static TranslationContext ofIdentifierForMemberOrderName(Identifier identifier) {
		return new DefaultTranslationContext(identifier.getTranslationContext() + "~memberOrderName");
	}	

	static TranslationContext ofClassForMemberOrderName(Class<?> class1) {
		return new DefaultTranslationContext(class1.getName() + "~memberOrderName");
	}
}

package org.apache.isis.viewer.wicket.model.models;

import java.util.Optional;

import org.apache.isis.applib.internal.base._Reduction;

class Util {

	final static class LowestCommonSuperclassFinder {

		private final _Reduction<Class<?>> reduction = _Reduction.of((common, next) -> {
			Class<?> refine = common;
			while(!refine.isAssignableFrom(next)) {
				refine = refine.getSuperclass();
			}
			return refine;
		});

		public void collect(Object pojo) {
			reduction.accept(pojo.getClass());
		}

		public Optional<Class<?>> getLowestCommonSuperclass() {
			return reduction.getResult();
		}
	}

}

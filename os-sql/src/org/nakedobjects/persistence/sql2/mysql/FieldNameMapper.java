package org.nakedobjects.persistence.sql2.mysql;



public class FieldNameMapper {
	/**
	 * Skips any space, and make each next word start with upper case character.
	 */
	public String getColumnName(String fieldName) {
		//return columnName.replace(' ', '_').toLowerCase();
		
		int length = fieldName.length();
		StringBuffer convertedName = new StringBuffer(length);
		for (int i = 0; i < length; i++) {
			char ch = fieldName.charAt(i);
			if(ch == ' ') {
				i++;
				ch = fieldName.charAt(i);
				Character.toUpperCase(ch);
			}
			convertedName.append(ch);
		}
		return convertedName.toString();
	}


}

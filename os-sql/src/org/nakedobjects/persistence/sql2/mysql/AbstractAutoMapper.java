package org.nakedobjects.persistence.sql2.mysql;

import java.util.Enumeration;
import java.util.Properties;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.Value;
import org.nakedobjects.persistence.sql2.CollectionMapper;
import org.nakedobjects.utility.ConfigurationException;
import org.nakedobjects.utility.Configuration;
import org.nakedobjects.utility.NotImplementedException;


public abstract class AbstractAutoMapper extends MySqlMapper {
	protected Field collectionFields[];
	protected CollectionMapper collectionMappers[];
	protected String columnNames[];
	protected boolean dbCreatesId;

	protected FieldNameMapper fieldMapper = new FieldNameMapper();
	protected Field fields[];
	protected String idColumn;
	protected NakedClass nakedClass;
	protected String table;
	protected TypeMapper typeMapper;

	public AbstractAutoMapper(String nakedClassName, String parameterBase) throws ConfigurationException,
			ObjectStoreException {
		nakedClass = NakedClassManager.getInstance().getNakedClass(nakedClassName);
		typeMapper = TypeMapper.getInstance();

		Configuration configParameters = Configuration.getInstance();

		table = configParameters.getString(parameterBase + "table");
		if (table == null) {
			table = nakedClassName.substring(nakedClassName.lastIndexOf('.') + 1).toLowerCase();
			if (nakedClassName.toLowerCase().startsWith("org.nakedobjects")) {
				table = "no_" + table;
			}
		}
		idColumn = configParameters.getString(parameterBase + "id");
		if (idColumn == null) {
			idColumn = "PK" + table + "ID";
		}

		dbCreatesId = configParameters.getBoolean(parameterBase + "db-ids", false);

		if (configParameters.getBoolean(parameterBase + "all-fields", true)) {
			setupFullMapping(nakedClassName, configParameters, parameterBase);
		} else {
			setupSpecifiedMapping(nakedClass, configParameters, parameterBase);
		}
	}

	protected String columnList() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < columnNames.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(columnNames[i]);
		}
		return sb.toString();
	}

	public void createTables() throws ObjectStoreException {
		if (!db.hasTable(table)) {
			StringBuffer columns = new StringBuffer();
			TypeMapper types = TypeMapper.getInstance();
			for (int f = 0; f < fields.length; f++) {
				Field field = fields[f];
				String type;
				if (field instanceof Value) {
					type = types.typeFor(fields[f].getType().getName());
					if (type == null) {
						throw new ObjectStoreException("No type specified for " + fields[f].getType().getName());
					}
				} else if (field instanceof OneToOneAssociation) {
					type = types.id();
				} else {
					throw new ObjectStoreException("Can't map field to column: " + field);
				}
				columns.append(columnNames[f]);
				columns.append(" ");
				columns.append(type);
				columns.append(",");
			}

			// TODO this needs to be modified for ReversedAutoAssociationMapper
			columns.append(idColumn + " int");

			db.update("create table " + table + " (" + columns + ")");

		}
		for (int i = 0; collectionMappers != null && i < collectionMappers.length; i++) {
			if (collectionMappers[i].needsTables()) {
				collectionMappers[i].createTables();
			}
		}
	}

	protected String fieldName(Field field) {
		return fieldMapper.getColumnName(field.getName());
	}

	private int findMatchingField(String fieldName) throws ConfigurationException {
		for (int i = 0; i < fields.length; i++) {
			if (fieldName(fields[i]).equals(fieldMapper.getColumnName(fieldName))) {
				return i;
			}
		}
		throw new ConfigurationException(fieldName + " does not exist in class " + nakedClass.fullName());
	}

	protected NakedObject loadObject(NakedClass nakedClass, Object oid) {
		NakedObject reference;
		if (loadedObjects.isLoaded(oid)) {
			reference = loadedObjects.getLoadedObject(oid);
		} else {
			reference = nakedClass.acquireInstance();
			reference.setOid(oid);
			loadedObjects.loaded(reference);
		}
		return reference;
	}

	public boolean needsTables() throws ObjectStoreException {
		for (int i = 0; collectionMappers != null && i < collectionMappers.length; i++) {
			if (collectionMappers[i].needsTables()) {
				return true;
			}
		}
		return !db.hasTable(table);
	}

	private void setupFullMapping(String nakedClassName, Configuration configParameters, String parameterBase) throws ObjectStoreException, ConfigurationException {
		Field[] allFields = nakedClass.getFields();

		int simpleFieldCount = 0;
		int collectionFieldCount = 0;
		for (int i = 0; i < allFields.length; i++) {
			if (allFields[i].isDerived()) {
				continue;
			} else if (allFields[i] instanceof OneToManyAssociation) {
				collectionFieldCount++;
			} else {
				simpleFieldCount++;
			}
		}

		columnNames = new String[simpleFieldCount];
		fields = new Field[simpleFieldCount];
		collectionFields = new Field[collectionFieldCount];
		collectionMappers = new CollectionMapper[collectionFieldCount];
		Properties collectionMappings = configParameters.getPropertySubset(parameterBase + "collection");

		for (int i = 0, simpleFieldNo = 0, collectionFieldNo = 0; i < allFields.length; i++) {
			if (allFields[i].isDerived()) {
				continue;
			} else if (allFields[i] instanceof OneToManyAssociation) {
				collectionFields[collectionFieldNo] = allFields[i];
				
				String type = collectionMappings.getProperty(allFields[i].getName());
				if(type == null || type.equals("association-table")) {
					collectionMappers[collectionFieldNo] = new AutoAssociationMapper(this, nakedClass, collectionFields[collectionFieldNo]);
				} else 	if(type.equals("fk-table")) 	{
					String property = parameterBase + allFields[i].getName() + ".element-type";
					String elementType = configParameters.getString(property);
					if(elementType == null) {
						throw new ConfigurationException("Expected porperty " + property);
					}
					collectionMappers[collectionFieldNo] = new ReversedAutoAssociationMapper(elementType, collectionFields[collectionFieldNo], parameterBase);
				
				} else {
					// TODO use other mappers where necessary					
					throw new NotImplementedException("for " + type);
				}

				collectionFieldNo++;
			} else if (allFields[i] instanceof OneToOneAssociation) {
				columnNames[simpleFieldNo] = "FK" + fieldName(allFields[i]);
				fields[simpleFieldNo] = allFields[i];
				simpleFieldNo++;
			} else {
				columnNames[simpleFieldNo] = fieldName(allFields[i]);
				fields[simpleFieldNo] = allFields[i];
				simpleFieldNo++;
			}
		}
		
		Properties columnMappings = configParameters.getPropertySubset(parameterBase + "column");
		for(Enumeration keys = columnMappings.keys(); keys.hasMoreElements();) {
			String columnName = (String) keys.nextElement();
			String fieldName = columnMappings.getProperty(columnName);
			columnNames[findMatchingField(fieldName)] = columnName;
		}
	}

	private void setupSpecifiedMapping(NakedClass nakedClass, Configuration configParameters, String parameterBase) throws ObjectStoreException {
		Properties columnMappings = configParameters.getProperties(parameterBase + "column");
		int columnsSize = columnMappings.size();
		columnNames = new String[columnsSize];
		fields = new Field[columnsSize];

		
		int i = 0;
		for (Enumeration names = columnMappings.propertyNames(); names.hasMoreElements(); i++) {
			String columnName = (String) names.nextElement();
			columnNames[i] = columnName;

			String fieldName = columnMappings.getProperty(columnName);
			fields[i] = nakedClass.getField(fieldName);
		}

		Properties collectionMappings = configParameters.getProperties(parameterBase + "collection");
		int collectionsSize = collectionMappings.size();
		collectionMappers = new AutoAssociationMapper[collectionsSize];
		collectionFields= new Field[collectionsSize];

		int j = 0;
		for(Enumeration names = collectionMappings.propertyNames(); names.hasMoreElements(); j++) {
			String collectionName = (String) names.nextElement();
			String type = collectionMappings.getProperty(collectionName);
			
			collectionFields[j] = nakedClass.getField(collectionName);
			if(type.equals("auto")) {
				collectionMappers[j] = new AutoAssociationMapper(this, nakedClass, collectionFields[j]);
			} else {
				// TODO use other mappers where necessary
				//new ReversedAutoAssociationMapper(nakedClass, collectionName, parameterBase);
				
				throw new NotImplementedException();
			}
		}
	}

	public String toString() {
		return "AutoMapper [table=" + table + ",id=" + idColumn + ",noColumns=" + fields.length + ",nakedClass="
				+ nakedClass.fullName() + "]";
	}

	protected String values(NakedClass cls, NakedObject object) throws ObjectStoreException {
		StringBuffer sb = new StringBuffer();
		Field[] fields = cls.getFields();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].isDerived() || fields[i] instanceof OneToManyAssociation) {
				continue;
			}
			sb.append(", ");
			Naked fieldValue = fields[i].get(object);
			if (fieldValue == null) {
				sb.append("NULL");
			} else if (fields[i].isValue()) {
				sb.append(typeMapper.valueAsDBString(fields[i], (NakedValue) fieldValue));
			} else {
				NakedObject ref = (NakedObject) fieldValue;
				long id = primaryKey(ref.getOid());
				sb.append(id);
			}
		}
		return sb.toString();
	}
}
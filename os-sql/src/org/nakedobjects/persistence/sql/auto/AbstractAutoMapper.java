package org.nakedobjects.persistence.sql.auto;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.Version;
import org.nakedobjects.object.persistence.LongNumberVersion;
import org.nakedobjects.persistence.sql.AbstractObjectMapper;
import org.nakedobjects.persistence.sql.CollectionMapper;
import org.nakedobjects.persistence.sql.DatabaseConnector;
import org.nakedobjects.persistence.sql.FieldNameMapper;
import org.nakedobjects.persistence.sql.SqlObjectStoreException;
import org.nakedobjects.persistence.sql.ValueMapper;
import org.nakedobjects.persistence.sql.ValueMapperLookup;
import org.nakedobjects.utility.NakedObjectConfiguration;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Enumeration;

import org.apache.log4j.Logger;


public abstract class AbstractAutoMapper extends AbstractObjectMapper {
    private static final Logger LOG = Logger.getLogger(AbstractAutoMapper.class);
	protected NakedObjectField oneToManyFields[];
	protected CollectionMapper collectionMappers[];
	protected String columnNames[];
	protected boolean dbCreatesId;

	protected FieldNameMapper fieldMapper = new FieldNameMapper();
	protected NakedObjectField oneToOnefields[];
	protected String idColumn;
	protected String lastActivityUserColumn;
	protected String lastActivityDateColumn;
	protected String versionColumn;
	protected NakedObjectSpecification nakedClass;
	protected String table;
	protected ValueMapperLookup typeMapper;

	public AbstractAutoMapper(String nakedClassName, String parameterBase) throws SqlObjectStoreException {
		nakedClass = NakedObjects.getSpecificationLoader().loadSpecification(nakedClassName);
		typeMapper = ValueMapperLookup.getInstance();

		setUpMapper(nakedClassName, parameterBase);
	}

	private void setUpMapper(String nakedClassName, String parameterBase) {
        NakedObjectConfiguration configParameters = NakedObjects.getConfiguration();

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
		
		lastActivityDateColumn = "modified_on";
		lastActivityUserColumn = "modifiied_by";
		versionColumn = "version";
		    

		dbCreatesId = configParameters.getBoolean(parameterBase + "db-ids", false);

		if (configParameters.getBoolean(parameterBase + "all-fields", true)) {
			setupFullMapping(nakedClassName, configParameters, parameterBase);
		} else {
		    setupSpecifiedMapping(nakedClass, configParameters, parameterBase);
		}
		
		LOG.info("table mapping: " + table +  " " + idColumn + " (" + columnList() + ")");
    }

    protected String columnList() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < columnNames.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(quote(columnNames[i]));
		}
		
		sb.append(", ");
		sb.append(quote(versionColumn));
		sb.append(", ");
		sb.append(quote(lastActivityUserColumn));
		sb.append(", ");
		sb.append(quote(lastActivityDateColumn));
			
		return sb.toString();
	}

	public void createTables(DatabaseConnector connection) throws SqlObjectStoreException {
		if (!connection.hasTable(table)) {
			StringBuffer sql = new StringBuffer();
			sql.append("create table ");
			sql.append(quote(table));
			sql.append(" (");
			for (int f = 0; f < oneToOnefields.length; f++) {
			    NakedObjectField  field = oneToOnefields[f];
				String type;
				if (field.isValue()) {
					ValueMapperLookup mappers = ValueMapperLookup.getInstance();
					ValueMapper mapper = mappers.mapperFor(oneToOnefields[f].getSpecification());
					if (mapper == null) {
						throw new SqlObjectStoreException("No type specified for " + oneToOnefields[f].getSpecification().getFullName());
					}
					type = mapper.columnType();
				} else if (field.isObject()) {
				    // TODO make this externally settable
					type = "INT";
				} else {
					throw new SqlObjectStoreException("Can't map field to column: " + field);
				}
				sql.append(quote(columnNames[f]));
				sql.append(" ");
				sql.append(type);
				sql.append(",");
			} 

			// TODO this needs to be modified for ReversedAutoAssociationMapper
			sql.append(quote(idColumn));
			sql.append(" int");

			sql.append(",");
			sql.append(quote(versionColumn));
			sql.append(" bigint");

			sql.append(",");
			sql.append(quote(lastActivityUserColumn));
			sql.append(" varchar(32)");

			sql.append(",");
			sql.append(quote(lastActivityDateColumn));
			sql.append(" timestamp)");

            connection.begin();
			connection.update(sql.toString());
			connection.commit();
		}
		for (int i = 0; collectionMappers != null && i < collectionMappers.length; i++) {
			if (collectionMappers[i].needsTables(connection)) {
				collectionMappers[i].createTables(connection);
			}
		}
	}

	protected String fieldName(NakedObjectField  field) {
		return fieldMapper.getColumnName(field.getId());
	}

	/*
	private int findMatchingField(String fieldName) throws SqlObjectStoreException {
	    String searchName = fieldName.toLowerCase();
		for (int i = 0; i < fields.length; i++) {
			if (fieldName(fields[i]).equals(fieldMapper.getColumnName(searchName))) {
				return i;
			}
		}
		throw new SqlObjectStoreException(fieldName + " does not exist in class " + nakedClass.getFullName());
	}
*/
	
	protected NakedObject getAdapter(NakedObjectSpecification specification, Oid oid) {
		NakedObjectLoader objectLoader = NakedObjects.getObjectLoader();
        if(objectLoader.isIdentityKnown(oid)) {
		    return objectLoader.getAdapterFor(oid);
		} else {
		    return objectLoader.recreateAdapterForPersistent(oid, specification);
		}
	}

    public boolean needsTables(DatabaseConnector connection) throws SqlObjectStoreException {
		for (int i = 0; collectionMappers != null && i < collectionMappers.length; i++) {
			if (collectionMappers[i].needsTables(connection)) {
				return true;
			}
		}
		return !connection.hasTable(table);
	}

	private void setupFullMapping(String nakedClassName, NakedObjectConfiguration configParameters, String parameterBase) throws SqlObjectStoreException {
	    NakedObjectField[] allFields = nakedClass.getFields();

		int simpleFieldCount = 0;
		int collectionFieldCount = 0;
		for (int i = 0; i < allFields.length; i++) {
			if (allFields[i].isDerived()) {
				continue;
			} else if (allFields[i].isCollection()) {
				collectionFieldCount++;
			} else {
				simpleFieldCount++;
			}
		}

		columnNames = new String[simpleFieldCount];
		oneToOnefields = new NakedObjectField [simpleFieldCount];
		oneToManyFields = new NakedObjectField [collectionFieldCount];
		collectionMappers = new CollectionMapper[collectionFieldCount];
		//Properties collectionMappings = configParameters.getPropertiesStrippingPrefix(parameterBase + "collection");
        NakedObjectConfiguration subset = NakedObjects.getConfiguration().createSubset(parameterBase + ".mapper.");

		for (int i = 0, simpleFieldNo = 0, collectionFieldNo = 0; i < allFields.length; i++) {
			if (allFields[i].isDerived()) {
				continue;
			} else if (allFields[i].isCollection()) {
				oneToManyFields[collectionFieldNo] = allFields[i];
				
				String type = subset.getString(allFields[i].getId());
				if(type == null || type.equals("association-table")) {
					collectionMappers[collectionFieldNo] = new AutoAssociationMapper(this, nakedClass, oneToManyFields[collectionFieldNo]);
				} else if(type.equals("fk-table")) 	{
					String property = parameterBase + allFields[i].getId() + ".element-type";
					String elementType = configParameters.getString(property);
					if(elementType == null) {
						throw new SqlObjectStoreException("Expected property " + property);
					}
					collectionMappers[collectionFieldNo] = new ReversedAutoAssociationMapper(elementType, oneToManyFields[collectionFieldNo], parameterBase);
				
				} else {
					// TODO use other mappers where necessary					
					throw new NotImplementedException("for " + type);
				}

				collectionFieldNo++;
			} else if (allFields[i].isObject()) {
				columnNames[simpleFieldNo] = "FK" + fieldName(allFields[i]);
				oneToOnefields[simpleFieldNo] = allFields[i];
				simpleFieldNo++;
			} else {
				columnNames[simpleFieldNo] = fieldName(allFields[i]);
				oneToOnefields[simpleFieldNo] = allFields[i];
				simpleFieldNo++;
			}
		}
	}

	private void setupSpecifiedMapping(NakedObjectSpecification nakedClass, NakedObjectConfiguration configParameters, String parameterBase) throws SqlObjectStoreException {
//		Properties columnMappings = configParameters.getProperties(parameterBase + "column");
        NakedObjectConfiguration columnMappings = NakedObjects.getConfiguration().createSubset(parameterBase + ".mapper.");
		int columnsSize = columnMappings.size();
		columnNames = new String[columnsSize];
		oneToOnefields = new NakedObjectField[columnsSize];

		
		int i = 0;
		for (Enumeration names = columnMappings.properties(); names.hasMoreElements(); i++) {
			String columnName = (String) names.nextElement();
			columnNames[i] = columnName;

			String fieldName = columnMappings.getString(columnName);
			oneToOnefields[i] = nakedClass.getField(fieldName);
		}

//		Properties collectionMappings = configParameters.getProperties(parameterBase + "collection");
        NakedObjectConfiguration collectionMappings = NakedObjects.getConfiguration().createSubset(parameterBase + ".mapper.");
		int collectionsSize = collectionMappings.size();
		collectionMappers = new AutoAssociationMapper[collectionsSize];
		oneToManyFields= new NakedObjectField[collectionsSize];

		int j = 0;
		for(Enumeration names = collectionMappings.properties(); names.hasMoreElements(); j++) {
			String collectionName = (String) names.nextElement();
			String type = collectionMappings.getString(collectionName);
			
			oneToManyFields[j] = nakedClass.getField(collectionName);
			if(type.equals("auto")) {
				collectionMappers[j] = new AutoAssociationMapper(this, nakedClass, oneToManyFields[j]);
			} else {
				// TODO use other mappers where necessary
				//new ReversedAutoAssociationMapper(nakedClass, collectionName, parameterBase);
				
				throw new NotImplementedException();
			}
		}
	}

	public String toString() {
		return "AutoMapper [table=" + table + ",id=" + idColumn + ",noColumns=" + oneToOnefields.length + ",nakedClass="
				+ nakedClass.getFullName() + "]";
	}

	protected String values(NakedObjectSpecification cls, NakedObject object) throws SqlObjectStoreException {
		StringBuffer sb = new StringBuffer();
		NakedObjectField[] fields = cls.getFields();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].isDerived() || fields[i].isCollection()) {
				continue;
			}
			sb.append(", ");
			Naked fieldValue = object.getField(fields[i]);
			if (fieldValue == null) {
				sb.append("NULL");
			} else if (fields[i].isValue()) {
				ValueMapper mapper = typeMapper.mapperFor(fields[i].getSpecification());
                sb.append(mapper.valueAsDBString((NakedValue) fieldValue));
			} else {
				NakedObject ref = (NakedObject) fieldValue;
				sb.append(primaryKey(ref.getOid()));
			}
		}
		return sb.toString();
	}

    protected String updateWhereClause(NakedObject object, boolean and) throws SqlObjectStoreException {
        Version version = object.getVersion();
        long versionNumber = ((LongNumberVersion) version).getSequence();
        return (and ? " and " +  versionNumber + " = \"version\"" : "");
    }
}
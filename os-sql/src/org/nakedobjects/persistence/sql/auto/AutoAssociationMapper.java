package org.nakedobjects.persistence.sql.auto;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.ResolveException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.persistence.sql.AbstractObjectMapper;
import org.nakedobjects.persistence.sql.CollectionMapper;
import org.nakedobjects.persistence.sql.DatabaseConnector;
import org.nakedobjects.persistence.sql.Results;
import org.nakedobjects.persistence.sql.SqlObjectStoreException;

import org.apache.log4j.Logger;

public class AutoAssociationMapper extends AbstractObjectMapper implements CollectionMapper {
	private static final Logger LOG = Logger.getLogger(AutoAssociationMapper.class);
	private String table;
	private String parentColumn;
	private String elementIdColumn;
	private String elementClassColumn;
	private FieldSpecification field;
	private AbstractAutoMapper mapper;

	public AutoAssociationMapper(AbstractAutoMapper mapper, NakedObjectSpecification nakedClass, FieldSpecification field) throws SqlObjectStoreException {
		this.mapper = mapper;
		this.field = field;

		// TODO load in properties
		String className = nakedClass.getShortName().toLowerCase();

		parentColumn = "FK" + className;
	
		String columnName = mapper.fieldMapper.getColumnName(field.getName());
		elementIdColumn = "PK" + columnName;
	
		elementClassColumn = columnName + "Class";

		table = className + "_" + columnName;
		if(nakedClass.getFullName().startsWith("org.nakedobjects.")) {
			table = "no_" + table;
		}
	}

	public boolean needsTables(DatabaseConnector connector) throws SqlObjectStoreException {
		return ! connector.hasTable(table);
	}
	
	public void createTables(DatabaseConnector connector) throws SqlObjectStoreException {
		// TODO load in properties

		String columns = parentColumn + " int, " + elementClassColumn + " varchar(255), " + elementIdColumn + " int";
		connector.update("create table " + table + " (" + columns + ")");
	}

	public void loadInternalCollection(DatabaseConnector connector, NakedObject parent)
			throws ResolveException, SqlObjectStoreException {
		InternalCollection collection = (InternalCollection) field.getPojo(parent);
		LOG.debug("Loading internal collection " + collection);
		String parentId = mapper.primaryKey(parent.getOid());
		
		String statement = "select " + elementIdColumn + "," + elementClassColumn + " from " + table + " where "
				+ parentColumn + " = " + parentId;
		Results rs = connector.select(statement);
		while (rs.next()) {
			String cls = rs.getString(elementClassColumn);
			NakedObjectSpecification elementCls = NakedObjects.getSpecificationLoader().loadSpecification(cls);
			Oid oid = recreateOid(rs, elementCls, elementIdColumn);
			NakedObject element = mapper.loadObject(elementCls, oid);
			LOG.debug("  element  " + element.getOid());
			collection.added(element);
		}
		rs.close();
	}

	public void saveInternalCollection(DatabaseConnector connector, NakedObject parent) throws SqlObjectStoreException {
		InternalCollection collection = (InternalCollection) field.getPojo(parent);
		LOG.debug("Saving internal collection " + collection);
		String parentId = mapper.primaryKey(parent.getOid());
		
		connector.update("delete from " + table + " where " + parentColumn + " = " + parentId);
		
		String columns = parentColumn + ", " + elementIdColumn + ", " + elementClassColumn;
		int size = collection.size();
		for (int i = 0; i < size; i++) {
			NakedObject element = collection.elementAt(i);
			
			String elementId = mapper.primaryKey(element.getOid());
			String cls = element.getSpecification().getFullName();
			String values = parentId + "," + elementId + ", '" + cls + "'";
			String statement = "insert into " + table + " (" + columns + ") values (" + values + ")";
			connector.update(statement);
		}
	}

}
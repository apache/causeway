package org.nakedobjects.persistence.sql.auto;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.ResolveException;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.persistence.sql.CollectionMapper;
import org.nakedobjects.persistence.sql.DatabaseConnector;
import org.nakedobjects.persistence.sql.Results;
import org.nakedobjects.persistence.sql.SqlObjectStoreException;

import org.apache.log4j.Logger;


/** used where there is a one to many association, and the elements are only known to parent */
public class ReversedAutoAssociationMapper extends AbstractAutoMapper implements CollectionMapper {
	private static final Logger LOG = Logger.getLogger(ReversedAutoAssociationMapper.class);
	private String table;
	private String parentColumn;
	private String elementIdColumn;
	private FieldSpecification field;
	
	public ReversedAutoAssociationMapper(String elemenType, FieldSpecification field, String parameterBase) throws SqlObjectStoreException {
		super(elemenType, parameterBase);
		
		this.field = field;

		// TODO load in properties
		String className = nakedClass.getShortName().toLowerCase();

		parentColumn = "FK" + className;
	
	
		table = fieldMapper.getColumnName(field.getName());
		
		String columnName = fieldMapper.getColumnName(field.getName());
		elementIdColumn = "PK" + columnName;

		if(nakedClass.getFullName().startsWith("org.nakedobjects.")) {
			table = "no_" + table;
		}
	}

	public void loadInternalCollection(DatabaseConnector connector, NakedObject parent)
			throws ResolveException, SqlObjectStoreException {
		InternalCollection collection = (InternalCollection) field.getPojo(parent);
		LOG.debug("Loading internal collection " + collection);
		String parentId = primaryKey(parent.getOid());
		
		String statement = "select " + elementIdColumn + "," + columnList() + " from " + table + " where "
				+ parentColumn + " = " + parentId;
		Results rs = connector.select(statement);
		while (rs.next()) {
			Oid oid = recreateOid(rs, nakedClass, elementIdColumn);
			NakedObject element = loadObject(nakedClass, oid);
			LOG.debug("  element  " + element);
			collection.added(element);
		}
        rs.close();
		collection.setResolved();
	}

	public void saveInternalCollection(DatabaseConnector connector, NakedObject parent) throws SqlObjectStoreException {
		InternalCollection collection = (InternalCollection) field.getPojo(parent);
		LOG.debug("Saving internal collection " + collection);
		String parentId = primaryKey(parent.getOid());
		
		connector.update("delete from " + table + " where " + parentColumn + " = " + parentId);
		
		String columns = parentColumn + ", " + elementIdColumn;
		int size = collection.size();
		for (int i = 0; i < size; i++) {
			NakedObject element = collection.elementAt(i);
			
			String elementId = primaryKey(element.getOid());
			String cls = element.getSpecification().getFullName();
			String values = parentId + "," + elementId + ", '" + cls + "'";
			String statement = "insert into " + table + " (" + columns + ") values (" + values + ")";
			connector.update(statement);
		}
	}

}
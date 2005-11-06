package org.nakedobjects.persistence.sql.auto;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectAssociation;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.ResolveException;
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
	private NakedObjectField field;
	
	public ReversedAutoAssociationMapper(String elemenType, NakedObjectField field, String parameterBase) throws SqlObjectStoreException {
		super(elemenType, parameterBase);
		
		this.field = field;

		// TODO load in properties
		String className = nakedClass.getShortName().toLowerCase();

		parentColumn = "FK" + className;
	
	
		table = fieldMapper.getColumnName(field.getId());
		
		String columnName = fieldMapper.getColumnName(field.getId());
		elementIdColumn = "PK" + columnName;

		if(nakedClass.getFullName().startsWith("org.nakedobjects.")) {
			table = "no_" + table;
		}
	}

	public void loadInternalCollection(DatabaseConnector connector, NakedObject parent)
			throws ResolveException, SqlObjectStoreException {
		LOG.debug("Loading internal collection " + field);
		String parentId = primaryKey(parent.getOid());
		
		String statement = "select " + elementIdColumn + "," + columnList() + " from " + table + " where "
				+ parentColumn + " = " + parentId;
		Results rs = connector.select(statement);
		while (rs.next()) {
			Oid oid = recreateOid(rs, nakedClass, elementIdColumn);
			NakedObject element = getAdapter(nakedClass, oid);
			LOG.debug("  element  " + element);
			parent.setAssociation((NakedObjectAssociation) field, element);
		}
        rs.close();
	}

	public void saveInternalCollection(DatabaseConnector connector, NakedObject parent) throws SqlObjectStoreException {
		InternalCollection collection = (InternalCollection) parent.getField(field);
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
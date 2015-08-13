package core.shared;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import core.load_data.DataStoreInfo;

public class Table implements Serializable {

	private static final long serialVersionUID = 1L;

	private final List<Column> columns = new LinkedList<Column>();

	private final String name;
	private final String primary;

	public Table(final String name, final String primary) {
		this.name = name;
		this.primary = primary;
	}
	
	public Table(final String name) {
		this.name = name;
		this.primary = "none";
	}

	public boolean add(final Column arg0) {
		return columns.add(arg0);
	}
	
	public String getName() {
		return name;
	}

	private void createIndex(final String column, final String type,
			final Connection connection) {
		try {
			String indexName = name + "_" + column + "_" + type + "_idx";
			String createIndex = "DO $$ BEGIN IF NOT EXISTS ( SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE c.relname = '"
					+ indexName
					+ "' AND n.nspname = 'public') THEN CREATE INDEX  "
					+ indexName
					+ " ON public."
					+ name
					+ " USING "
					+ type
					+ " (" + column + ");END IF;END$$;";
			connection.prepareCall(createIndex).execute();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}

	public void createTable(final Connection connection) {
		try {
			
			connection.prepareCall(dropTable()).execute();
			connection.prepareCall(dropSequence()).execute();
			
			//Create sequence 
			connection.prepareCall(createSequence()).execute();
			columns.get(0).setType(columns.get(0).getType() + String.format(" DEFAULT NEXTVAL('%s')", ("sq" + this.name)));
			
			
			String sql = "CREATE TABLE IF NOT EXISTS " + name + " ( ";
			Iterator<Column> iterator = columns.iterator();
			while (iterator.hasNext()) {
				Column column = iterator.next();
				sql += column.getColumn() + " " + column.getType();
				if (iterator.hasNext())
					sql += ", ";
			}
			
			sql += ")";
			
			System.out.println(sql);
			
			connection.prepareCall(sql).execute();
			
			//connection.close();
			for (Column column : columns)
				if (column.getIndex())
					createIndex(column.getColumn(), "btree", connection);
				else if (column.getSpatialIndex())
					createIndex(column.getColumn(), "gist", connection);
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}
	
	private String createSequence() {
		return "CREATE SEQUENCE  sq" + this.name; 
	}
	
	private String dropTable() {
		return "DROP TABLE IF EXISTS "+ name + " CASCADE";
	}
	
	private String dropSequence() {
		return "DROP SEQUENCE IF EXISTS sq" + name;
	}
	
	public String insertStatement() {
		String result = "INSERT INTO " + this.name + " (";
		Iterator<Column> it = columns.iterator();
		
		
		it.next(); //To skip primary key, once we have a sequence
		while(it.hasNext()) {
			Column col = it.next();
			if(it.hasNext()) result += col.getColumn() + ",";
			else result += col.getColumn();
		}
		
		result += ") VALUES (";
		it = columns.iterator();
		String add = "";
		it.next(); //To skip primary key, once we have a sequence
		while(it.hasNext()) {
			Column col = it.next();
			if (col.isSpatial()) add = "ST_GeomFromText(?)";
			else if(col.isTimeZone()) {
				add = "to_timestamp(?, 'yyyy-mm-dd hh24:mi:ss')";
//				add = "to_timestamp(?, 'yyyy-mm-dd')";
			}
			else add = "?";
			
			if(it.hasNext()) result += add + ",";
			else result += add;
		}
		
		result += ")";
		
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Table other = (Table) obj;
		if (columns == null) {
			if (other.columns != null)
				return false;
		} else if (!columns.equals(other.columns))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (columns == null ? 0 : columns.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "Table [name=" + name + ", columns=" + columns + "]";
	}

}

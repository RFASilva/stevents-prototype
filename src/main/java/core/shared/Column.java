package core.shared;

import java.io.Serializable;

public class Column implements Serializable {

	private static final long serialVersionUID = 1L;

	private String column;

	private Boolean index;

	private Boolean spatialIndex;

	private String type;

	public Column(final String column, final Boolean index,
			final Boolean spatialIndex, final String type) {
		this.column = column;
		this.index = index;
		this.spatialIndex = spatialIndex;
		this.type = type;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Column other = (Column) obj;
		if (column == null) {
			if (other.column != null)
				return false;
		} else if (!column.equals(other.column))
			return false;
		if (index == null) {
			if (other.index != null)
				return false;
		} else if (!index.equals(other.index))
			return false;
		if (spatialIndex == null) {
			if (other.spatialIndex != null)
				return false;
		} else if (!spatialIndex.equals(other.spatialIndex))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	public String getColumn() {
		return column;
	}

	public Boolean getIndex() {
		return index;
	}

	public Boolean getSpatialIndex() {
		return spatialIndex;
	}

	public String getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (column == null ? 0 : column.hashCode());
		result = prime * result + (index == null ? 0 : index.hashCode());
		result = prime * result
				+ (spatialIndex == null ? 0 : spatialIndex.hashCode());
		result = prime * result + (type == null ? 0 : type.hashCode());
		return result;
	}

	public void setColumn(final String column) {
		this.column = column;
	}

	public void setIndex(final Boolean index) {
		this.index = index;
	}

	public void setSpatialIndex(final Boolean spatialIndex) {
		this.spatialIndex = spatialIndex;
	}

	public void setType(final String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Column [column=" + column + ", index=" + index
				+ ", spatialIndex=" + spatialIndex + ", type=" + type + "]";
	}

	public boolean isSpatial() {
		return type.equals("GEOMETRY");
	}
	
	public boolean isTimeZone() {
		return type.equals("TIMESTAMP WITHOUT TIME ZONE");
	}
}

package core.shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

public abstract class Entity implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Geometry geometry;

	protected Map<String, Integer> properties;

	protected Integer upGeoHash;

	protected Geometry upGeometry;

	public Entity() {
		this.properties = new HashMap<String, Integer>();
	}
	
	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public Integer getProperty(String key) {
		return properties.get(key);
	}

	public void setProperties(Map<String, Integer> properties) {
		this.properties = properties;
	}

	public Integer getUpGeoHash() {
		return upGeoHash;
	}

	public void setUpGeoHash(Integer upGeoHash) {
		this.upGeoHash = upGeoHash;
	}

	public Geometry getUpGeometry() {
		return upGeometry;
	}

	public void setUpGeometry(Geometry upGeometry) {
		this.upGeometry = upGeometry;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((geometry == null) ? 0 : geometry.hashCode());
		result = prime * result
				+ ((properties == null) ? 0 : properties.hashCode());
		result = prime * result
				+ ((upGeoHash == null) ? 0 : upGeoHash.hashCode());
		result = prime * result
				+ ((upGeometry == null) ? 0 : upGeometry.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (geometry == null) {
			if (other.geometry != null)
				return false;
		} else if (!geometry.equals(other.geometry))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (upGeoHash == null) {
			if (other.upGeoHash != null)
				return false;
		} else if (!upGeoHash.equals(other.upGeoHash))
			return false;
		if (upGeometry == null) {
			if (other.upGeometry != null)
				return false;
		} else if (!upGeometry.equals(other.upGeometry))
			return false;
		return true;
	}

	public void addProperty(String key, Integer value) {
		properties.put(key, value);
	}
	

}

package core.time_series.spatial_utilities.snn_bf.data.marin;

import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.GenericPoint;

public class MarinPoint extends GenericPoint<Double> {

	private Integer heading;

	public MarinPoint(boolean allowsRep){
		super(2, allowsRep);
		setHeading(new Integer(-1));
	}

	public MarinPoint(Double x, Double y, boolean allowsRep) {
		super(x, y, allowsRep);
		setHeading(new Integer(-1));
	}

	public Integer getHeading() {
		return heading;
	}

	public void setHeading(Integer heading) {
		this.heading = heading;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof MarinPoint)) 
			return false;

		MarinPoint point = (MarinPoint) obj;

		if(this.isAllowsRep()){
			if(this.getId() != point.getId())
				return false;
		}
		else {
			for(int i = 0; i < this.getDimensions(); ++i)
				if(!this.getCoord(i).equals(point.getCoord(i)))
					return false;
			
			if(!this.getHeading().equals(point.getHeading()))
				return false;
		}
		
		return true;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("{" + this.getId() + "=[ ");
		buffer.append(this.getCoord(0));
		buffer.append(", ");
		buffer.append(this.getCoord(1));
		buffer.append(", ");
		buffer.append(this.getHeading());

		buffer.append(" ]}");

		return buffer.toString();
	}
}

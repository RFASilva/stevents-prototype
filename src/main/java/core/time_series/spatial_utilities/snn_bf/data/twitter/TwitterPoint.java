package core.time_series.spatial_utilities.snn_bf.data.twitter;

import com.savarese.spatial.GenericPoint;

public class TwitterPoint extends GenericPoint<Double> {

	public TwitterPoint(boolean allowsRep){
		super(3, allowsRep);
	}

	public TwitterPoint(Double x, Double y, Double time, boolean allowsRep) {
		super(x, y, time, allowsRep);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof TwitterPoint)) 
			return false;

		TwitterPoint point = (TwitterPoint) obj;

		if(this.isAllowsRep()){
			if(this.getId() != point.getId())
				return false;
		}
		else {
			for(int i = 0; i < this.getDimensions(); ++i)
				if(!this.getCoord(i).equals(point.getCoord(i)))
					return false;
			
//			if(!this.getTimeStamp().equals(point.getTimeStamp()))
//				return false;
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
		buffer.append(this.getCoord(2));

		buffer.append(" ]}");

		return buffer.toString();
	}
	
}

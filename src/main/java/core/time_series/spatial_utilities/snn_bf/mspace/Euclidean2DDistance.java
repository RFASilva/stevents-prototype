/*
 * Copyright 2010 Savarese Software Research Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.savarese.com/software/ApacheLicense-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package core.time_series.spatial_utilities.snn_bf.mspace;

import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.Distance;
import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.Point;

/**
 * The EuclideanDistance class determines the distance between two
 * points in a Euclidean space.
 */
public class Euclidean2DDistance<Coord extends Number & Comparable<? super Coord>,
P extends Point<Coord>>
implements Distance<Coord, P>
{
	long numberCalculations;
	
	/**
	 * Returns the euclidean distance between two points.
	 *
	 * @param from The first end point.
	 * @param to The second end point.
	 * @return The distance between from and to.
	 */
	public double distance(P from, P to) {	
		return StrictMath.sqrt(distance2(from, to));
	}

	/**
	 * Returns the square of the euclidean distance between two points.
	 *
	 * @param from The first end point.
	 * @param to The second end point.
	 * @return The square of the euclidean distance between from and to.
	 */
	public double distance2(P from, P to) {
		double d = 0;

		for(int i = 0; i < 2; ++i) {
			double diff = (to.getCoord(i).doubleValue() -
					from.getCoord(i).doubleValue());
			d+=(diff*diff);
		}
		
		updateNumberCalculations();

		return d;
	}
	
	/**
	 * Returns the number of distance calculations.
	 * 
	 * @return The number of distance calculations.
	 */
	public long getNumberCalculations(){
		return numberCalculations;
	}
	
	/**
	 * Increments the number of distance calculations.
	 */
	public void updateNumberCalculations(){
		numberCalculations++;
	}
	
	/**
	 * Resets the number of distance calculations.
	 */
	public void resetNumberCalculations(){
		numberCalculations = 0;
	}
}

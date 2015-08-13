package core.time_series.spatial_utilities.snn_bf.file;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import core.time_series.spatial_utilities.snn_bf.util.Pair;

/**
 * Java interface of the reader of objects P.
 * 
 * @author Bruno Filipe Faustino
 * @since 2012-06-25
 */
public interface Reader<P> {
	
	/**
	 * Get method for the number of objects in file.
	 * 
	 * @return the number of objects in file.
	 */
	public long getNumObjects();
	
	/**
	 * Get method for the number of objects inserted.
	 * 
	 * @return the number of objects inserted.
	 */
	public long getNumObjectsIns();
	
	/**
	 * Get method for the number of dimensions.
	 * 
	 * @return the number of dimensions.
	 */
	public int getNumDimensions();
	
	/**
	 * Get method for the objects iterator.
	 * 
	 * @return the objects iterator.
	 */
	public Iterator<P> getIterator();
	
	/**
	 * Get method for the number of distance calculations.
	 * 
	 * @return the number of distance calculations.
	 */
	public long getNumberCalculations();
	
	/**
	 * Reads the number of objects in the file and the number of dimensions 
	 * that define the respective objects.
	 */
	public void readNumObjsDim();
	
	/**
	 * Reads from the file the number of objects given.
	 * 
	 * @param numObjects the number of objects to be read.
	 */
	public void readObjects(long numObjectsToRead);
	
	/**
	 * Get method for the K Nearest Neighbours obtained from the tree.
	 * 
	 * @return the map with K Nearest Neighbours of each object P and the 
	 * respective distance from the object P and its neighbour.
	 */
	public Map<Long, Set<Pair<Long,Double>>> getKNNs(int k);
}

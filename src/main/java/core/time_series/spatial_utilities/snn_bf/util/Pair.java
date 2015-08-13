package core.time_series.spatial_utilities.snn_bf.util;

public class Pair<L,R>{

	private final L left;
	private final R right;

	public Pair(L left, R right){
		this.left = left;
		this.right = right;
	}

	public L getLeft(){ 
		return left; 
		}
	
	public R getRight(){ 
		return right; 
		}

	@Override
	public int hashCode() { 
		return left.hashCode() ^ right.hashCode(); 
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) 
			return false;
		else if (!(o instanceof Pair)) 
			return false;
		else {
			Pair other = (Pair) o;
			
			return this.left.equals(other.getLeft()) &&
					this.right.equals(other.getRight());
		}
	}
	
	@Override
	public String toString() {
		return "[left=" + left + ", right=" + right + "]";
	}
	
}

package stevents_changes;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import core.load_data.DataStoreInfo;
import core.load_data.Functions;

public class Transformations {

	public static Complex[] fft(String sql) {
		FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD); 

		BigDecimal[] temp = pullTimeSeries(sql);
		
		int value = (int) Functions.computeNextPowerOfTwo(temp.length);
//		System.out.println(temp.length + (value - temp.length));
		
		double[] values = new double[temp.length + (value - temp.length)];
		
		for(int i = 0; i < temp.length; i++)
			values[i] = temp[i].doubleValue();
		
		return fft.transform(values, TransformType.FORWARD);
	}
	
	private static BigDecimal[] pullTimeSeries(String sql) {

		try {
			Connection connection = DataStoreInfo.getMetaStore();

			Statement st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(Manager.FETCH_SIZE);
			ResultSet data = st.executeQuery(sql);
			
			data.next();
			
			BigDecimal[] result = (BigDecimal[]) data.getArray(1).getArray();
			connection.close();
			return result;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


	public static void main(String[] args) {
		
		String sql = "select array_agg(value) from timeseriesfires_portugalday where type='NrElements' and up_geo_hash='-1848798023' group by type, up_geo_hash";
		Complex[] values = Transformations.fft(sql);
		
		PrintWriter writer;
		try {
			writer = new PrintWriter("d:/fourriertransformation.txt", "UTF-8");
			
			
			for(int i=0; i < values.length; i++) {
				
				double magnitude = Math.sqrt( (values[i].getReal()*values[i].getReal()) + (values[i].getImaginary()*values[i].getImaginary()));
				writer.println(magnitude);
			}
			
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	
	
}

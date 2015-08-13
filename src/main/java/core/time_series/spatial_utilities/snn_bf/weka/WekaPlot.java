package core.time_series.spatial_utilities.snn_bf.weka;

import java.awt.BorderLayout;
import java.io.InputStream;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.gui.visualize.VisualizePanel;

public class WekaPlot {
	
	private InputStream is;
	
	public WekaPlot(InputStream is){
		this.is = is;
	}

	public void plot() throws WekaPlotException{
		try {
			
			DataSource source = new DataSource(is);
	
			Instances train = source.getDataSet();
	
			VisualizePanel vp = new VisualizePanel();
			vp.setInstances(train);
	
			final javax.swing.JFrame jf = 
					new javax.swing.JFrame("Weka - SNN Clustering Results");
			jf.setSize(800,600);
			jf.getContentPane().setLayout(new BorderLayout());
			jf.getContentPane().add(vp, BorderLayout.CENTER);
			jf.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					jf.dispose();
				}
			});
			jf.setVisible(true);			
		} catch (Exception e1) {
			throw new WekaPlotException();
		}
	}
}

/*import java.io.FileReader;

//TODO: REMOVE COMMENTS
 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import core.Config;
import core.load_data.DataStoreInfo;
import core.shared.Column;
import core.shared.Table;


public class FiresPortugal {

	private static final long BATCHINSERT_SIZE = Config.getConfigInt("insert_chunk_size");

	private static String insertStatement;

	private static CellProcessor[] getProcessors() {

		final CellProcessor[] processors = new CellProcessor[] { 
				new NotNull(), 
				new UniqueHashCode(), // codigo
				new NotNull(), // tipo
				new NotNull(), // distrito
				new NotNull(), // concelho 
				new NotNull(), // freguesia
				new NotNull(), // local
				new NotNull(), // INE
				new NotNull(), // X
				new NotNull(), // Y
				new NotNull(), // date alerta
				new NotNull(), //time alerta
				new NotNull(), //data de extincao
				new NotNull(), //hora de extincao
				new NotNull(), //data 1interv
				new NotNull(), //hora 1interv
				new NotNull(), //fonte alerta
				new NotNull(), //NUT
				new NotNull(), //AA_apovoamento
				new NotNull(), //AA_mato
				new NotNull(), //AA_agricola
				new NotNull(), //AA_florestal
				new NotNull(), //AA_total
				new ParseBool(), //reacendimento
				new ParseBool(), //queimada
				new ParseBool(), //falsoalarme
				new ParseBool(), //fogacho
				new ParseBool(), //incendio
				new ParseBool(), //agricola
				new NotNull(), // perimetro
				new NotNull(), // aps
				new NotNull(), // causa
				new NotNull(), // tipo de causa
		};

		return processors;
	}


	public static void createTable() {

		Table firesPortugal = new Table("fires_portugal", "pk_id");

		firesPortugal.add(new Column("pk_id", false, false, "NUMERIC"));
		firesPortugal.add(new Column("codigo", false, false, "TEXT"));
		firesPortugal.add(new Column("tipo", false, false, "TEXT"));
		firesPortugal.add(new Column("distrito", false, false, "TEXT"));
		firesPortugal.add(new Column("concelho", false, false, "TEXT"));
		firesPortugal.add(new Column("freguesia", false, false, "TEXT"));
		firesPortugal.add(new Column("local", false, false, "TEXT"));
		firesPortugal.add(new Column("ine", false, false, "NUMERIC"));
		firesPortugal.add(new Column("latitude", false, false, "NUMERIC"));
		firesPortugal.add(new Column("longitud", false, false, "NUMERIC"));	

		firesPortugal.add(new Column("year", false, false, "NUMERIC"));
		firesPortugal.add(new Column("month", false, false, "NUMERIC"));
		firesPortugal.add(new Column("day", false, false, "NUMERIC"));
		firesPortugal.add(new Column("hour", false, false, "NUMERIC"));
		firesPortugal.add(new Column("minute", false, false, "NUMERIC"));

		firesPortugal.add(new Column("data_extincao", false, false, "TEXT"));
		firesPortugal.add(new Column("hora_extincao", false, false, "TEXT"));
		firesPortugal.add(new Column("data_interv", false, false, "TEXT"));
		firesPortugal.add(new Column("hora_interv", false, false, "TEXT"));
		firesPortugal.add(new Column("fonte_alerta", false, false, "TEXT"));		
		firesPortugal.add(new Column("NUT", false, false, "TEXT"));

		firesPortugal.add(new Column("aa_apovoamento", false, false, "NUMERIC"));
		firesPortugal.add(new Column("aa_mato", false, false, "NUMERIC"));
		firesPortugal.add(new Column("aa_agricola", false, false, "NUMERIC"));
		firesPortugal.add(new Column("aa_florestal", false, false, "NUMERIC"));
		firesPortugal.add(new Column("aa_total", false, false, "NUMERIC"));

		firesPortugal.add(new Column("reacendimento", false, false, "boolean"));

		firesPortugal.add(new Column("queimada", false, false, "BOOLEAN"));
		firesPortugal.add(new Column("falsoalarme", false, false, "BOOLEAN"));
		firesPortugal.add(new Column("fogacho", false, false, "BOOLEAN"));
		firesPortugal.add(new Column("incendio", false, false, "BOOLEAN"));
		firesPortugal.add(new Column("agricola", false, false, "BOOLEAN"));

		firesPortugal.add(new Column("perimetro", false, false, "TEXT"));
		firesPortugal.add(new Column("aps", false, false, "TEXT"));
		firesPortugal.add(new Column("causa", false, false, "TEXT"));
		firesPortugal.add(new Column("tipocausa", false, false, "TEXT"));

		Connection connection = DataStoreInfo.getMetaStore();
		
		firesPortugal.createTable(connection);

		insertStatement = firesPortugal.insertStatement();
		System.out.println(insertStatement);
	}


	private static Geometry convertCoords(double x, double y) throws NoSuchAuthorityCodeException, FactoryException, MismatchedDimensionException, TransformException {
		GeometryFactory geofact = new GeometryFactory(new PrecisionModel(), 920790);
		Geometry geom = geofact.createPoint( new Coordinate(x, y));

		CoordinateReferenceSystem from;
		CoordinateReferenceSystem to;

		from = CRS.decode("EPSG:20790", true); // Datum Lisboa/Hayford-Gauss com falsa origem - Coordinates Militares
		to = CRS.decode("EPSG:4326", true);
		MathTransform transform  = CRS.findMathTransform(from, to, true); 
		return JTS.transform(geom, transform);


	}


	public static void readAndWriteWithCsvMapReader(String fileName) throws Exception {
		Connection connection = DataStoreInfo.getMetaStore();

		ICsvMapReader mapReader = null;
		try {

			PreparedStatement ps = connection.prepareStatement(insertStatement); 

			mapReader = new CsvMapReader(new FileReader(fileName), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

			// the header columns are used as the keys to the Map
			final String[] header = mapReader.getHeader(true);
			final CellProcessor[] processors = getProcessors();

			Map<String, Object> line;
			int batchCount = 0;

			while( (line = mapReader.read(header, processors)) != null ) {

//				System.out.println(mapReader.getRowNumber());
				
				// Obter informacao
				String codigo = (String) line.get("Código");
				String tipo = (String) line.get("Tipo");
				String distrito = (String) line.get("Distrito");
				String concelho = (String) line.get("Concelho");
				String freguesia = (String) line.get("Freguesia");
				String local = (String) line.get("Local");

				int ine = Integer.parseInt((String)line.get("INE"));

				double longitud = Double.parseDouble((String)line.get("x"));
				double latitude = Double.parseDouble((String)line.get("y"));
				Geometry geom;
				try {
					geom = convertCoords(longitud, latitude);
				} catch(Exception e) {
					System.out.println("Falhou conversao de coordenadas");
					continue;
				}

				String data = (String) line.get("DataAlerta");
				String[] temp = data.split("-");

				int year = Integer.parseInt(temp[0]);
				int month = Integer.parseInt(temp[1]);
				int day = Integer.parseInt(temp[2]);
				
				//NAO FACO IDEIA PORQUE NEM ME VOU CHATEAR MAS OS FICHEIROS SAO IGUAIS E PROVOCAM COMPORTAMENTOS DIFERENTES
				if(year < 2001) {
					
					year =  Integer.parseInt(temp[2]);
					month = Integer.parseInt(temp[1]);
					day =Integer.parseInt(temp[0]);
				}

				String horaAlerta = (String) line.get("HoraAlerta");
				String[] temp2 = horaAlerta.split(":");

				int hour = Integer.parseInt(temp2[0]);
				int minute = Integer.parseInt(temp2[1]);


				String dataExtincao = (String) line.get("DataExtinção");
				String horaExtincao = (String) line.get("HoraExtinção");
				String dataInterv = (String) line.get("Data1Interv");
				String horaInterv = (String) line.get("Hora1Interv");
				String fonteAlerta = (String) line.get("FonteAlerta");

				String nut = (String) line.get("NUT");

				double aa_apovoamento = Double.parseDouble((String) line.get("AA_Povoamento"));
				double aa_mato = Double.parseDouble((String) line.get("AA_Mato"));
				double aa_agricola = Double.parseDouble((String) line.get("AA_Agricola"));
				double aa_espacoflorestal = Double.parseDouble((String) line.get("AA_EspaçoFlorestal (Pov+mato)"));
				double aa_total = Double.parseDouble((String) line.get("AA_Total(pov+mato+agric)"));

				boolean reacendimento = (boolean) line.get("Reacendimento");
				boolean queimada = (boolean) line.get("Queimada");
				boolean falsoAlarme = (boolean)  line.get("FalsoAlarme");
				boolean fogacho = (boolean) line.get("Fogacho");
				boolean incendio = (boolean) line.get("Incendio");
				boolean agricola = (boolean) line.get("Agricola");

				String perimetro = (String) line.get("Perimetro");
				String aps = (String) line.get("APS");
				String causa = (String) line.get("Causa");
				String tipoCausa = (String) line.get("TipoCausa");


				ps.setString(1, codigo);
				ps.setString(2, tipo);
				ps.setString(3, distrito);
				ps.setString(4, concelho);
				ps.setString(5, freguesia);
				ps.setString(6, local);
				ps.setInt(7, ine);
				ps.setDouble(8, geom.getCoordinates()[0].y);
				ps.setDouble(9, geom.getCoordinates()[0].x);

				
				ps.setInt(10, year);
				ps.setInt(11, month);
				ps.setInt(12, day);
				ps.setInt(13, hour);
				ps.setInt(14, minute);

				ps.setString(15,  dataExtincao);
				ps.setString(16,  horaExtincao);
				ps.setString(17,  dataInterv);
				ps.setString(18,  horaInterv);
				ps.setString(19,  fonteAlerta);
				ps.setString(20, nut);

				ps.setDouble(21, aa_apovoamento);
				ps.setDouble(22, aa_mato);
				ps.setDouble(23, aa_agricola);
				ps.setDouble(24, aa_espacoflorestal);
				ps.setDouble(25, aa_total);

				ps.setBoolean(26, reacendimento);
				ps.setBoolean(27, queimada);
				ps.setBoolean(28, falsoAlarme);
				ps.setBoolean(29, fogacho);
				ps.setBoolean(30, incendio);
				ps.setBoolean(31, agricola);

				ps.setString(32, perimetro);
				ps.setString(33, aps);
				ps.setString(34, causa);
				ps.setString(35, tipoCausa);
				
				

				ps.addBatch();
				if (batchCount == BATCHINSERT_SIZE) {
					ps.executeBatch();
					batchCount = 0;
				} else
					batchCount++;
			}
			ps.executeBatch();


		}
		finally {
			if( mapReader != null ) {
				mapReader.close();
			}
		}
	}



	public static void main(String[] args) {


		String fileName = "data/PortugalFires/Lista Incendios_%s.csv";

		try {
			FiresPortugal.createTable();
			for(int i = 2001; i <= 2012; i++) {
				String year = i + "";
				FiresPortugal.readAndWriteWithCsvMapReader(String.format(fileName, year));
				System.out.println("Fires regarding year " + year + " were added");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




}
*/
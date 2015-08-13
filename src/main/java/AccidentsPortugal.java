import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import core.Config;
import core.load_data.DataStoreInfo;
import core.shared.Column;
import core.shared.Table;


public class AccidentsPortugal {

	private static final long BATCHINSERT_SIZE = Config.getConfigInt("insert_chunk_size");

	private static String insertStatement;

	private static CellProcessor[] getProcessors() {
												
		
		final CellProcessor[] processors = new CellProcessor[] { 
				new NotNull(), // Id__Aciden
				new NotNull(), // Datahora
				new NotNull(), // Ano
				new NotNull(), // Nr_Process
				new NotNull(), // Nordem
				new StrNotNullOrEmpty(), // Entidades
				new NotNull(), // Codposto
				new NotNull(), // Localizaco
				new NotNull(), // Distrito
				new NotNull(), // Distrito2
				new NotNull(), //Concelho
				new NotNull(), //Concelho2
				new Optional(), //COD_Distrito
				new Optional(), //COD_Concelho
				new Optional(), //COD_Freguesia
				new NotNull(), //Freguesia3
				new NotNull(), //Freguesia2
				new NotNull(), //Pov__Proxi
				new NotNull(), //Latitude
				new NotNull(), //Longitude
				new NotNull(), //Tipos_Vias
				new NotNull(), //Tipos_Vias2
				new NotNull(), //Cod_Via
				new NotNull(), //Cod_Via2
				new NotNull(), //Km
				new NotNull(), //Km2
				new NotNull(), //N_arruamen
				new NotNull(), //N_arruam_1
				new NotNull(), //Sentidos
				new NotNull(), //Fgraves								
				new NotNull(), //Fleves
				new NotNull(), //Mortos
				new NotNull(), //Natureza
				new NotNull(), //N_Ciclomot
				new NotNull(), //N_Ligeiros
				new NotNull(), //N_Outros__
				new NotNull(), //N_Pesados
				new NotNull(), //Mortos_30		
				new NotNull(), //FGraves30
				new NotNull(), //FLeves30
				new NotNull(), //F_Atmosfericos
				new NotNull(), //C_Luminiosidade		
		};

		return processors;
	}


	public static void createTable() {

		Table accPortugal = new Table("accidents_portugal", "pk_id");

		accPortugal.add(new Column("pk_id", false, false, "NUMERIC"));
		accPortugal.add(new Column("id_acident", false, false, "TEXT"));
		
		accPortugal.add(new Column("year", false, false, "NUMERIC"));
		accPortugal.add(new Column("month", false, false, "NUMERIC"));
		accPortugal.add(new Column("day", false, false, "NUMERIC"));
		accPortugal.add(new Column("hour", false, false, "NUMERIC"));
		accPortugal.add(new Column("minute", false, false, "NUMERIC"));
		
		accPortugal.add(new Column("nr_process", false, false, "TEXT"));
		accPortugal.add(new Column("nr_ordem", false, false, "TEXT"));
		
		accPortugal.add(new Column("entidades", false, false, "TEXT"));
		accPortugal.add(new Column("codposto", false, false, "TEXT"));
		accPortugal.add(new Column("localizaco", false, false, "TEXT"));
		accPortugal.add(new Column("distrito", false, false, "TEXT"));
		accPortugal.add(new Column("distrito2", false, false, "TEXT"));
		accPortugal.add(new Column("concelho", false, false, "TEXT"));	
		accPortugal.add(new Column("concelho2", false, false, "TEXT"));

		accPortugal.add(new Column("COD_Distrito", false, false, "TEXT"));
		accPortugal.add(new Column("COD_Concelho", false, false, "TEXT"));
		accPortugal.add(new Column("COD_Freguesia", false, false, "TEXT"));
		accPortugal.add(new Column("Freguesia3", false, false, "TEXT"));
		accPortugal.add(new Column("Freguesia2", false, false, "TEXT"));	
		accPortugal.add(new Column("Pov__Proxi", false, false, "TEXT"));
		
		accPortugal.add(new Column("latitude", false, false, "NUMERIC"));
		accPortugal.add(new Column("longitud", false, false, "NUMERIC"));
		accPortugal.add(new Column("Tipos_Vias", false, false, "TEXT"));
		accPortugal.add(new Column("Tipos_Vias2", false, false, "TEXT"));

		accPortugal.add(new Column("Cod_Via", false, false, "TEXT"));
		accPortugal.add(new Column("Cod_Via2", false, false, "TEXT"));
		accPortugal.add(new Column("Km", false, false, "NUMERIC"));
		accPortugal.add(new Column("Km2", false, false, "NUMERIC"));
		accPortugal.add(new Column("N_arruamen", false, false, "TEXT"));
		accPortugal.add(new Column("N_arruam_1", false, false, "TEXT"));
		accPortugal.add(new Column("Sentidos", false, false, "TEXT"));
		
		accPortugal.add(new Column("Fgraves", false, false, "NUMERIC"));
		accPortugal.add(new Column("Fleves", false, false, "NUMERIC"));
		accPortugal.add(new Column("Mortos", false, false, "NUMERIC"));
		accPortugal.add(new Column("Natureza", false, false, "TEXT"));

		accPortugal.add(new Column("N_Ciclomot", false, false, "NUMERIC"));
		accPortugal.add(new Column("N_Ligeiros", false, false, "NUMERIC"));
		accPortugal.add(new Column("N_Outros", false, false, "NUMERIC"));
		accPortugal.add(new Column("N_Pesados", false, false, "NUMERIC"));
		
		accPortugal.add(new Column("Mortes30", false, false, "NUMERIC"));
		accPortugal.add(new Column("FGraves30", false, false, "NUMERIC"));
		accPortugal.add(new Column("FLeves30", false, false, "NUMERIC"));
		accPortugal.add(new Column("F_Atmosfericos", false, false, "TEXT"));
		accPortugal.add(new Column("C_Luminiosidade", false, false, "TEXT"));
		
		Connection connection = DataStoreInfo.getMetaStore();
		accPortugal.createTable(connection);

		insertStatement = accPortugal.insertStatement();
		System.out.println(insertStatement);
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

				
				// Obter informacao
				String id = (String) line.get("Id__Aciden");
				ps.setInt(1, Integer.parseInt(id));
				
				String datahora = (String) line.get("Datahora");
				
				
				String[] temp = datahora.split(" ");
				String[] tempfirst = temp[0].split(":");
				String[] tempSecond = temp[1].split(":");
				
				ps.setInt(2, Integer.parseInt(tempfirst[0]));
				ps.setInt(3, Integer.parseInt(tempfirst[1]));
				ps.setInt(4, Integer.parseInt(tempfirst[2]));
				ps.setInt(5, Integer.parseInt(tempSecond[0]));
				ps.setInt(6, Integer.parseInt(tempSecond[1]));
				
				String ano = (String) line.get("Ano");
				String nrproces = (String) line.get("Nr_Process");
				String nordem= (String) line.get("Nordem");

				String entidades  = (String) line.get("Entidades");
				String codposto = (String) line.get("Codposto");
				String localizacao = (String) line.get("Localizaco");
				String distrito = (String) line.get("Distrito");
				String distrito2 = (String) line.get("Distrito2");

				String concelho = (String) line.get("Concelho");
				String concelho2 = (String) line.get("Concelho2");
				
				ps.setString(7, nrproces);
				ps.setString(8, nordem);
				
				ps.setString(9, entidades);
				ps.setString(10, codposto);
				ps.setString(11, localizacao);
				ps.setString(12, distrito);
				ps.setString(13, distrito2);
				ps.setString(14, concelho);
				ps.setString(15, concelho2);
				
				
				
				String coddistrito = (String) line.get("COD_Distrito");
				String codconcelho = (String) line.get("COD_Concelho");
				String codfreguesia = (String) line.get("COD_Freguesia");
				String freguesia3 = (String) line.get("Freguesia3");
				String freguesia2 = (String) line.get("Freguesia2");
				String pov_proxi = (String) line.get("Pov__Proxi");
				
				ps.setString(16, coddistrito);
				ps.setString(17, codconcelho);
				ps.setString(18, codfreguesia);
				ps.setString(19, freguesia3);
				ps.setString(20, freguesia2);
				ps.setString(21, pov_proxi);
				
				String latitude = (String) line.get("Latitude");
				latitude = latitude.replace(",", ".");
				String longitude = (String) line.get("Longitude");
				longitude = longitude.replace(",", ".");
				
				ps.setDouble(22, Double.parseDouble(latitude));
				ps.setDouble(23, Double.parseDouble(longitude));
				
				String tipovias = (String) line.get("Tipos_Vias");
				String tipovias2 = (String) line.get("Tipos_Vias2");
				
				ps.setString(24, tipovias);
				ps.setString(25, tipovias2);

				String codvia = (String) line.get("Cod_Via");
				String codvia2 = (String) line.get("Cod_Via2");
				
				ps.setString(26, codvia);
				ps.setString(27, codvia2);
				
				String km = (String) line.get("Km");
				km = km.replace(",", ".");
				String km2 = (String) line.get("Km2");
				km2 = km2.replace(",", ".");
				
				ps.setDouble(28, Double.parseDouble(km));
				ps.setDouble(29, Double.parseDouble(km2));
				
				String nrarruamen = (String) line.get("N_arruamen");
				String nrarruamen1 = (String) line.get("N_arruam_1");
				String sentidos = (String) line.get("Sentidos");
				
				ps.setString(30, nrarruamen);
				ps.setString(31, nrarruamen1);
				ps.setString(32, sentidos);
				
				
				
				String fgraves = (String) line.get("Fgraves");
				String fleves = (String) line.get("Fleves");
				String mortos = (String) line.get("Mortos");
				String natureza = (String) line.get("Natureza");
				String nciclomot = (String) line.get("N_Ciclomot");
				String nligeiros = (String) line.get("N_Ligeiros");
				String noutros = (String) line.get("N_Outros__");
				String npesados = (String) line.get("N_Pesados");
				
				ps.setInt(33, Integer.parseInt(fgraves));
				ps.setInt(34, Integer.parseInt(fleves));
				ps.setInt(35, Integer.parseInt(mortos));
				ps.setString(36, natureza);
				ps.setInt(37, Integer.parseInt(nciclomot));
				ps.setInt(38, Integer.parseInt(nligeiros));
				ps.setInt(39, Integer.parseInt(noutros));
				ps.setInt(40, Integer.parseInt(npesados));
				
				
				
				String mortos30 = (String) line.get("Mortos_30");
				String fgraves30 = (String) line.get("FGraves30");
				String fleves30 = (String) line.get("FLeves30");
				
				ps.setInt(41, Integer.parseInt(mortos30));
				ps.setInt(42, Integer.parseInt(fgraves30));
				ps.setInt(43, Integer.parseInt(fleves30));
				
				String fatmosfericos = (String) line.get("F_Atmosfericos");
				String cluminosidade = (String) line.get("C_Luminiosidade");
				
				ps.setString(44, fatmosfericos);
				ps.setString(45, cluminosidade);

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
		
		String fileName = "data/AccidentsPortugal/accidentsPortugal.csv";
		AccidentsPortugal.createTable();
		
		try {
			AccidentsPortugal.readAndWriteWithCsvMapReader(fileName);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




}

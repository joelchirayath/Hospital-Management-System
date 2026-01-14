package giis.demo.util;

import java.io.FileInputStream;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;

import giis.demo.jdbc.ICD10Manager.ICD10DataImporter;



/**
 * Encapsula los datos de acceso JDBC, lectura de la configuracion y scripts de
 * base de datos para creacion y carga.
 */
public class Database extends DbUtil {
	// Localizacion de ficheros de configuracion y carga de bases de datos
	private static final String APP_PROPERTIES = "src/main/resources/application.properties";
	private static final String SQL_SCHEMA = "src/main/resources/schema.sql";
	private static final String SQL_LOAD = "src/main/resources/data.sql";
	// parametros de la base de datos leidos de application.properties (base de
	// datos local sin usuario/password)
	private String driver;
	private String url;
	private static boolean databaseCreated = false;
	private static boolean dataLoaded = false; // NEW: ensure data.sql runs only once


	/**
	 * Crea una instancia, leyendo los parametros de driver y url de
	 * application.properties
	 */
	public Database() {
		Properties prop = new Properties();
		try (FileInputStream fs = new FileInputStream(APP_PROPERTIES)) {
			prop.load(fs);
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
		driver = prop.getProperty("datasource.driver");
		url = prop.getProperty("datasource.url");
		if (driver == null || url == null) {
			throw new ApplicationException("Configuracion de driver y/o url no encontrada en application.properties");
		}
		
		DbUtils.loadDriver(driver);
	}

	@Override
	public String getUrl() {
		return url;
	}

	/**
	 * Creacion de una base de datos limpia a partir del script schema.sql en
	 * src/main/properties (si onlyOnce=true solo ejecutara el script la primera vez
	 */
	public void createDatabase(boolean onlyOnce) {
		// actua como singleton si onlyOnce=true: solo la primera vez que se instancia
		// para mejorar rendimiento en pruebas
		if (!databaseCreated || !onlyOnce) {

			executeScript(SQL_SCHEMA);

			databaseCreated = true; // NOSONAR
		}
	}

	/**
	 * Carga de datos iniciales a partir del script data.sql en src/main/properties
	 * (si onlyOnce=true solo ejecutara el script la primera vez
	 */
	public void loadDatabase() {
	    // Only load seed data once per JVM
	    if (!dataLoaded) {
	        executeScript(SQL_LOAD);

	        ICD10DataImporter importer = new ICD10DataImporter();
	        importer.importICD10Data("src/main/java/giis/demo/jdbc/ICD10Manager/icd.csv");

	        dataLoaded = true; // mark as loaded
	        System.out.println("[Database] Initial data loaded from data.sql");
	    } else {
	        System.out.println("[Database] Data already loaded, skipping data.sql");
	    }
	}


	public void executeBatchUpdate(String sql, Object[][] paramsArray) {
		Connection conn = null;
		try {

			conn = DriverManager.getConnection(getUrl());

			conn.setAutoCommit(false);

			QueryRunner runner = new QueryRunner();

			runner.batch(conn, sql, paramsArray);

			conn.commit();
		} catch (SQLException e) {

			DbUtils.rollbackAndCloseQuietly(conn);
			throw new UnexpectedException(e);
		} finally {

			DbUtils.closeQuietly(conn);
		}
	}

}

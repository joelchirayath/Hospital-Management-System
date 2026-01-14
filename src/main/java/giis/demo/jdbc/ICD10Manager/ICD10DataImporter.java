package giis.demo.jdbc.ICD10Manager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

/**
 * Clase para importar datos del ICD-10 desde un archivo CSV a la base de datos
 */
public class ICD10DataImporter {

	private Database database;

	public ICD10DataImporter() {
		this.database = new Database();
	}

	/**
	 * Importa los datos del ICD-10 desde un archivo CSV
	 * 
	 * @param csvFilePath Ruta al archivo CSV con los datos del ICD-10
	 */
	public void importICD10Data(String csvFilePath) {
		System.out.println("Iniciando importación de datos ICD-10 desde: " + csvFilePath);

		List<ICD10Record> records = readCSVFile(csvFilePath);

		if (records.isEmpty()) {
			System.out.println("No se encontraron registros para importar.");
			return;
		}

		insertICD10Data(records);

		System.out.println("Importación completada. Total de registros procesados: " + records.size());
	}

	/**
	 * Lee el archivo CSV y parsea los datos
	 */
	private List<ICD10Record> readCSVFile(String csvFilePath) {
		List<ICD10Record> records = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
			String line;
			boolean isFirstLine = true;

			while ((line = br.readLine()) != null) {
				// Saltar la línea de encabezado
				if (isFirstLine) {
					isFirstLine = false;
					continue;
				}

				ICD10Record record = parseCSVLine(line);
				if (record != null) {
					records.add(record);
				}
			}

		} catch (IOException e) {
			throw new ApplicationException("Error al leer el archivo CSV: " + e.getMessage());
		}

		return records;
	}

	/**
	 * Parsea una línea del CSV
	 */
	private ICD10Record parseCSVLine(String line) {
		try {
			// Dividir la línea por comas
			String[] parts = line.split(",", 4); // Máximo 4 partes

			if (parts.length < 4) {
				System.out.println("Línea ignorada (formato incorrecto): " + line);
				return null;
			}

			ICD10Record record = new ICD10Record();
			record.setChapter(cleanField(parts[0]));
			record.setSection(cleanField(parts[1]));
			record.setCode(cleanField(parts[2]));
			record.setDescription(cleanField(parts[3]));

			return record;

		} catch (Exception e) {
			System.out.println("Error al parsear línea: " + line + " - " + e.getMessage());
			return null;
		}
	}

	/**
	 * Limpia el campo de comillas y espacios
	 */
	private String cleanField(String field) {
		if (field == null || field.trim().isEmpty()) {
			return "";
		}
		// Remover comillas dobles si están presentes
		return field.trim().replaceAll("^\"|\"$", "");
	}

	/**
	 * Inserta los datos en la base de datos
	 */
	private void insertICD10Data(List<ICD10Record> records) {
		// SQL con la columna 'section'
		String sql = "INSERT INTO icd10_codes (code, description, chapter, section) VALUES (?, ?, ?, ?)";

		// Preparar el array de parámetros para batch update
		Object[][] paramsArray = new Object[records.size()][4];

		for (int i = 0; i < records.size(); i++) {
			ICD10Record record = records.get(i);
			paramsArray[i][0] = record.getCode();
			paramsArray[i][1] = record.getDescription();
			paramsArray[i][2] = record.getChapter(); // category = chapter
			paramsArray[i][3] = record.getSection(); // section = section
		}

		try {
			database.executeBatchUpdate(sql, paramsArray);
			System.out.println("Datos insertados correctamente en la tabla icd10_codes");
		} catch (Exception e) {
			// Si falla el batch, intentar inserción individual para identificar errores
			System.out.println("Error en inserción por lotes, intentando inserción individual...");
			insertIndividualRecords(records, sql);
		}
	}

	/**
	 * Inserta registros individualmente para identificar errores específicos
	 */
	private void insertIndividualRecords(List<ICD10Record> records, String sql) {
		int successCount = 0;
		int errorCount = 0;

		try (Connection conn = DriverManager.getConnection(database.getUrl());
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			for (ICD10Record record : records) {
				try {
					pstmt.setString(1, record.getCode());
					pstmt.setString(2, record.getDescription());
					pstmt.setString(3, record.getChapter());
					pstmt.setString(4, record.getSection());
					pstmt.executeUpdate();
					successCount++;
				} catch (SQLException e) {
					errorCount++;
					System.out.println("Error insertando código: " + record.getCode() + " - " + e.getMessage());
				}
			}

			System.out.println(
					"Inserción individual completada: " + successCount + " éxitos, " + errorCount + " errores");

		} catch (SQLException e) {
			throw new ApplicationException("Error en la conexión a la base de datos: " + e.getMessage());
		}
	}

	/**
	 * Clase interna para representar un registro del ICD-10
	 */
	private static class ICD10Record {
		private String chapter;
		private String section;
		private String code;
		private String description;

		public String getChapter() {
			return chapter;
		}

		public void setChapter(String chapter) {
			this.chapter = chapter;
		}

		public String getSection() {
			return section;
		}

		public void setSection(String section) {
			this.section = section;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	/**
	 * Método main para ejecutar la importación directamente
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Uso: java ICD10DataImporter <ruta_al_archivo_csv>");
			return;
		}

		String csvFilePath = args[0];
		ICD10DataImporter importer = new ICD10DataImporter();

		try {
			importer.database.createDatabase(true);
			importer.importICD10Data(csvFilePath);
		} catch (Exception e) {
			System.err.println("Error durante la importación: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
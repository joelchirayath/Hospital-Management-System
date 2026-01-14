package giis.demo.jdbc.models.apointment.disease;

import java.util.ArrayList;
import java.util.List;

import giis.demo.service.appointment.icd10.ICDDTO;
import giis.demo.util.Database;

public class ICDModel {

	private Database db;

	public ICDModel() {
		this.db = new Database();
		db.createDatabase(true);
		db.loadDatabase();
	}

	public List<String> getAllSections() {
		List<String> sections = new ArrayList<>();
		try {
			String sql = "SELECT DISTINCT section FROM icd10_codes ORDER BY section";
			List<Object[]> results = db.executeQueryArray(sql);

			for (Object[] row : results) {
				sections.add((String) row[0]);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error getting sections from database");
		}
		return sections;
	}

	public List<ICDDTO> searchDiseases(String searchTerm, String section, String chapter) {
		List<ICDDTO> diseases = new ArrayList<>();

		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT code, description, chapter, section");
			sql.append(" FROM icd10_codes WHERE 1=1");

			List<Object> params = new ArrayList<>();

			// Filtro por searchTerm (description o category)
			if (searchTerm != null && !searchTerm.trim().isEmpty()) {
				String searchPattern = "%" + searchTerm.trim() + "%";
				sql.append(" AND (description LIKE ? OR chapter LIKE ?)");
				params.add(searchPattern);
				params.add(searchPattern);
			}

			// Filtro por section
			if (section != null && !section.trim().isEmpty()) {
				sql.append(" AND section = ?");
				params.add(section.trim());
			}

			// Filtro por chapter
			if (chapter != null && !chapter.trim().isEmpty()) {
				sql.append(" AND chapter = ?");
				params.add(chapter.trim());
			}

			sql.append(" ORDER BY code");

			List<Object[]> results = db.executeQueryArray(sql.toString(), params.toArray());

			for (Object[] row : results) {
				ICDDTO disease = new ICDDTO((String) row[0], // code
						(String) row[1], // description
						(String) row[2], // category
						(String) row[3] // section
				);
				// Si tu ICDDTO tiene campo chapter, agrégalo aquí
				diseases.add(disease);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error searching diseases in database");
		}
		return diseases;
	}

	public List<ICDDTO> getAllDiseases() {
		List<ICDDTO> diseases = new ArrayList<>();

		try {
			String sql = "SELECT code, description, chapter, section chapter FROM icd10_codes ORDER BY code";
			List<Object[]> results = db.executeQueryArray(sql);

			for (Object[] row : results) {
				ICDDTO disease = new ICDDTO((String) row[0], // code
						(String) row[1], // description
						(String) row[2], // category
						(String) row[3] // section
				);
				diseases.add(disease);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error getting all diseases from database");
		}
		return diseases;
	}

	// Método sobrecargado para compatibilidad con versiones anteriores
	public List<ICDDTO> searchDiseases(String searchTerm, String section) {
		return searchDiseases(searchTerm, section, null);
	}
}
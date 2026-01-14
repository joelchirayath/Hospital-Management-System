package giis.demo.jdbc.models.patient.adder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import giis.demo.service.patient.PatientDTO;
import giis.demo.util.Database;

public class AddPatientModelImpl implements AddPatientModel {

	private Database db;
    
    public AddPatientModelImpl() {
        this.db = new Database();
        db.createDatabase(true);
        db.loadDatabase();
    }
	
	@Override
	public Optional<List<PatientDTO>> getPossiblePatients(String string) {
		String searchTerm = "%" + string + "%";
	    List<PatientDTO> patients = new ArrayList<>();
	    
	    try {
	        String sql = "SELECT id, name, surname, dni, phone, email, address, date_of_birth, gender " +
	                    "FROM patients " +
	                    "WHERE name LIKE ? OR surname LIKE ? OR dni LIKE ? " +
	                    "ORDER BY name, surname";
	        
	        List<Object[]> results = db.executeQueryArray(sql, searchTerm, searchTerm, searchTerm);
	        
	        for (Object[] row : results) {
	            PatientDTO patient = new PatientDTO();
	            patient.setId((Integer) row[0]);
	            patient.setName((String) row[1]);
	            patient.setSurname((String) row[2]);
	            patient.setDni((String) row[3]);
	            patient.setPhone((String) row[4]);
	            patient.setEmail((String) row[5]);
	            patient.setAddress((String) row[6]);
	            patient.setDate_of_birth((String) row[7]);
	            patient.setGender((String) row[8]);
	            
	            patients.add(patient);
	        }
	        
	        return Optional.of(patients);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.print("No patients found ");
	        return Optional.of(new ArrayList<>()); 
	    }
	}

	@Override
    public void createPatient(PatientDTO dto) {
        try {
            String sql = "INSERT INTO patients (name, surname, dni, phone, email, address, date_of_birth, gender) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            db.executeUpdate(sql, 
                dto.getName(),
                dto.getSurname(),
                dto.getDni(),
                dto.getPhone(),
                dto.getEmail(),
                dto.getAddress(),
                dto.getDate_of_birth(),
                dto.getGender()
            );
            
            System.out.println("MODEL Patient created successfully: " + dto.getFullName());
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("MODEL Error creating patient: " + e.getMessage(), e);
        }
    }

	@Override
    public void updatePatient(PatientDTO dto) {
        try {
            String sql = "UPDATE patients SET name = ?, surname = ?, phone = ?, email = ?, " +
                        "address = ?, date_of_birth = ?, gender = ? " +
                        "WHERE id = ? AND dni = ?";
            
             db.executeUpdate(sql,
                dto.getName(),
                dto.getSurname(),
                dto.getPhone(),
                dto.getEmail(),
                dto.getAddress(),
                dto.getDate_of_birth(),
                dto.getGender(),
                dto.getId(),
                dto.getDni()
            );
            
            
            System.out.println("MODEL Patient updated successfully: " + dto.getFullName());
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("MODEL Error updating patient: " + e.getMessage(), e);
        }
    }
	
}

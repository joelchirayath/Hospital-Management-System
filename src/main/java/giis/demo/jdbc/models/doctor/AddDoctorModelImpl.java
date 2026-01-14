package giis.demo.jdbc.models.doctor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import giis.demo.service.doctor.DoctorDTO;
import giis.demo.util.Database;

public class AddDoctorModelImpl implements AddDoctorModel {

	private Database db;
    
    public AddDoctorModelImpl() {
        this.db = new Database();
        db.createDatabase(true);
        db.loadDatabase();
    }
	
	@Override
	public Optional<List<DoctorDTO>> getPossibleDoctor(String string) {
		String searchTerm = "%" + string + "%";
	    List<DoctorDTO> doctors = new ArrayList<>();
		
	    try {
	    	String sql = "SELECT id, dni, name, surname, email, specialization " +
                    "FROM doctors " + 
                    "WHERE name LIKE ? OR surname LIKE ? OR dni LIKE ? " +
                    "ORDER BY name, surname";
	        
	        List<Object[]> results = db.executeQueryArray(sql, searchTerm, searchTerm, searchTerm);
	        
	        for (Object[] row : results) {
	        	DoctorDTO doctor = new DoctorDTO();
	            doctor.setId((Integer) row[0]);
	            doctor.setDni((String) row[1]); 
	            doctor.setName((String) row[2]);
	            doctor.setSurname((String) row[3]);
	            doctor.setEmail((String) row[4]);
	            doctor.setSpecialization((String) row[5]);
	          
	            
	            doctors.add(doctor);

	        }
	        
	        return Optional.of(doctors);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.print("No doctors found ");
	        return Optional.of(new ArrayList<>()); 
	    }
		
	}

}

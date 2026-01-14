package giis.demo.service.patient;

import java.util.List;
import java.util.Optional;

import giis.demo.jdbc.models.patient.adder.AddPatientModel;
import giis.demo.jdbc.models.patient.adder.AddPatientModelImpl;

public class PatientAdder {
	
	private AddPatientModel model;
	
	

	public PatientAdder() {
		model = new AddPatientModelImpl();
	}



	public Optional<List<PatientDTO>> getpossiblePatients(String string) {
		return model.getPossiblePatients(string);
		
	}



	public void createPatient(PatientDTO dto) {
		model.createPatient(dto);
		
	}



	public void updatePatient(PatientDTO dto) {
		model.updatePatient(dto);
		
	}
	
	

}

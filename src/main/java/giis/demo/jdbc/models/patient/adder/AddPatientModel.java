package giis.demo.jdbc.models.patient.adder;

import java.util.List;
import java.util.Optional;

import giis.demo.service.patient.PatientDTO;

public interface AddPatientModel {

	Optional<List<PatientDTO>> getPossiblePatients(String string);

	void createPatient(PatientDTO dto);

	void updatePatient(PatientDTO dto);

}

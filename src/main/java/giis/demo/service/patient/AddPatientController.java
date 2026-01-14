package giis.demo.service.patient;

import java.util.List;
import java.util.Optional;

public interface AddPatientController {

	Optional<List<PatientDTO>> getpossiblePatients(String string);
	
	void crerateNewPatient(PatientDTO nuevoPaciente) ;
	
   void updatePatientInfo(PatientDTO pacienteActualizado);
}

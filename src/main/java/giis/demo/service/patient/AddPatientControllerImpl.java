package giis.demo.service.patient;

import java.util.List;
import java.util.Optional;

public class AddPatientControllerImpl implements AddPatientController {

	
	private PatientAdder adder;
	
	public AddPatientControllerImpl() {
		adder = new PatientAdder();
	}

	@Override
	public Optional<List<PatientDTO>> getpossiblePatients(String string) {
		return adder.getpossiblePatients(string);
	}

	@Override
	public void updatePatientInfo(PatientDTO dto) {
		adder.updatePatient(dto);
		
	}

	@Override
	public void crerateNewPatient(PatientDTO dto) {
		adder.createPatient(dto);
		
	}

}

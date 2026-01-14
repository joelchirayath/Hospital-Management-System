package giis.demo.service.doctor;

import java.util.List;
import java.util.Optional;

public class AddDoctorControllerImpl implements AddDoctorController {

	private DoctorAdder adder;
	
	
	
	public AddDoctorControllerImpl() {
		this.adder = new DoctorAdder();
	}



	@Override
	public Optional<List<DoctorDTO>> getpossibleDoctors(String string) {
		
		return adder.getPossibleDoctors(string);
	}

}

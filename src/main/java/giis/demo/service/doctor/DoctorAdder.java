package giis.demo.service.doctor;

import java.util.List;
import java.util.Optional;

import giis.demo.jdbc.models.doctor.AddDoctorModel;
import giis.demo.jdbc.models.doctor.AddDoctorModelImpl;

public class DoctorAdder {

	private AddDoctorModel model;
	
	
	public DoctorAdder() {
		
		this.model = new AddDoctorModelImpl();
	}


	public Optional<List<DoctorDTO>> getPossibleDoctors(String string) {
		
		return model.getPossibleDoctor(string);
	}

}

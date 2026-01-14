package giis.demo.jdbc.models.doctor;

import java.util.List;
import java.util.Optional;

import giis.demo.service.doctor.DoctorDTO;

public interface AddDoctorModel {

	Optional<List<DoctorDTO>> getPossibleDoctor(String string);

}

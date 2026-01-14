package giis.demo.service.doctor;

import java.util.List;
import java.util.Optional;

public interface AddDoctorController {

	Optional<List<DoctorDTO>> getpossibleDoctors(String string);

}

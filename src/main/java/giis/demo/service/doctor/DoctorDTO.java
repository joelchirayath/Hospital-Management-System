package giis.demo.service.doctor;

import java.time.LocalDateTime;

public class DoctorDTO {
    private Integer id;
    private String dni;
    private String name;
    private String surname;
    private String email;
    private String specialization;
    private LocalDateTime createdAt;

    // Default constructor
    public DoctorDTO() {
    }

    // Constructor with all fields except id and createdAt
    public DoctorDTO(String dni, String name, String surname, String email, String specialization) {
        this.dni = dni;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.specialization = specialization;
    }

    // Constructor with all fields
    public DoctorDTO(Integer id, String dni, String name, String surname, String email, String specialization, LocalDateTime createdAt) {
        this.id = id;
        this.dni = dni;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.specialization = specialization;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Utility methods
    public String getFullName() {
        return name + " " + surname;
    }

    // toString method for debugging and display
    @Override
    public String toString() {
        return getFullName() + " ( " +dni +" )";
    }

    // equals and hashCode based on dni (unique identifier)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DoctorDTO doctorDTO = (DoctorDTO) o;

        return dni != null ? dni.equals(doctorDTO.dni) : doctorDTO.dni == null;
    }

    @Override
    public int hashCode() {
        return dni != null ? dni.hashCode() : 0;
    }

    // Builder pattern for fluent object creation
    public static class Builder {
        private Integer id;
        private String dni;
        private String name;
        private String surname;
        private String email;
        private String specialization;
        private LocalDateTime createdAt;

        public Builder withId(Integer id) {
            this.id = id;
            return this;
        }

        public Builder withDni(String dni) {
            this.dni = dni;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withSurname(String surname) {
            this.surname = surname;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withSpecialization(String specialization) {
            this.specialization = specialization;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public DoctorDTO build() {
            DoctorDTO doctorDTO = new DoctorDTO();
            doctorDTO.setId(this.id);
            doctorDTO.setDni(this.dni);
            doctorDTO.setName(this.name);
            doctorDTO.setSurname(this.surname);
            doctorDTO.setEmail(this.email);
            doctorDTO.setSpecialization(this.specialization);
            doctorDTO.setCreatedAt(this.createdAt);
            return doctorDTO;
        }
    }

    // Static factory method for builder
    public static Builder builder() {
        return new Builder();
    }
}
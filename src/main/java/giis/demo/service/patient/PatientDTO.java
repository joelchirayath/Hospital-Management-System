package giis.demo.service.patient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PatientDTO {
    private Integer id;
    private String name;
    private String surname;
    private String dni;
    private String phone;
    private String email;
    private String address;
    private String date_of_birth;
    private String gender;
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getDate_of_birth() { return date_of_birth; }
    public void setDate_of_birth(String date_of_birth) { this.date_of_birth = date_of_birth; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getFullName() {
        return name + " " + surname;
    }
    
    public Date getBirthDate() {
        if (date_of_birth == null || date_of_birth.isEmpty()) {
            return null;
        }
        try {
            // El formato debe coincidir con cómo se almacenan las fechas en tu BD (yyyy-MM-dd)
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.parse(date_of_birth);
        } catch (ParseException e) {
            System.err.println("Error parsing birth date: " + date_of_birth);
            return null;
        }
    }
    
    @Override
    public String toString() {
    	return getFullName() + " ( " + dni +" )";
    }
}
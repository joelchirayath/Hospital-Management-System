-- Desactivar claves foráneas temporalmente
PRAGMA foreign_keys = OFF;

-- ===========================================
-- ========== ELIMINAR TODAS LAS TABLAS ======
-- ===========================================
-- Primero tablas hijas (con dependencias)
DROP TABLE IF EXISTS clinical_order_messages;
DROP TABLE IF EXISTS clinical_orders;
DROP TABLE IF EXISTS inventory_orders;
DROP TABLE IF EXISTS assigned_prescriptions;
DROP TABLE IF EXISTS prescribed_medications;
DROP TABLE IF EXISTS patient_contact_info;
DROP TABLE IF EXISTS vaccine_appointment_vaccine;
DROP TABLE IF EXISTS vaccine_appointment;
DROP TABLE IF EXISTS vaccines;
DROP TABLE IF EXISTS consultation_causes;
DROP TABLE IF EXISTS personal_problems;
DROP TABLE IF EXISTS familiar_antecedents;
DROP TABLE IF EXISTS medical_records;
DROP TABLE IF EXISTS doctors_appointments;
DROP TABLE IF EXISTS consultation_cause_options;
DROP TABLE IF EXISTS familiar_antecedent_options;
DROP TABLE IF EXISTS personal_problem_options;

-- Luego tablas intermedias
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS working_day;
DROP TABLE IF EXISTS medications;
DROP TABLE IF EXISTS prescriptions;
DROP TABLE IF EXISTS inventory_items;

-- Finalmente tablas maestras (padres)
DROP TABLE IF EXISTS icd10_codes;
DROP TABLE IF EXISTS vaccine_model;
DROP TABLE IF EXISTS nurses;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS doctors;
CREATE TABLE doctors (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dni TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    surname TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    specialization TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE patients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    surname TEXT NOT NULL,
    dni TEXT UNIQUE NOT NULL,
    phone TEXT,
    email TEXT,
    address TEXT,
    date_of_birth DATE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    gender TEXT
);
CREATE TABLE rooms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    location TEXT
);
CREATE TABLE nurses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dni TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    surname TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE icd10_codes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code VARCHAR(10) NOT NULL,
    description TEXT NOT NULL,
    chapter TEXT,
    section TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE vaccine_model (
    vaccine_name TEXT PRIMARY KEY NOT NULL,
    recommended_doses INTEGER
);
CREATE TABLE appointments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date TEXT NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    office TEXT NOT NULL,
    patient_id INTEGER NOT NULL,
    notes TEXT,
    status TEXT DEFAULT 'scheduled',
    urgent INTEGER DEFAULT 0,     
    attended INTEGER DEFAULT NULL,
    check_in_time TEXT,
    check_out_time TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id)
);
CREATE TABLE working_day (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    dni         TEXT NOT NULL,
    worker_type TEXT NOT NULL CHECK(worker_type IN ('Doctor', 'Nurse')),
    dayOfweek   TEXT NOT NULL,
    day         TEXT NOT NULL,
    month       TEXT NOT NULL,
    year        TEXT NOT NULL,
    start       TEXT NOT NULL,
    end         TEXT NOT NULL,
    type        TEXT NOT NULL
);
CREATE TABLE prescriptions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE medications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    medication_name TEXT NOT NULL,
    amount INTEGER NOT NULL,
    duration INTEGER NOT NULL,
    interval_hours  INTEGER NOT NULL
);
CREATE TABLE inventory_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    item_name TEXT NOT NULL,
    category TEXT NOT NULL,            
    subcategory TEXT,                  
    model_code TEXT,                   
    batch_number TEXT,                 
    location TEXT,                     
    quantity_on_hand INTEGER NOT NULL DEFAULT 0,
    minimum_quantity INTEGER NOT NULL DEFAULT 0,
    expiry_date DATE,                                         
    supplier TEXT,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE medical_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    appointment_id INTEGER NOT NULL,
    doctor_id INTEGER NOT NULL,
    icd10_code VARCHAR(10) NOT NULL,
    diagnosis_date TEXT NOT NULL,
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (icd10_code) REFERENCES icd10_codes(code)
);
CREATE TABLE doctors_appointments (
    id_doctors INT NOT NULL,
    id_appointments INT NOT NULL,
    PRIMARY KEY (id_doctors, id_appointments),
    FOREIGN KEY (id_doctors) REFERENCES doctors(id),
    FOREIGN KEY (id_appointments) REFERENCES appointments(id)
);
CREATE TABLE familiar_antecedents (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    antecedent TEXT NOT NULL,
    notes TEXT,
    doctor_id INTEGER NOT NULL,
    doctor_name TEXT NOT NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);
CREATE TABLE personal_problems (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    problem TEXT NOT NULL,
    notes TEXT,
    doctor_id INTEGER NOT NULL,
    doctor_name TEXT NOT NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);
CREATE TABLE consultation_causes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    appointment_id INTEGER NOT NULL,
    cause TEXT NOT NULL,
    notes TEXT,
    doctor_id INTEGER NOT NULL,
    doctor_name TEXT NOT NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);
CREATE TABLE personal_problem_options (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    problem TEXT NOT NULL UNIQUE
);
CREATE TABLE familiar_antecedent_options (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    antecedent TEXT NOT NULL UNIQUE
);
CREATE TABLE consultation_cause_options (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cause TEXT NOT NULL UNIQUE
);
CREATE TABLE vaccines (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    vaccine_name TEXT NOT NULL,
    dose_number INTEGER NOT NULL,
    scheduled_date TEXT NOT NULL,
    administered_date TEXT,
    administered_by_doctor_id INTEGER,
    administered_by_doctor_name TEXT,
    needs_booster INTEGER DEFAULT 0, 
    status TEXT DEFAULT 'scheduled', 
    notes TEXT,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (administered_by_doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (vaccine_name) REFERENCES vaccine_model(vaccine_name)
);
CREATE TABLE vaccine_appointment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    doctor_id INTEGER NOT NULL,
    patient_id INTEGER NOT NULL,
    date TEXT NOT NULL, 
    hour TEXT NOT NULL, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (patient_id) REFERENCES patients(id)
);
CREATE TABLE vaccine_appointment_vaccine (
    vaccine_appointment_id INTEGER NOT NULL,
    vaccine_name TEXT NOT NULL,
    dose_type TEXT NOT NULL, 
    PRIMARY KEY (vaccine_appointment_id, vaccine_name),
    FOREIGN KEY (vaccine_appointment_id) REFERENCES vaccine_appointment(id) ON DELETE CASCADE,
    FOREIGN KEY (vaccine_name) REFERENCES vaccine_model(vaccine_name)
);
CREATE TABLE assigned_prescriptions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    appointment_id INTEGER NOT NULL,
    prescription_id INTEGER NOT NULL,
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(id)
);
CREATE TABLE prescribed_medications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    medication_id INTEGER NOT NULL,
    appointment_id INTEGER NOT NULL,
    prescribed_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    amount INTEGER NOT NULL,
    duration INTEGER NOT NULL,
    interval_hours  INTEGER NOT NULL,
    notes TEXT,
    FOREIGN KEY (medication_id) REFERENCES medications(id),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) 
);
CREATE TABLE patient_contact_info (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    appointment_id INTEGER NOT NULL,
    phone TEXT,
    email TEXT,
    address TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    UNIQUE(patient_id, appointment_id)
);
CREATE TABLE inventory_orders (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    inventory_item_id INTEGER NOT NULL,
    item_name         TEXT NOT NULL,
    model_code        TEXT,
    quantity_in_stock INTEGER NOT NULL,
    quantity_ordered  INTEGER NOT NULL,
    status            TEXT NOT NULL DEFAULT 'Pending',
    supplier          TEXT,                              
    requested_by      TEXT,
    order_date        DATETIME DEFAULT CURRENT_TIMESTAMP,
    notes             TEXT,
    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
);
CREATE TABLE clinical_orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    appointment_id INTEGER NOT NULL,
    requesting_doctor_id INTEGER NOT NULL,
    assigned_doctor_id INTEGER NOT NULL,
    concept TEXT NOT NULL,
    initial_description TEXT NOT NULL,
    status TEXT DEFAULT 'open', 
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    closed_at DATETIME,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (requesting_doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (assigned_doctor_id) REFERENCES doctors(id)
);
CREATE TABLE clinical_order_messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    clinical_order_id INTEGER NOT NULL,
    doctor_id INTEGER NOT NULL,
    message_text TEXT NOT NULL,
    file_name TEXT,
    file_data BLOB,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (clinical_order_id) REFERENCES clinical_orders(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

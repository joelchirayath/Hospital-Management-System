-- Datos para carga inicial de la base de datos

-- Insert doctors
DELETE FROM doctors;
INSERT INTO doctors (dni, name, surname, email, specialization) VALUES
('23456789B', 'María', 'González Pérez', 'mariagonzalez.oviedohospital@gmail.com', 'Cardiology'),
('34567890C', 'Carlos', 'Martínez López', 'carlosmartinez.oviedohospital@gmail.com', 'Pediatrics'),
('45678901D', 'Ana', 'Rodríguez Sánchez', 'anarodriguez.oviedohosital@protonmail.com', 'Dermatology'),
('56789012E', 'Javier', 'Gómez Ruiz', 'javier.gomez@hospital.com', 'Neurology'),
('67890123F', 'Laura', 'Díaz Castro', 'laura.diaz@hospital.com', 'Oncology'),
('78901234G', 'Pablo', 'Vázquez Gil', 'pablo.vazquez@hospital.com', 'Ophthalmology'),
('89012345H', 'Sofía', 'Hernández Mora', 'sofia.hernandez@hospital.com', 'Gastroenterology'),
('90123456I', 'David', 'López Torres', 'daniel.lopez@hospital.com', 'Traumatology');

-- Insert patients
DELETE FROM patients;
INSERT INTO patients (name, surname, dni, phone, email, date_of_birth, gender, address) VALUES
('Ana', 'García López', '12345678A', '600111222', 'ana.garcia@email.com', '1985-03-15', 'Female', 'Road of the Duck, 35'),
('Anastasia', 'Marin Calma', '12340099A', '600123123', 'anastasia.m.c@email.com', '1993-10-09', 'Female', 'Big Place Street, 6'),
('Carlos', 'Martínez Ruiz', '87654321B', '600333444', 'carlos.martinez@email.com', '1978-07-22', 'Male', 'Sweet street, 3'),
('Elena', 'Fernández Castro', '11223344C', '600555666', 'elena.fernandez@email.com', '1990-11-30', 'Female', '5th avenue, 105'),
('Miguel', 'Rodríguez Santos', '55667788D', '600777888', 'miguel.rodriguez@email.com', '1982-05-10', 'Male', 'Road of the Gold , 17'),
('David', 'Sánchez Ruiz', '44556677F', '600123456', 'david.sanchez@email.com', '1988-09-18', 'Male', 'mistery Road, 67');

-- Insert appointments
DELETE FROM appointments;
INSERT INTO appointments (date, start_time, end_time, office, patient_id, notes, attended) VALUES
('2025-12-04', '09:00', '09:30', 'General Medicine Room 1', 1, 'Routine checkup', NULL),
('2025-12-04', '10:00', '10:30', 'Pediatrics Office', 2, 'Follow-up visit', NULL),
('2025-12-04', '11:00', '11:30', 'Cardiology Room', 3, 'Vaccination', NULL),
('2025-12-04', '09:00', '09:30', 'Orthopedics Office', 4, 'Consultation', NULL),
('2025-12-04', '14:00', '14:45', 'Cardiology Room', 5, 'Annual physical', NULL),
('2025-12-04', '10:30', '11:15', 'Radiology Room', 1, 'Pain consultation', NULL),
('2025-12-04', '12:00', '12:30', 'Radiology Room', 2, 'Blood pressure check', NULL),
('2025-12-04', '15:30', '16:00', 'Pediatrics Office', 3, 'Dermatology follow-up', NULL),
('2025-12-04', '16:15', '16:45', 'ENT (Ear, Nose, Throat)', 4, 'Dietary consultation', NULL),
('2025-12-04', '09:15', '09:45', 'Pediatrics Office', 5, 'Routine screening', NULL),
('2025-12-04', '11:30', '12:00', 'Cardiology Room', 1, 'Post-surgery checkup', NULL),
('2025-12-04', '08:45', '09:15', 'ENT (Ear, Nose, Throat)', 2, 'Dental cleaning', NULL),
('2025-12-04', '13:00', '13:30', 'Orthopedics Office', 3, 'Cardiology review', NULL),
('2025-12-04', '10:45', '11:15', 'Radiology Room', 1, 'Allergy consultation', NULL),
('2025-12-04', '15:00', '15:30', 'Pediatrics Office', 2, 'Vision test', NULL),
('2025-12-04', '09:30', '10:00', 'ENT (Ear, Nose, Throat)', 3, 'Neurology evaluation', NULL);


-- Insert sample ICD-10 codes
DELETE FROM icd10_codes;

-- CORREGIDO: Solo appointments que existen (1-6)
DELETE FROM medical_records;
INSERT INTO medical_records (patient_id, appointment_id, doctor_id, icd10_code, diagnosis_date, notes) VALUES
(1, 1, 1, 'I10', '2024-01-15', 'Stage 1 hypertension, lifestyle modifications recommended'),
(1, 2, 1, 'E11', '2024-03-20', 'Type 2 diabetes, controlled with metformin'),
(2, 3, 2, 'J45', '2024-02-10', 'Moderate persistent asthma, inhaler prescribed'),
(3, 4, 3, 'G43', '2024-04-05', 'Chronic migraine without aura, preventive treatment started'),
(4, 5, 1, 'M17', '2024-01-30', 'Bilateral knee osteoarthritis, physical therapy recommended'),
(5, 6, 2, 'E66', '2024-05-10', 'Obesity, BMI 32, dietary counseling provided'),
(6, 7, 3, 'I10', '2024-06-12', 'Hypertension control'),
(7, 8, 4, 'J45', '2024-07-01', 'Asthma follow-up'),
(1, 9, 1, 'E66', '2024-07-15', 'Obesity treatment'),
(2, 10, 2, 'G43', '2024-08-03', 'Migraine evaluation'),
(3, 11, 5, 'M17', '2024-09-10', 'Arthritis review'),
(4, 12, 6, 'E11', '2024-09-25', 'Diabetes check'),
(5, 13, 1, 'I10', '2024-10-02', 'Hypertension recheck'),
(6, 14, 3, 'E66', '2024-10-20', 'Weight management'),
(7, 15, 4, 'J45', '2024-11-01', 'Asthma routine control'),
(1, 16, 5, 'G43', '2024-11-12', 'Migraine review');

-- Insert nurses
DELETE FROM nurses;
INSERT INTO nurses (dni, name, surname, email) VALUES
('56789012E', 'Elena', 'García', 'e.garcia@hospital.com'),
('67890123F', 'Javier', 'López', 'javi_l@hospital.com'),
('78901234G', 'Sofía', 'Martínez', 's.martinez@clinic.net'),
('89012345H', 'María', 'Fernández', 'mariafdez@clinic.net'),
('90123456I', 'Pablo', 'Ruiz', 'pablo.ruiz.enfermero@salud.org');

-- Insert rooms
DELETE FROM rooms;
INSERT INTO rooms (name, location) VALUES
('General Medicine Room 1', 'Building A, Floor 1, Room 101'),
('Pediatrics Office', 'Building A, Floor 1, Room 102'),
('Cardiology Room', 'Building B, Floor 2, Room 203'),
('Orthopedics Office', 'Building B, Floor 2, Room 204'),
('Radiology Room', 'Building C, Floor 1, Room 105'),
('ENT (Ear, Nose, Throat)', 'Building C, Floor 1, Room 106');

DELETE FROM doctors_appointments;
INSERT INTO doctors_appointments (id_doctors, id_appointments) VALUES
(1, 1),
(2, 2),
(1, 3),
(5, 4),
(3, 4),
(1, 5),
(1,14),
(2,15),
(3,16),
(2, 5);

DELETE FROM personal_problem_options;
INSERT INTO personal_problem_options (problem) VALUES 
('Smoking'),
('Alcohol consumption'),
('Drug use'),
('Sedentary lifestyle'),
('Poor diet'),
('Stress'),
('Anxiety'),
('Depression'),
('Sleep problems'),
('Workplace bullying'),
('Financial stress'),
('Family problems');

DELETE FROM familiar_antecedent_options;
INSERT INTO familiar_antecedent_options (antecedent) VALUES 
('Hypertension'),
('Diabetes Type 1'),
('Diabetes Type 2'),
('Heart disease'),
('Cancer'),
('Asthma'),
('Allergies'),
('Migraine'),
('Obesity'),
('High cholesterol'),
('Arthritis'),
('Alzheimer'),
('Mental health disorders');

DELETE FROM consultation_cause_options;
INSERT INTO consultation_cause_options (cause) VALUES 
('Routine checkup'),
('Follow-up visit'),
('Pain consultation'),
('Vaccination'),
('Blood test results'),
('Medication review'),
('Chronic condition management'),
('Acute illness'),
('Injury assessment'),
('Preventive care'),
('Mental health consultation'),
('Lifestyle counseling');

DELETE FROM vaccines;
INSERT INTO vaccines (patient_id, vaccine_name, dose_number, scheduled_date, administered_date, administered_by_doctor_name, needs_booster, status) VALUES
(5, 'Influenza Vaccine', 1, '2024-10-15', '2024-10-15', 'María González Pérez', 1, 'administered'),
(5, 'COVID-19 Vaccine', 3, '2024-11-01', NULL, NULL, 0, 'scheduled'),
(5, 'Tetanus Vaccine', 1, '2024-09-20', '2024-09-20', 'Carlos Martínez López', 1, 'administered'),
(3, 'Hepatitis B Vaccine', 2, '2024-08-10', '2024-08-10', 'Ana Rodríguez Sánchez', 0, 'administered');

DELETE FROM prescriptions;
INSERT INTO prescriptions (name) VALUES 
('Physical Therapy'),
('Rehabilitation Exercises'),
('Occupational Therapy'),
('Psychotherapy'),
('Therapeutic Ultrasound'),
('Laser Therapy'),
('Dietary Changes'),
('Exercise Routine'),
('Surgical Intervention'),
('Radiation Therapy'),
('Chemotherapy'),
('Pulmonary Rehabilitation'),
('Cardiac Rehabilitation'),
('Neurological Rehabilitation'),
('Speech Therapy'),
('Respiratory Therapy');

DELETE FROM medications;
INSERT INTO medications (medication_name, amount, duration, interval_hours) VALUES
('Ibuprofen', 400, 7, 8),
('Amoxicillin', 500, 10, 12),
('Lisinopril', 10, 30, 24),
('Metformin', 850, 30, 12),
('Atorvastatin', 20, 30, 24);

-- Insert inventory_items
INSERT INTO inventory_items
(item_name, category, subcategory, model_code, batch_number, location,
 quantity_on_hand, minimum_quantity, expiry_date, supplier)
SELECT
 'Paracetamol 500mg', 'Medicine', 'Tablet', 'MED-PA-500', 'B1001',
 'Main Pharmacy', 150, 50, '2026-02-10', 'PharmaCo'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items
    WHERE item_name   = 'Paracetamol 500mg'
      AND model_code  = 'MED-PA-500'
      AND batch_number = 'B1001'
);

INSERT INTO inventory_items
(item_name, category, subcategory, model_code, batch_number, location,
 quantity_on_hand, minimum_quantity, expiry_date, supplier)
SELECT
 'Ibuprofen 200mg', 'Medicine', 'Tablet', 'MED-IB-200', 'B1002',
 'Ward A', 20, 30, '2025-03-01', 'HealthCorp'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items
    WHERE item_name   = 'Ibuprofen 200mg'
      AND model_code  = 'MED-IB-200'
      AND batch_number = 'B1002'
);

INSERT INTO inventory_items
(item_name, category, subcategory, model_code, batch_number, location,
 quantity_on_hand, minimum_quantity, expiry_date, supplier)
SELECT
 'Amoxicillin 500mg', 'Medicine', 'Capsule', 'MED-AM-500', 'B1003',
 'Ward B', 0, 20, '2024-12-01', 'Antibiotix'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items
    WHERE item_name   = 'Amoxicillin 500mg'
      AND model_code  = 'MED-AM-500'
      AND batch_number = 'B1003'
);

INSERT INTO inventory_items
(item_name, category, subcategory, model_code, batch_number, location,
 quantity_on_hand, minimum_quantity, expiry_date, supplier)
SELECT
 'Vitamin D 1000IU', 'Medicine', 'Supplement', 'MED-VD-01', 'B1005',
 'Cardiology', 5, 8, NULL, 'NutraLife'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items
    WHERE item_name   = 'Vitamin D 1000IU'
      AND model_code  = 'MED-VD-01'
      AND batch_number = 'B1005'
);

INSERT INTO inventory_items
(item_name, category, subcategory, model_code, batch_number, location,
 quantity_on_hand, minimum_quantity, expiry_date, supplier)
SELECT
 'Syringe Pump', 'Equipment', 'Infusion', 'EQ-SP-01', 'EQ9002',
 'ICU Store', 1, 2, NULL, 'MedEquip'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items
    WHERE item_name   = 'Syringe Pump'
      AND model_code  = 'EQ-SP-01'
      AND batch_number = 'EQ9002'
);

INSERT INTO inventory_items
(item_name, category, subcategory, model_code, batch_number, location,
 quantity_on_hand, minimum_quantity, expiry_date, supplier)
SELECT
 'Ultrasound Scanner', 'Equipment', 'Diagnostic', 'EQ-US-01', 'EQ9004',
 'Ward B', 3, 2, NULL, 'ScanVision'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items
    WHERE item_name   = 'Ultrasound Scanner'
      AND model_code  = 'EQ-US-01'
      AND batch_number = 'EQ9004'
);

INSERT INTO inventory_items
(item_name, category, subcategory, model_code, batch_number, location,
 quantity_on_hand, minimum_quantity, expiry_date, supplier)
SELECT
 'Wheelchair', 'Equipment', 'Mobility', 'EQ-WC-01', 'EQ9005',
 'Admin Office', 10, 3, NULL, 'ComfortMove'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items
    WHERE item_name   = 'Wheelchair'
      AND model_code  = 'EQ-WC-01'
      AND batch_number = 'EQ9005'
);

INSERT INTO inventory_items
(item_name, category, subcategory, model_code, batch_number, location,
 quantity_on_hand, minimum_quantity, expiry_date, supplier)
SELECT
 'Surgical Gloves M', 'Consumables', 'Gloves', 'CON-SG-M', 'C2001',
 'OT Store', 300, 100, '2025-07-01', 'CareSoft'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items
    WHERE item_name   = 'Surgical Gloves M'
      AND model_code  = 'CON-SG-M'
      AND batch_number = 'C2001'
);

INSERT INTO inventory_items
(item_name, category, subcategory, model_code, batch_number, location,
 quantity_on_hand, minimum_quantity, expiry_date, supplier)
SELECT
 'IV Cannula 20G', 'Consumables', 'IV Set', 'CON-IV-20', 'C2003',
 'ICU Store', 40, 50, '2024-10-20', 'MedFlow'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items
    WHERE item_name   = 'IV Cannula 20G'
      AND model_code  = 'CON-IV-20'
      AND batch_number = 'C2003'
);

INSERT INTO inventory_items
(item_name, category, subcategory, model_code, batch_number, location,
 quantity_on_hand, minimum_quantity, expiry_date, supplier)
SELECT
 'Face Masks Box', 'Consumables', 'Mask', 'CON-FM-01', 'C2004',
 'Main Pharmacy', 500, 100, NULL, 'AirSafe'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items
    WHERE item_name   = 'Face Masks Box'
      AND model_code  = 'CON-FM-01'
      AND batch_number = 'C2004'
);

INSERT INTO inventory_items
(item_name, category, subcategory, model_code, batch_number, location,
 quantity_on_hand, minimum_quantity, expiry_date, supplier)
SELECT
 'Bandage Roll', 'Consumables', 'Dressing', 'CON-BR-01', 'C2005',
 'Ward B', 10, 10, NULL, 'HealTech'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items
    WHERE item_name   = 'Bandage Roll'
      AND model_code  = 'CON-BR-01'
      AND batch_number = 'C2005'
);

INSERT INTO inventory_items
(item_name, category, subcategory, model_code, batch_number, location,
 quantity_on_hand, minimum_quantity, expiry_date, supplier)
SELECT
 'Sterile Gauze Pack', 'Others', 'Dressing', 'OTH-GZ-01', 'O1001',
 'OT Store', 90, 50, NULL, 'CareSoft Medical'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items
    WHERE item_name   = 'Sterile Gauze Pack'
      AND model_code  = 'OTH-GZ-01'
      AND batch_number = 'O1001'
);

INSERT INTO inventory_items
(item_name, category, subcategory, model_code, batch_number, location,
 quantity_on_hand, minimum_quantity, expiry_date, supplier)
SELECT
 'Printer Paper A4', 'Others', 'Office Supply', 'OTH-PP-A4', 'O1003',
 'Admin Office', 200, 50, NULL, 'OfficeCentral'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items
    WHERE item_name   = 'Printer Paper A4'
      AND model_code  = 'OTH-PP-A4'
      AND batch_number = 'O1003'
);

INSERT INTO inventory_items
(item_name, category, subcategory, model_code, batch_number, location,
 quantity_on_hand, minimum_quantity, expiry_date, supplier)
SELECT
 'Batteries Pack AA', 'Others', 'Electronics', 'OTH-BAT-AA', 'O1004',
 'Main Pharmacy', 3, 10, NULL, 'PowerPlus'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items
    WHERE item_name   = 'Batteries Pack AA'
      AND model_code  = 'OTH-BAT-AA'
      AND batch_number = 'O1004'
);

INSERT INTO inventory_items
(item_name, category, subcategory, model_code, batch_number, location,
 quantity_on_hand, minimum_quantity, expiry_date, supplier)
SELECT
 'Water Bottles 500ml', 'Others', 'Refreshment', 'OTH-WB-500', 'O1005',
 'Ward B', 50, 20, '2024-09-15', 'HydroLife'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items
    WHERE item_name   = 'Water Bottles 500ml'
      AND model_code  = 'OTH-WB-500'
      AND batch_number = 'O1005'
);

INSERT INTO vaccine_model (vaccine_name, recommended_doses) VALUES
('Hepatitis B (HepB)', 3),
('Difteria, Tétanos y Tosferina (DTaP)', 5),
('Haemophilus influenzae tipo b (Hib)', 3),
('Polio inactivada (IPV)', 4),
('Neumococo conjugada (PCV13)', 4),
('Rotavirus (RV)', 2),
('Sarampión, Paperas, Rubéola (MMR)', 2),
('Varicela (Var)', 2),
('Virus del Papiloma Humano (HPV)', 2),
('Meningococo conjugada (MenACWY)', 2),
('Tétanos, Difteria, Tosferina (Tdap)', 1),
('Influenza (Gripe estacional)', 1),
('COVID-19 (mRNA)', 2),
('Fiebre Amarilla', 1),
('Hepatitis A (HepA)', 2);

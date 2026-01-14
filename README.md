# 🏥 Hospital Management System  
### Java Desktop Application (Swing + SQLite) | SCRUM-Based Project

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=java&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-003B57?style=flat-square&logo=sqlite&logoColor=white)
![Swing](https://img.shields.io/badge/UI-Java%20Swing-blue?style=flat-square)
![SCRUM](https://img.shields.io/badge/Methodology-SCRUM-purple?style=flat-square)
![Status](https://img.shields.io/badge/Status-Academic%20Project-success?style=flat-square)

---

## 📌 Overview

**Hospital Management System** is a Java-based desktop application designed to manage core hospital operations such as patient handling, appointments, medical records, and reporting.

The system was developed using **Java Swing** for the user interface and **SQLite** for data persistence, following **SCRUM agile methodology** with incremental feature delivery across multiple sprints.

> The application supports multiple hospital roles, including **receptionist, doctor, nurse, and manager**, each with role-specific workflows and interfaces.

---

## 🧑‍🤝‍🧑 Team & Agile Methodology

- **Team Size:** 3 members  
- **Methodology:** SCRUM  
- **Total Sprints:** 3  
- **Sprint Duration:** 3 weeks  
- **Total Development Time:** 9 weeks  

### SCRUM Practices Followed

- Sprint planning & backlog refinement  
- Task decomposition  
- Incremental feature delivery  
- Sprint reviews  
- Continuous testing & integration  

---

## ⚙️ Technologies Used

### Core Stack

- **Language:** Java  
- **UI Framework:** Java Swing  
- **Database:** SQLite  

### Architecture & Tooling

- **Architecture:** Layered Architecture  
  - UI → Controller → Service → Model → Database  
- **Build Tool:** Maven / Gradle  
- **Version Control:** Git & GitHub  

---

## 🧩 Key Features (High-Level)

- ✔ Patient management  
- ✔ Doctor & nurse management  
- ✔ Appointment scheduling (normal & urgent)  
- ✔ Medical records handling  
- ✔ ICD-10 disease integration  
- ✔ Role-based UI windows  
- ✔ Statistical reports & analytics  
- ✔ Time-based appointment analysis  
- ✔ Charts & data visualization  
- ✔ Input validation & error handling  
- ✔ Database integrity (foreign keys)  
- ✔ Modular & extensible design  

> ⚠️ Feature details are intentionally kept high-level to avoid coupling the README to specific user stories.

---

## 🗂️ Project Structure

```text
src/main/java/
 └── giis/demo/
     ├── ui/            # Swing UI windows
     ├── service/       # Business logic
     ├── model/         # Domain models
     ├── util/          # Database & utilities
     └── dto/           # Data Transfer Objects
```
## 🚀 How to Run

### Prerequisites

- Java (JDK installed)
- SQLite
- IDE (IntelliJ IDEA or Eclipse)

### Setup

```bash
git clone https://github.com/joelchirayath/Hospital-Management-System
```
- Open the project in your IDE
- Ensure SQLite is available
- Run the main application entry point
- The database initializes automatically if not present

## 🧪 Testing & Validation

### Testing Approach

- Manual UI testing per sprint  
- Database integrity testing  

### Quality Assurance

- Edge-case handling (empty data, long ranges, multi-selection)  
- Regression testing after each sprint  
- Incremental validation during feature integration  

## 📄 License
This project is licensed under the **MIT License**.  

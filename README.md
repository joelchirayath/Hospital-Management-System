Hospital Management System (Java + SQLite)
📌 Project Overview

This project is a Hospital Management System developed in Java using Swing for the UI and SQLite for data persistence.
The system was built following SCRUM methodology, emphasizing incremental delivery, collaboration, and continuous improvement.

The application supports multiple hospital roles such as receptionist, doctor, nurse, and manager, providing tools for appointment management, medical records, reporting, and operational analytics.

Over the course of development, 26 different features were implemented across multiple functional areas.

🧑‍🤝‍🧑 Team & Methodology

Team size: 3 members

Development methodology: SCRUM

Total sprints: 3

Sprint duration: 3 weeks per sprint

Total development time: 9 weeks

Each sprint followed standard SCRUM practices:

Sprint planning

Task breakdown

Incremental feature delivery

Sprint review and refinement

Continuous integration and testing

⚙️ Technologies Used

Language: Java

UI Framework: Java Swing

Database: SQLite

Architecture: Layered (UI → Controller → Service → Model → DB)

Build Tool: Maven / Gradle (depending on setup)

Version Control: Git & GitHub

🧩 Key Features (High-Level)

The system includes (but is not limited to):

Patient management

Doctor and nurse management

Appointment booking (normal & urgent)

Medical records handling

ICD-10 disease integration

Role-based UI windows

Statistical reports and analytics

Time-based appointment analysis

Data visualization (charts & graphs)

Validation and error handling

Database integrity via foreign keys

Modular, extensible design

⚠️ Feature details are intentionally kept high-level to avoid coupling the README to specific user stories.

🗂️ Project Structure (Simplified)
src/main/java/
 └── giis/demo/
     ├── ui/            # Swing UI windows
     ├── service/       # Business logic
     ├── model/         # Data models
     ├── util/          # Database & utilities
     └── dto/           # Data Transfer Objects

🧪 Testing & Validation

Manual UI testing per sprint

Database integrity testing

Edge-case handling (empty results, long ranges, multiple selections)

Regression testing to ensure new features do not affect existing ones

🎯 Learning Outcomes

This project strengthened skills in:

Java desktop application development

SQL & relational database design

SCRUM and agile teamwork

Clean architecture and separation of concerns

Debugging and incremental refactoring

Collaborative development using Git

🚀 How to Run

Clone the repository:

git clone <repository-url>


Open the project in your IDE (Eclipse / IntelliJ).

Ensure SQLite is available.

Run the main application entry point.

The database will initialize automatically if not present.

📄 License

This project was developed for educational purposes as part of a university course.
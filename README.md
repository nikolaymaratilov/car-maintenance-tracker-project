# 🚗 Car Maintenance Tracker
Car Maintenance Tracker is a web application that allows users to manage their vehicles, track service history, monitor maintenance costs, and record upcoming maintenance tasks. The project is built with Spring Boot, Spring Security, Thymeleaf, MySQL, and follows a clean MVC architecture.

## ✨ Features

### 👤 User Management
- User registration and login
- Profile editing
- Secure authentication with Spring Security
- Role-based access control (USER, ADMIN)

### 🚘 Car Management
- Add new cars with detailed specifications
- Edit and delete car entries
- View complete car details
- Upload and manage car images (via microservice)

### 🛠️ Maintenance Tracking
- Add maintenance/service records
- Track cost, description, date, and type of service
- Edit and delete maintenance entries
- Dashboard with all maintenance activities
- Full history of maintenance for each car

### 🧩 Microservice Integration
- PDF generation microservice for exporting car data
- Avatar/image processing microservice

## 🏛️ Architecture
The project is structured into layered components: controller, service, repository, model, config. Each layer has a clear responsibility, following MVC and clean architecture principles.

## 🗄️ Database
The application uses MySQL as its database. Main tables include: users, roles, cars, maintenance_records, pictures. The ER structure follows: User (1) — (M) Car (1) — (M) MaintenanceRecord.

## 🛠️ Technologies Used
Java 17, Spring Boot, Spring MVC, Spring Security, Spring Data JPA, Thymeleaf, MySQL, Maven, REST Microservices.

## 📄 License
This project is open-source and free to use for educational purposes.

## 🙌 Author
Developed by **Nikolay Maratilov** as a full-stack Spring Boot project.

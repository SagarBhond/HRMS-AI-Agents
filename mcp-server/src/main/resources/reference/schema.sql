-- Reference schema for the SINGLE shared MySQL database
-- (Hibernate ddl-auto=update will create/evolve this automatically at
-- runtime — this file is for reference / manual review / seeding only.)

CREATE DATABASE IF NOT EXISTS hrms_db;
USE hrms_db;

-- Owned by the Employee Agent
CREATE TABLE IF NOT EXISTS employee (
    employee_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name           VARCHAR(100) NOT NULL,
    last_name            VARCHAR(100),
    email                VARCHAR(150) NOT NULL UNIQUE,
    department            VARCHAR(100),
    designation           VARCHAR(100),
    manager_employee_id   BIGINT,
    date_of_joining        DATE,
    status                ENUM('ACTIVE','ON_LEAVE','SUSPENDED','TERMINATED') NOT NULL DEFAULT 'ACTIVE'
);

-- Owned by the Leave Agent
CREATE TABLE IF NOT EXISTS leave_request (
    leave_request_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id            BIGINT NOT NULL,
    leave_type              ENUM('SICK','CASUAL','EARNED','UNPAID','MATERNITY','PATERNITY') NOT NULL,
    start_date               DATE NOT NULL,
    end_date                 DATE NOT NULL,
    status                   ENUM('PENDING','APPROVED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    approved_by_manager_id    VARCHAR(50),
    reason                   VARCHAR(500),
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- Owned by the Leave Agent
CREATE TABLE IF NOT EXISTS leave_balance (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id    BIGINT NOT NULL,
    leave_type     ENUM('SICK','CASUAL','EARNED','UNPAID','MATERNITY','PATERNITY') NOT NULL,
    year           INT NOT NULL,
    total_days     DOUBLE NOT NULL DEFAULT 0,
    used_days      DOUBLE NOT NULL DEFAULT 0,
    remaining_days DOUBLE NOT NULL DEFAULT 0,
    UNIQUE KEY uq_leave_balance (employee_id, leave_type, year),
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- Owned by the Attendance Agent
CREATE TABLE IF NOT EXISTS attendance (
    attendance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id   BIGINT NOT NULL,
    work_date     DATE NOT NULL,
    check_in      TIME,
    check_out     TIME,
    hours_worked  DOUBLE NOT NULL DEFAULT 0,
    status        ENUM('PRESENT','ABSENT','HALF_DAY','ON_LEAVE','HOLIDAY','WEEK_OFF') NOT NULL,
    UNIQUE KEY uq_attendance (employee_id, work_date),
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- Owned by the Payroll Agent
CREATE TABLE IF NOT EXISTS payroll (
    salary_slip_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id             BIGINT NOT NULL,
    month                    INT NOT NULL,
    year                     INT NOT NULL,
    basic_salary              DOUBLE NOT NULL DEFAULT 0,
    allowances                DOUBLE NOT NULL DEFAULT 0,
    deductions                DOUBLE NOT NULL DEFAULT 0,
    unpaid_leave_deduction     DOUBLE NOT NULL DEFAULT 0,
    net_salary                 DOUBLE NOT NULL DEFAULT 0,
    status                    ENUM('DRAFT','GENERATED','PAID') NOT NULL DEFAULT 'DRAFT',
    UNIQUE KEY uq_salary_slip (employee_id, month, year),
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- Owned by the HR Agent
CREATE TABLE IF NOT EXISTS hr (
    event_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id   BIGINT NOT NULL,
    event_type    ENUM('ONBOARDING','PROMOTION','TRANSFER','DESIGNATION_CHANGE','EXIT') NOT NULL,
    event_date    DATE NOT NULL,
    details       VARCHAR(1000),
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- Owned by Auth Service. ADMIN may intentionally have no linked employee profile.
CREATE TABLE IF NOT EXISTS auth_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        ENUM('EMPLOYEE','MANAGER','HR','ADMIN') NOT NULL,
    employee_id BIGINT UNIQUE,
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

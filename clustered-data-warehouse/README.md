# 📊 FX Data Warehouse - CSV Importer

#### A beginner-friendly Spring Boot application that imports FX (Foreign Exchange) deals from CSV files into a MySQL database with validation and error handling.
## 🎯 What This Project Does

This application acts like a smart kitchen for processing financial data:

    CSV File = Customer order tickets

    Valid Deals = Perfectly cooked meals (saved in database)

    Invalid Deals = Burnt meals (logged as errors)

    No Rollback = Even if one order burns, keep cooking the others!

## 🚀 Quick Start Prerequisites

    Java 21

    Docker Desktop

    Postman (for API testing)

1. Start the Database
```
- # Navigate to your project folder
- docker-compose up db -d
```
2. Run the Application
```
# In your IDE: Run ClusteredDataWarehouseApplication
# Or via command line:
./mvnw spring-boot:run
```
3. Test the Application
- Option A: Auto-import on Startup

The application automatically imports from:
```
C:\Users\amine\Desktop\clustered-data-warehouse\clustered-data-warehouse\src\main\resources\sample-deals.csv
```
- Option B: Use Postman API
```
    Health Check: GET http://localhost:8080/api/deals/health

    Fixed Import: POST http://localhost:8080/api/deals/import-fixed

    File Upload: POST http://localhost:8080/api/deals/upload
```
## 📁 Project Structure
text
```
clustered-data-warehouse/
├── src/main/java/com/amine/fx/
│   ├── model/           # Database tables
│   │   ├── Deal.java       # Valid deals storage
│   │   └── DealError.java  # Error records
│   ├── repository/      # Database managers
│   │   ├── DealRepository.java
│   │   └── DealErrorRepository.java
│   ├── service/         # Business logic
│   │   └── DealImportService.java  # The "Head Chef"
│   └── controller/      # REST API
│       └── DealController.java
├── src/main/resources/
│   ├── sample-deals.csv # Test data
│   └── application.properties
└── docker-compose.yml   # Database setup
```
## 🗃️ Database Tables
### 📊 fx_deals (Successful Deals)

**The Actual Data in fx_deals:**

| id | deal_unique_id | from_currency | to_currency | deal_timestamp | amount |
|----|----------------|---------------|-------------|----------------|---------|
| 1 | D001 | USD | EUR | 2025-11-13 09:00:00 | 1000.5000 |
| 2 | D002 | GBP | USD | 2025-11-13 10:00:00 | 2000.0000 |
| 3 | D003 | EUR | JPY | 2025-11-13 11:30:00 | 1500.7500 |
| 4 | D005 | MAD | USD | 2025-11-14 10:30:00 | 3500.0000 |

### ❌ fx_deal_errors (Failed Deals)
**The Actual Data in fx_deal_errors:**

| id | deal_unique_id | from_currency | to_currency | error_reason | amount |
|----|----------------|---------------|-------------|--------------|---------|
| 1 | D002 | GBP | USD | Duplicate deal ID | 2000 |
| 2 | D004 | EUR | [empty] | Missing required fields | 1500.75 |


## 📊 Sample Data

**The sample-deals.csv contains test cases:**

| Deal ID | From | To | Timestamp | Amount | Expected Result |
|---------|------|----|-----------|--------|-----------------|
| D001 | USD | EUR | 2025-11-13T10:00:00 | 1000.50 | ✅ Success |
| D002 | GBP | USD | 2025-11-13T11:00:00 | 2000 | ✅ Success |
| D003 | EUR | JPY | 2025-11-13T12:30:00 | 1500.75 | ✅ Success |
| D002 | GBP | USD | 2025-11-13T11:00:00 | 2000 | ❌ Duplicate |
| D004 | EUR | [empty] | 2025-11-13T12:30:00 | 1500.75 | ❌ Missing field |
| D005 | MAD | [space]USD | 2025-11-14T11:30:00 | 3500 | ✅ Success (spaces trimmed) |
## 🔧 Validation Rules

The application checks each deal for:

    ✅ Required fields: All 5 fields must be present

    ✅ Unique deal ID: No duplicate deals allowed

    ✅ Currency format: Exactly 3 characters (USD, EUR, etc.)

    ✅ Valid timestamp: Must be in "YYYY-MM-DDTHH:MM:SS" format

    ✅ Positive amount: Must be greater than 0

    ✅ Different currencies: From and To currencies can't be the same

## 🛠️ API Endpoints
#### Health Check
```
GET /api/deals/health
```
Response: **✅ FX Data Warehouse is running!**
#### Fixed File Import

```
POST /api/deals/import-fixed
```
Response:
```
{
"status": "success",
"message": "Fixed file import completed",
"filePath": "C:\\...\\sample-deals.csv"
}
```
#### File Upload
```
POST /api/deals/upload
Content-Type: multipart/form-data
```
Body: **form-data with key file and CSV file**

Success Response:

```
{
"status": "success",
"message": "CSV file processed successfully",
"filename": "sample-deals.csv",
"size": "245 bytes"
}
```
## 🐛 Testing with Postman
### Step 1: Setup Postman Request

    Method: POST

    URL: http://localhost:8080/api/deals/upload

    Body → form-data

    Key: file (type: File)

    Value: Select your CSV file

### Step 2: Verify Configuration

Make sure:

    ✅ Body type is form-data (not raw or x-www-form-urlencoded)

    ✅ Key is exactly file (lowercase)

    ✅ Type is File (click dropdown if it says "Text")

    ✅ File is selected (shows file name in value column)

## 🗄️ Database Commands
#### Check Successful Deals
sql
```
USE fxwarehouse;
SELECT * FROM fx_deals;
```
#### Check Errors
sql
```
USE fxwarehouse;
SELECT * FROM fx_deal_errors;
```
#### Reset Database
sql
```
USE fxwarehouse;
DROP TABLE IF EXISTS fx_deals, fx_deal_errors;
```
## 🚨 Common Issues & Solutions
### "CSV file not found"

    Solution: Check file path in ClusteredDataWarehouseApplication.java

### "Required part 'file' is not present"

    Solution: Check Postman configuration (Key = file, Type = File)

### "Duplicate deal ID"

    Solution: Each deal must have a unique ID in the CSV

### Database connection issues

    Solution: Run docker-compose up db -d and check port 3307

## 🎓 Learning Points

**This project demonstrates:**

    Spring Boot - Modern Java framework

    JPA/Hibernate - Database object mapping

    REST APIs - Web service creation

    Docker - Containerized database

    File Processing - CSV reading and validation

    Error Handling - Graceful failure management

    Testing - API testing with Postman

## 📈 Next Steps

Want to enhance this project?

    Add a web interface for file upload

    Implement user authentication

    Add email notifications for failed imports

    Create data analytics dashboard

    Add unit tests with JUnit

## 🤝 Contributing

This is a learning project! Feel free to:

    Add new validation rules

    Improve error messages

    Add more API endpoints

    Enhance documentation

## 📄 License

This project is created for educational purposes as part of a coding challenge.
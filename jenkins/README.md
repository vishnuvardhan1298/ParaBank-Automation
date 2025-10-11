# ParaBank Automation Project

## 🔧 Tech Stack
- Selenium WebDriver
- Java (JDK 21)
- TestNG
- Maven
- Jenkins CI/CD
- ExtentReports

## 🧪 Test Coverage
- Login, Account, Transfer, Loan, Contact, Navigation UI
- Cross-browser: Chrome & Edge
- Data-driven testing with TestNG DataProvider

## 📂 Project Structure
- `src/`: Test classes and Page Objects
- `test-output/`: TestNG reports
- `target/`: ExtentReports, screenshots
- `jenkins/`: CI/CD job setup and screenshots

## 🚀 Jenkins Integration
- Automated builds triggered via Jenkins
- Artifacts archived: ExtentReport.html, screenshots
- Job configuration included in `jenkins/` folder

## 📸 Screenshots
- Captured on failure and archived
- Stored in `target/screenshots/`

## 📦 How to Run
```bash
mvn clean test

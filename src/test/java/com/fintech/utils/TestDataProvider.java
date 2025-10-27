package com.fintech.utils;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.text.SimpleDateFormat;


import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.annotations.DataProvider;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;


public class TestDataProvider {

    // 🔹 Auth & Login
    @DataProvider(name = "userCredentials")
    public Object[][] userCredentials() {
        return new Object[][] {
            {"john", "demo"},
            {"john", "wrong"},
            {"notexist", "demo"}
        };
    }

    @DataProvider(name = "excelLoginData")
    public Object[][] getExcelLoginData() {
        return ExcelUtils.readExcelData("src/test/resources/LoginData.xlsx", "Login");
    }

    @DataProvider(name = "loginScenarios")
    public Object[][] getLoginScenarios() {
        return ExcelUtils.readExcelData("src/test/resources/LoginScenarios.xlsx", "LoginTest");
    }

    @DataProvider(name = "loanDataExcel")
    public Object[][] getLoanDataExcel() {
        List<Object[]> data = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(new File("src/test/resources/LoanData.xlsx"))) {
            Sheet sheet = workbook.getSheet("Loantest");

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                if (rowHasEmptyCells(row)) {
                    System.out.println("⚠️ Skipping malformed row: " + row.getRowNum());
                    continue;
                }

                String scenarioLabel = getStringCell(row, 0);
                Double amount = getDoubleCell(row, 1);
                Double downPayment = getDoubleCell(row, 2);
                Double accountId = getDoubleCell(row, 3);
                Boolean expectSuccess = getBooleanCell(row, 4);

                data.add(new Object[]{scenarioLabel, amount, downPayment, accountId, expectSuccess});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data.toArray(new Object[0][]);
    }

    // 🔹 Contact Module
    @DataProvider(name = "contactDataExcel")
    public Object[][] getContactDataExcel() {
        List<Object[]> data = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(new File("src/test/resources/ContactData.xlsx"))) {
            Sheet sheet = workbook.getSheet("Contact");

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                String name = getStringCell(row, 0);
                String email = getStringCell(row, 1);
                Double phone = getDoubleCell(row, 2);
                String message = getStringCell(row, 3);
                Boolean expectSuccess = getBooleanCell(row, 4);

                data.add(new Object[]{name, email, phone, message, expectSuccess});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data.toArray(new Object[0][]);
    }
   
    private Double getDoubleCell(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        try {
            return Double.parseDouble(cell.getStringCellValue().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean getBooleanCell(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null || cell.getCellType() == CellType.BLANK) return false;
        if (cell.getCellType() == CellType.BOOLEAN) return cell.getBooleanCellValue();
        return Boolean.parseBoolean(cell.getStringCellValue().trim());
    }


    // 🔹 Optional: Account Summary or Sorting
    @DataProvider(name = "accountFunctionalityData")
    public Object[][] getAccountFunctionalityData() {
        List<Object[]> data = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(new File("src/test/resources/AccountFunctionality.xlsx"))) {
            Sheet sheet = workbook.getSheet("DynamicTests");

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                
                if (rowHasEmptyCells(row)) {
                    System.out.println("⚠️ Skipping malformed row in AccountFunctionality.xlsx: Row " + row.getRowNum());
                    continue;
                }

                String scenario = getStringCell(row, 0);
                String source = getStringCell(row, 1);
                String destination = getStringCell(row, 2);
                Double amount = getDoubleCell(row, 3);
                String fromDate = getDateCell(row, 4);
                String toDate = getDateCell(row, 5);
                String sortBy = getStringCell(row, 6);
                String sortOrder = getStringCell(row, 7);
                Double expectedBalance = getDoubleCell(row, 8);
                Boolean expectVisible = getBooleanCell(row, 9);
                
                System.out.println("✅ Loaded scenario: " + scenario);

                data.add(new Object[]{scenario, source, destination, amount, fromDate, toDate, sortBy, sortOrder, expectedBalance, expectVisible});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data.toArray(new Object[0][]);
    }

    private String getStringCell(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null || cell.getCellType() == CellType.BLANK) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        return cell.toString().trim();
    }
    private String getDateCell(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null || cell.getCellType() == CellType.BLANK) return "";

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return sdf.format(cell.getDateCellValue()); // ✅ Converts Excel date to string
        } else {
            return cell.getStringCellValue().trim(); // fallback
        }
    }

   
    private boolean rowHasEmptyCells(Row row) {
        String scenario = getStringCell(row, 0);
        if (scenario.isEmpty()) return true;

        switch (scenario.toLowerCase()) {
            case "missing info":
                return false; // allow this row even if fields are blank
            case "balance update after transfer":
                return isBlank(row, 1) || isBlank(row, 2) || isBlank(row, 3) || isBlank(row, 4);
            case "filter transactions by date range":
                return isBlank(row, 1) || isBlank(row, 4) || isBlank(row, 5);
            case "sort transactions by date/amount":
                return isBlank(row, 1) || isBlank(row, 6) || isBlank(row, 7);
            case "validate account summary":
                return isBlank(row, 1) || isBlank(row, 8);
            default:
                return true; // unknown scenario
        }
    }

    private boolean isBlank(Row row, int index) {
        Cell cell = row.getCell(index);
        return cell == null || cell.getCellType() == CellType.BLANK;
    }
    @DataProvider(name = "navLinksExcel")
    public Object[][] getNavLinksExcel() {
        List<Object[]> data = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(new File("src/test/resources/NavigationLinks.xlsx"))) {
            Sheet sheet = workbook.getSheet("NavLinks");

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String linkText = getStringCell(row, 0);
                String expectedUrlFragment = getStringCell(row, 1);
                String pageClassName = getStringCell(row, 2);

                Class<?> pageClass = Class.forName("com.fintech.pages." + pageClassName);
                data.add(new Object[]{linkText, expectedUrlFragment, pageClass});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data.toArray(new Object[0][]);
    }
    @DataProvider(name = "transferDataExcel")
    public Object[][] getTransferDataExcel() {
        List<Object[]> data = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(new File("src/test/resources/TransferScenarios.xlsx"))) {
            Sheet sheet = workbook.getSheet("TransferTest");

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String scenarioLabel = getStringCell(row, 0);
                String amount = getStringCell(row, 1);
                Boolean expectSuccess = getBooleanCell(row, 2);

                data.add(new Object[]{scenarioLabel, amount, expectSuccess});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data.toArray(new Object[0][]);
    }
}

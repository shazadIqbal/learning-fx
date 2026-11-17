package com.mls.learningfx.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.mls.learningfx.model.MonthlySale;
import com.mls.learningfx.model.ProductSale;
import com.mls.learningfx.model.SaleDetail;
import com.mls.learningfx.repository.MonthlyReportRepository;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.*;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class MonthlyReportService {

    // PDF save karne ka folder
    private static final Path REPORT_FOLDER =
            Paths.get("src", "main", "resources", "reports");

    // PDF colors
    private static final Color BLUE = new Color(250, 6, 6);
    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color GREY = new Color(100, 116, 139);
    private static final Color LIGHT_ROW = new Color(244, 246, 249);
    private static final Color BORDER = new Color(226, 232, 240);

    // PDF fonts
    private static final Font TITLE_FONT =
            new Font(Font.HELVETICA, 22, Font.BOLD, BLUE);

    private static final Font SUB_FONT =
            new Font(Font.HELVETICA, 10, Font.NORMAL, GREY);

    private static final Font HEADER_FONT =
            new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);

    private static final Font CELL_FONT =
            new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);

    private static final Font SECTION_FONT =
            new Font(Font.HELVETICA, 14, Font.BOLD, Color.BLACK);

    private static final Font TOTAL_FONT =
            new Font(Font.HELVETICA, 12, Font.BOLD, GREEN);


    // Repositories aur services
    private final MonthlyReportRepository reportRepository =
            new MonthlyReportRepository();

    private final ExpenseService expenseService =
            new ExpenseService();


    // ============================================================
    // MAIN METHOD: PDF banata hai
    // ============================================================
    public Path generateMonthlyReport(YearMonth from, YearMonth to)
            throws IOException {

        // Step 1: Dates check karo
        if (from.isAfter(to)) {
            throw new IOException("Start month, End month se pehle honi chahiye");
        }

        // Step 2: Database se data lo
        List<MonthlySale> rows;
        List<ProductSale> products;
        List<SaleDetail> saleDetails;
        try {
            rows = reportRepository.findMonthlySales(from, to);
            products = reportRepository.findProductSales(from, to);
            saleDetails = reportRepository.findSaleDetails(from, to);
        } catch (SQLException e) {
            throw new IOException("Database se data load nahi hua", e);
        }

        // Step 3: Har month ka expense lo
        Map<YearMonth, BigDecimal> expenseByMonth = loadExpenses(from, to);

        // Step 4: Sab months ka total expense nikalo
        BigDecimal grandExpense = BigDecimal.ZERO;
        for (BigDecimal e : expenseByMonth.values()) {
            grandExpense = grandExpense.add(e);
        }

        // Step 5: Agar na sale hai na expense, to error do
        if (rows.isEmpty() && grandExpense.signum() == 0) {
            throw new IOException("Is range mein koi sale ya expense nahi mila");
        }

        // Step 6: PDF ki file ka path banao
        Files.createDirectories(REPORT_FOLDER);
        Path filePath = REPORT_FOLDER.resolve(
                "monthly-report-" + from + "-to-" + to + ".pdf");

        // Step 7: PDF likho
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);

        try (OutputStream out = new FileOutputStream(filePath.toFile())) {

            PdfWriter.getInstance(document, out);
            document.open();

            addTitle(document, from, to);
            addMonthlyTable(document, from, to, rows, expenseByMonth);
            addProductTable(document, products);
            addSaleTable(document, saleDetails);
            addSummary(document, rows, grandExpense);

            document.close();

        } catch (DocumentException e) {
            throw new IOException("PDF create nahi hua", e);
        }

        return filePath.toAbsolutePath();
    }


    // ============================================================
    // HELPER 1: Har month ka expense map mein rakhta hai
    // ============================================================
    private Map<YearMonth, BigDecimal> loadExpenses(YearMonth from, YearMonth to) {

        Map<YearMonth, BigDecimal> map = new HashMap<>();

        YearMonth month = from;
        while (!month.isAfter(to)) {

            BigDecimal total = expenseService.getMonthlyTotal(
                    month.getYear(), month.getMonthValue());

            if (total == null) {
                total = BigDecimal.ZERO;
            }

            map.put(month, total);
            month = month.plusMonths(1);
        }

        return map;
    }


    // ============================================================
    // HELPER 2: PDF ka title
    // ============================================================
    private void addTitle(Document document, YearMonth from, YearMonth to)
            throws DocumentException {

        document.add(new Paragraph("Monthly Sale Report", TITLE_FONT));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        Paragraph sub = new Paragraph(
                from.format(formatter) + " to " + to.format(formatter), SUB_FONT);
        sub.setSpacingAfter(20);

        document.add(sub);
    }


    // ============================================================
    // HELPER 3: Monthly table (Sales + Expense + Net)
    // ============================================================
    private void addMonthlyTable(Document document, YearMonth from, YearMonth to,
                                 List<MonthlySale> rows,
                                 Map<YearMonth, BigDecimal> expenseByMonth)
            throws DocumentException {

        // Sales ko month ke hisaab se rakho taake dhoondna aasaan ho
        Map<YearMonth, MonthlySale> salesByMonth = new HashMap<>();
        for (MonthlySale sale : rows) {
            salesByMonth.put(YearMonth.from(sale.getDay()), sale);
        }

        // 7 columns ka table
        PdfPTable table = new PdfPTable(
                new float[]{0.5f, 1.6f, 0.9f, 2f, 1.8f, 1.8f, 1.9f});
        table.setWidthPercentage(100);

        // Heading row
        header(table, "#", Element.ALIGN_CENTER);
        header(table, "Month", Element.ALIGN_LEFT);
        header(table, "Qty", Element.ALIGN_CENTER);
        header(table, "Sales", Element.ALIGN_RIGHT);
        header(table, "Profit", Element.ALIGN_RIGHT);
        header(table, "Expense", Element.ALIGN_RIGHT);
        header(table, "Net", Element.ALIGN_RIGHT);

        int number = 1;
        YearMonth month = from;

        // Har month ke liye ek row
        while (!month.isAfter(to)) {

            MonthlySale sale = salesByMonth.get(month);
            BigDecimal expense = expenseByMonth.get(month);

            // Agar is month mein kuch nahi hua, row skip karo
            boolean noSale = (sale == null);
            boolean noExpense = (expense.signum() == 0);

            if (!(noSale && noExpense)) {

                // Agar sale nahi hui to sab 0 rakho
                int qty = 0;
                double sales = 0;
                double profit = 0;

                if (!noSale) {
                    qty = sale.getTotalQty();
                    sales = sale.getTotalSales();
                    profit = sale.getTotalProfit();
                }

                // Net = Profit - Expense
                double net = profit - expense.doubleValue();

                // Ek row white, ek row halki grey
                Color bg = Color.WHITE;
                if (number % 2 == 0) {
                    bg = LIGHT_ROW;
                }

                cell(table, String.valueOf(number), Element.ALIGN_CENTER, bg);
                cell(table, month.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        Element.ALIGN_LEFT, bg);
                cell(table, String.valueOf(qty), Element.ALIGN_CENTER, bg);
                cell(table, money(sales), Element.ALIGN_RIGHT, bg);
                cell(table, money(profit), Element.ALIGN_RIGHT, bg);
                cell(table, money(expense.doubleValue()), Element.ALIGN_RIGHT, bg);
                cell(table, money(net), Element.ALIGN_RIGHT, bg);

                number++;
            }

            month = month.plusMonths(1);
        }

        document.add(table);
    }


    // ============================================================
    // HELPER 4: Product-wise table
    // ============================================================
    private void addProductTable(Document document, List<ProductSale> products)
            throws DocumentException {

        Paragraph heading = new Paragraph("Product-wise Details", SECTION_FONT);
        heading.setSpacingBefore(25);
        heading.setSpacingAfter(10);
        document.add(heading);

        // 7 columns
        PdfPTable table = new PdfPTable(
                new float[]{0.5f, 2.4f, 1.6f, 1.6f, 1.3f, 2f, 1.8f});
        table.setWidthPercentage(100);

        // 7 headings (columns ke barabar)
        header(table, "#", Element.ALIGN_CENTER);
        header(table, "Product", Element.ALIGN_LEFT);
        header(table, "Base Price", Element.ALIGN_RIGHT);
        header(table, "Sell Price", Element.ALIGN_RIGHT);
        header(table, "Qty Sold", Element.ALIGN_CENTER);
        header(table, "Sales", Element.ALIGN_RIGHT);
        header(table, "Profit", Element.ALIGN_RIGHT);

        int number = 1;

        for (ProductSale p : products) {

            Color bg = Color.WHITE;
            if (number % 2 == 0) {
                bg = LIGHT_ROW;
            }

            cell(table, String.valueOf(number), Element.ALIGN_CENTER, bg);
            cell(table, p.getName(), Element.ALIGN_LEFT, bg);
            cell(table, amount(p.getBasePrice()), Element.ALIGN_RIGHT, bg);
            cell(table, amount(p.getSellPrice()), Element.ALIGN_RIGHT, bg);
            cell(table, String.valueOf(p.getTotalQty()), Element.ALIGN_CENTER, bg);
            cell(table, amount(p.getTotalSales()), Element.ALIGN_RIGHT, bg);
            cell(table, amount(p.getTotalProfit()), Element.ALIGN_RIGHT, bg);

            number++;
        }

        document.add(table);

        Paragraph note = new Paragraph("All amounts are in Rs.", SUB_FONT);
        note.setSpacingBefore(5);
        document.add(note);
    }


    // ============================================================
    // HELPER 4B: Sale-wise table (date ke saath)
    // ============================================================
    private void addSaleTable(Document document, List<SaleDetail> saleDetails)
            throws DocumentException {

        Paragraph heading = new Paragraph("Sale-wise Details", SECTION_FONT);
        heading.setSpacingBefore(25);
        heading.setSpacingAfter(10);
        document.add(heading);

        // 6 columns
        PdfPTable table = new PdfPTable(
                new float[]{0.5f, 1.8f, 2.4f, 1f, 2f, 1.8f});
        table.setWidthPercentage(100);

        // 6 headings (columns ke barabar)
        header(table, "#", Element.ALIGN_CENTER);
        header(table, "Date", Element.ALIGN_LEFT);
        header(table, "Product", Element.ALIGN_LEFT);
        header(table, "Qty", Element.ALIGN_CENTER);
        header(table, "Sales", Element.ALIGN_RIGHT);
        header(table, "Profit", Element.ALIGN_RIGHT);

        int number = 1;

        for (SaleDetail sd : saleDetails) {

            Color bg = Color.WHITE;
            if (number % 2 == 0) {
                bg = LIGHT_ROW;
            }

            cell(table, String.valueOf(number), Element.ALIGN_CENTER, bg);
            cell(table, sd.getDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    Element.ALIGN_LEFT, bg);
            cell(table, sd.getProductName(), Element.ALIGN_LEFT, bg);
            cell(table, String.valueOf(sd.getQty()), Element.ALIGN_CENTER, bg);
            cell(table, amount(sd.getSales()), Element.ALIGN_RIGHT, bg);
            cell(table, amount(sd.getProfit()), Element.ALIGN_RIGHT, bg);

            number++;
        }

        document.add(table);
    }


    // ============================================================
    // HELPER 5: Neeche ka summary (Total + Net Profit)
    // ============================================================
    private void addSummary(Document document, List<MonthlySale> rows,
                            BigDecimal grandExpense)
            throws DocumentException {

        // Sab months ka total jama karo
        int totalQty = 0;
        double totalSales = 0;
        double totalProfit = 0;

        for (MonthlySale sale : rows) {
            totalQty += sale.getTotalQty();
            totalSales += sale.getTotalSales();
            totalProfit += sale.getTotalProfit();
        }

        // Net Profit = Profit - Expense
        double netProfit = totalProfit - grandExpense.doubleValue();

        PdfPTable summary = new PdfPTable(new float[]{2f, 3f});
        summary.setWidthPercentage(60);
        summary.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summary.setSpacingBefore(20);

        summaryRow(summary, "Total Qty", String.valueOf(totalQty), false);
        summaryRow(summary, "Total Sales", money(totalSales), false);
        summaryRow(summary, "Total Profit", money(totalProfit), false);
        summaryRow(summary, "Total Expense", money(grandExpense.doubleValue()), false);
        summaryRow(summary, "Net Profit", money(netProfit), true);

        document.add(summary);
    }


    // Table heading banane ka method
    private void header(PdfPTable table, String text, int align) {

        PdfPCell cell =
                new PdfPCell(new Phrase(text, HEADER_FONT));

        cell.setBackgroundColor(BLUE);
        cell.setHorizontalAlignment(align);
        cell.setPadding(8);
        cell.setBorderColor(BLUE);

        table.addCell(cell);
    }


    // Normal table cell banane ka method
    private void cell(PdfPTable table, String text,
                      int align, Color bg) {

        PdfPCell cell =
                new PdfPCell(new Phrase(text, CELL_FONT));

        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(align);
        cell.setPadding(7);
        cell.setBorderColor(BORDER);

        table.addCell(cell);
    }


    // Summary ki row banane ka method
    private void summaryRow(PdfPTable table,
                            String label,
                            String value,
                            boolean bold) {

        Font font = CELL_FONT;

        if (bold) {
            font = TOTAL_FONT;
        }

        PdfPCell labelCell =
                new PdfPCell(new Phrase(label, font));

        PdfPCell valueCell =
                new PdfPCell(new Phrase(value, font));

        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        for (PdfPCell cell : new PdfPCell[]{labelCell, valueCell}) {

            if (bold) {
                cell.setBorder(Rectangle.TOP);
            } else {
                cell.setBorder(Rectangle.NO_BORDER);
            }

            cell.setBorderColor(GREY);
            cell.setPadding(5);
        }

        table.addCell(labelCell);
        table.addCell(valueCell);
    }


    // Amount ko currency format mein convert karna
    private String money(double amount) {

        return String.format("Rs. %,.2f", amount);
    }


    // Normal price format
    private String amount(double amount) {

        return String.format("%,.2f", amount);
    }


}
package com.mls.learningfx.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mls.learningfx.model.MonthlySale;
import com.mls.learningfx.model.ProductSale;
import com.mls.learningfx.repository.MonthlyReportRepository;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MonthlyReportService {

    private static final Path REPORT_FOLDER = Paths.get("src", "main", "resources", "reports");

    private static final Color BLUE = new Color(250, 6, 6);
    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color GREY = new Color(100, 116, 139);
    private static final Color LIGHT_ROW = new Color(244, 246, 249);
    private static final Color BORDER = new Color(226, 232, 240);

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 22, Font.BOLD, BLUE);
    private static final Font SUB_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, GREY);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
    private static final Font CELL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font SECTION_FONT = new Font(Font.HELVETICA, 14, Font.BOLD, Color.BLACK);
    private static final Font TOTAL_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, GREEN);

    private final MonthlyReportRepository reportRepository = new MonthlyReportRepository();

    public Path generateMonthlyReport(YearMonth from, YearMonth to) throws IOException {

        if (from.isAfter(to)) {
            throw new IOException("Start month, End month se pehle honi chahiye");
        }

        List<MonthlySale> rows;
        List<ProductSale> products;
        try {
            rows = reportRepository.findMonthlySales(from, to);
            products = reportRepository.findProductSales(from, to);
        } catch (SQLException e) {
            throw new IOException("Database se data load nahi hua", e);
        }
        if (rows.isEmpty()) {
            throw new IOException("Is range mein koi sale nahi mili: " + from + " se " + to);
        }

        Files.createDirectories(REPORT_FOLDER);
        Path filePath = REPORT_FOLDER.resolve("monthly-report-" + from + "-to-" + to + ".pdf");

        Document document = new Document(PageSize.A4, 40, 40, 40, 40);

        try (OutputStream out = new FileOutputStream(filePath.toFile())) {

            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("Monthly Sale Report", TITLE_FONT);
            document.add(title);

            DateTimeFormatter longMonth = DateTimeFormatter.ofPattern("MMMM yyyy");
            Paragraph sub = new Paragraph(
                    from.format(longMonth) + "  to  " + to.format(longMonth), SUB_FONT);
            sub.setSpacingAfter(20);
            document.add(sub);

            // ---------- 1. Month-wise table ----------
            PdfPTable table = new PdfPTable(new float[]{0.6f, 2.5f, 1.2f, 2.2f, 2.2f});
            table.setWidthPercentage(100);
            table.setHeaderRows(1);
            header(table, "#", Element.ALIGN_CENTER);
            header(table, "Month", Element.ALIGN_LEFT);
            header(table, "Qty", Element.ALIGN_CENTER);
            header(table, "Sales", Element.ALIGN_RIGHT);
            header(table, "Profit", Element.ALIGN_RIGHT);

            int n = 1, totalQty = 0;
            double grandSales = 0, grandProfit = 0;

            for (MonthlySale r : rows) {
                Color bg = (n % 2 == 0) ? LIGHT_ROW : Color.WHITE;
                cell(table, String.valueOf(n), Element.ALIGN_CENTER, bg);
                cell(table, r.getDay().format(DateTimeFormatter.ofPattern("MMM yyyy")), Element.ALIGN_LEFT, bg);
                cell(table, String.valueOf(r.getTotalQty()), Element.ALIGN_CENTER, bg);
                cell(table, money(r.getTotalSales()), Element.ALIGN_RIGHT, bg);
                cell(table, money(r.getTotalProfit()), Element.ALIGN_RIGHT, bg);
                totalQty += r.getTotalQty();
                grandSales += r.getTotalSales();
                grandProfit += r.getTotalProfit();
                n++;
            }
            document.add(table);

            // ---------- 2. Product-wise table ----------
            Paragraph prodTitle = new Paragraph("Product-wise Details", SECTION_FONT);
            prodTitle.setSpacingBefore(25);
            prodTitle.setSpacingAfter(10);
            document.add(prodTitle);

            PdfPTable ptable = new PdfPTable(new float[]{0.5f, 2.4f, 1.6f, 1.6f, 1.3f, 2f, 1.8f});
            ptable.setWidthPercentage(100);
            ptable.setHeaderRows(1);
            header(ptable, "#", Element.ALIGN_CENTER);
            header(ptable, "Product", Element.ALIGN_LEFT);
            header(ptable, "Base Price", Element.ALIGN_RIGHT);
            header(ptable, "Sell Price", Element.ALIGN_RIGHT);
            header(ptable, "Qty Sold", Element.ALIGN_CENTER);
            header(ptable, "Sales", Element.ALIGN_RIGHT);
            header(ptable, "Profit", Element.ALIGN_RIGHT);

            int pn = 1;
            for (ProductSale p : products) {
                Color bg = (pn % 2 == 0) ? LIGHT_ROW : Color.WHITE;
                cell(ptable, String.valueOf(pn), Element.ALIGN_CENTER, bg);
                cell(ptable, p.getName(), Element.ALIGN_LEFT, bg);
                cell(ptable, amount(p.getBasePrice()), Element.ALIGN_RIGHT, bg);
                cell(ptable, amount(p.getSellPrice()), Element.ALIGN_RIGHT, bg);
                cell(ptable, String.valueOf(p.getTotalQty()), Element.ALIGN_CENTER, bg);
                cell(ptable, amount(p.getTotalSales()), Element.ALIGN_RIGHT, bg);
                cell(ptable, amount(p.getTotalProfit()), Element.ALIGN_RIGHT, bg);
                pn++;
            }
            document.add(ptable);

            Paragraph note = new Paragraph("All amounts are in Rs.", SUB_FONT);
            note.setSpacingBefore(5);
            document.add(note);

            // ---------- 3. Totals ----------
            PdfPTable summary = new PdfPTable(new float[]{2f, 3f});
            summary.setWidthPercentage(60);
            summary.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summary.setSpacingBefore(20);

            summaryRow(summary, "Total Qty", String.valueOf(totalQty), false);
            summaryRow(summary, "Total Sales", money(grandSales), false);
            summaryRow(summary, "Total Profit", money(grandProfit), true);

            document.add(summary);

            document.close();   // stream band hone se PEHLE

        } catch (DocumentException e) {
            throw new IOException("PDF create nahi hua", e);
        }

        return filePath.toAbsolutePath();
    }

    private void header(PdfPTable t, String text, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, HEADER_FONT));
        c.setBackgroundColor(BLUE);
        c.setHorizontalAlignment(align);
        c.setPadding(8);
        c.setBorderColor(BLUE);
        t.addCell(c);
    }

    private void cell(PdfPTable t, String text, int align, Color bg) {
        PdfPCell c = new PdfPCell(new Phrase(text, CELL_FONT));
        c.setBackgroundColor(bg);
        c.setHorizontalAlignment(align);
        c.setPadding(7);
        c.setBorderColor(BORDER);
        t.addCell(c);
    }

    private void summaryRow(PdfPTable t, String label, String value, boolean bold) {
        Font f = bold ? TOTAL_FONT : CELL_FONT;
        PdfPCell l = new PdfPCell(new Phrase(label, f));
        PdfPCell v = new PdfPCell(new Phrase(value, f));
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        for (PdfPCell c : new PdfPCell[]{l, v}) {
            c.setBorder(bold ? Rectangle.TOP : Rectangle.NO_BORDER);
            c.setBorderColor(GREY);
            c.setPadding(5);
        }
        t.addCell(l);
        t.addCell(v);
    }

    private String money(double v) {
        return String.format("Rs. %,.2f", v);
    }

    private String amount(double v) {
        return String.format("%,.2f", v);
    }
}
package com.mls.learningfx.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mls.learningfx.model.Item;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SaleReportService {

    // Saved inside the project's resources folder (relative to project root)
    private static final Path REPORT_FOLDER = Paths.get("src", "main", "resources", "reports");

    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color GREY = new Color(100, 116, 139);
    private static final Color LIGHT_ROW = new Color(244, 246, 249);
    private static final Color BORDER = new Color(226, 232, 240);

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 22, Font.BOLD, BLUE);
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, GREY);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
    private static final Font CELL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font TOTAL_LABEL_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
    private static final Font TOTAL_VALUE_FONT = new Font(Font.HELVETICA, 14, Font.BOLD, GREEN);

    /**
     * Creates a PDF of the given sale items and returns the saved file path.
     */
    public Path generateSaleReport(List<Item> items) throws IOException {

        Files.createDirectories(REPORT_FOLDER);

        LocalDateTime now = LocalDateTime.now();
        String fileName = "sale-report-"
                + now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".pdf";
        Path filePath = REPORT_FOLDER.resolve(fileName);

        Document document = new Document(PageSize.A4, 40, 40, 40, 40);

        try {
            // document.close() also closes this stream, so no try-with-resources here
            OutputStream out = new FileOutputStream(filePath.toFile());
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, now);
            addItemsTable(document, items);
            addSummary(document, items);
            addFooter(document);

        } catch (DocumentException e) {
            throw new IOException("Could not create PDF report", e);
        } finally {
            if (document.isOpen()) {
                document.close();   // writes the PDF to disk
            }
        }

        return filePath.toAbsolutePath();
    }

    private void addHeader(Document document, LocalDateTime now) throws DocumentException {

        Paragraph title = new Paragraph("Sale Report", TITLE_FONT);
        title.setAlignment(Element.ALIGN_LEFT);
        document.add(title);

        String dateText = "Generated on "
                + now.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        Paragraph date = new Paragraph(dateText, SUBTITLE_FONT);
        date.setSpacingAfter(20);
        document.add(date);
    }

    private void addItemsTable(Document document, List<Item> items) throws DocumentException {

        PdfPTable table = new PdfPTable(new float[]{0.6f, 4f, 1.2f, 1.8f, 2f});
        table.setWidthPercentage(100);
        table.setHeaderRows(1);

        addHeaderCell(table, "#", Element.ALIGN_CENTER);
        addHeaderCell(table, "Product", Element.ALIGN_LEFT);
        addHeaderCell(table, "Qty", Element.ALIGN_CENTER);
        addHeaderCell(table, "Price", Element.ALIGN_RIGHT);
        addHeaderCell(table, "Subtotal", Element.ALIGN_RIGHT);

        int row = 1;
        for (Item item : items) {
            Color bg = (row % 2 == 0) ? LIGHT_ROW : Color.WHITE;

            addCell(table, String.valueOf(row), Element.ALIGN_CENTER, bg);
            addCell(table, item.getName(), Element.ALIGN_LEFT, bg);
            addCell(table, String.valueOf(item.getQuantity()), Element.ALIGN_CENTER, bg);
            addCell(table, money(item.getPrice()), Element.ALIGN_RIGHT, bg);
            addCell(table, money(item.getSubTotal()), Element.ALIGN_RIGHT, bg);
            row++;
        }

        document.add(table);
    }

    private void addSummary(Document document, List<Item> items) throws DocumentException {

        int totalQty = 0;
        double grandTotal = 0;
        for (Item item : items) {
            totalQty += item.getQuantity();
            grandTotal += item.getSubTotal();
        }

        PdfPTable summary = new PdfPTable(new float[]{3f, 2f});
        summary.setWidthPercentage(45);
        summary.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summary.setSpacingBefore(15);

        addSummaryRow(summary, "Products", String.valueOf(items.size()), CELL_FONT);
        addSummaryRow(summary, "Total Quantity", String.valueOf(totalQty), CELL_FONT);
        addSummaryRow(summary, "GRAND TOTAL", money(grandTotal), TOTAL_VALUE_FONT);

        document.add(summary);
    }

    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph("Thank you for your purchase!", SUBTITLE_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(40);
        document.add(footer);
    }

    // ---------- small helpers ----------

    private void addHeaderCell(PdfPTable table, String text, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(BLUE);
        cell.setHorizontalAlignment(align);
        cell.setPadding(8);
        cell.setBorderColor(BLUE);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, int align, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, CELL_FONT));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(align);
        cell.setPadding(7);
        cell.setBorderColor(BORDER);
        table.addCell(cell);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font valueFont) {
        boolean isTotal = valueFont == TOTAL_VALUE_FONT;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, isTotal ? TOTAL_LABEL_FONT : CELL_FONT));
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));

        for (PdfPCell cell : new PdfPCell[]{labelCell, valueCell}) {
            cell.setBorder(isTotal ? Rectangle.TOP : Rectangle.NO_BORDER);
            cell.setBorderColor(GREY);
            cell.setPaddingTop(isTotal ? 8 : 4);
            cell.setPaddingBottom(4);
        }
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String money(Double value) {
        return String.format("Rs. %,.2f", value == null ? 0.0 : value);
    }
}
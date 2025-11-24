package com.smarttrip.tripservice.util;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;

public class InvoiceGenerator {

    // 🖼 Custom background renderer for gradient + footer bar
    static class GradientBackground extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContentUnder();
            Rectangle rect = document.getPageSize();

            // Create vertical gradient from sky-blue to white
            PdfShading shading = PdfShading.simpleAxial(
                    writer,
                    rect.getLeft(), rect.getBottom(),
                    rect.getLeft(), rect.getTop(),
                    new Color(173, 216, 230), // Light sky blue
                    Color.WHITE
            );

            PdfShadingPattern pattern = new PdfShadingPattern(shading);
            canvas.setShadingFill(pattern);
            canvas.rectangle(rect.getLeft(), rect.getBottom(), rect.getWidth(), rect.getHeight());
            canvas.fill();

            // Footer bar
            canvas.setColorFill(new Color(37, 99, 235)); // SmartTrip Blue
            canvas.rectangle(rect.getLeft(), rect.getBottom(), rect.getWidth(), 40);
            canvas.fill();

            ColumnText.showTextAligned(
                    canvas,
                    Element.ALIGN_CENTER,
                    new Phrase("SmartTrip © 2025 | Your Journey, Our Plan.", new Font(Font.HELVETICA, 10, Font.ITALIC, Color.WHITE)),
                    (rect.getLeft() + rect.getRight()) / 2,
                    rect.getBottom() + 15,
                    0
            );
        }
    }

    public static byte[] generateInvoice(String userEmail, String destination, String startDate,
                                         String endDate, double amount, String currency,
                                         String paymentMethod, String orderId) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 70, 60);
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // ✨ Add gradient background
            GradientBackground event = new GradientBackground();
            writer.setPageEvent(event);

            document.open();

            // 🌍 SmartTrip Header
            Font titleFont = new Font(Font.HELVETICA, 24, Font.BOLD, new Color(37, 99, 235));
            Paragraph title = new Paragraph("✈ SmartTrip", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph tagline = new Paragraph("Your Journey, Our Plan.", new Font(Font.HELVETICA, 13, Font.ITALIC, new Color(0, 102, 204)));
            tagline.setAlignment(Element.ALIGN_CENTER);
            document.add(tagline);

            document.add(new Paragraph(" "));

            // 🧾 Invoice Card Container
            PdfPTable card = new PdfPTable(1);
            card.setWidthPercentage(90);
            card.setSpacingBefore(20);

            PdfPCell outerCell = new PdfPCell();
            outerCell.setPadding(20);
            outerCell.setBackgroundColor(new Color(255, 255, 255, 230)); // translucent white
            outerCell.setBorderColor(new Color(200, 200, 200));

            // Inner content
            Paragraph invoiceTitle = new Paragraph("TRAVEL INVOICE", new Font(Font.HELVETICA, 16, Font.BOLD, new Color(37, 99, 235)));
            invoiceTitle.setAlignment(Element.ALIGN_CENTER);
            outerCell.addElement(invoiceTitle);

            outerCell.addElement(new Paragraph(" "));
            outerCell.addElement(new Paragraph("Invoice ID: " + orderId));
            outerCell.addElement(new Paragraph("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))));
            outerCell.addElement(new Paragraph("Traveler Email: " + userEmail));
            outerCell.addElement(new Paragraph("Destination: " + destination));
            outerCell.addElement(new Paragraph("Trip Duration: " + startDate + " → " + endDate));
            outerCell.addElement(new Paragraph("Payment Method: " + paymentMethod));
            outerCell.addElement(new Paragraph("Total Amount: " + currency + " " + String.format("%.2f", amount)));
            outerCell.addElement(new Paragraph("Status: ✅ Confirmed"));

            card.addCell(outerCell);
            document.add(card);

            // 💡 Travel tips
            document.add(new Paragraph("\n✈ Travel Tips", new Font(Font.HELVETICA, 14, Font.BOLD, new Color(37, 99, 235))));
            document.add(new Paragraph("• Keep digital copies of tickets and IDs.", new Font(Font.HELVETICA, 11)));
            document.add(new Paragraph("• Exchange local currency before arrival.", new Font(Font.HELVETICA, 11)));
            document.add(new Paragraph("• Arrive early for airport check-ins.", new Font(Font.HELVETICA, 11)));

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
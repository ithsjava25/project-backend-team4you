package backendlab.team4you.protocol;

import backendlab.team4you.exceptions.ProtocolNotFoundException;
import backendlab.team4you.exceptions.ProtocolNotReadyForPdfException;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRole;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import java.io.InputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import backendlab.team4you.meeting.Meeting;
import backendlab.team4you.registry.Registry;

@Service
@Transactional(readOnly = true)
public class ProtocolPdfService {

    private final ProtocolRepository protocolRepository;
    private final ProtocolViewService protocolViewService;

    private static final float MARGIN_LEFT = 50;
    private static final float START_Y = 750;
    private static final float BOTTOM_MARGIN = 60;

    public ProtocolPdfService(
            ProtocolRepository protocolRepository,
            ProtocolViewService protocolViewService
    ) {
        this.protocolRepository = protocolRepository;
        this.protocolViewService = protocolViewService;
    }

    public byte[] generatePdf(Long protocolId, UserEntity viewer) {
        Protocol protocol = protocolRepository.findById(protocolId)
                .orElseThrow(() -> new ProtocolNotFoundException(protocolId));

        if (!protocol.isReadyForPdf()) {
            throw new ProtocolNotReadyForPdfException(protocolId);
        }

        List<ProtocolParagraphViewDto> paragraphViews =
                protocolViewService.getParagraphsForViewer(protocolId, viewer);

        Meeting meeting = protocol.getMeeting();
        Registry registry = meeting.getRegistry();

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            try (PdfWriter writer = new PdfWriter(document)) {

                writer.writeLine(protocol.getTitle(), true, 16);
                writer.addSpacing(12);

                writer.writeLine("Diarium: " + registry.getName(), false, 12);
                writer.writeLine("Sammanträde: " + meeting.getTitle(), false, 12);
                writer.writeLine("Datum: " + meeting.getStartsAt().toLocalDate(), false, 12);
                writer.writeLine("Plats: " + (meeting.getLocation() != null ? meeting.getLocation() : "Ej angiven"),
                        false, 12);

                writer.addSpacing(16);

                writer.writeLine("Paragrafer", true, 14);
                writer.addSpacing(8);

                for (ProtocolParagraphViewDto paragraph : paragraphViews) {
                    writer.writeLine(paragraph.heading(), true, 12);

                    writer.writeLine("Ärendenummer: " + paragraph.caseNumber(),
                            false, 12);

                    if (paragraph.decisionRestricted()) {
                        writer.writeLine("Beslut: Beslutet omfattas av sekretess.",
                                false, 12);
                    } else {
                        if (paragraph.decisionLabel() != null) {
                            writer.writeLine("Beslut: " + paragraph.decisionLabel(),
                                    false, 12);
                        }

                        if (paragraph.decisionText() != null && !paragraph.decisionText().isBlank()) {
                            writer.writeLine("Beslutstext:", false, 12);

                            for (String line : splitText(paragraph.decisionText(), 85)) {
                                writer.writeLine(line, false, 12);
                            }
                        }
                    }
                    writer.addSpacing(12);
                }
            }
            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private List<String> splitText(String text, int maxCharsPerLine) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (String word : text.split("\\s+")) {
            if (currentLine.isEmpty()) {
                currentLine.append(word);
                continue;
                }

            if (currentLine.length() + word.length() + 1 > maxCharsPerLine) {
                lines.add(currentLine.toString());
                if (word.length() <= maxCharsPerLine) {
                    currentLine = new StringBuilder(word);
                    } else {
                    int start = 0;
                    while (start < word.length()) {
                        int end = Math.min(start + maxCharsPerLine, word.length());
                        String chunk = word.substring(start, end);
                        if (end < word.length()) {
                            lines.add(chunk);
                            } else {
                            currentLine = new StringBuilder(chunk);
                            }
                        start = end;
                        }
                    }
            } else {
                if (!currentLine.isEmpty()) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    public byte[] generateFullPdf(Long protocolId) {
        return generatePdf(protocolId, createSystemAdminUser());
    }

    private UserEntity createSystemAdminUser() {
        UserEntity systemUser = new UserEntity();
        systemUser.setName("system");
        systemUser.setRole(UserRole.ADMIN);
        return systemUser;
    }

    private static class PdfWriter implements AutoCloseable {

        private final PDDocument document;
        private final PDType0Font regularFont;
        private final PDType0Font boldFont;

        private PDPageContentStream content;
        private float currentY;

        PdfWriter(PDDocument document) throws IOException {
            this.document = document;
            this.regularFont = loadFont(document, "/fonts/NotoSans-Regular.ttf");
            this.boldFont = loadFont(document, "/fonts/NotoSans-Bold.ttf");
            addNewPage();
        }

        private static PDType0Font loadFont(PDDocument document, String path) throws IOException {
            InputStream inputStream = ProtocolPdfService.class.getResourceAsStream(path);

            if (inputStream == null) {
                throw new IllegalStateException("Font file missing from classpath: " + path);
            }

            try (inputStream) {
                return PDType0Font.load(document, inputStream);
            }
        }

        void addNewPage() throws IOException {
            if (content != null) {
                content.endText();
                content.close();
            }

            PDPage page = new PDPage();
            document.addPage(page);

            content = new PDPageContentStream(document, page);
            content.beginText();
            content.newLineAtOffset(MARGIN_LEFT, START_Y);
            currentY = START_Y;
        }

        void writeLine(String text, boolean bold, float fontSize) throws IOException {
            if (currentY <= BOTTOM_MARGIN) {
                addNewPage();
            }

            content.setFont(bold ? boldFont : regularFont, fontSize);
            content.showText(text == null ? "" : text);
            content.newLineAtOffset(0, -(fontSize + 6));
            currentY -= (fontSize + 6);
        }

        void addSpacing(float spacing) throws IOException {
            if (currentY - spacing <= BOTTOM_MARGIN) {
                addNewPage();
                return;
            }

            content.newLineAtOffset(0, -spacing);
            currentY -= spacing;
        }

        @Override
        public void close() throws IOException {
            if (content != null) {
                content.endText();
                content.close();
            }
        }
    }
}

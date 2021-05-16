import com.lowagie.text.pdf.PdfWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;

import static org.thymeleaf.templatemode.TemplateMode.HTML;

public class FlyingSaucerTest {

    private static final String PDF_OUTPUT_FILE = "test.pdf";
    private static final String HTML_OUTPUT_FILE = "test.html";

    private ITextRenderer renderer;
    private TemplateEngine templateEngine;

    @Before
    public void setUp() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(HTML);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        renderer = new ITextRenderer();
    }

    @Test
    public void generatePdf() throws Exception {
        String renderedHtmlContent = templateEngine.process("template", new Context());
        String xHtml = convertToXhtml(renderedHtmlContent);

        try (OutputStream htmlOutputStream = new FileOutputStream(HTML_OUTPUT_FILE)) {
            htmlOutputStream.write(xHtml.getBytes(StandardCharsets.UTF_8));
        }

        renderer.setDocumentFromString(xHtml, FileSystems.getDefault().getPath("src/test/resources").toUri().toURL().toString());
        renderer.setPDFVersion(PdfWriter.VERSION_1_7);
        renderer.layout();

        try (OutputStream pdfOutputStream = new FileOutputStream(PDF_OUTPUT_FILE)) {
            renderer.createPDF(pdfOutputStream);
        }
    }

    private String convertToXhtml(String html) {
        Document document = Jsoup.parse(html);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        return document.html();
    }
}

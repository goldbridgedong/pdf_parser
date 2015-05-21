package chapter15;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;

import com.itextpdf.text.pdf.parser.ContentByteUtils;
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor;

public class ParsingPDF {
    /** The resulting PDF. */
    public static final String PDF_DOC = "resources/pdfs/Hackers_Toeic_2013_Listening.pdf";

    static final String RESULT_PDF = "results/chapter15/Hackers_Toeic_2013_Listening_result.pdf";
    static final String RESULT_TEXT = "results/chapter15/Hackers_Toeic_2013_Listening_result.txt";

    static PrintWriter printWriter;
    static PdfReader pdfReader;
    static PdfStamper pdfStamper;

    /**
     * Extracts text from a PDF document.
     * @param src  the original PDF document
     * @param dest the resulting text file
     * @throws IOException
     */
    public void extractText() throws IOException, DocumentException {

        printWriter = new PrintWriter(new FileOutputStream(RESULT_TEXT));
        pdfReader = new PdfReader(PDF_DOC);
        pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(RESULT_PDF));

        // for each page, I'll reset listener so ...
        for (int page = 1; page <= pdfReader.getNumberOfPages(); page++) {

            ArrayList<String> texts = new ArrayList<String>();
            ArrayList<Rectangle> rects = new ArrayList<Rectangle>();
            PdfContentStreamProcessor processor = new PdfContentStreamProcessor(
                    new ContinuousTextRenderListener(texts, rects));

            PdfDictionary pageDic = pdfReader.getPageN(page);
            PdfDictionary resourcesDic = pageDic.getAsDict(PdfName.RESOURCES);
            processor.processContent(ContentByteUtils.getContentBytesForPage(pdfReader, page), resourcesDic);

            showTextInfo(page, texts, rects);
        }
        printWriter.flush();
        printWriter.close();
        pdfStamper.close();
        pdfReader.close();
    }

    static String linkInfo = "http://endic.naver.com/search.nhn?sLn=kr&isOnlyViewEE=N&query=";
/*
{0,"1"}{1," "}{2," "}{3,"진"}{4,"단"}{5,"고"}{6,"사"}{7," "}
{8,"D"}{9,"i"}{10,"a"}{11,"g"}{12,"o"}{13,"n"}{14,"o"}{15,"s"}{16,"t"}{17,"i"}{18,"c"}{19," "}
{20,"T"}{21,"e"}{22,"s"}{23,"t"}

{"1",0,0}{"",1,1}{"진단고사",2,6}{"Diagonostic",7,18}{"Test",19,23}
 */
    public void showTextInfo(int page, ArrayList<String> texts, ArrayList<Rectangle> rects) {
        PdfContentByte pdf_out = pdfStamper.getOverContent(page);
        pdf_out.saveState();
        pdf_out.setLineWidth(0.01f);

        printWriter.printf("Page %d/#%d\n",page,texts.size());

        for (int i = 0, spanStart = 0; i < texts.size(); i++) {
            if (texts.get(i).matches("[\\s+|*?.'“\\()]")) { // any white space characters
                Rectangle uRect = unionRectangles(rects, spanStart, i - 1);
                String uText = mergeString(texts, spanStart, i - 1);
                if (canLinkable(uText)) {
                    printWriter.printf("{\"%s\",%d,%d}", uText, spanStart, i - 1);
                    pdf_out.rectangle(uRect.x, uRect.y, uRect.width, uRect.height);
                    pdf_out.setAction(new PdfAction(linkInfo + uText), uRect.x, uRect.y, uRect.x + uRect.width, uRect.y + uRect.height);
                }
                spanStart = i+1;
            }
        }
        printWriter.printf("\n");

        pdf_out.stroke();
        pdf_out.restoreState();
    }

    public String mergeString(ArrayList<String> texts, int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i <= to; i++) {
            sb.append(texts.get(i));
        }
        return sb.toString();
    }

    public Rectangle unionRectangles(ArrayList<Rectangle> rects, int from, int to) {

        Rectangle resultRect = new Rectangle(rects.get(from));
        for (int i = from+1; i <= to; i++) {
            resultRect = resultRect.union(rects.get(i));
        }
        return resultRect;
    }

    public boolean canLinkable(String text) {
        if (text.length() > 1)
            return true;

        if (text.trim().isEmpty())
            return false;

        return false;
    }
    /**
     * Main method.
     * @param    args    no arguments needed
     * @throws DocumentException
     * @throws IOException
     */
    public static void main(String[] args) throws DocumentException, IOException {
        ParsingPDF example = new ParsingPDF();
        example.extractText();
    }
}

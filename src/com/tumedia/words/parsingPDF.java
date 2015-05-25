import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;

import com.itextpdf.text.pdf.parser.ContentByteUtils;
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.utilities.MorphologyAnalyzer;

public class ParsingPDF {
    /** The resulting PDF. */
    public static final String PDF_DOC = "resources/pdfs/reading11_advanced2.pdf";

    static final String RESULT_PDF = "results/chapter15/reading11_advanced2_result.pdf";
    static final String RESULT_TEXT = "results/chapter15/reading11_advanced2_result.txt";
/*
    public static final String PDF_DOC = "resources/pdfs/Hackers_Toeic_2013_Listening.pdf";
    static final String RESULT_PDF = "results/chapter15/Hackers_Toeic_2013_Listening_result.pdf";
    static final String RESULT_TEXT = "results/chapter15/Hackers_Toeic_2013_Listening_result.txt";
*/
    static PrintWriter printWriter;
    static PdfReader pdfReader;
    static PdfStamper pdfStamper;
    static MorphologyAnalyzer morphAnalyzer;

    /**
     * Extracts text from a PDF document.
     * @param src  the original PDF document
     * @param dest the resulting text file
     * @throws IOException
     */
    public void extractText() throws IOException, DocumentException, JWNLException {

        printWriter = new PrintWriter(new FileOutputStream(RESULT_TEXT));
        pdfReader = new PdfReader(PDF_DOC);
        pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(RESULT_PDF));
        morphAnalyzer = new MorphologyAnalyzer();


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

    static String linkUrl = "http://endic.naver.com/search.nhn";
    static String linkOption = "?sLn=kr&isOnlyViewEE=N&query=";

    public void showTextInfo(int page, ArrayList<String> texts, ArrayList<Rectangle> rects) throws JWNLException{
        PdfContentByte pdf_out = pdfStamper.getOverContent(page);
        pdf_out.saveState();
        pdf_out.setLineWidth(0.01f);

        printWriter.printf("PageStart %03d\n",page);

        for (int i = 0, spanStart = 0; i < texts.size(); i++) {
            if (texts.get(i).matches("[\\s+|?*?.'“\\()]")) { // any white space characters
                Rectangle uRect = unionRectangles(rects, spanStart, i - 1);
                String uText = mergeString(texts, spanStart, i - 1);
                if (canLinkable(uText)) {
                    //printWriter.printf("{\"%s\"}\n", uText);
                    //printWriter.printf("{\"%s\",%d,%d}", uText, spanStart, i - 1);
                    printWriter.printf("[%d,%d,%d,%d], \"%s\"}\n", uRect.x, uRect.y, uRect.x+uRect.width, uRect.y+uRect.height,linkUrl);

                    //pdf_out.rectangle(uRect.x, uRect.y, uRect.width, uRect.height);
                    pdf_out.setAction(new PdfAction(linkUrl+linkOption + uText), uRect.x, uRect.y, uRect.x + uRect.width, uRect.y + uRect.height);
                }
                spanStart = i+1;
            }
        }
        printWriter.printf("PageEnd\n");

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

    public boolean canLinkable(String phrase) throws JWNLException {
        if (phrase.trim().isEmpty())
            return false;

        if (phrase.length() > 1 && isAlphabet(phrase)) {
            String result = morphAnalyzer.go(phrase);
            if (result != null) { // && !result.equals(phrase)) {
                printWriter.printf("{\"%s\",\"%s\",", phrase, result);
                return true;
            }
            return false;
        }

        return false;
    }
    public boolean isAlphabet(String word) {
        for (char c : word.toCharArray()) {
            if(!(Character.isLetter(c)))
                return false;
        }
        return true;
    }

    /**
     * Main method.
     * @param    args    no arguments needed
     * @throws DocumentException
     * @throws IOException
     */
    public static void main(String[] args) throws DocumentException, IOException, JWNLException {
        ParsingPDF example = new ParsingPDF();
        example.extractText();
    }
}

package chapter15;

import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class ContinuousTextRenderListener implements RenderListener {

    private ArrayList<String> texts;
    private ArrayList<Rectangle> rects;


    /**
     * Creates a RenderListener that will look for text.
     */
    public ContinuousTextRenderListener(ArrayList<String> texts, ArrayList<Rectangle> rects) {
        this.texts = texts;
        this.rects = rects;

    }

    /**
     * @see RenderListener#beginTextBlock()
     */
    public void beginTextBlock() {
        //text_out.print("<");
    }

    /**
     * @see RenderListener#endTextBlock()
     */
    public void endTextBlock() {
        //text_out.println(mergedText + ">");
    }

    /**
     * @see RenderListener#renderImage(ImageRenderInfo)
     */
    public void renderImage(ImageRenderInfo renderInfo) {
        //text_out.println("*");
    }

    /**
     * @see RenderListener#renderText(TextRenderInfo)
     */
    public void renderText(TextRenderInfo renderInfo) {
        //rects.add(getTextRectangle(renderInfo));
        //texts.add(renderInfo.getText());
        List<TextRenderInfo> cr = renderInfo.getCharacterRenderInfos();

        for (int i = 0; i < cr.size(); i++) {
            cr.get(i).getText();
            texts.add(cr.get(i).getText());
            rects.add(getTextRectangle(cr.get(i)));
        }
    }

    private Rectangle getTextRectangle(TextRenderInfo renderInfo) {
        int x = (int) renderInfo.getDescentLine().getStartPoint().get(0);
        int y = (int) renderInfo.getDescentLine().getStartPoint().get(1);
        int width = (int) (renderInfo.getAscentLine().getEndPoint().get(0) - renderInfo.getAscentLine().getStartPoint().get(0));
        int height = (int) (renderInfo.getAscentLine().getEndPoint().get(1) - renderInfo.getDescentLine().getStartPoint().get(1));

        return new Rectangle(x,y,width,height);
    }
}

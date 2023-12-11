import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.net.URI;

public abstract class PiecePanel extends JPanel {
    PieceColor color;
    public PiecePanel(PieceColor color) {
        this.color = color;
        setOpaque(false);
    }
    @Override
    protected abstract void paintComponent(Graphics g);
    public void draw(Graphics2D g2d, int x, int y, int width, int height, String svgFilePath) {
        try {
            URI svgURI = new File(svgFilePath).toURI();;
            SVGDiagram diagram = SVGCache.getSVGUniverse().getDiagram(svgURI);
            float scaleX = (float) width / diagram.getWidth() * 0.8f; //
            float scaleY = (float) height / diagram.getHeight() * 0.8f; //
            AffineTransform transform = AffineTransform.getTranslateInstance(x + width * 0.1, y + height * 0.1);
            transform.concatenate(AffineTransform.getScaleInstance(scaleX, scaleY));
            AffineTransform oldTransform = g2d.getTransform();
            g2d.transform(transform);
            diagram.setIgnoringClipHeuristic(true);
            diagram.render(g2d);
            g2d.setTransform(oldTransform);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

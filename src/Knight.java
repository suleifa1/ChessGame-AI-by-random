import javax.swing.*;
import java.awt.*;

public class Knight extends Piece {
    private final PiecePanel knightPanel;
    public Knight(PieceColor color, int row, int col) {
        super(PieceType.KNIGHT, color, row, col);
        this.knightPanel = new KnightPanel(color);
    }
    @Override
    public boolean isMoveLegal(int toRow, int toCol, JPanel[][] squareLabels, Piece[][] pieces) {
        if(toCol == col & toRow == row)
            return false;
        int rowDiff = Math.abs(toRow - row);
        int colDiff = Math.abs(toCol - col);
        if ((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)) {
            if (pieces[toRow][toCol] != null && pieces[toRow][toCol].getColor() == color) {
                return false;
            }
            return true;
        }
        return false;
    }
    @Override
    public JPanel getPiecePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(knightPanel, BorderLayout.CENTER);
        panel.setOpaque(false);
        return panel;
    }
    public static class KnightPanel extends PiecePanel {
        public KnightPanel(PieceColor color) {
            super(color);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            String svgFilePath = (this.color == PieceColor.WHITE ? "src/images/white_knight.svg" : "src/images/black_knight.svg");
            draw(g2d, 0, 0, getWidth(), getHeight(), svgFilePath);
        }
    }
}

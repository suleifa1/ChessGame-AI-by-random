
import javax.swing.*;
import java.awt.*;
public class Bishop extends Piece {
    private final PiecePanel bishopPanel;
    public Bishop(PieceColor color, int row, int col) {
        super(PieceType.BISHOP, color, row, col);
        this.bishopPanel = new BishopPanel(color);
    }
    @Override
    public boolean isMoveLegal(int toRow, int toCol, JPanel[][] squareLabels, Piece[][] pieces) {
        if(toCol == col & toRow == row)
            return false;
        int rowDiff = Math.abs(toRow - row);
        int colDiff = Math.abs(toCol - col);
        if (rowDiff == colDiff) {
            int rowStep = toRow > row ? 1 : -1;
            int colStep = toCol > col ? 1 : -1;
            int r = row + rowStep;
            int c = col + colStep;
            while (r != toRow && c != toCol) {
                if (pieces[r][c] != null) {
                    return false;
                }
                r += rowStep;
                c += colStep;
            }
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
        panel.add(bishopPanel, BorderLayout.CENTER);
        panel.setOpaque(false);
        return panel;
    }
    public static class BishopPanel extends PiecePanel {
        public BishopPanel(PieceColor color) {
            super(color);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            String svgFilePath = (this.color == PieceColor.WHITE ? "src/images/white_bishop.svg" : "src/images/black_bishop.svg");
            draw(g2d, 0, 0, getWidth(), getHeight(), svgFilePath);
        }

    }
}

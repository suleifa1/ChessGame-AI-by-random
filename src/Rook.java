import javax.swing.*;
import java.awt.*;

public class Rook extends Piece {
    private final PiecePanel rookPanel;
    private  boolean hasMoved;
    public Rook(PieceColor color, int row, int col) {
        super(PieceType.ROOK, color, row, col);
        this.rookPanel = new RookPanel(color);
        this.hasMoved = false;
    }



    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
    @Override
    public boolean isMoveLegal(int toRow, int toCol, JPanel[][] squareLabels, Piece[][] pieces) {
        if(toCol == col & toRow == row)
            return false;
        if (toRow == row) {
            int colStep = toCol > col ? 1 : -1;
            for (int c = col + colStep; c != toCol; c += colStep) {
                if (pieces[row][c] != null) {
                    return false;
                }
            }
        }
        else if (toCol == col) {
            int rowStep = toRow > row ? 1 : -1;
            for (int r = row + rowStep; r != toRow; r += rowStep) {
                if (pieces[r][col] != null) {
                    return false;
                }
            }
        }
        else {
            return false;
        }
        if (pieces[toRow][toCol] != null && pieces[toRow][toCol].getColor() == color) {
            return false;
        }
        return true;
    }
    @Override
    public JPanel getPiecePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(rookPanel, BorderLayout.CENTER);
        panel.setOpaque(false);
        return panel;
    }
    public static class RookPanel extends PiecePanel {
        public RookPanel(PieceColor color) {
            super(color);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            String svgFilePath = (this.color == PieceColor.WHITE ? "src/images/white_rook.svg" : "src/images/black_rook.svg");
            draw(g2d, 0, 0, getWidth(), getHeight(), svgFilePath);
        }
    }
}


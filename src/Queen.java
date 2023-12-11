import javax.swing.*;
import java.awt.*;

public class Queen extends Piece {
    private final PiecePanel queenPanel;
    public Queen(PieceColor color, int row, int col) {
        super(PieceType.QUEEN, color, row, col);
        this.queenPanel = new QueenPanel(color);
    }
    @Override
    public boolean isMoveLegal(int toRow, int toCol, JPanel[][] squareLabels, Piece[][] pieces) {
        if(toCol == col & toRow == row)
            return false;
        int rowDiff = Math.abs(toRow - row);
        int colDiff = Math.abs(toCol - col);
        boolean isHorizontalMove = toRow == row;
        boolean isVerticalMove = toCol == col;
        boolean isDiagonalMove = rowDiff == colDiff;
        if (!(isHorizontalMove || isVerticalMove || isDiagonalMove)) {
            return false;
        }
        int rowStep = toRow > row ? 1 : -1;
        int colStep = toCol > col ? 1 : -1;
        if (isHorizontalMove) {
            rowStep = 0;
        } else if (isVerticalMove) {
            colStep = 0;
        }
        int r = row + rowStep;
        int c = col + colStep;
        while (r != toRow || c != toCol) {
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
    @Override
    public JPanel getPiecePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(queenPanel, BorderLayout.CENTER);
        panel.setOpaque(false);
        return panel;
    }
    public static class QueenPanel extends PiecePanel {
        public QueenPanel(PieceColor color) {
            super(color);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            String svgFilePath = (this.color == PieceColor.WHITE ? "src/images/white_queen.svg" : "src/images/black_queen.svg");
            draw(g2d, 0, 0, getWidth(), getHeight(), svgFilePath);
        }
    }
}

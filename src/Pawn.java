import javax.swing.*;
import java.awt.*;

public class Pawn extends Piece {
    private boolean hasMoved;
    private boolean enPassantCaptureAllowed;
    private final PiecePanel pawnPanel;

    public Pawn(PieceColor color, int row, int col) {
        super(PieceType.PAWN, color, row, col);
        this.pawnPanel = new PawnPanel(color);
        hasMoved = false;
        enPassantCaptureAllowed = false;
    }

    @Override
    public boolean isMoveLegal(int toRow, int toCol, JPanel[][] squareLabels, Piece[][] pieces) {
        if (toCol == col & toRow == row)
            return false;
        int rowDirection = this.getColor() == PieceColor.WHITE ? -1 : 1;
        int rowDiff = (toRow - this.getRow()) * rowDirection;
        int colDiff = Math.abs(toCol - this.getCol());

        if (colDiff == 1 && rowDiff == 1) {
            if (pieces[toRow][toCol] != null && pieces[toRow][toCol].getColor() != this.getColor()) {
                return true;
            }

            if (pieces[toRow - rowDirection][toCol] != null
                    && pieces[toRow - rowDirection][toCol] instanceof Pawn adjacentPawn
                    && pieces[toRow - rowDirection][toCol].getColor() != this.getColor()) {
                if (adjacentPawn.isEnPassantCaptureAllowed()) {
                    return true;
                }
            }
        } else if (colDiff == 0) {
            if (rowDiff == 1 && pieces[toRow][toCol] == null) {
                return true;
            } else if (!hasMoved() && rowDiff == 2) {
                if (pieces[toRow][toCol] == null && pieces[this.getRow() + rowDirection][this.getCol()] == null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void move(int x, int y) {
        super.move(x, y);
        if (!hasMoved) {
            setHasMoved(true);
        } else {
            setEnPassantCaptureAllowed(false);
        }
    }

    @Override
    public JPanel getPiecePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(pawnPanel, BorderLayout.CENTER);
        panel.setOpaque(false);
        return panel;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
        setEnPassantCaptureAllowed(true);
    }

    public boolean isEnPassantCaptureAllowed() {
        return enPassantCaptureAllowed;
    }

    public void setEnPassantCaptureAllowed(boolean enPassantCaptureAllowed) {
        this.enPassantCaptureAllowed = enPassantCaptureAllowed;
    }
    public static class PawnPanel extends PiecePanel {
        public PawnPanel(PieceColor color) {
            super(color);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            String svgFilePath = (this.color == PieceColor.WHITE ? "src/images/white_pawn.svg" : "src/images/black_pawn.svg");
            draw(g2d, 0, 0, getWidth(), getHeight(), svgFilePath);
        }
    }
}

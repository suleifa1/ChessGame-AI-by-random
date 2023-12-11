import javax.swing.*;
import java.awt.*;
public class King extends Piece {
    private final PiecePanel kingPanel;
    private boolean hasMoved;
    private boolean wasInCheck;

    public boolean isWasInCheck() {
        return wasInCheck;
    }

    public void setWasInCheck(boolean wasInCheck) {
        this.wasInCheck = wasInCheck;
    }

    public King(PieceColor color, int row, int col) {
        super(PieceType.KING, color, row, col);
        this.hasMoved = false;
        this.kingPanel = new KingPanel(color);
        this.wasInCheck = false;
    }



    @Override
    public boolean isMoveLegal(int toRow, int toCol, JPanel[][] squareLabels, Piece[][] pieces) {
        if (toCol == col & toRow == row)
            return false;
        int rowDiff = Math.abs(toRow - row);
        int colDiff = Math.abs(toCol - col);

        if (!hasMoved() && row == toRow && (colDiff == 3 || colDiff == 4) && !isWasInCheck()) {
            return isKingKastling(toRow,toCol,pieces);
        }
        if ((rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1) || (rowDiff == 1 && colDiff == 1)) {
            if (pieces[toRow][toCol] != null && pieces[toRow][toCol].getColor() == color) {
                return false;
            }
            return true;
        }

        // Check for castling move


        return false;
    }
    private boolean isKingKastling(int toRow, int toCol, Piece[][] pieces){
        if(!(pieces[toRow][toCol] instanceof Rook)) {
            return false;

        }
        int rookCol = toCol == 0 ? 0 : 7;
        Piece rook = pieces[toRow][rookCol];
        if (((Rook) rook).hasMoved())
            return false;


        // Check if the squares between the king and rook are empty and not under attack
        int step = rookCol < col ? -1 : 1;
        int dec = step == - 1 ? 1 : -1;
        boolean canCastle = true;
        for (int i = col + step; i != rookCol + dec ; i += step) {
            if (pieces[row][i] != null || isSquareUnderAttack(color, row, i, pieces)) {
                canCastle = false;
                break;
            }
        }
        return canCastle;
    }
    private boolean isSquareUnderAttack(PieceColor color, int row, int col, Piece[][] pieces) {
        PieceColor opponentColor = color == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = pieces[r][c];
                if (piece != null && piece.getColor() == opponentColor) {
                    if (piece.isMoveLegal(row, col, null, pieces)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
    @Override
    public JPanel getPiecePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(kingPanel, BorderLayout.CENTER);
        panel.setOpaque(false);
        return panel;
    }

    @Override
    public void move(int x, int y) {
        super.move(x, y);
        if(!hasMoved)
            setHasMoved(true);
    }

    public static class KingPanel extends PiecePanel {
        public KingPanel(PieceColor color) {
            super(color);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            String svgFilePath = (this.color == PieceColor.WHITE ? "src/images/white_king.svg" : "src/images/black_king.svg");
            draw(g2d, 0, 0, getWidth(), getHeight(), svgFilePath);
        }
    }
}
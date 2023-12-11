import javax.swing.*;
public abstract class Piece {
    protected PieceType type;
    protected PieceColor color;
    protected int row;
    protected int col;
    public Piece(PieceType type, PieceColor color, int row, int col) {
        this.type = type;
        this.color = color;
        this.row = row;
        this.col = col;
    }

    public abstract boolean isMoveLegal(int toRow, int toCol, JPanel[][] squareLabels, Piece[][] pieces);
    public void move(int toRow, int toCol) {
        this.row = toRow;
        this.col = toCol;
    }
    public PieceType getType() {
        return type;
    }
    public PieceColor getColor() {
        return color;
    }
    public int getRow() {
        return row;
    }
    public int getCol() {
        return col;
    }
    public abstract  JPanel getPiecePanel();

}

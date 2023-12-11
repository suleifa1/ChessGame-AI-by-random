/**
 *  ChessGame
 *  @autor Faiz Suleimanov
 *  @version 2.0
 */

import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.TranscoderException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;


public class ChessGui extends JLayeredPane {
    // The JFrame object for the game window.
    private final JFrame frame;

    // The JPanel object to hold the chess board.
    private final JPanel boardPanel;

    // An array of JPanel objects to represent the individual squares on the chess board.
    private final JPanel[][] squarePanels = new JPanel[8][8];

    // An array of Piece objects to represent the state of the chess board.
    private final Piece[][] pieces = new Piece[8][8];

    // A JPanel object used to display the piece being dragged during a move.
    private JPanel dragPanel;

    // A JPanel object used to display the top panel of the game window.
    private JPanel topPanel;

    // A JLabel object used to display the current player's color.
    private JLabel currentPlayerLabel;

    // A Timer object used to keep track of the game time.
    private Timer gameTimer;

    // A PieceColor object representing the current player's color.
    private PieceColor currentPlayer = PieceColor.WHITE;

    // Integers representing the source and destination rows and columns of the last move made.
    private int lastMoveSourceRow = -1;
    private int lastMoveSourceCol = -1;
    private int lastMoveDestRow = -1;
    private int lastMoveDestCol = -1;

    // An integer representing the size of the chess board.
    private int boardSize;

    // A boolean indicating whether a castling move is being made.
    private boolean isCastling = false;

    // Integers representing the x and y coordinates of the castling move for the AI player.
    private int xCastleAi = 0;
    private int yCastleAi = 0;

    // A list of Move objects representing the moves made during the game.
    private List<Move> moves = new ArrayList<>();

    // Integers representing the x and y coordinates of the first square clicked during a move.
    private int x1 = 0;
    private int y1 = 0;

    // A JTextArea object used to display the opening moves of the game.
    private JTextArea openingText;

    // A list of strings representing the moves made during the game.
    private final List<String> gameMoves = new ArrayList<>();

    // A list of Opening objects representing the common chess openings.
    private final List<Opening> openings = new ArrayList<>();

    // A JCheckBoxMenuItem object used to toggle the AI player on and off.
    private JCheckBoxMenuItem toggleAI;

    // JLabel objects used to display the time remaining for each player.
    private JLabel whitePlayerTimerLabel;
    private JLabel blackPlayerTimerLabel;

    // JLayeredPane objects used to display animations during the game.
    public JLayeredPane animationLayer;
    public JLayeredPane centerPanel;

    // Integers representing the initial time per player and the time increment.
    private int initialTimePerPlayer; // Initial time for each player (in seconds, for example, 300 seconds = 5 minutes)
    private int timeIncrement; // The increment time in seconds

    // Integers representing the time remaining for each player.
    private int whitePlayerTime;
    private int blackPlayerTime;

    // A Timer object used to update the player's time remaining.
    private Timer timer;

    // A double representing the progress of an animation.
    private double progress;

    // Integers representing the x and y coordinates of a square on the chess board.
    private int x, y;

    // Long integers representing the start time of the white and black players.
    private long whiteStartTime = 0;
    private long blackStartTime = 0;

    // Integers representing the row and column of the square being dragged during a move.
    private int dragCol, dragRow;

    // A boolean indicating whether an animation is currently in progress.
    private volatile boolean animationInProgress = false;

    // Two Lists of Long integers representing the duration of each move made by the white and black players.
    private final List<Long> whiteMoveDurations = new ArrayList<>();
    private final List<Long> blackMoveDurations = new ArrayList<>();

    // An integer representing the number of half moves made without a capture or pawn move.
    private int halfMovesWithoutCaptureOrPawnMove = 0;

    /**
     Updates the label that displays the remaining time for each player. The time is displayed in minutes and seconds format.
     The remaining time for the white player is displayed in the whitePlayerTimerLabel, and the remaining time for the
     black player is displayed in the blackPlayerTimerLabel.
     */
    private void updateTimerLabel() {
        int whiteMinutes = whitePlayerTime / 60;
        int whiteSeconds = whitePlayerTime % 60;
        whitePlayerTimerLabel.setText(String.format("%02d:%02d", whiteMinutes, whiteSeconds));
        int blackMinutes = blackPlayerTime / 60;
        int blackSeconds = blackPlayerTime % 60;
        blackPlayerTimerLabel.setText(String.format("%02d:%02d", blackMinutes, blackSeconds));
    }


    /**

     Highlights the source and destination squares of the last move made on the board.
     If no move has been made, does nothing.
     */
    private void showLastMove() {
        if (lastMoveSourceRow == -1 || lastMoveSourceCol == -1 ||
                lastMoveDestRow == -1 || lastMoveDestCol == -1) {
            return;
        }
// Highlight the source and destination squares of the last move in yellow
        squarePanels[lastMoveSourceRow][lastMoveSourceCol].setBackground(Color.YELLOW);
        squarePanels[lastMoveDestRow][lastMoveDestCol].setBackground(Color.YELLOW);
    }


    /**

     Clears the highlighting of the last move by resetting the background color of the
     source and destination squares to their original color.
     If the source and destination squares of the last move have not been set, does nothing.
     */
    private void clearLastMoveHighlight() {
        if (lastMoveSourceRow == -1 || lastMoveSourceCol == -1 ||
                lastMoveDestRow == -1 || lastMoveDestCol == -1) {
            return;
        }
        squarePanels[lastMoveSourceRow][lastMoveSourceCol].setBackground(getSquareColor(lastMoveSourceRow, lastMoveSourceCol));
        squarePanels[lastMoveDestRow][lastMoveDestCol].setBackground(getSquareColor(lastMoveDestRow, lastMoveDestCol));
    }


    /**

     Returns the color of a square on the chessboard based on its row and column.
     @param row the row of the square
     @param col the column of the square
     @return the color of the square
     */
    private Color getSquareColor(int row, int col) {
        return (row + col) % 2 == 0 ? Color.WHITE : Color.LIGHT_GRAY;
    }

    /**

     Creates an SVG file of the current state of the chess board.
     @throws IOException if there is an error writing to the output file.
     @throws TranscoderException if there is an error in the transcoding process.
     */
    private void makeSvg() throws IOException, TranscoderException {
// Create an SVG document
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
// Create an instance of the SVG Graphics 2D
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        svgGenerator.setSVGCanvasSize(new Dimension(frame.getWidth(), frame.getHeight()));
// Paint the JFrame's content on the SVG Graphics 2D
        frame.getContentPane().paint(svgGenerator);
// Save the SVG content to a file
        String outputFilePath = "svg_example.svg";
        try (FileWriter fileWriter = new FileWriter(outputFilePath)) {
            svgGenerator.stream(fileWriter, true);
        }
    }


    /**

     Creates a PNG file of the current state of the chess board.
     @throws IOException if there is an error writing to the output file.
     */
    private void makePng() throws IOException {

        Container contentPane = frame.getContentPane();
        BufferedImage image = new BufferedImage(contentPane.getWidth(), contentPane.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        contentPane.printAll(g2d);
        g2d.dispose();
        ImageIO.write(image, "png", new File("out.png"));
    }


    /**

     Creates a mouse adapter to handle user interaction with a chess piece on the board.

     When the user clicks on a piece, highlights all valid moves for the piece.

     When the user releases the piece after dragging it, moves the piece to the new position

     if the move is legal. Animates the movement of the piece to the new position.

     If the piece is a pawn and reaches the opposite end of the board, prompts the user to choose

     a piece to promote the pawn to.

     If the 50-move rule applies, prompts the user to declare a draw.

     If the game is over, prompts the user to restart the game.

     @param row The row of the chess piece

     @param col The column of the chess piece

     @return A new MouseAdapter object to handle user interaction
     */
    private MouseAdapter createMouseAdapter(int row, int col) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!gameTimer.isRunning() && currentPlayer == PieceColor.WHITE) {
                    gameTimer.start();
                    whiteStartTime = System.currentTimeMillis();
                }
                if (animationInProgress || isCastling)
                    return;
                dragRow = row;
                dragCol = col;
                if (pieces[dragRow][dragCol] != null) {
                    JPanel componentPanel = null;
                    for (Component component : squarePanels[dragRow][dragCol].getComponents()) {
                        if (component instanceof JPanel) {
                            componentPanel = (JPanel) component;
                            break;
                        }
                    }
                    if (componentPanel != null) {
                        dragPanel = componentPanel;
                        x = squarePanels[dragRow][dragCol].getX();
                        y = squarePanels[dragRow][dragCol].getY();
                    }
                }
                if (!moves.isEmpty()){
                    moves.clear();
                    clearHighlights();
                }
                highlightValidMoves(row, col);


            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (animationInProgress || isCastling)
                    return;
                Point releasePoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), animationLayer);
                int releaseRow = releasePoint.y / squarePanels[0][0].getHeight();
                int releaseCol = releasePoint.x / squarePanels[0][0].getWidth();
                Piece piece = pieces[dragRow][dragCol];
                if (piece == null) {
                    return;
                }
                clearHighlights();
                if (piece.getColor() == currentPlayer && piece.isMoveLegal(releaseRow, releaseCol, squarePanels, pieces)) {
                    if (piece instanceof King && pieces[releaseRow][releaseCol] instanceof Rook && pieces[releaseRow][releaseCol].getColor() == currentPlayer) {
                        int newCol = dragCol - releaseCol == -3 ? 6 : 2;
                        int rookCol = releaseCol == 7 ? dragCol + 1 : dragCol - 1;
                        isCastling = true;
                        animationInProgress = true;
                        makeMove(dragRow, dragCol, releaseRow, newCol);
                        Timer wait = new Timer(1000, e12 -> {
                            makeMove(releaseRow, releaseCol, dragRow, rookCol);
                            makeCastle();
                        });
                        wait.setRepeats(false);
                        wait.start();
                        return;
                    }
                    makeEnPassant(releaseRow, releaseCol, piece);
                    clearLastMoveHighlight();
                    Piece tempPiece = pieces[releaseRow][releaseCol];
                    pieces[releaseRow][releaseCol] = pieces[dragRow][dragCol];
                    pieces[dragRow][dragCol] = null;
                    boolean inCheck = isInCheck(currentPlayer);
                    pieces[dragRow][dragCol] = pieces[releaseRow][releaseCol];
                    pieces[releaseRow][releaseCol] = tempPiece;
                    if (inCheck) {
                        JOptionPane.showMessageDialog(ChessGui.this, "Invalid move! You cannot move into check.");
                        return;
                    }
                    dragPanel.setLocation(x, y);
                    animationLayer.add(dragPanel);
                    lastMoveSourceRow = dragRow;
                    lastMoveSourceCol = dragCol;
                    lastMoveDestRow = releaseRow;
                    lastMoveDestCol = releaseCol;
                    if (pieces[dragRow][dragCol] instanceof Pawn || pieces[releaseRow][releaseCol] != null) {
                        halfMovesWithoutCaptureOrPawnMove = 0;
                    } else {
                        halfMovesWithoutCaptureOrPawnMove++;
                    }
                    pieces[releaseRow][releaseCol] = pieces[dragRow][dragCol];
                    pieces[dragRow][dragCol] = null;
                    piece.move(releaseRow, releaseCol);
                    if (piece instanceof Rook) {
                        ((Rook) piece).setHasMoved(true);
                    }
                    if (piece instanceof King) {
                        ((King) piece).setHasMoved(true);
                    }
                    // Animate the piece movement
                    progress = 0;
                    if (timer != null) {
                        timer.stop();
                    }
                    int deltaX = Math.abs(releaseCol - dragCol) * squarePanels[0][0].getWidth();
                    int deltaY = Math.abs(releaseRow - dragRow) * squarePanels[0][0].getHeight();
                    int maxMovement = Math.max(deltaX, deltaY);
                    int minSteps = (int) Math.ceil(maxMovement / 10.0);
                    int n = Math.max(minSteps, 50);
                    int delay = 500 / n;
                    long startTime = System.currentTimeMillis();
                    timer = new Timer(delay, e1 -> {
                        progress += 1.0 / n;
                        if (progress >= 1) {
                            timer.stop();
                            long endTime = System.currentTimeMillis();
                            long elapsedTime = endTime - startTime;
                            completeAnAnimation(releaseRow, releaseCol);
                        } else {
                            int xOffset = (int) ((releaseCol - dragCol) * progress * squarePanels[0][0].getWidth());
                            int yOffset = (int) ((releaseRow - dragRow) * progress * squarePanels[0][0].getHeight());
                            dragPanel.setLocation(xOffset + x, yOffset + y);
                            animationLayer.repaint();
                        }
                    });
                    squarePanels[dragRow][dragCol].remove(dragPanel);
                    squarePanels[dragRow][dragCol].repaint();
                    animationInProgress = true;
                    timer.start();
                    if (piece instanceof Pawn && ((piece.color == PieceColor.WHITE && releaseRow == 0) || (piece.color == PieceColor.BLACK && releaseRow == 7))) {
                        Piece promotedPiece = promotePawn(piece.color, piece);
                        pieces[releaseRow][releaseCol] = promotedPiece;
                        JPanel newPiecePanel = promotedPiece.getPiecePanel();
                        squarePanels[releaseRow][releaseCol].removeAll();
                        squarePanels[releaseRow][releaseCol].add(newPiecePanel);
                        squarePanels[releaseRow][releaseCol].validate();
                        squarePanels[releaseRow][releaseCol].repaint();
                    }
                    if (gameTimer.isRunning()) {
                        if (currentPlayer == PieceColor.WHITE) {
                            long duration = System.currentTimeMillis() - whiteStartTime;
                            whiteMoveDurations.add(duration);
                            blackStartTime = System.currentTimeMillis();
                        } else {
                            long duration = System.currentTimeMillis() - blackStartTime;
                            blackMoveDurations.add(duration);
                            whiteStartTime = System.currentTimeMillis();
                        }
                    }

                    if (halfMovesWithoutCaptureOrPawnMove >= 100) {
                        int option = JOptionPane.showConfirmDialog(ChessGui.this, "50-move rule applies. Do you want to declare a draw?", "Draw Offer", JOptionPane.YES_NO_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            resetBoard();
                        }
                    }
                    endPlayerTurn();
                    if (generateAllMoves(currentPlayer).size() == 0 && isInCheck(currentPlayer)) {
                        String winner = (currentPlayer == PieceColor.WHITE ? "Black wins!" : "White wins!");
                        int option = JOptionPane.showConfirmDialog(ChessGui.this, winner + " Restart game?", "Game Over", JOptionPane.YES_NO_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            resetBoard();
                        }
                        return;
                    } else if (generateAllMoves(currentPlayer).size() == 0) {
                        String result = "Stalemate!";
                        int option = JOptionPane.showConfirmDialog(ChessGui.this, result + " Restart game?", "Game Over", JOptionPane.YES_NO_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            resetBoard();
                        }
                        return;
                    } else if (isInCheck(currentPlayer)) {
                        setKingInCheck(currentPlayer);
                    }
                    currentPlayerLabel.setText((currentPlayer == PieceColor.WHITE ? "White's turn" : "Black's turn"));
                    showLastMove();
                    if (toggleAI.isSelected())
                        scheduleRandomBlackMove();
                    gameMoves.add(moveToString(dragRow, dragCol, releaseRow, releaseCol));
                    String currentOpening = getCurrentOpening(gameMoves);
                    openingText.setText("Current opening: " + currentOpening);
                }
            }
        };
    }


    /**
     * Sets the flag to indicate whether the king of the specified color is in check.
     *
     * @param color The color of the king (PieceColor.WHITE or PieceColor.BLACK).
     */
    private void setKingInCheck(PieceColor color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = pieces[row][col];
                if (piece instanceof King && piece.getColor() == color) {
                    ((King) piece).setWasInCheck(true);
                    return;
                }
            }
        }
    }


    /**

     Completes the animation of a piece's movement on the chessboard. This method is called
     after the piece's movement animation has finished.
     @param releaseRow The destination row of the moving piece.
     @param releaseCol The destination column of the moving piece.
     */
    private void completeAnAnimation(int releaseRow, int releaseCol) {
        animationLayer.remove(dragPanel);
        animationLayer.repaint();
        squarePanels[releaseRow][releaseCol].removeAll();
        if (releaseCol == 0) {
            JLabel rankLabel = new JLabel(Integer.toString(8 - releaseRow), SwingConstants.CENTER);
            squarePanels[releaseRow][releaseCol].add(rankLabel, BorderLayout.WEST);
        }
        if (releaseRow == 7) {
            JLabel fileLabel = new JLabel(Character.toString((char) (releaseCol + 'a')), SwingConstants.CENTER);
            squarePanels[releaseRow][releaseCol].add(fileLabel, BorderLayout.SOUTH);
        }
        squarePanels[releaseRow][releaseCol].add(dragPanel);
        squarePanels[releaseRow][releaseCol].validate();
        squarePanels[releaseRow][releaseCol].repaint();
        animationInProgress = false;
    }


    /**

     Handles the en passant rule for pawn captures in chess. This method is called when a pawn
     is moved to determine if an en passant capture should occur.
     @param releaseRow The destination row of the moving pawn.
     @param releaseCol The destination column of the moving pawn.
     @param piece The moving pawn.
     */
    private void makeEnPassant(int releaseRow, int releaseCol, Piece piece) {
        if (piece instanceof Pawn movingPawn) {
            int rowDirection = movingPawn.getColor() == PieceColor.WHITE ? -1 : 1;
            if (pieces[releaseRow][releaseCol] == null
                    && releaseRow == movingPawn.getRow() + rowDirection
                    && Math.abs(releaseCol - movingPawn.getCol()) == 1
                    && pieces[releaseRow - rowDirection][releaseCol] instanceof Pawn
                    && pieces[releaseRow - rowDirection][releaseCol].getColor() != movingPawn.getColor()) {
// Remove the opponent's pawn from the board
                pieces[releaseRow - rowDirection][releaseCol] = null;
                squarePanels[releaseRow - rowDirection][releaseCol].removeAll();
                squarePanels[releaseRow - rowDirection][releaseCol].repaint();
            }
        }
    }

    /**
     * This method is called when a castling move has been made.
     * It handles the post-castling procedures such as updating
     * player turn timers, resetting the half-move counter, and
     * updating the UI to display the correct player's turn.
     */
    private void makeCastle() {
        isCastling = false;
        if (currentPlayer == PieceColor.WHITE) {
            long duration = System.currentTimeMillis() - whiteStartTime;
            whiteMoveDurations.add(duration);
            blackStartTime = System.currentTimeMillis();
            if (toggleAI.isSelected())
                scheduleRandomBlackMove();
        } else {
            long duration = System.currentTimeMillis() - blackStartTime;
            blackMoveDurations.add(duration);
            whiteStartTime = System.currentTimeMillis();
        }
        halfMovesWithoutCaptureOrPawnMove = 0;
        endPlayerTurn();
        currentPlayerLabel.setText((currentPlayer == PieceColor.WHITE ? "White's turn" : "Black's turn"));
        showLastMove();
    }


    /**

     This method is mainly used for implementing computer moves, but it is also used for castling by the real player. It moves the piece from the source square to the destination square, animating the movement and updating the game state accordingly. If the move is a castling move, it calls the castleAi method to animate the rook's movement as well. The method also handles promotions for pawns that reach the opposite end of the board. Additionally, it tracks the half-move clock and checks for draw conditions, displaying a dialog if the 50-move rule applies. At the end of the method, it switches the current player and shows the last move made.
     @param sourceRow the row index of the source square
     @param sourceCol the column index of the source square
     @param destRow the row index of the destination square
     @param destCol the column index of the destination square
     */
    private void makeMove(int sourceRow, int sourceCol, int destRow, int destCol) {
        dragRow = sourceRow;
        dragCol = sourceCol;
        if (!gameTimer.isRunning() && currentPlayer == PieceColor.BLACK) {
            gameTimer.start();
            blackStartTime = System.currentTimeMillis();
        }
        if (pieces[dragRow][dragCol] != null) {
            JPanel componentPanel = null;
            for (Component component : squarePanels[dragRow][dragCol].getComponents()) {
                if (component instanceof JPanel) {
                    componentPanel = (JPanel) component;
                    break;
                }
            }
            if (componentPanel != null) {
                dragPanel = componentPanel;
                x1 = squarePanels[dragRow][dragCol].getX();
                y1 = squarePanels[dragRow][dragCol].getY();
            }
        }
        Piece piece = pieces[dragRow][dragCol];
        if (piece == null) {
            return;
        }
        if (piece instanceof King && pieces[destRow][destCol] instanceof Rook && pieces[destRow][destCol].getColor() == currentPlayer) {
            int newCol = dragCol - destCol == -3 ? 6 : 2;
            int rookCol = destCol == 7 ? dragCol + 1 : dragCol - 1;
            isCastling = true;
            castleAi(dragRow, dragCol, destRow, newCol);
            Timer wait = new Timer(1000, e -> {
                castleAi(destRow, destCol, dragRow, rookCol);
                makeCastle();
            });
            wait.setRepeats(false);
            wait.start();
            return;
        }
        makeEnPassant(destRow, destCol, piece);
        dragPanel.setLocation(x1, y1);
        animationLayer.add(dragPanel);
        clearLastMoveHighlight();
        lastMoveSourceRow = dragRow;
        lastMoveSourceCol = dragCol;
        lastMoveDestRow = destRow;
        lastMoveDestCol = destCol;
        if (pieces[dragRow][dragCol] instanceof Pawn || pieces[destRow][destCol] != null) {
            halfMovesWithoutCaptureOrPawnMove = 0;
        } else {
            halfMovesWithoutCaptureOrPawnMove++;
        }
        pieces[destRow][destCol] = pieces[dragRow][dragCol];
        pieces[dragRow][dragCol] = null;
        piece.move(destRow, destCol);
        if (piece instanceof Rook) {
            ((Rook) piece).setHasMoved(true);
        }
        if (piece instanceof King) {
            ((King) piece).setHasMoved(true);
        }
        // Animate the piece movement
        progress = 0;
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(500 / 60, e1 -> {
            progress += 0.05;
            if (progress >= 1) {
                timer.stop();
                animationLayer.remove(dragPanel);
                animationLayer.repaint();
                squarePanels[destRow][destCol].removeAll();
                if (destCol == 0) {
                    JLabel rankLabel = new JLabel(Integer.toString(8 - destRow), SwingConstants.CENTER);
                    squarePanels[destRow][destCol].add(rankLabel, BorderLayout.WEST);
                }
                if (destRow == 7) {
                    JLabel fileLabel = new JLabel(Character.toString((char) (destCol + 'a')), SwingConstants.CENTER);
                    squarePanels[destRow][destCol].add(fileLabel, BorderLayout.SOUTH);
                }
                squarePanels[destRow][destCol].add(dragPanel);
                squarePanels[destRow][destCol].revalidate();
                squarePanels[destRow][destCol].repaint();
                animationInProgress = false;
                if (piece instanceof Pawn && (piece.color == PieceColor.BLACK && destRow == 7)) {
                    choseRandomOption(destRow, destCol);
                    squarePanels[destRow][destCol].revalidate();
                    squarePanels[destRow][destCol].repaint();
                }
                gameMoves.add(moveToString(sourceRow, sourceCol, destRow, destCol));
                String currentOpening = getCurrentOpening(gameMoves);
                openingText.setText("Current opening: " + currentOpening);
            } else {
                int xOffset = (int) ((destCol - dragCol) * progress * squarePanels[0][0].getWidth());
                int yOffset = (int) ((destRow - dragRow) * progress * squarePanels[0][0].getHeight());
                dragPanel.setLocation(xOffset + x1, yOffset + y1);
                animationLayer.repaint();
            }
        });
        squarePanels[dragRow][dragCol].remove(dragPanel);
        squarePanels[dragRow][dragCol].repaint();
        animationInProgress = true;
        timer.start();
        if (isCastling)
            return;
        if (currentPlayer == PieceColor.WHITE) {
            long duration = System.currentTimeMillis() - whiteStartTime;
            whiteMoveDurations.add(duration);
            blackStartTime = System.currentTimeMillis();
            scheduleRandomBlackMove();
        } else {
            long duration = System.currentTimeMillis() - blackStartTime;
            blackMoveDurations.add(duration);
            whiteStartTime = System.currentTimeMillis();
        }
        if (pieces[lastMoveDestRow][lastMoveDestCol] instanceof Pawn || pieces[dragRow][dragCol] != null) {
            halfMovesWithoutCaptureOrPawnMove = 0;
        } else {
            halfMovesWithoutCaptureOrPawnMove++;
        }
        if (halfMovesWithoutCaptureOrPawnMove >= 100) {
            int option = JOptionPane.showConfirmDialog(ChessGui.this, "50-move rule applies. Do you want to declare a draw?", "Draw Offer", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                resetBoard();
            }
        }
        endPlayerTurn();
        if (generateAllMoves(currentPlayer).size() == 0 && isInCheck(currentPlayer)) {
            String winner = (currentPlayer == PieceColor.WHITE ? "Black wins!" : "White wins!");
            int option = JOptionPane.showConfirmDialog(ChessGui.this, winner + " Restart game?", "Game Over", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                resetBoard();
            }
            return;
        } else if (isInCheck(currentPlayer)) {
            setKingInCheck(currentPlayer);
        }
        currentPlayerLabel.setText((currentPlayer == PieceColor.WHITE ? "White's turn" : "Black's turn"));
        showLastMove();
    }

    private void choseRandomOption(int destRow, int destCol) {
        Random r = new Random();
        Piece[] option = new Piece[]{new Queen(PieceColor.BLACK, destRow, destCol),
                new Bishop(PieceColor.BLACK, destRow, destCol),
                new Rook(PieceColor.BLACK, destRow, destCol),
                new Knight(PieceColor.BLACK, destRow, destCol)};
        Piece promPiece = option[r.nextInt(4)];
        JPanel newPiecePanel = promPiece.getPiecePanel();
        pieces[destRow][destCol] = promPiece;
        squarePanels[destRow][destCol].removeAll();
        if (destCol == 0) {
            JLabel rankLabel = new JLabel(Integer.toString(8 - destRow), SwingConstants.CENTER);
            squarePanels[destRow][destCol].add(rankLabel, BorderLayout.WEST);
        }
        if (destRow == 7) {
            JLabel fileLabel = new JLabel(Character.toString((char) (destCol + 'a')), SwingConstants.CENTER);
            squarePanels[destRow][destCol].add(fileLabel, BorderLayout.SOUTH);
        }
        squarePanels[destRow][destCol].add(newPiecePanel);
    }
    /**

     This method is used for castling a piece on the computer's side.

     @param sourceRow the row of the piece being moved

     @param sourceCol the column of the piece being moved

     @param destRow the row of the destination square

     @param destCol the column of the destination square
     */
    private void castleAi(int sourceRow, int sourceCol, int destRow, int destCol) {
        dragRow = sourceRow;
        dragCol = sourceCol;
        if (pieces[dragRow][dragCol] != null) {
            JPanel componentPanel = null;
            for (Component component : squarePanels[dragRow][dragCol].getComponents()) {
                if (component instanceof JPanel) {
                    componentPanel = (JPanel) component;
                    break;
                }
            }
            if (componentPanel != null) {
                dragPanel = componentPanel;
                xCastleAi = squarePanels[dragRow][dragCol].getX();
                yCastleAi = squarePanels[dragRow][dragCol].getY();
            }
        }
        Piece piece = pieces[dragRow][dragCol];
        if (piece == null) {
            return;
        }
        dragPanel.setLocation(xCastleAi, yCastleAi);
        animationLayer.add(dragPanel);
        clearLastMoveHighlight();
        lastMoveSourceRow = dragRow;
        lastMoveSourceCol = dragCol;
        lastMoveDestRow = destRow;
        lastMoveDestCol = destCol;
        pieces[destRow][destCol] = pieces[dragRow][dragCol];
        pieces[dragRow][dragCol] = null;
        piece.move(destRow, destCol);
        if (piece instanceof Rook) {
            ((Rook) piece).setHasMoved(true);
        }
        if (piece instanceof King) {
            ((King) piece).setHasMoved(true);
        }
        // Animate the piece movement
        progress = 0;
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(500 / 60, e1 -> {
            progress += 0.05;
            if (progress >= 1) {
                timer.stop();
                completeAnAnimation(destRow, destCol);
                if (piece instanceof Pawn && (piece.color == PieceColor.BLACK && destRow == 7)) {
                    choseRandomOption(destRow, destCol);
                    squarePanels[destRow][destCol].validate();
                    squarePanels[destRow][destCol].repaint();
                }
                gameMoves.add(moveToString(sourceRow, sourceCol, destRow, destCol));
                String currentOpening = getCurrentOpening(gameMoves);
                openingText.setText("Current opening: " + currentOpening);
            } else {
                int xOffset = (int) ((destCol - dragCol) * progress * squarePanels[0][0].getWidth());
                int yOffset = (int) ((destRow - dragRow) * progress * squarePanels[0][0].getHeight());
                dragPanel.setLocation(xOffset + xCastleAi, yOffset + yCastleAi);
                animationLayer.repaint();
            }
        });
        squarePanels[dragRow][dragCol].remove(dragPanel);
        squarePanels[dragRow][dragCol].repaint();
        animationInProgress = true;
        timer.start();
        if (isCastling)
            return;
        if (currentPlayer == PieceColor.WHITE) {
            long duration = System.currentTimeMillis() - whiteStartTime;
            whiteMoveDurations.add(duration);
            blackStartTime = System.currentTimeMillis();
            scheduleRandomBlackMove();
        } else {
            long duration = System.currentTimeMillis() - blackStartTime;
            blackMoveDurations.add(duration);
            whiteStartTime = System.currentTimeMillis();
        }
        endPlayerTurn();
        currentPlayerLabel.setText((currentPlayer == PieceColor.WHITE ? "White's turn" : "Black's turn"));
        showLastMove();
    }


    /**
     * This method schedules a random move for the black player with a delay of 750 milliseconds. It uses a Timer object to delay the execution of the makeRandomBlackMove() method, which selects a random valid move and makes it using the makeMove() method. The Timer is set to only execute once by calling setRepeats(false) and starts the Timer by calling start().
     */
    private void scheduleRandomBlackMove() {
        int delay = 750;
        Timer timer = new Timer(delay, e -> makeRandomBlackMove());
        timer.setRepeats(false);
        timer.start();
    }


    /**

     This method generates a random move for the black player and makes the move using the makeMove method. If there are no legal moves for the black player, it checks if the black player is in checkmate or if the game is in a stalemate, and displays an appropriate dialog message.
     */
    private void makeRandomBlackMove() {

        List<Move> allMoves = generateAllMoves(PieceColor.BLACK);
        if (!allMoves.isEmpty()) {
            int randomIndex = (int) (Math.random() * allMoves.size());
            Move randomMove = allMoves.get(randomIndex);
            int sourceRow = randomMove.startRow;
            int sourceCol = randomMove.startCol;
            int destRow = randomMove.endRow;
            int destCol = randomMove.endCol;
            makeMove(sourceRow, sourceCol, destRow, destCol);
        } else if (isInCheck(PieceColor.BLACK) && allMoves.isEmpty()) {
            JOptionPane.showMessageDialog(ChessGui.this, "Checkmate. AI lost.");
        } else {
            JOptionPane.showMessageDialog(ChessGui.this, "Stalemate.");
        }
    }


    /**

     Generates all valid moves for a given piece on the board, taking into account the current state of the game.
     The method considers all possible destination squares for the piece and checks if the move is legal, meaning
     it doesn't expose the current player's king to a check. If a move is legal, it is added to the list of valid moves.
     The method uses a temporary copy of the board to check the legal moves without modifying the actual game state.
     @param piece the piece for which the valid moves are generated
     @return a list of valid moves for the piece
     */
    private List<Move> generateMoves(Piece piece) {
        List<Move> allMoves = new ArrayList<>();
        for (int newRow = 0; newRow < 8; newRow++) {
            for (int newCol = 0; newCol < 8; newCol++) {
                if (piece.isMoveLegal(newRow, newCol, squarePanels, pieces)) {
                    if(piece instanceof King ){
                        int rowDiff = Math.abs(piece.getRow() - newRow);
                        int colDiff = Math.abs(piece.getCol() - newCol);
                        if(rowDiff == 0 && colDiff == 3 || colDiff == 4 ){
                            allMoves.add(new Move(piece.getRow(), piece.getCol(), newRow, newCol));
                        }

                    }

                    Piece tempPiece = pieces[newRow][newCol];
                    pieces[newRow][newCol] = pieces[piece.getRow()][piece.getCol()];
                    pieces[piece.getRow()][piece.getCol()] = null;
                    boolean inCheck = isInCheck(currentPlayer);
                    pieces[piece.getRow()][piece.getCol()] = pieces[newRow][newCol];
                    pieces[newRow][newCol] = tempPiece;
                    if (!inCheck) {
                        allMoves.add(new Move(piece.getRow(), piece.getCol(), newRow, newCol));
                    }
                }
            }
        }
        return allMoves;
    }


    /**

     Generates all valid moves for all pieces of a given color on the board, taking into account the current state of the game.
     The method uses the generateMoves() method to generate the valid moves for each piece of the given color on the board.
     @param color the color of the pieces for which the valid moves are generated
     @return a list of valid moves for all pieces of the given color
     */
    private List<Move> generateAllMoves(PieceColor color) {
        List<Move> allMoves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (pieces[row][col] != null && pieces[row][col].getColor() == color) {
                    allMoves.addAll(generateMoves(pieces[row][col]));
                }
            }
        }
        return allMoves;
    }


    /**

     Highlights all valid moves for the selected piece on the chess board.
     @param row the row of the selected piece
     @param col the column of the selected piece
     */
    private void highlightValidMoves(int row, int col) {
        Piece piece = pieces[row][col];
// If the selected piece doesn't belong to the current player, return without highlighting any moves.
        if (!(piece.getColor() == currentPlayer))
            return;
// Generate all valid moves for the selected piece.
        moves = generateMoves(piece);
// Highlight each valid move on the chess board by changing its background color to green.
        for (Move mov : moves) {
            squarePanels[mov.endRow][mov.endCol].setBackground(Color.GREEN);
        }
    }


    /**

     This method clears the highlights from all squares on the board. It is called whenever the user deselects a piece or makes a move.
     */
    private void clearHighlights() {
        for (Move mov : moves) {
            squarePanels[mov.endRow][mov.endCol].setBackground(getSquareColor(mov.endRow, mov.endCol));
        }
    }


    /**

     This method updates the size of the chess board based on the current size of the frame. It calculates the minimum size of the board based on the width and height of the frame, subtracting 100 pixels from each dimension to account for the size of the side panels. It then sets the bounds of the board panel and animation layer to match the calculated size, and sets the preferred size of the center panel to match the board size. Finally, it revalidates the center panel to ensure that the changes take effect.
     */
    private void updateBoardSize() {
        boardSize = Math.min(getWidth() - 100, getHeight() - 100);
        boardPanel.setBounds(0, 0, boardSize, boardSize);
        animationLayer.setBounds(0, 0, boardSize, boardSize);
        centerPanel.setPreferredSize(new Dimension(boardSize, boardSize));
        centerPanel.revalidate();
    }


    /**

     Determines if the specified color's king is in check.
     @param color the color of the king to check
     @return true if the king of the specified color is in check, false otherwise
     */
    private boolean isInCheck(PieceColor color) {
        int kingRow = -1, kingCol = -1;
        // Find the king's position
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = pieces[row][col];
                if (piece != null && piece.getColor() == color && piece instanceof King) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
            if (kingRow != -1 && kingCol != -1) {
                break;
            }
        }

        // Check if any opponent piece can capture the king
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = pieces[row][col];
                if (piece != null && piece.getColor() != color) {
                    if (piece.isMoveLegal(kingRow, kingCol, squarePanels, pieces) ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**

     This method sets up the chessboard based on a given FEN (Forsyth-Edwards Notation) string. It parses the FEN string to
     create the appropriate pieces and sets their initial positions. It also sets up the castling and en passant flags, and
     updates the current player. At the end of the method, it calls createBoard to update the GUI with the new board state.
     @param fen the FEN string representing the desired board state
     */
    private void setBoardFromFEN(String fen) {
        String[] fenParts = fen.split(" ");
        String[] fenBoard = fenParts[0].split("/");
        Map<Character, PieceType> fenToPieceTypeMap = new HashMap<>();
        fenToPieceTypeMap.put('k', PieceType.KING);
        fenToPieceTypeMap.put('q', PieceType.QUEEN);
        fenToPieceTypeMap.put('r', PieceType.ROOK);
        fenToPieceTypeMap.put('b', PieceType.BISHOP);
        fenToPieceTypeMap.put('n', PieceType.KNIGHT);
        fenToPieceTypeMap.put('p', PieceType.PAWN);
        halfMovesWithoutCaptureOrPawnMove = Integer.parseInt(fenParts[4]);
        for (int row = 0; row < 8; row++) {
            int col = 0;
            for (char c : fenBoard[row].toCharArray()) {
                if (Character.isDigit(c)) {
                    col += Character.getNumericValue(c);
                } else {
                    PieceType pieceType = fenToPieceTypeMap.get(Character.toLowerCase(c));
                    PieceColor pieceColor = Character.isLowerCase(c) ? PieceColor.BLACK : PieceColor.WHITE;
                    if (pieceType == PieceType.KING) {
                        pieces[row][col] = new King(pieceColor, row, col);
                        if (!((row == 0 && col == 4) || (row == 7 && col == 4))) {
                            ((King) pieces[row][col]).setHasMoved(true);
                        }
                    }
                    if (pieceType == PieceType.BISHOP) {
                        pieces[row][col] = new Bishop(pieceColor, row, col);
                    }
                    if (pieceType == PieceType.KNIGHT) {
                        pieces[row][col] = new Knight(pieceColor, row, col);
                    }
                    if (pieceType == PieceType.PAWN) {
                        pieces[row][col] = new Pawn(pieceColor, row, col);
                        if (!((pieceColor == PieceColor.WHITE && row == 6) || (pieceColor == PieceColor.BLACK && row == 1))) {
                            ((Pawn) pieces[row][col]).setHasMoved(true);
                        }
                    }
                    if (pieceType == PieceType.QUEEN) {
                        pieces[row][col] = new Queen(pieceColor, row, col);
                    }
                    if (pieceType == PieceType.ROOK) {
                        pieces[row][col] = new Rook(pieceColor, row, col);
                        if (!((row == 0 && (col == 0 || col == 7)) || (row == 7 && (col == 0 || col == 7)))) {
                            ((Rook) pieces[row][col]).setHasMoved(true);
                        }
                    }
                    col++;
                }
            }
        }
        String castlingInfo = fenParts[2];
        if (pieces[0][4] instanceof King) {
            ((King) pieces[0][4]).setHasMoved(!castlingInfo.contains("k") && !castlingInfo.contains("q"));
        }
        if (pieces[0][7] instanceof Rook) {
            ((Rook) pieces[0][7]).setHasMoved(!castlingInfo.contains("k"));
        }
        if (pieces[0][0] instanceof Rook) {
            ((Rook) pieces[0][0]).setHasMoved(!castlingInfo.contains("q"));
        }
        if (pieces[7][4] instanceof King) {
            ((King) pieces[7][4]).setHasMoved(!castlingInfo.contains("K") && !castlingInfo.contains("Q"));
        }
        if (pieces[7][7] instanceof Rook) {
            ((Rook) pieces[7][7]).setHasMoved(!castlingInfo.contains("K"));
        }
        if (pieces[7][0] instanceof Rook) {
            ((Rook) pieces[7][0]).setHasMoved(!castlingInfo.contains("Q"));
        }
        currentPlayer = fenParts[1].equalsIgnoreCase("w") ? PieceColor.WHITE : PieceColor.BLACK;
        currentPlayerLabel.setText((currentPlayer == PieceColor.WHITE ? "White's turn" : "Black's turn"));
        createBoard();
    }


    /**
     * This method shows a dialog displaying a bar chart of the move durations for both white and black players.
     */
    private void showMoveDurations() {
        JDialog moveDurationsDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Move Durations");
        moveDurationsDialog.setSize(600, 400);
        moveDurationsDialog.setLocationRelativeTo(this);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < whiteMoveDurations.size() || i < blackMoveDurations.size(); i++) {
            String whiteDuration = (i < whiteMoveDurations.size()) ? String.valueOf(whiteMoveDurations.get(i)) : "0";
            String blackDuration = (i < blackMoveDurations.size()) ? String.valueOf(blackMoveDurations.get(i)) : "0";
            dataset.addValue(Long.parseLong(whiteDuration), "White", String.valueOf(i + 1));
            dataset.addValue(Long.parseLong(blackDuration), "Black", String.valueOf(i + 1));
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "Move Durations",
                "Move Number",
                "Duration (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.WHITE);
        renderer.setSeriesPaint(1, Color.BLACK);
        renderer.setItemMargin(0.1);
        renderer.setShadowVisible(false);
        ChartPanel chartPanel = new ChartPanel(chart);
        moveDurationsDialog.getContentPane().add(chartPanel);
        moveDurationsDialog.setVisible(true);
    }


    /**

     Promotes a pawn to a new piece when it reaches the opposite end of the board.
     @param color the color of the new piece
     @param piece the pawn to be promoted
     @return the new piece that replaces the pawn
     */
    private Piece promotePawn(PieceColor color, Piece piece) {
        Object[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int choice = JOptionPane.showOptionDialog(
                ChessGui.this,
                "Choose a piece to promote your pawn to:",
                "Pawn Promotion",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );
        return switch (choice) {
            case 1 -> new Rook(color, piece.getRow(), piece.getCol());
            case 2 -> new Bishop(color, piece.getRow(), piece.getCol());
            case 3 -> new Knight(color, piece.getRow(), piece.getCol());
            default -> new Queen(color, piece.getRow(), piece.getCol());
        };
    }


    /**
     * Resets the chess board to its initial state.
     */
    private void resetBoard() {
        // Reset move durations list
        whiteMoveDurations.clear();
        blackMoveDurations.clear();
        // Disable AI mode
        toggleAI.setSelected(false);
        // Clear game moves list and opening text
        gameMoves.clear();
        openingText.setText("Start a Game:");
        // Reset half moves without capture or pawn move counter
        halfMovesWithoutCaptureOrPawnMove = 0;
        // Reset board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                pieces[row][col] = null;
                squarePanels[row][col] = null;
            }
        }
        boardPanel.removeAll();
        initializeBoardPanel();
        boardPanel.repaint();
        boardPanel.revalidate();
        // Reset game timer and player times
        gameTimer.stop();
        whitePlayerTime = initialTimePerPlayer;
        blackPlayerTime = initialTimePerPlayer;
        updateTimerLabel();
        // Reset current player and label
        currentPlayer = PieceColor.WHITE;
        currentPlayerLabel.setText("White's turn");
        // Reset last move
        lastMoveSourceRow = -1;
        lastMoveSourceCol = -1;
        lastMoveDestRow = -1;
        lastMoveDestCol = -1;
    }


    /**

     Initializes the top panel of the chess GUI, which includes the reset board button,

     show move durations button, player timers, current player label, opening text,

     and the game timer.
     */
    private void initializeTopPanel() {
        topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        GridBagConstraints gbc = new GridBagConstraints();

// Create buttons panel and add reset and show move durations buttons
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setPreferredSize(new Dimension(200, 50));
        JButton resetButton = new JButton("Reset Board");
        resetButton.addActionListener(e -> resetBoard());
        buttons.add(resetButton, BorderLayout.NORTH);
        JButton showMoveDurationsButton = new JButton("Show Move Durations");
        showMoveDurationsButton.addActionListener(e -> showMoveDurations());
        buttons.add(showMoveDurationsButton, BorderLayout.SOUTH);

// Add buttons panel to top panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1;
        topPanel.add(buttons, gbc);

// Create player timers panel and add white and black player timers
        JPanel times = new JPanel();
        whitePlayerTimerLabel = new JLabel("00:00");
        whitePlayerTimerLabel.setBorder(BorderFactory.createLineBorder(Color.black));
        whitePlayerTimerLabel.setBackground(Color.WHITE);
        whitePlayerTimerLabel.setOpaque(true);
        times.add(whitePlayerTimerLabel);
        blackPlayerTimerLabel = new JLabel("00:00");
        blackPlayerTimerLabel.setBorder(BorderFactory.createLineBorder(Color.black));
        blackPlayerTimerLabel.setBackground(Color.BLACK);
        blackPlayerTimerLabel.setForeground(Color.WHITE);
        blackPlayerTimerLabel.setOpaque(true);
        times.add(blackPlayerTimerLabel);

// Add player timers panel and current player label to top panel
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0;
        topPanel.add(times, gbc);
        currentPlayerLabel = new JLabel("White's turn");
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0;
        topPanel.add(currentPlayerLabel, gbc);

// Add opening text area to top panel
        openingText = new JTextArea("Start a Game:");
        openingText.setLineWrap(true);
        openingText.setOpaque(false);
        openingText.setFocusable(false);
        openingText.setWrapStyleWord(true);
        openingText.setPreferredSize(buttons.getPreferredSize());
        openingText.setFont(new JLabel().getFont());
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 1;
        topPanel.add(openingText, gbc);

// Create game timer and add it to top panel
        gameTimer = new Timer(1000, e -> {
            if (currentPlayer == PieceColor.WHITE) {
                whitePlayerTime--;
                updateTimerLabel();
                if (whitePlayerTime <= 0) {
                    gameTimer.stop();
                    int option = JOptionPane.showConfirmDialog(ChessGui.this, "Black player wins! White player ran out of time. Restart game?", "Game Over", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        resetBoard();
                    }
                }
            } else {
                blackPlayerTime--;
                updateTimerLabel();
                if (blackPlayerTime <= 0) {
                    gameTimer.stop();
                    int option = JOptionPane.showConfirmDialog(ChessGui.this, "White player wins! Black player ran out of time. Restart game?", "Game Over", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        resetBoard();
                    }
                }
            }
        });
    }


    /**
     * Ends the current player's turn by adding time increment to the player's remaining time and
     * updating the current player to the other color.
     */
    private void endPlayerTurn() {
        // If it's white's turn, add time increment to white player's time, else add it to black player's time
        if (currentPlayer == PieceColor.WHITE) {
            whitePlayerTime += timeIncrement;
        } else {
            blackPlayerTime += timeIncrement;
        }
        // Update the current player to the other color
        currentPlayer = currentPlayer == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        // Update the timer label to reflect the new player's time remaining
        updateTimerLabel();
    }

    /**

     Creates the animation layer for the chess board. The animation layer is a JLayeredPane that
     is used to display animations, such as piece movement animations, on top of the chess board.
     It sets the preferred size and minimum size of the animation layer to the board size and 500,
     respectively.
     */
    private void createAnimationLayer() {
        animationLayer = new JLayeredPane();
        animationLayer.setPreferredSize(new Dimension(boardSize, boardSize));
        animationLayer.setMinimumSize(new Dimension(500, 500));
    }


    /**

     Creates the center panel which will contain the chess board and the animation layer.
     The center panel is a JLayeredPane with a preferred size of boardSize x boardSize and a minimum size of 500 x 500.
     */
    private void createCenterPanel() {
        centerPanel = new JLayeredPane();
        centerPanel.setPreferredSize(new Dimension(boardSize, boardSize));
        centerPanel.setMinimumSize(new Dimension(500, 500));
    }


    /**

     Initializes a list of chess openings with their corresponding moves and adds them to the openings list.
     */
    private void initializeOpenings() {
        List<String> englundGambitMoves = new ArrayList<>();
        englundGambitMoves.add("d2 - d4");
        englundGambitMoves.add("e7 - e5");
        englundGambitMoves.add("d4 - e5");
        Opening englundGambit = new Opening("Englund Gambit", englundGambitMoves);
        openings.add(englundGambit);
        List<String> italianGameMoves = new ArrayList<>();
        italianGameMoves.add("e2 - e4");
        italianGameMoves.add("e7 - e5");
        italianGameMoves.add("g1 - f3");
        italianGameMoves.add("b8 - c6");
        italianGameMoves.add("f1 - c4");
        Opening italianGame = new Opening("Italian Game", italianGameMoves);
        openings.add(italianGame);
        List<String> sicilianDefenseMoves = new ArrayList<>();
        sicilianDefenseMoves.add("e2 - e4");
        sicilianDefenseMoves.add("c7 - c5");
        Opening sicilianDefense = new Opening("Sicilian Defense", sicilianDefenseMoves);
        openings.add(sicilianDefense);
        List<String> frenchDefenseMoves = new ArrayList<>();
        frenchDefenseMoves.add("e2 - e4");
        frenchDefenseMoves.add("e7 - e6");
        Opening frenchDefense = new Opening("French Defense", frenchDefenseMoves);
        openings.add(frenchDefense);
        List<String> caroKannDefenseMoves = new ArrayList<>();
        caroKannDefenseMoves.add("e2 - e4");
        caroKannDefenseMoves.add("c7 - c6");
        Opening caroKannDefense = new Opening("Caro-Kann Defense", caroKannDefenseMoves);
        openings.add(caroKannDefense);
        List<String> slavDefenseMoves = new ArrayList<>();
        slavDefenseMoves.add("d2 - d4");
        slavDefenseMoves.add("d7 - d5");
        slavDefenseMoves.add("c2 - c4");
        slavDefenseMoves.add("c7 - c6");
        Opening slavDefense = new Opening("Slav Defense:", slavDefenseMoves);
        openings.add(slavDefense);
        List<String> grunfeldDefenseMoves = new ArrayList<>();
        grunfeldDefenseMoves.add("d2 - d4");
        grunfeldDefenseMoves.add("g8 - f6");
        grunfeldDefenseMoves.add("c2 - c4");
        grunfeldDefenseMoves.add("g7 - g6");
        grunfeldDefenseMoves.add("g1 - f3");
        grunfeldDefenseMoves.add("d7 - d5");
        Opening grunfeldDefense = new Opening("Grunfeld Defense", grunfeldDefenseMoves);
        openings.add(grunfeldDefense);
    }


    /**

     Determines the current opening of a game given a list of moves.
     @param gameMoves the list of moves played in the game
     @return the name of the opening played, or "Unknown" if it doesn't match any known openings
     */
    private String getCurrentOpening(List<String> gameMoves) {
        for (Opening opening : openings) {
            List<String> openingMoves = opening.getMoves();
            if (gameMoves.size() >= openingMoves.size()) {
                boolean match = true;
                for (int i = 0; i < openingMoves.size(); i++) {
                    if (!gameMoves.get(i).equals(openingMoves.get(i))) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return opening.getName();
                }
            }
        }
        return "Unknown";
    }


    /**
     This method takes in the source and destination coordinates of a move and returns the corresponding string representation in English notation. The notation consists of the source column letter, source row number, a dash, and the destination column letter and row number. The board is assumed to be an 8x8 grid with columns labeled a-h and rows labeled 1-8.
     Here's an example: if the source coordinates are (6, 4) and the destination coordinates are (4, 4), the method would return "e2 - e4".
     @param sourceRow the row index of the source square (0-7)
     @param sourceCol the column index of the source square (0-7)
     @param destRow the row index of the destination square (0-7)
     @param destCol the column index of the destination square (0-7)
     @return a string representing the move in English notation
     */
    private String moveToString(int sourceRow, int sourceCol, int destRow, int destCol) {
        String[] columns = {"a", "b", "c", "d", "e", "f", "g", "h"};
        int[] rows = {8, 7, 6, 5, 4, 3, 2, 1};
        return columns[sourceCol] + rows[sourceRow] + " - " + columns[destCol] + rows[destRow];
    }


    /**

     This method initializes the board panel by creating a grid of square panels, adding appropriate borders and labels, and placing the initial pieces in their starting positions. The board is assumed to be an 8x8 grid with columns labeled a-h and rows labeled 1-8. Each square panel is a JPanel with a background color alternating between white and light gray to create a checkerboard pattern. The pieces are represented by their corresponding JPanel objects, which are added to the square panels at their starting positions. The mouse listener for each square panel is created using the createMouseAdapter() method.
     */
    private void initializeBoardPanel() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                setSquares(row, col);
                if (row == 0 && col == 0) {
                    pieces[row][col] = new Rook(PieceColor.BLACK, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 0 && col == 1) {
                    pieces[row][col] = new Knight(PieceColor.BLACK, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 0 && col == 2) {
                    pieces[row][col] = new Bishop(PieceColor.BLACK, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 0 && col == 3) {
                    pieces[row][col] = new Queen(PieceColor.BLACK, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 0 && col == 4) {
                    pieces[row][col] = new King(PieceColor.BLACK, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 0 && col == 5) {
                    pieces[row][col] = new Bishop(PieceColor.BLACK, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 0 && col == 6) {
                    pieces[row][col] = new Knight(PieceColor.BLACK, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 0 && col == 7) {
                    pieces[row][col] = new Rook(PieceColor.BLACK, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 1 & col < 8) {
                    pieces[row][col] = new Pawn(PieceColor.BLACK, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 7 && col == 0) {
                    pieces[row][col] = new Rook(PieceColor.WHITE, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 7 && col == 1) {
                    pieces[row][col] = new Knight(PieceColor.WHITE, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 7 && col == 2) {
                    pieces[row][col] = new Bishop(PieceColor.WHITE, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 7 && col == 3) {
                    pieces[row][col] = new Queen(PieceColor.WHITE, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 7 && col == 4) {
                    pieces[row][col] = new King(PieceColor.WHITE, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 7 && col == 5) {
                    pieces[row][col] = new Bishop(PieceColor.WHITE, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 7 && col == 6) {
                    pieces[row][col] = new Knight(PieceColor.WHITE, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 7 && col == 7) {
                    pieces[row][col] = new Rook(PieceColor.WHITE, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                if (row == 6 & col < 8) {
                    pieces[row][col] = new Pawn(PieceColor.WHITE, row, col);
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                squarePanels[row][col].addMouseListener(createMouseAdapter(row, col));
                boardPanel.add(squarePanels[row][col]);
            }
        }
    }


    /**

     Sets up the JPanel object for the square at the specified row and column of the chess board.

     @param row the row of the square

     @param col the column of the square
     */
    private void setSquares(int row, int col) {
        squarePanels[row][col] = new JPanel();
        squarePanels[row][col].setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.LIGHT_GRAY);
        squarePanels[row][col].setLayout(new BorderLayout());
        squarePanels[row][col].setOpaque(true);
        squarePanels[row][col].setBorder(BorderFactory.createLineBorder(Color.BLACK));
        if (col == 0) {
            JLabel rankLabel = new JLabel(Integer.toString(8 - row), SwingConstants.CENTER);
            squarePanels[row][col].add(rankLabel, BorderLayout.WEST);
        }
        if (row == 7) {
            JLabel fileLabel = new JLabel(Character.toString((char) (col + 'a')), SwingConstants.CENTER);
            squarePanels[row][col].add(fileLabel, BorderLayout.SOUTH);
        }
    }


    /**
     * This method creates the game board by initializing each square panel, setting its background color, adding labels to indicate the rank and file, adding a mouse listener, and adding the square panel to the board panel.
     * The method checks if a piece is present in a square, and if it is, adds the piece's panel to the corresponding square panel. The pieces are represented by a two-dimensional array called pieces, where each element corresponds to a square on the board.
     * If a piece is not present in a square, the square panel is left blank.
     */
    private void createBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                setSquares(row, col);
                if (pieces[row][col] != null) {
                    squarePanels[row][col].add(pieces[row][col].getPiecePanel(), BorderLayout.CENTER);
                }
                squarePanels[row][col].addMouseListener(createMouseAdapter(row, col));
                boardPanel.add(squarePanels[row][col]);
            }
        }
    }


    /**
     * Creates the menu bar and adds menu items.
     */
    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        JMenu saveas = new JMenu("Save as...");
        menuBar.add(saveas);
        JMenu settings = new JMenu("Settings");
        menuBar.add(settings);

        // Create menu items and add them to the appropriate menus
        JMenuItem save_as_svg = new JMenuItem("Save as SVG");
        saveas.add(save_as_svg);
        JMenuItem save_as_png = new JMenuItem("Save as PNG");
        saveas.add(save_as_png);
        JMenuItem exitgame = new JMenuItem("Close game");
        saveas.add(exitgame);
        JMenuItem openfen = new JMenuItem("Load from FEN");
        settings.add(openfen);
        toggleAI = new JCheckBoxMenuItem("Toggle AI");
        settings.add(toggleAI);
        JMenuItem configTime = new JMenuItem("Time settings");
        settings.add(configTime);

        // Add event listeners to the menu items
        toggleAI.addActionListener(e -> {
            JCheckBoxMenuItem source = (JCheckBoxMenuItem) e.getSource();
            if (source.isSelected()) {
                if (currentPlayer == PieceColor.BLACK) {
                    makeRandomBlackMove();
                }
            }
        });
        configTime.addActionListener(e -> showSettingsDialog());
        save_as_svg.addActionListener(e -> {
            try {
                makeSvg();
            } catch (IOException | TranscoderException ex) {
                throw new RuntimeException(ex);
            }
        });
        save_as_png.addActionListener(e -> {
            try {
                makePng();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        exitgame.addActionListener(e -> System.exit(0));
        openfen.addActionListener(e -> {
            TextFieldDialog textFieldDialog = new TextFieldDialog(frame, "r3k3/p4R1Q/P7/8/8/6K1/8/8 b - - 0 1");
            textFieldDialog.setVisible(true);
            if (!textFieldDialog.getSavedText().isEmpty()) {
                // Clear the board and reset the last move
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        pieces[row][col] = null;
                        squarePanels[row][col] = null;
                    }
                }
                lastMoveSourceRow = -1;
                lastMoveSourceCol = -1;
                lastMoveDestRow = -1;
                lastMoveDestCol = -1;
                // Reset the game timer and move durations
                gameTimer.stop();
                whitePlayerTime = initialTimePerPlayer;
                blackPlayerTime = initialTimePerPlayer;
                updateTimerLabel();
                whiteMoveDurations.clear();
                blackMoveDurations.clear();
                toggleAI.setSelected(false);
                boardPanel.removeAll();
                // Set the board to the FEN string entered by the user
                setBoardFromFEN(textFieldDialog.getSavedText());
                boardPanel.repaint();
                boardPanel.revalidate();
            }
        });
    }


    /**

     Displays a dialog window for the user to configure game settings such as time limit and time increment.
     Upon clicking the "OK" button, the settings are applied and the board is reset.
     */
    private void showSettingsDialog() {
        JDialog settingsDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Settings");
        settingsDialog.setLayout(new GridLayout(0, 2));
        settingsDialog.add(new JLabel("Time for the game (seconds):"));
        JSpinner initialTimeSpinner = new JSpinner(new SpinnerNumberModel(initialTimePerPlayer, 1, Integer.MAX_VALUE, 1));
        settingsDialog.add(initialTimeSpinner);
        settingsDialog.add(new JLabel("Increment (seconds):"));
        JSpinner incrementSpinner = new JSpinner(new SpinnerNumberModel(timeIncrement, 0, Integer.MAX_VALUE, 1));
        settingsDialog.add(incrementSpinner);
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            initialTimePerPlayer = (int) initialTimeSpinner.getValue();
            timeIncrement = (int) incrementSpinner.getValue();
            settingsDialog.dispose();
            resetBoard();
        });
        settingsDialog.add(okButton);
        settingsDialog.pack();
        settingsDialog.setLocationRelativeTo(ChessGui.this);
        settingsDialog.setVisible(true);
    }


    /**

     Displays a message dialog with the current time settings and two options - "Agree" and "Change".
     If the user chooses "Change", the settings dialog is shown. If the user chooses "Agree",
     the board will be loaded with the current time settings.
     */
    private void information() {
// Format the time in minutes and seconds
        String formattedTime = String.format("%02d:%02d", initialTimePerPlayer / 60, initialTimePerPlayer % 60);
// Create the message string
        String message = "Time: " + formattedTime + " seconds\nIncrement: " + timeIncrement + " seconds";
// Create the options for the message dialog
        Object[] options = {"Agree", "Change"};
// Show the message dialog and store the user's choice
        int result = JOptionPane.showOptionDialog(
                frame,
                message,
                "Time Settings",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
// If the user chose "Change", show the settings dialog
        if (result == JOptionPane.NO_OPTION) {
            showSettingsDialog();
        }
// If the user chose "Agree", reset the board with the current time settings
        if (result == JOptionPane.YES_OPTION) {
            resetBoard();
        }
    }


    /**

     Creates a new ChessGui with the specified time and increment for each player.
     @param time the initial time allotted for each player, in seconds.
     @param increment the amount of time added to a player's time after each move, in seconds.
     */
    public ChessGui(int time, int increment) {
        initialTimePerPlayer = time;
        timeIncrement = increment;
        whitePlayerTime = initialTimePerPlayer;
        blackPlayerTime = initialTimePerPlayer;
        boardPanel = new JPanel(new GridLayout(8, 8));
        initializeTopPanel();
        initializeBoardPanel();
        initializeOpenings();
        setLayout(new BorderLayout());
        JPanel denis = new JPanel(new GridBagLayout());
        createAnimationLayer();
        createCenterPanel();
        int verticalInsetSize = 20;
        centerPanel.setBorder(BorderFactory.createMatteBorder(verticalInsetSize, 0, verticalInsetSize, 0, Color.WHITE));
        centerPanel.add(boardPanel, JLayeredPane.DEFAULT_LAYER);
        centerPanel.add(animationLayer, JLayeredPane.PALETTE_LAYER);
        boardPanel.setBounds(0, 0, boardSize, boardSize);
        animationLayer.setBounds(0, 0, boardSize, boardSize);
        denis.add(centerPanel);
        centerPanel.setPreferredSize(new Dimension(boardSize, boardSize));
        centerPanel.setMinimumSize(new Dimension(500, 500));
        add(denis, BorderLayout.CENTER);
        add(getTopPanel(), BorderLayout.NORTH);
        frame = new JFrame();
        createMenu();
        frame.setContentPane(this);
        frame.setVisible(true);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        boardSize = Math.min(getWidth(), getHeight()) - (Math.abs(getHeight() - getWidth())) / 2;
        boardPanel.setPreferredSize(new Dimension(boardSize, boardSize));
        boardPanel.setMinimumSize(new Dimension(500, 500));
        frame.setLocationRelativeTo(null);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateBoardSize();
            }
        });
        information();
    }
    public JPanel getTopPanel() {
        return topPanel;
    }

    public static void main(String[] args) {
        int time = 100; // default time in seconds
        int increment = 0; // default increment in seconds
        // parse command line arguments
        for (int i = 0; i < args.length; i += 2) {
            String option = args[i];
            String value = args[i + 1];
            if ("-h".equals(option)) {
                time = Integer.parseInt(value);
            } else if ("-i".equals(option)) {
                increment = Integer.parseInt(value);
            } else {
                System.out.println("Invalid option: " + option);
                System.exit(1);
            }
        }
        // create and show GUI
        int finalTime = time;
        int finalIncrement = increment;
        SwingUtilities.invokeLater(() -> new ChessGui(finalTime, finalIncrement).setVisible(true));
    }
}
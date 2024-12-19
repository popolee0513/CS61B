package game2048;

import java.util.Formatter;
import java.util.Observable;

/** The state of a game of 2048.
 *  @author Peter
 */
public class Model extends Observable {

    /** Current contents of the board. */
    private Board board;

    /** Current score. */
    private int score;

    /** Maximum score so far. Updated when game ends. */
    private int maxScore;

    /** True if the game is over. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r). Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed. */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true if the game is over (no moves, or
     *  a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    public boolean moveTileUpAsFarAsPossible(Tile tile,Side side){
        if (tile == null){
            return false;
        }
        int col ;
        int row ;
        if (side == Side.WEST){
            row = Side.EAST.row(tile.col(),tile.row(), board.size());
            col = Side.EAST.col(tile.col(),tile.row(), board.size());
        }

        else if (side ==Side.EAST){
            row = Side.WEST.row(tile.col(),tile.row(), board.size());
            col = Side.WEST.col(tile.col(),tile.row(), board.size());
        }
        else if (side == Side.SOUTH){
            row = Side.SOUTH.row(tile.col(),tile.row(), board.size());
            col = Side.SOUTH.col(tile.col(),tile.row(), board.size());
        }
        else {
            col = tile.col();
            row = tile.row();
        }

        int cur_value = tile.value();
        Tile next_tile = null;
        boolean can_move = true;
        boolean changed = false;

        if (row!=board.size()-1){
            while (can_move &  row < board.size()-1){
                if (board.tile(col, row+1) == null){
                    row += 1;
                }
                else {
                    next_tile = board.tile(col, row+1);
                    int next_value = next_tile.value();
                    can_move = false ;
                    if (cur_value != next_value || next_tile.wasMerged()){
                        board.move(col,row,tile);
                    }
                    else{
                        board.move(col,row+1,tile);
                        score += cur_value*2;
                    }
                    changed = true;

                }
            }
            if (next_tile == null & row == board.size()-1){
                board.move(col,row,tile);
                changed = true;
            }

        }
        return changed;
    }
    public boolean tiltColumn(int x,Side side){
        boolean changed = false;
        for (int row = board.size() - 1; row >= 0; row--){

            Tile cur_tile = board.tile(x, row);
            boolean result = moveTileUpAsFarAsPossible(cur_tile,side);
            if (result){
                changed = true;
            }
        }
        return changed;

    }

    /** Tilt the board toward SIDE. Return true if this changes the board.
     *
     *  1. If two Tile objects are adjacent in the direction of motion and have
     *     the same value, they are merged into one Tile of twice the original
     *     value and that new value is added to the score.
     *  2. A tile that is the result of a merge will not merge again on that tilt.
     *     So each move, every tile will only ever be part of at most one merge.
     *  3. When three adjacent tiles in the direction of motion have the same
     *     value, the leading two tiles in the direction of motion merge, and the
     *     trailing tile does not. */
    public boolean tilt(Side side) {
        boolean changed = false;
        if (side == Side.WEST){
           board.setViewingPerspective(Side.WEST);
        }
        if (side ==Side.EAST){
            board.setViewingPerspective(Side.EAST);
        }
        if (side == Side.SOUTH){
            board.setViewingPerspective(Side.SOUTH);
        }


        // TODO: Modify this.board (and perhaps this.score) to account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.
        for (int col = 0; col < board.size(); col++){
            boolean result = tiltColumn(col,side);
            if (result){
                changed = true;
            }
        }
            checkGameOver();
        if (changed) {
            board.setViewingPerspective(Side.NORTH);
            setChanged();
        }
        return changed;
    }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately. */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null. */
    public static boolean emptySpaceExists(Board b) {
        for (int col = 0; col < b.size(); col++) {
            for (int row = 0; row < b.size(); row++) {
                Tile cur = b.tile(col, row);
                if (cur == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns true if any tile is equal to the maximum valid value.
     *  Maximum valid value is given by MAX_PIECE. */
    public static boolean maxTileExists(Board b) {
        for (int col = 0; col < b.size(); col++) {
            for (int row = 0; row < b.size(); row++) {
                Tile cur = b.tile(col, row);
                if (cur != null && cur.value() == MAX_PIECE) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns true if there are any valid moves on the board.
     *  There are two ways that there can be valid moves:
     *  1. There is at least one empty space on the board.
     *  2. There are two adjacent tiles with the same value. */
    public static boolean adjacentCheck(Board board,int col, int row, int val) {
        int curVal = val;

        if (col - 1 >= 0 && board.tile(col - 1, row).value() == curVal) {
            return true;
        }
        if (row - 1 >= 0 && board.tile(col, row - 1).value() == curVal) {
            return true;
        }
        if (col + 1 < board.size() && board.tile(col + 1, row).value() == curVal) {
            return true;
        }
        if (row + 1 < board.size() && board.tile(col, row + 1).value() == curVal) {
            return true;
        }

        return false;
    }

    /** Checks if there is at least one move left on the board. */
    public static boolean atLeastOneMoveExists(Board b) {
        if (emptySpaceExists(b)) {
            return true;
        } else {
            for (int col = 0; col < b.size(); col++) {
                for (int row = 0; row < b.size(); row++) {
                    int cur = b.tile(col, row).value();
                    if (adjacentCheck(b, col, row, cur)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }
    @Override
    /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }
    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}


package com.example.tutorialgame.gamestates.playing.playingstates.tictactoe;

public class TicTacToeAI {
    private final int SIZE = 3;
    private final int[][] board;
    public static final int PLAYER   = 1;
    public static final int COMPUTER = 2;
    private static final Integer INF = Integer.MAX_VALUE;

    public TicTacToeAI(int[][] arr) {
        board = arr;
    }

    public void markSquare(int row, int col, int whoPlayed) {
        if(row != -1)
            board[row][col] = whoPlayed;
    }

    public boolean isAvailableSquare(int row, int col) {
        return board[row][col] == 0;
    }

    public boolean isBoardFull() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (board[r][c] == 0)
                    return false;
        return true;
    }

    public boolean checkWin(int whoPlayed) {
        for (int i = 0; i < SIZE; i++) {
            if (board[i][0] == whoPlayed &&
                    board[i][1] == whoPlayed &&
                    board[i][2] == whoPlayed)
                return true;
            if (board[0][i] == whoPlayed &&
                    board[1][i] == whoPlayed &&
                    board[2][i] == whoPlayed)
                return true;
        }
        // אלכסונים
        if (board[0][0] == whoPlayed &&
                board[1][1] == whoPlayed &&
                board[2][2] == whoPlayed)
            return true;

        return board[0][2] == whoPlayed &&
                board[1][1] == whoPlayed &&
                board[2][0] == whoPlayed;
    }

    private float miniMax(int depth, int who, float alpha, float beta) {
        if (checkWin(COMPUTER)) return INF - depth;
        if (checkWin(PLAYER))   return -INF + depth;
        if (isBoardFull())      return 0;

        if (who == COMPUTER) {
            float maxEval = -INF;
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if (isAvailableSquare(r, c)) {
                        board[r][c] = COMPUTER;
                        float eval = miniMax(depth + 1, PLAYER, alpha, beta);
                        board[r][c] = 0;
                        maxEval = Math.max(maxEval, eval);
                        alpha   = Math.max(alpha, eval);
                        if (beta <= alpha) return maxEval; // חיתוך מוחלט
                    }
                }
            }
            return maxEval;
        } else {
            float minEval = INF;
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if (isAvailableSquare(r, c)) {
                        board[r][c] = PLAYER;
                        float eval = miniMax(depth + 1, COMPUTER, alpha, beta);
                        board[r][c] = 0;
                        minEval = Math.min(minEval, eval);
                        beta    = Math.min(beta, eval);
                        if (beta <= alpha) return minEval; // חיתוך מוחלט
                    }
                }
            }
            return minEval;
        }
    }

    public int[] findBestMove() {
        float bestVal = -INF;
        int[] bestMove = {-1, -1};
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (isAvailableSquare(r, c)) {
                    board[r][c] = COMPUTER;
                    float moveVal = miniMax(0, PLAYER, -INF, INF);
                    board[r][c] = 0;
                    if (moveVal > bestVal) {
                        bestVal  = moveVal;
                        bestMove[0] = r;
                        bestMove[1] = c;
                    }
                }
            }
        }
        return bestMove;
    }

    public void resetBoard() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                board[r][c] = 0;
    }
}

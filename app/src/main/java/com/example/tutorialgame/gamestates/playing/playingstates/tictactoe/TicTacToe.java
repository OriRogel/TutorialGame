package com.example.tutorialgame.gamestates.playing.playingstates.tictactoe;

import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.engine.ui.customviews.buttons.ButtonImages;
import com.example.tutorialgame.engine.ui.customviews.buttons.rects.RectButton;
import com.example.tutorialgame.gamestates.GameState;
import com.example.tutorialgame.gamestates.playing.PlayingManager;

public class TicTacToe extends GameState {
    private final PlayingManager playingManager;
    private final TicTacToeAI ticTacToeAI;

    private final int[][] board = new int[3][3];
    private final RectF[] grid = new RectF[9];
    private boolean aiNeedsToPlay = false;
    private int whoPlayed = TicTacToeAI.PLAYER; // 1=PLAYER, 2=COMPUTER
    private final float startX;
    private final float cellSize;

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintX   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintO   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean playerWin, compWin, draw;
    private final RectButton resetButton = new RectButton(50, 700, ButtonImages.MENU_REPLAY.getWidth(), ButtonImages.MENU_REPLAY.getHeight(), false);

    public TicTacToe(Game game, PlayingManager playingManager) {
        super(game);
        this.playingManager = playingManager;
        this.ticTacToeAI   = new TicTacToeAI(board);

        // חישוב מיקום גאדג'ט הריבועים
        startX  = (SCREEN_WIDTH - SCREEN_HEIGHT) / 2f;
        cellSize = SCREEN_HEIGHT / 3f;

        // יצירת המלבנים בלולאה
        int idx = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                float left = startX + col * cellSize;
                float top = row  * cellSize;
                float right = left + cellSize;
                float bottom = top  + cellSize;
                grid[idx++] = new RectF(left, top, right, bottom);
            }
        }

        // צבעי ציור
        linePaint.setColor(Color.RED);
        linePaint.setStrokeWidth(GameConstants.Sprite.SCALE_MULTIPLIER * 2);
        linePaint.setStyle(Paint.Style.STROKE);

        paintX.setColor(Color.BLUE);
        paintX.setTextSize(GameConstants.Sprite.TILE_SIZE * 5);

        paintO.setColor(Color.YELLOW);
        paintO.setTextSize(GameConstants.Sprite.TILE_SIZE * 5);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(GameConstants.Sprite.TILE_SIZE);
    }

    @Override
    public void update(double delta) {
        // רק ברגע שנכנס תור ה־AI נדליק את הדגל
        if (whoPlayed == TicTacToeAI.COMPUTER && !aiNeedsToPlay) {
            aiNeedsToPlay = true;
        }

        // מפעילים AI פעם אחת בלבד
        if (aiNeedsToPlay) {
            int[] move = ticTacToeAI.findBestMove();
            ticTacToeAI.markSquare(move[0], move[1], TicTacToeAI.COMPUTER);
            whoPlayed = TicTacToeAI.PLAYER;
            aiNeedsToPlay = false;
        }

        if(ticTacToeAI.checkWin(TicTacToeAI.PLAYER))
            playerWin = true;
        else if (ticTacToeAI.checkWin(TicTacToeAI.COMPUTER))
            compWin = true;
        else if (ticTacToeAI.isBoardFull())
            draw = true;


    }

    @Override
    public void render(Canvas c) {
        // ציור הרשת
        for (RectF cell : grid) {
            c.drawRect(cell, linePaint);
        }
        // ציור הסימנים
        for (int r = 0; r < 3; r++) {
            for (int col = 0; col < 3; col++) {
                int v = board[r][col];

                float x = grid[r * 3 + col].left + cellSize * 0.1f;
                float y = grid[r * 3 + col].bottom;

                if (v == TicTacToeAI.PLAYER) {
                    c.drawText("X", x, y, paintX);
                }
                else if (v == TicTacToeAI.COMPUTER) {
                    c.drawText("O", x, y, paintO);
                }
            }
        }


        if (playerWin)
            c.drawText("You win! wtf?!?!?>!?", 50, 500, textPaint);
        else if (compWin)
            c.drawText("Did you really expect fot a different result?", 50, 500, textPaint);
        else if (draw)
            c.drawText("Php... whatever", 50, 500, textPaint);



    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) return;

        float x = event.getX(), y = event.getY();
        // איתור אינדקס התא
        if(!playerWin && !compWin && !draw) {
            for (int i = 0; i < grid.length; i++) {
                if (grid[i].contains(x, y)) {
                    int row = i / 3, col = i % 3;
                    if (ticTacToeAI.isAvailableSquare(row, col)
                            && whoPlayed == TicTacToeAI.PLAYER) {
                        ticTacToeAI.markSquare(row, col, TicTacToeAI.PLAYER);
                        whoPlayed = TicTacToeAI.COMPUTER;
                        aiNeedsToPlay = false;  // יאותת ל־update להפעיל AI
                    }
                    break;
                }
            }
        }

//        if(isIn(event, resetButton)) {
            whoPlayed = TicTacToeAI.PLAYER;
            aiNeedsToPlay = false;
            ticTacToeAI.resetBoard();
            playerWin = false;
            draw = false;
            compWin = false;
//        }
    }
}

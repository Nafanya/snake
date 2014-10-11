package com.nikitayaschenko.android.snake;

/**
 * Created by Nikita Yaschenko on 07.10.14.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;


class SnakeView extends SurfaceView implements Runnable {
    private static final int FIELD_WIDTH = 30;
    private static final int FIELD_HEIGHT = 45;

    private Context context;

    private int screenWidth = 0;
    private int screenHeight = 0;
    private float cellWidth = 0;
    private float cellHeight = 0;

    private SnakeGame snake;
    private boolean lose;

    private Paint textPaint;
    private Paint snakePaint;
    private Paint eyePaint;
    private Paint borderPaint;
    private Paint foodPaint;
    private Paint filterPaint;

    private Bitmap appleBitmap;
    private Bitmap grassBitmap;
    private BitmapShader backgroundShader;
    private Paint fillPaint;

    private long lastGameUpdate;
    private final static long GAME_UPDATE_INTERVAL = 200 * 1000 * 1000;

    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;

    public SnakeView(Context context) {
        super(context);
        this.context = context;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        setupDimensions(size.x, size.y);

        setupResources();

        holder = getHolder();
        snake = new SnakeGame(FIELD_WIDTH, FIELD_HEIGHT);
        lose = false;

        this.setOnTouchListener(new OnSwipeTouchListener(getContext()) {

            public void onSwipeTop() {
                snake.setDirection(SnakeGame.Direction.UP);
            }

            public void onSwipeRight() {
                snake.setDirection(SnakeGame.Direction.RIGHT);
            }

            public void onSwipeLeft() {
                snake.setDirection(SnakeGame.Direction.LEFT);
            }

            public void onSwipeBottom() {
                snake.setDirection(SnakeGame.Direction.DOWN);
            }
        });

    }

    public void resume() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException ignore) {}
    }

    public void run() {
        while (running) {
            recalcGame();
            if (holder.getSurface().isValid()) {
                Canvas canvas = holder.lockCanvas();
                draw(canvas);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void setupResources() {
        fillPaint = new Paint();

        filterPaint = new Paint();
        filterPaint.setFilterBitmap(true);

        textPaint = new Paint();
        textPaint.setColor(Color.YELLOW);
        textPaint.setTextSize(Math.min(screenHeight, screenWidth) / 10);

        snakePaint = new Paint();
        snakePaint.setColor(Color.YELLOW);
        snakePaint.setStyle(Paint.Style.FILL);

        eyePaint = new Paint();
        eyePaint.setColor(Color.BLUE);
        eyePaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);

        foodPaint = new Paint();
        foodPaint.setColor(Color.RED);

        Bitmap apple = BitmapFactory.decodeResource(context.getResources(), R.drawable.red_apple);
        appleBitmap = Bitmap.createScaledBitmap(apple, (int)cellWidth, (int)cellHeight, false);
        apple.recycle();
        apple = null;

        Bitmap fillBMP = BitmapFactory.decodeResource(context.getResources(), R.drawable.grass);
        backgroundShader = new BitmapShader(fillBMP, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setShader(backgroundShader);
    }

    private void setupDimensions(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        cellWidth = (float)screenWidth / FIELD_WIDTH;
        cellHeight = (float)screenHeight / FIELD_HEIGHT;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        setupDimensions(w, h);
    }

    private void recalcGame() {
        if (lose) return;
        long now = System.nanoTime();
        long elapsed = now - lastGameUpdate;
        if (elapsed > GAME_UPDATE_INTERVAL) {
            SnakeGame.Cell cell = snake.tick();
            lose = cell == SnakeGame.Cell.SNAKE;
            lastGameUpdate = now;
        }
    }

    private void drawSnake(Canvas canvas, int i, int j) {
        float left = i * cellWidth;
        float top = j * cellHeight;
        float w = cellWidth / 4;
        float h = cellHeight / 4;
        canvas.drawRoundRect(
                new RectF(left, top, left + cellWidth, top + cellHeight),
                w, h, snakePaint);
        canvas.drawRoundRect(
                new RectF(left, top, left + cellWidth, top + cellHeight),
                w, h, borderPaint);
    }

    private void drawFood(Canvas canvas, int i, int j) {
        float left = i * cellWidth;
        float top = j * cellHeight;
        canvas.drawBitmap(appleBitmap, left, top, null);
    }

    private void drawHead(Canvas canvas, int i, int j, SnakeGame.Direction d) {
        float left = i * cellWidth;
        float top = j * cellHeight;
        float right = (i + 1) * cellWidth;
        float bottom = (j + 1) * cellHeight;

        float eyeX1 = left + cellWidth * 0.25f;
        float eyeX2 = left + cellWidth * 0.75f;
        float eyeY1 = top + cellHeight * 0.25f;
        float eyeY2 = top + cellHeight * 0.75f;
        float eyeR = cellWidth / 6;
        float tongueLenX = cellWidth * 0.5f;
        float tongueLenY = cellHeight * 0.5f;
        switch (d) {
            case DOWN:
                canvas.drawCircle(eyeX1, eyeY2, eyeR, eyePaint);
                canvas.drawCircle(eyeX2, eyeY2, eyeR, eyePaint);
                canvas.drawRect(eyeX1, bottom, eyeX2, bottom + tongueLenY, foodPaint);
                break;
            case UP:
                canvas.drawCircle(eyeX1, eyeY1, eyeR, eyePaint);
                canvas.drawCircle(eyeX2, eyeY1, eyeR, eyePaint);
                canvas.drawRect(eyeX1, top - tongueLenY, eyeX2, top, foodPaint);
                break;
            case LEFT:
                canvas.drawCircle(eyeX1, eyeY1, eyeR, eyePaint);
                canvas.drawCircle(eyeX1, eyeY2, eyeR, eyePaint);
                canvas.drawRect(left - tongueLenX, eyeY1, left, eyeY2, foodPaint);
                break;
            case RIGHT:
                canvas.drawCircle(eyeX2, eyeY1, eyeR, eyePaint);
                canvas.drawCircle(eyeX2, eyeY2, eyeR, eyePaint);
                canvas.drawRect(right, eyeY1, right + tongueLenX, eyeY2, foodPaint);
                break;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.drawRect(0, 0, getWidth(), getHeight(), fillPaint);

        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_HEIGHT; j++) {
                SnakeGame.Cell c = snake.field[i][j];
                switch (c) {
                    case SNAKE:
                        drawSnake(canvas, i, j);
                        if (snake.isHead(i, j)) {
                            drawHead(canvas, i, j, snake.getDirection());
                        }
                        //TODO: drawTail
                        break;
                    case FOOD:
                        drawFood(canvas, i, j);
                        break;
                    case EMPTY:
                        break;
                }
            }
        }

        canvas.restore();
    }
}
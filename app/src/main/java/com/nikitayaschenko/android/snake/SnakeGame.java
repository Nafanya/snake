package com.nikitayaschenko.android.snake;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by Nikita Yaschenko on 07.10.14.
 */
public class SnakeGame {
    private static final int FOOD_AMOUNT = 10;
    private static final int SCORE_PER_FOOD = 10;

    private int width;
    private int height;
    private boolean lose;

    private LinkedList<Coord> snake;
    private LinkedList<Coord> food;
    private Direction direction;
    private Random random;

    public Cell[][] field;
    private int score;
    private boolean directionChanged;

    public SnakeGame(int width, int height) {
        this.width = width;
        this.height = height;
        lose = false;
        score = 0;

        setupField();
    }

    public int getScore() {
        return score;
    }

    public Cell tick() {
        if (lose) {
            return Cell.SNAKE;
        }
        directionChanged = false;
        Coord head = snake.get(0);
        Coord next = next(head, direction);
        Cell nextCell = checkCell(next);
        boolean ateFood = false;
        switch (nextCell) {
            case SNAKE:
                lose = true;
                break;
            case FOOD:
                ateFood = true;
                score += SCORE_PER_FOOD;
                if (food.size() < 5) {
                    addFood(random.nextInt(3));
                }
                for (Coord f : food) {
                    if (f.x == next.x && f.y == next.y) {
                        food.remove(f);
                        break;
                    }
                }
                break;
            case EMPTY:
                break;
        }
        snake.addFirst(next);
        if (!ateFood) {
            snake.removeLast();
        }
        if (food.isEmpty()) {
            addFood(random.nextInt(6));
        }
        updateField();
        return Cell.EMPTY;
    }

    private void updateField() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                field[i][j] = Cell.EMPTY;
            }
        }
        for (Coord c : food) {
            field[c.x][c.y] = Cell.FOOD;
        }
        for (Coord c : snake) {
            field[c.x][c.y] = Cell.SNAKE;
        }
    }

    private void setupField() {
        field = new Cell[width][height];
        random = new Random();
        snake = new LinkedList<Coord>();
        food = new LinkedList<Coord>();
        field = new Cell[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                field[i][j] = Cell.EMPTY;
            }
        }

        int sx = width / 2;
        int sy = height / 2;
        for (int i = 0; i < 3; i++) {
            snake.add(new Coord(sx, sy + i));
        }
        direction = Direction.LEFT;

        addFood(FOOD_AMOUNT);
    }

    private void addFood(int amount) {
        for (int i = 0; i < amount; i++) {
            boolean found = false;
            int x = 0, y = 0;
            while (!found) {
                x = random.nextInt(width);
                y = random.nextInt(height);
                found = true;
                for (Coord p : snake) {
                    if (p.x == x && p.y == y) {
                        found = false;
                    }
                }
                for (Coord p : food) {
                    if (p.x == x && p.y == y) {
                        found = false;
                    }
                }
            }
            food.add(new Coord(x, y));
        }
    }

    private Cell checkCell(Coord coord) {
        for (Coord c : food) {
            if (c.x == coord.x && c.y == coord.y) {
                return Cell.FOOD;
            }
        }
        for (Coord c : snake) {
            if (c.x == coord.x && c.y == coord.y) {
                return Cell.SNAKE;
            }
        }
        return Cell.EMPTY;
    }

    private Coord next(Coord c, Direction direction) {
        int x = c.x;
        int y = c.y;
        switch (direction) {
            case UP:
                y--;
                break;
            case DOWN:
                y++;
                break;
            case LEFT:
                x--;
                break;
            case RIGHT:
                x++;
                break;
        }
        if (x < 0) x += width;
        if (x == width) x = 0;
        if (y < 0) y += height;
        if (y == height) y = 0;
        return new Coord(x, y);
    }

    public void setDirection(Direction d) {
        if (directionChanged) {
            return;
        }
        directionChanged = true;
        switch (direction) {
            case UP: if (d == Direction.DOWN) return; break;
            case DOWN: if (d == Direction.UP) return; break;
            case LEFT: if (d == Direction.RIGHT) return; break;
            case RIGHT: if (d == Direction.LEFT) return; break;
        }
        direction = d;
    }

    public boolean isHead(int x, int y) {
        Coord c = snake.getFirst();
        return c.x == x && c.y == y;
    }

    public Direction getDirection() {
        return direction;
    }

    public enum Cell {
        EMPTY, SNAKE, FOOD,
    }

    public enum Direction {
        UP, RIGHT, DOWN, LEFT,
    }

    private class Coord {
        public final int x, y;

        Coord(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }


}

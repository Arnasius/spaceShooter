package com.spaceinvader.spaceinvaders;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class SpaceInvaders extends Application {

    static final Image PLAYER_IMG = new Image("C:\\Users\\Arnas\\IdeaProjects\\spaceInvaders\\src\\main\\java\\gameImages\\player.png");
    static final Image ENEMY_IMG =
            new Image("C:\\Users\\Arnas\\IdeaProjects\\spaceInvaders\\src\\main\\java\\gameImages\\enemy.png");
    private static final Random RAND = new Random();
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_SIZE = 60;
    boolean gameOver = false;        Ship player;final int MAX_ENEMIES = 8,  MAX_SHOTS = MAX_ENEMIES * 2;
    List<Shoot> shoots;
    List<Background> bg;
    List<Enemy> enemies;
    private GraphicsContext graphics;
    private double mouseX;
    private int points;

    public static void main(String[] args) {
        launch();
    }

    public void start(Stage level) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        graphics = canvas.getGraphicsContext2D();
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> run(graphics)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        canvas.setCursor(Cursor.MOVE);
        canvas.setOnMouseMoved(e -> mouseX = e.getX());
        canvas.setOnMouseClicked(e -> {
            if(shoots.size() < MAX_SHOTS) shoots.add(player.shoot());
            if(gameOver) {
                gameOver = false;
                createLevel();
            }
        });
        createLevel();
        level.setScene(new Scene(new StackPane(canvas)));
        level.setTitle("Space Invaders");
        level.show();
    }

    private void createLevel() {
        bg = new ArrayList<>();
        shoots = new ArrayList<>();
        enemies = new ArrayList<>();
        player = new Ship(WIDTH / 2, HEIGHT - PLAYER_SIZE, PLAYER_SIZE, PLAYER_IMG);
        points = 0;
        IntStream.range(0, MAX_ENEMIES).mapToObj(i -> this.newEnemy()).forEach(enemies::add);
    }

    private void run(GraphicsContext graphics) {
        graphics.setFill(Color.grayRgb(20));
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setFont(Font.font(20));
        graphics.setFill(Color.WHITE);
        graphics.fillText("Points: " + points, 50,  20);

        if(gameOver) {
            graphics.setFont(Font.font(35));
            graphics.setFill(Color.YELLOW);
            graphics.fillText("Game Over \n Your total score is: " + points + " \n Click to play again", WIDTH / 2, HEIGHT / 2);
        }
        bg.forEach(Background::draw);

        player.update();
        player.draw();
        player.posX = (int) mouseX;

        enemies.stream().peek(Ship::update).peek(Ship::draw).forEach(e -> {
            if(player.collide(e) && !player.dead) {
                player.explode();
            }
        });

        for (int i = shoots.size() - 1; i >=0 ; i--) {
            Shoot shoot = shoots.get(i);
            if(shoot.posY < 0 || shoot.gone)  {
                shoots.remove(i);
                continue;
            }
            shoot.update();
            shoot.draw();
            for (Enemy enemy : enemies) {
                if(shoot.collide(enemy) && !enemy.dead) {
                    points++;
                    enemy.explode();
                    shoot.gone = true;
                }
            }
        }

        for (int i = enemies.size() - 1; i >= 0; i--){
            if(enemies.get(i).destroyed)  {
                enemies.set(i, newEnemy());
            }
        }

        gameOver = player.destroyed;
        if(RAND.nextInt(10) > 2) {
            bg.add(new Background());
        }
        for (int i = 0; i < bg.size(); i++) {
            if(bg.get(i).posY > HEIGHT)
                bg.remove(i);
        }
    }

    Enemy newEnemy() {
        return new Enemy(50 + RAND.nextInt(WIDTH - 100), 0, PLAYER_SIZE, ENEMY_IMG);
    }

    int distance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
    }

    public class Ship {

        int posX, posY, size;
        boolean dead, destroyed;
        Image img;

        public Ship(int posX, int posY, int size, Image image) {
            this.posX = posX;
            this.posY = posY;
            this.size = size;
            img = image;
        }

        public Shoot shoot() {
            return new Shoot(posX + size / 2 - Shoot.size / 2, posY - Shoot.size);
        }

        public void update() {
            if(dead) destroyed = true;

        }

        public void draw() {
                graphics.drawImage(img, posX, posY, size, size);
        }

        public boolean collide(Ship other) {
            int d = distance(this.posX + size / 2, this.posY + size /2,
                    other.posX + other.size / 2, other.posY + other.size / 2);
            return d < other.size / 2 + this.size / 2 ;
        }

        public void explode() {
            dead = true;
        }

    }

    public class Enemy extends Ship {

        int SPEED = (points /5)+2;

        public Enemy(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
        }

        public void update() {
            super.update();
            if(!dead && !destroyed) posY += SPEED;
            if(posY > HEIGHT) destroyed = true;
        }
    }

    public class Shoot {

        static final int size = 6;
        public boolean gone;
        int posX, posY, speed = 10;

        public Shoot(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
        }

        public void update() {
            posY-=speed;
        }


        public void draw() {
            graphics.setFill(Color.RED);
            graphics.fillOval(posX, posY, size, size);
        }

        public boolean collide(Ship Ship) {
            int distance = distance(this.posX + size / 2, this.posY + size / 2,
                    Ship.posX + Ship.size / 2, Ship.posY + Ship.size / 2);
            return distance  < Ship.size / 2 + size / 2;
        }


    }

    public class Background {
        int posX, posY;
        private int propB, propA, propC, propD, propE;
        private double lucidity;

        public Background() {
            posX = RAND.nextInt(WIDTH);
            posY = 0;
            propA = RAND.nextInt(5) + 1;
            propB =  RAND.nextInt(5) + 1;
            propC = RAND.nextInt(100) + 150;
            propD = RAND.nextInt(100) + 150;
            propE = RAND.nextInt(100) + 150;
            lucidity = RAND.nextFloat();
            if(lucidity < 0) lucidity *=-1;
            if(lucidity > 0.5) lucidity = 0.5;
        }

        public void draw() {
            if(lucidity > 0.8) lucidity -=0.01;
            if(lucidity < 0.1) lucidity +=0.01;
            graphics.setFill(Color.rgb(propC, propD, propE, lucidity));
            graphics.fillOval(posX, posY, propA, propB);
            posY+=20;
        }
    }





}
package com.example.game_demo_2;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class Invaders extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final Random RAND = new Random();
    private static final int PLAYER_SIZE = 60;
    private static final int EXPLOSION_STEPS = 15;


    static final Image PLAYER_IMG = new Image("file:C:/Users/vkobi/OneDrive/Desktop/Image_opi/player.png");
    static final Image INVADER_IMG = new Image("file:C:/Users/vkobi/OneDrive/Desktop/Image_opi/invader.png");


    final int MAX_INVADERS = 10, MAX_SHOTS = MAX_INVADERS * 2;
    boolean gameOver = false;
    private GraphicsContext gc;


    Rocket player;
    List<Shot> shots;
    //List<Space> space;
    List<Invader> invaders;

    private double mouseX;


    public void start(Stage stage) throws Exception {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> run(gc)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        canvas.setCursor(Cursor.MOVE);
        canvas.setOnMouseMoved(e -> mouseX = e.getX());
        canvas.setOnMouseClicked(e -> {
            if (shots.size() < MAX_SHOTS) {
                shots.add(player.shoot());
            }
            if (gameOver) {
                gameOver = false;
                setup();
            }
        });
        setup();
        stage.setScene(new Scene(new StackPane(canvas)));
        stage.setTitle("Space Invaders");
        stage.show();
    }

    private void setup() {
        shots = new ArrayList<>();
        invaders = new ArrayList<>();
        player = new Rocket(WIDTH / 2, HEIGHT - PLAYER_SIZE, PLAYER_SIZE, PLAYER_IMG);
        IntStream.range(0, MAX_INVADERS).mapToObj(i -> this.newInvader()).forEach(invaders::add);
    }

    private void run(GraphicsContext gc) {
        gc.setFill(Color.grayRgb(20));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);

        if (gameOver) {
            gc.setFont(Font.font(35));
            gc.setFill(Color.YELLOW);
            gc.fillText("Game Over\n Click to play again", WIDTH / 2, HEIGHT / 2.5);
        }

        player.Update();
        player.draw();
        player.posX = (int) mouseX;

        invaders.stream().peek(Rocket::Update).peek(Rocket::draw).forEach(e -> {
            if (player.colide(e) && !player.exploding) {
                player.explode();
            }
        });

        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            if (shot.posY < 0 || shot.toRemove) {
                shots.remove(i);
                continue;
            }
            shot.Update();
            shot.draw();

            for (Invader invader : invaders) {
                if (shot.colide(invader) && !invader.exploding) {
                    invader.explode();
                    shot.toRemove = true;
                }
            }
        }

        for (int i = invaders.size() - 1; i >= 0; i--) {
            if (invaders.get(i).destroyed) {
                invaders.set(i, newInvader());
            }
        }

        gameOver = player.destroyed;
    }


    public class Rocket {
        int posX, posY, size;
        boolean exploding, destroyed;
        Image img;
        int explosionStep = 0;

        public Rocket(int posX, int posY, int size, Image image) {
            this.posX = posX;
            this.posY = posY;
            this.size = size;
            img = image;
        }

        public Shot shoot() {
            return new Shot(posX + size / 2 - Shot.size / 2, posY - Shot.size);
        }

        public void Update() {
            if (exploding) {
                explosionStep++;
            }
            destroyed = explosionStep > 1; //EXPLOSION_STEPS;
        }

        public void draw() {
            gc.drawImage(img, posX, posY, size, size);
        }

        public boolean colide(Rocket other) {
            int d = distance(this.posX + size / 2, this.posY + size / 2,
                    other.posX + other.size / 2, other.posY + other.size / 2);
            return d < other.size / 2 + this.size / 2;
        }

        public void explode() {
            exploding = true;
            explosionStep = -1;
        }
    }


    public class Invader extends Rocket {

        int SPEED = 10;

        public Invader(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
        }

        public void Update() {
            super.Update();
            if (!exploding && !destroyed) {
                posY += SPEED;
            }
            if (posY > HEIGHT) {
                destroyed = true;
            }
        }
    }


    public class Shot {
        public boolean toRemove;
        int posX, posY, speed = 15;
        static final int size = 6;

        public Shot(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
        }

        public void Update() {
            posY -= speed;
        }

        public void draw() {
            gc.setFill(Color.RED);
            gc.fillOval(posX, posY, size, size);
        }

        public boolean colide(Rocket Rocket) {
            int distance = distance(this.posX + size / 2, this.posY + size / 2,
                    Rocket.posX + Rocket.size / 2, Rocket.posY + Rocket.size / 2);
            return distance < Rocket.size / 2 + size / 2;
        }
    }


//    public class Space{
//        int posX, posY
//    }

    Invader newInvader() {
        return new Invader(50 + RAND.nextInt(WIDTH - 100), 0, PLAYER_SIZE, INVADER_IMG);
    }

    int distance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
    }

    public static void main(String[] args) {
        launch();
    }
}
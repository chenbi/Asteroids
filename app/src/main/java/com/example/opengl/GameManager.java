package com.example.opengl;

/**
 * Created by chen.
 */
public class GameManager
{
    int mapWidth=800;
    int mapHeight=600;
    private boolean playing = false;

    SpaceShip ship;   //  first game object
    Border border;
    Star[] stars;
    int numStars = 200;
    Bullet[] bullets;
    int numBullets = 20;
    Asteroid[] asteroids;
    int numAsteroids;
    int numAsteroidsRemaining;
    int baseNumAsteroids =10;
    int levelNumber = 1;
    Scores[] scores;
    int numLives = 3;
    Life[] lives;

    int screenWidth,screenHeight;

    // how many metres of the virtual world
    int metresToShowX = 390;
    int metresToShowY = 220;

    public GameManager(int x,int y)
    {
        screenHeight = y;
        screenWidth = x;

        asteroids = new Asteroid[500];

        lives= new Life[50];
        scores= new Scores[500];
    }

    public void switchPlayingStatus()
    {
        playing = !playing;
    }

    public boolean isPlaying()
    {
        return playing;
    }


}

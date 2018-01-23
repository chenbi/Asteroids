package com.example.opengl;

/**
 * Created by chen.
 */

import android.graphics.PointF;
import android.graphics.Rect;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.orthoM;

public class GameBoard implements Renderer
{
    boolean debugging = true;   // for debug use

    long frameCounter = 0;  // for monitoringand controlling the frames per second
    long averageFPS = 0;
    private long fps;

    private final float[] viewportMatrix = new float[16];   // for convert into opengl coordinate(-1.0~1.0)

    private GameManager gm;   // help manage current game states
    private SoundManager sm;
    private InputController ic;

    private final GameButton[] gameButtons =new GameButton[5];

    PointF handyPointF;   // capture various PointF details without creating new objects
    PointF handyPointF2;

    public GameBoard (GameManager gameManager, SoundManager soundManager, InputController inputController)
    {
        gm = gameManager;
        sm = soundManager;
        ic = inputController;

        handyPointF = new PointF();
        handyPointF2 = new PointF();

        // debug
        Log.e("init GameBoard:","screenWidth="+gm.screenWidth+",screenHeight="+gm.screenHeight);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {
        glClearColor(0.f,0.f,0.f,0.f);

        GLManager.buildProgram();

        createObjects();
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused,int width,int height)
    {
        glViewport(0, 0, width, height);   //  make full screen

        orthoM(viewportMatrix, 0, 0, gm.metresToShowX, 0, gm.metresToShowY, 0f, 1f);
    }

    private void createObjects()
    {
        gm.player= new Player (gm.mapWidth/2,gm.mapHeight/2);    // pass in the worldLocation of player
        gm.border = new Border(gm.screenWidth,gm.screenHeight);      // pass in as the map size
        gm.stars = new Star[gm.numStars];
        gm.ufo = new UFO (gm.mapWidth,gm.mapHeight);
        gm.ufo.setActive (false);

        for(int i=0;i<gm.numStars;i++)
        {
            gm.stars[i] = new Star(gm.mapWidth,gm.mapHeight);
        }
        gm.bullets = new Bullet[gm.numBullets];
        for(int i=0;i<gm.numBullets;i++)
        {
            gm.bullets[i] = new Bullet(gm.player.getWorldLocation().x,gm.player.getWorldLocation().y);

        }

        gm.numAsteroids = gm.baseNumAsteroids * gm.levelNumber;  // determine the number of asteroids
        gm.numAsteroidsRemaining = gm.numAsteroids;   // set how many asteroids need to be destroyed by player
        for(int i=0;i<gm.numAsteroids * gm.levelNumber; i++)
        {
            gm.asteroids[i] = new Asteroid(gm.levelNumber,gm.mapWidth,gm.mapHeight);
        }

        // HUD objects
        for(int i=0;i<gm.numLives;i++)
        {
            gm.lives[i] = new Life (gm,i);
        }
        for(int i=0;i<gm.numAsteroidsRemaining;i++)
        {
            gm.scores[i] = new Scores (gm,i);
        }
        ArrayList<Rect> buttonsToDraw = ic.getButtons();
        int i=0;
        for(Rect rect:buttonsToDraw)
        {
            gameButtons[i] = new GameButton(rect.top,rect.left,rect.bottom,rect.right,gm);
            i++;
        }
    }

    @Override
    public void onDrawFrame(GL10 glUnused)
    {
        long startFrameTime = System.currentTimeMillis();

        if(gm.isPlaying())
            update(fps);

        draw();

        long timeThisFrame = System.currentTimeMillis() - startFrameTime;
        if(timeThisFrame >= 1)
            fps = 1000/timeThisFrame;
        if(debugging)
        {
            frameCounter++;
            averageFPS += fps;
            if(frameCounter > 100)
            {
                averageFPS /= frameCounter;
                frameCounter = 0;
                Log.e("averageFPS:",""+averageFPS+",player.x="+gm.player.getWorldLocation().x+",player.y="+gm.player.getWorldLocation().y);
            }
        }
    }

    private void update(long fps)
    {
        for(int i=0;i<gm.numStars;i++)
        {
            gm.stars[i].update();
        }
        gm.player.update(fps);

        for(int i=0;i<gm.numBullets;i++)
        {
            gm.bullets[i].update(fps,gm.player.getWorldLocation());
        }
        //update the asteroids
        for(int i=0;i<gm.numAsteroids;i++)
        {
            if(gm.asteroids[i].isActive)
                gm.asteroids[i].update(fps);
        }

        // now all the objects are in their new location, so we start collision detection
        // check if the player needs containing in the map
        if(CollisionCheck.contain(gm.mapWidth,gm.mapHeight,gm.player.cp))
        {
            lifeLost();
        }
        // check if the asteroids need containing in the map
        for(int i=0;i<gm.numAsteroids;i++)
        {
            if(gm.asteroids[i].isActive)
            {
                if(CollisionCheck.contain(gm.mapWidth,gm.mapHeight,gm.asteroids[i].boundaries))
                {
                    gm.asteroids[i].bounce();
                    sm.playSound("blip");
                }
            }
        }
        // check if the bullet needs containing in the map
        // first to check if the bullet is out of sight.(if it is, it should be reset for the balance of the game)
        for(int i=0;i<gm.numBullets;i++)
        {
            if(gm.bullets[i].isInFlight())
            {
                handyPointF = gm.bullets[i].getWorldLocation();
                handyPointF2 = gm.player.getWorldLocation();
                // if the bullet out of sight
                if(handyPointF.x > handyPointF2.x + gm.metresToShowX/2)
                {
                    gm.bullets[i].resetBullet(gm.player.getWorldLocation());
                }
                else if(handyPointF.y > handyPointF2.y + gm.metresToShowY/2)
                {
                    gm.bullets[i].resetBullet(gm.player.getWorldLocation());
                }
                else if(handyPointF.x < handyPointF2.x - gm.metresToShowX/2)
                {
                    gm.bullets[i].resetBullet(gm.player.getWorldLocation());
                }
                else if(handyPointF.y < handyPointF2.y - gm.metresToShowY/2)
                {
                    gm.bullets[i].resetBullet(gm.player.getWorldLocation());
                }

                // if the bullet out of the border
                if(CollisionCheck.contain(gm.mapWidth,gm.mapHeight,gm.bullets[i].boundaries))
                {
                    gm.bullets[i].resetBullet(gm.player.getWorldLocation());
                    sm.playSound("ricochet");
                }

                // check collision between asteroids and bullets
                for(int bulletNum = 0;bulletNum<gm.numBullets;bulletNum++)
                {
                    for(int asteroidNum =0 ;asteroidNum<gm.numAsteroids;asteroidNum++)
                    {
                        if(gm.bullets[bulletNum].isInFlight() && gm.asteroids[asteroidNum].isActive())
                        {
                            if(CollisionCheck.detect(gm.bullets[bulletNum].boundaries, gm.asteroids[asteroidNum].boundaries))
                            {
                                gm.bullets[bulletNum].resetBullet(gm.player.getWorldLocation());
                                destroyAsteroid(asteroidNum);
                            }
                        }
                    }
                }

                // check collision between asteroids and player
                for(int asteroidNum =0;asteroidNum<gm.numAsteroids;asteroidNum++)
                {
                    if(gm.asteroids[asteroidNum].isActive())
                    {
                        if(CollisionCheck.detect(gm.player.cp,gm.asteroids[asteroidNum].boundaries))
                        {
                            destroyAsteroid(asteroidNum);
                            lifeLost();
                        }
                    }
                }
            }
        }
    }

    private void draw()
    {
        handyPointF = gm.player.getWorldLocation();  // get where is the player

        orthoM(viewportMatrix, 0, handyPointF.x - gm.metresToShowX / 2, handyPointF.x + gm.metresToShowX / 2,
                handyPointF.y - gm.metresToShowY / 2, handyPointF.y+gm.metresToShowY/2,0f,1f);

        glClear(GL_COLOR_BUFFER_BIT);

        //draw the player
        gm.player.draw(viewportMatrix);
        //draw the border
        gm.border.draw(viewportMatrix);
        //draw stars
        for(int i=0;i<gm.numStars;i++)
        {
            if(gm.stars[i].isActive)
                gm.stars[i].draw(viewportMatrix);
        }
        //draw the bullets
        for(int i=0;i<gm.numBullets;i++)
        {
            gm.bullets[i].draw(viewportMatrix);
        }
        //draw the asteroids
        for(int i=0;i<gm.numAsteroids;i++)
        {
            if(gm.asteroids[i].isActive)
                gm.asteroids[i].draw(viewportMatrix);
        }

        //draw the buttons
        for(int i=0;i<gameButtons.length;i++)
        {
            gameButtons[i].draw();
        }
        //draw the life icons
        for(int i=0;i<gm.numLives;i++)
        {
            gm.lives[i].draw();
        }
        //draw the level icons
        for(int i=0;i<gm.numAsteroidsRemaining;i++)
        {
            gm.scores[i].draw();
        }
    }

    public void lifeLost()
    {
        gm.player.setWorldLocation(gm.mapWidth/2,gm.mapHeight/2);
        sm.playSound("shipexplode");
        gm.numLives -= 1; // deduct a life
        if(gm.numLives==0)
        {
            gm.levelNumber = 1;
            gm.numLives=3;
            createObjects();
            gm.switchPlayingStatus();
            sm.playSound("gameover");
        }
    }

    public void destroyAsteroid(int asteroidIndex)
    {
        gm.asteroids[asteroidIndex].setActive(false);
        sm.playSound("explode");
        gm.numAsteroidsRemaining --;

        if(gm.numAsteroidsRemaining==0)
        {
            gm.levelNumber++;
            gm.numLives++;
            sm.playSound("nextLevel");
            createObjects();
        }
    }


}

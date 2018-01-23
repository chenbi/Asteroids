package com.example.opengl;

import android.app.Activity;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Display;

public class MainActivity extends Activity
{
    private GLSurfaceView customView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // get a display object to access screen detail
        Display display = getWindowManager().getDefaultDisplay();

        //load the resolution into a point object
        Point resolution = new Point();
        display.getSize(resolution);
        customView= new CustomView (this,resolution.x,resolution.y);

        setContentView(customView);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        customView.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        customView.onResume();
    }

}

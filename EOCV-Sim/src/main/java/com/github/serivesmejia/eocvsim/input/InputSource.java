package com.github.serivesmejia.eocvsim.input;

import com.github.serivesmejia.eocvsim.EOCVSim;
import org.opencv.core.Mat;

public abstract class InputSource {

    public transient boolean isDefault = false;
    public transient EOCVSim eocvSim = null;

    protected transient String name = "";

    protected transient boolean isPaused = false;
    private transient boolean beforeIsPaused = false;

    public boolean init() {
        return false;
    }

    public abstract void reset();

    public abstract void close();

    public abstract void onPause();

    public abstract void onResume();

    public Mat update() {
        return null;
    }

    public InputSource cloneSource() {
        return null;
    }

    public final void setPaused(boolean paused) {

        isPaused = paused;

        if (beforeIsPaused != isPaused) {
            if (isPaused) {
                onPause();
            } else {
                onResume();
            }
        }

        beforeIsPaused = paused;

    }

}

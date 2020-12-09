package com.github.serivesmejia.eocvsim;

import com.github.serivesmejia.eocvsim.config.ConfigManager;
import com.github.serivesmejia.eocvsim.gui.Visualizer;
import com.github.serivesmejia.eocvsim.gui.Visualizer.AsyncPleaseWaitDialog;
import com.github.serivesmejia.eocvsim.input.InputSourceManager;
import com.github.serivesmejia.eocvsim.pipeline.PipelineManager;
import com.github.serivesmejia.eocvsim.tuner.TunerManager;
import com.github.serivesmejia.eocvsim.util.fps.FpsCounter;
import com.github.serivesmejia.eocvsim.util.fps.FpsLimiter;
import com.github.serivesmejia.eocvsim.util.Log;
import com.github.serivesmejia.eocvsim.util.SysUtil;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.awt.*;
import java.util.ArrayList;

public class EOCVSim {

    public static String VERSION = "2.0.0";

    public static int DEFAULT_EOCV_WIDTH = 320;
    public static int DEFAULT_EOCV_HEIGHT = 240;

    private final ArrayList<Runnable> runnsOnMain = new ArrayList<>();

    public volatile Visualizer visualizer = new Visualizer(this);

    public ConfigManager configManager = new ConfigManager();
    public InputSourceManager inputSourceManager = new InputSourceManager(this);
    public PipelineManager pipelineManager = null; //we'll initialize pipeline manager after loading native lib
    public TunerManager tunerManager = new TunerManager(this);

    public final FpsLimiter fpsLimiter = new FpsLimiter(60);
    public final FpsCounter fpsCounter = new FpsCounter();

    public void init() {

        Log.info("EOCVSim", "Initializing EasyOpenCV Simulator v" + VERSION);
        Log.white();

        SysUtil.loadCvNativeLib();
        Log.white();

        Thread.currentThread().setPriority((int) (Thread.MAX_PRIORITY * 0.8));

        pipelineManager = new PipelineManager(this);

        configManager.init(); //load config

        visualizer.initAsync(configManager.getConfig().simTheme); //create gui in new thread

        inputSourceManager.init(); //loading user created input sources

        //create a dialog to give user visual feedback
        AsyncPleaseWaitDialog lookForPipelineAPWD = visualizer.asyncPleaseWaitDialog("Looking for pipelines...", "Scanning classpath", "Exit", new Dimension(300, 150), true);
        lookForPipelineAPWD.onCancel(() -> System.exit(0));

        pipelineManager.init(lookForPipelineAPWD); //init pipeline manager (scan for pipelines)

        lookForPipelineAPWD.destroyDialog(); //destroy dialog since wait's over.

        tunerManager.init(); //init tunable variables manager

        visualizer.waitForFinishingInit(); //wait for visualizer to finish its work

        visualizer.updateSourcesList(); //update sources and pick first one
        visualizer.sourceSelector.setSelectedIndex(0);

        visualizer.updatePipelinesList(); //update pipelines and pick first one (DefaultPipeline)
        visualizer.pipelineSelector.setSelectedIndex(0);

        beginLoop();

    }

    public void beginLoop() {

        Log.info("EOCVSim", "Begin EOCVSim loop");
        Log.white();

        inputSourceManager.inputSourceLoader.saveInputSourcesToFile();
        while (!Thread.interrupted()) {

            Telemetry telemetry = pipelineManager.currentTelemetry;

            //run all pending requested runnables
            for (Runnable runn : runnsOnMain.toArray(new Runnable[0])) {
                runn.run();
                runnsOnMain.remove(runn);
            }

            updateVisualizerTitle();

            inputSourceManager.update(pipelineManager.isPaused());
            tunerManager.update();

            //if we dont have a mat from the inputsource, we'll just skip this frame.
            if (inputSourceManager.lastMatFromSource == null || inputSourceManager.lastMatFromSource.empty()) continue;

            try {

                pipelineManager.update(inputSourceManager.lastMatFromSource);

                if (!pipelineManager.isPaused())
                    visualizer.matPoster.post(pipelineManager.lastOutputMat);

                if (telemetry != null) {
                    telemetry.errItem.setCaption("");
                    telemetry.errItem.setValue("");
                }

            } catch (Exception ex) {

                Log.error("Error while processing pipeline", ex);

                if (telemetry != null) {
                    telemetry.errItem.setCaption("[/!\\]");
                    telemetry.errItem.setValue("Error while processing pipeline\nCheck console for details.");
                    telemetry.update();
                }

            }

            visualizer.updateTelemetry(pipelineManager.currentTelemetry);

            if(!pipelineManager.isPaused()) fpsCounter.update();

            try {
                fpsLimiter.sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

        }

        Log.warn("EOCVSim", "Main thread interrupted (" + Integer.toHexString(hashCode()) + ")");

    }

    public void destroy(DestroyReason reason) {

        String hexCode = Integer.toHexString(this.hashCode());

        Log.warn("EOCVSim", "Destroying current EOCVSim (" + hexCode + ") due to " + reason.toString());
        Log.info("EOCVSim", "Trying to save config file...");

        configManager.saveToFile();

        visualizer.close();
        Thread.currentThread().interrupt();

    }

    public void destroy() {
        destroy(DestroyReason.USER_REQUESTED);
    }

    public void restart() {

        Log.info("EOCVSim", "Restarting...");
        Log.white();

        destroy(DestroyReason.RESTART);

        Log.white();

        new Thread(() -> new EOCVSim().init(), "main").start(); //run next instance on a separate thread for the old one to get interrupted and ended

    }

    public void updateVisualizerTitle() {

        String pipelineFpsMsg = " (" + fpsCounter.getFPS() + " Pipeline FPS)";
        String posterFpsMsg = " (" + visualizer.matPoster.fpsCounter.getFPS() + " Poster FPS)";

        String isPaused = pipelineManager.isPaused() ? " (Paused)" : "";

        String memoryMsg = " (" + SysUtil.getMemoryUsageMB() + " MB Java memory used)";

        String msg = pipelineFpsMsg + posterFpsMsg + isPaused + memoryMsg;

        if (pipelineManager.currentPipeline == null) {
            visualizer.setTitleMessage("No pipeline" + msg);
        } else {
            visualizer.setTitleMessage(pipelineManager.currentPipelineName + msg);
        }

    }

    public void runOnMainThread(Runnable runn) {
        runnsOnMain.add(runn);
    }

    public enum DestroyReason {USER_REQUESTED, THEME_CHANGING, RESTART}

}

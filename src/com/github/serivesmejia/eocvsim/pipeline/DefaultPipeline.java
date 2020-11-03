package com.github.serivesmejia.eocvsim.pipeline;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import org.openftc.easyopencv.OpenCvPipeline;

public class DefaultPipeline extends OpenCvPipeline {

    @Override
    public void init(Mat input) { }

    @Override
    public Mat processFrame(Mat input) {

        double aspectRatio = (double)input.height() / (double)input.width();

        double aspectRatioPercentage = aspectRatio / (580.0/480.0);

        telemetry.addData("[>]", "Default pipeline selected.");
        telemetry.addData("[Aspect Ratio]", aspectRatio + " (" + String.format("%.2f", aspectRatioPercentage * 100) + "%)");
        telemetry.update();

        // Outline
        Imgproc.putText (
                input,
                "Default pipeline selected",
                new Point(0, 22 * aspectRatioPercentage),
                Imgproc.FONT_HERSHEY_PLAIN,
                2 * aspectRatioPercentage,
                new Scalar(255, 255, 255),
                (int) Math.round(5 * aspectRatioPercentage)
        );

        //Text
        Imgproc.putText (
                input,
                "Default pipeline selected",
                new Point(0, 22 * aspectRatioPercentage),
                Imgproc.FONT_HERSHEY_PLAIN,
                2 * aspectRatioPercentage,
                new Scalar(0, 0, 0),
                (int) Math.round(2 * aspectRatioPercentage)
        );

        return input;

    }
}

package com.github.serivesmejia.eocvsim.tuner;

import com.github.serivesmejia.eocvsim.EOCVSim;
import com.github.serivesmejia.eocvsim.gui.tuner.TunableFieldPanel;
import com.github.serivesmejia.eocvsim.tuner.field.BooleanField;
import com.github.serivesmejia.eocvsim.tuner.field.PointField;
import com.github.serivesmejia.eocvsim.tuner.field.ScalarField;
import com.github.serivesmejia.eocvsim.tuner.field.StringField;
import com.github.serivesmejia.eocvsim.tuner.field.numeric.DoubleField;
import com.github.serivesmejia.eocvsim.tuner.field.numeric.FloatField;
import com.github.serivesmejia.eocvsim.tuner.field.numeric.IntegerField;
import com.github.serivesmejia.eocvsim.tuner.field.numeric.LongField;
import com.github.serivesmejia.eocvsim.util.Log;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.openftc.easyopencv.OpenCvPipeline;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class TunerManager {

    private final EOCVSim eocvSim;

    private final List<TunableField> fields = new ArrayList<>();

    private boolean firstInit = true;

    public TunerManager(EOCVSim eocvSim) {
        this.eocvSim = eocvSim;
    }

    public void init() {

        if (firstInit) {
            eocvSim.pipelineManager.onPipelineChange.doPersistent(this::reset);
            firstInit = false;
        }

        if (eocvSim.pipelineManager.currentPipeline == null) return;

        addFieldsFrom(eocvSim.pipelineManager.currentPipeline);
        eocvSim.visualizer.updateTunerFields(getTunableFieldPanels());

    }

    public void update() {
        //update all fields
        for (TunableField field : fields) {
            try {
                field.update();
            } catch(Exception ex) {
                Log.error("Error while updating field " + field.getFieldName(), ex);
            }
        }
    }

    public void reset() {
        fields.clear();
        init();
    }

    public void addFieldsFrom(OpenCvPipeline pipeline) {

        if (pipeline == null) return;

        Field[] fields = pipeline.getClass().getFields();

        for (Field field : fields) {

            TunableField toAddField = null; //for code simplicity

            try {

                if (Modifier.isFinal(field.getModifiers())) continue;

                if (field.getType() == int.class) {
                    toAddField = new IntegerField(pipeline, field, eocvSim);
                } else if (field.getType() == float.class) {
                    toAddField = new FloatField(pipeline, field, eocvSim);
                } else if (field.getType() == double.class) {
                    toAddField = new DoubleField(pipeline, field, eocvSim);
                } else if (field.getType() == long.class) {
                    toAddField = new LongField(pipeline, field, eocvSim);
                } else if (field.getType() == boolean.class) {
                    toAddField = new BooleanField(pipeline, field, eocvSim);
                } else if (field.getType() == String.class) {
                    toAddField = new StringField(pipeline, field, eocvSim);
                } else if (field.getType() == Scalar.class) {
                    toAddField = new ScalarField(pipeline, field, eocvSim);
                } else if (field.getType() == Point.class) {
                    toAddField = new PointField(pipeline, field, eocvSim);
                }

            } catch (Exception ex) {
                Log.error("TunerManager", "Reflection error while processing field: " + field.getName(), ex);
            }

            if (toAddField != null) {
                this.fields.add(toAddField);
            }

        }

    }

    public List<TunableFieldPanel> getTunableFieldPanels() {

        List<TunableFieldPanel> panels = new ArrayList<>();

        for (TunableField field : fields) {
            panels.add(new TunableFieldPanel(field, eocvSim));
        }

        return panels;

    }

}

package mandelbrot;

import mandelbrot.events.AdvancedChangeAdapter;
import mandelbrot.events.AdvancedChangeListener;
import mandelbrot.events.ConfigChangeListener;
import utils.Complex;
import utils.ImageProperties;
import utils.JSliderAdvanced;
import utils.SpringUtilities;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Manages Config
 *
 * @author Huw Jones
 * @since 28/02/2016
 */
public class ConfigManager {

    private Main mainWindow;

    private JPanel panel_config;
    private JPanel panel_labelled;
    private JPanel panel_singlets;

    // Control Panel
    private JLabel label_iterations;
    private JSpinner spinner_iterations;

    private JLabel label_translateX;
    private JSpinner spinner_shiftX;

    private JLabel label_translateY;
    private JSpinner spinner_shiftY;

    private JLabel label_scale;
    private JSpinner spinner_scale;

    private JLabel label_colour;
    private JSliderAdvanced slider_colour;

    private JLabel label_openCL;
    private JCheckBox check_openCL;

    private JButton btn_render;


    // Variables
    double xShift;
    double yShift;
    double scaleFactor;
    int iterations;
    double colourShift;
    Complex selectedPoint;
    int escapeRadius = 2;

    boolean openCL = true;

    private ArrayList<ConfigChangeListener> listeners;

    public ConfigManager(Main mainWindow){
        listeners = new ArrayList<>();
        this.mainWindow = mainWindow;
        initPanel();
    }

    public void addConfigChangeListener(ConfigChangeListener listener){
        listeners.add(listener);
    }

    //region Initialise Components
    private void initPanel(){
        panel_config = new JPanel();
        panel_config.setLayout(new BoxLayout(panel_config, BoxLayout.PAGE_AXIS));
        panel_config.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Controls"));

        panel_labelled = new JPanel(new SpringLayout());
        panel_config.add(panel_labelled);
        initLabelledComponents();

        panel_singlets = new JPanel(new GridBagLayout());
        panel_config.add(panel_singlets);
        initSingletComponents();
    }

    private void initLabelledComponents(){

        // Iterations
        label_iterations = new JLabel("Iterations:", JLabel.TRAILING);
        panel_labelled.add(label_iterations);

        spinner_iterations = new JSpinner(new SpinnerNumberModel(100, 1, Integer.MAX_VALUE, 1));
        spinner_iterations.addChangeListener(new optionChangeHandler());
        panel_labelled.add(spinner_iterations);

        // Scale
        label_scale = new JLabel("Scale:", JLabel.TRAILING);
        panel_labelled.add(label_scale);

        spinner_scale = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 0.1d));
        spinner_scale.addChangeListener(new optionChangeHandler());
        panel_labelled.add(spinner_scale);

        // x shift
        label_translateX = new JLabel("X Shift:", JLabel.TRAILING);
        panel_labelled.add(label_translateX);

        spinner_shiftX = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0.1));
        spinner_shiftX.addChangeListener(new optionChangeHandler());
        panel_labelled.add(spinner_shiftX);

        // y shift
        label_translateY = new JLabel("Y Shift:", JLabel.TRAILING);
        panel_labelled.add(label_translateY);

        spinner_shiftY = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0.1));
        spinner_shiftY.addChangeListener(new optionChangeHandler());
        panel_labelled.add(spinner_shiftY);

        // Colour Shift
        label_colour = new JLabel("Colour Shift:", JLabel.TRAILING);
        panel_labelled.add(label_colour);

        slider_colour = new JSliderAdvanced(0, 720, 0);
        slider_colour.setMajorTickSpacing(45);
        slider_colour.setMinorTickSpacing(1);
        slider_colour.setPaintTicks(true);
        slider_colour.addAdvancedChangeListener(new colourShiftChangeHandler());
        panel_labelled.add(slider_colour);

        // Use OpenCL
        label_openCL = new JLabel("Use OpenCL:", JLabel.TRAILING);
        panel_labelled.add(label_openCL);
        check_openCL = new JCheckBox();
        check_openCL.setSelected(true);
        check_openCL.addChangeListener(new openCLChangeHandler());
        panel_labelled.add(check_openCL);

        SpringUtilities.makeCompactGrid(panel_labelled, 6, 2, 6, 6, 6, 6);
    }

    private void initSingletComponents(){
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.weightx = 1;

        btn_render = new JButton("Render");
        btn_render.addActionListener(new renderHandler());
        panel_singlets.add(btn_render, constraints);
    }
    //endregion

    //region Event Triggers
    private void xShiftChange(){
        for(ConfigChangeListener l: listeners){
            l.xShiftChange(xShift);
        }
    }

    private void yShiftChange(){
        for(ConfigChangeListener l: listeners){
            l.yShiftChange(yShift);
        }
    }

    private void iterationChange(){
        for(ConfigChangeListener l: listeners){
            l.iterationChange(iterations);
        }
    }

    private void scaleChange(){
        for(ConfigChangeListener l: listeners){
            l.scaleChange(scaleFactor);
        }
    }

    private void configChange(){
        ImageProperties p = new ImageProperties(iterations, scaleFactor, xShift, yShift);
        for(ConfigChangeListener l :listeners){
            l.configChange(p);
        }
    }

    private void colourShiftChange(){
        for(ConfigChangeListener l : listeners){
            l.colourShiftChange(colourShift);
        }
    }

    private void selectedPointChange(){
        for(ConfigChangeListener l : listeners){
            l.selectedPointChange(selectedPoint);
        }
    }
    //endregion

    //region Get/Set Methods
    public JPanel getConfigPanel(){
        return this.panel_config;
    }

    public double getShiftX() {
        return (double) spinner_shiftX.getValue();
    }

    public double getShiftY() {
        return (double) spinner_shiftY.getValue();
    }

    public int getIterations() {
        return (int) spinner_iterations.getValue();
    }

    public double getScaleFactor() {
        return (double) spinner_scale.getValue();
    }

    public float getTint () {
        return (float) slider_colour.getValue() / 720f;
    }

    public int getEscapeRadiusSquared() { return escapeRadius * escapeRadius; }

    public Complex getSelectedPoint() {
        if(selectedPoint == null) return null;
        return selectedPoint.clone();
    }
    public void setSelectedPoint(Complex complex){
        this.selectedPoint = complex;
        selectedPointChange();
    }


    public boolean useOpenCL() { return openCL;}

    //endregion

    //region Event Handlers
    private class renderHandler implements ActionListener {

        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            mainWindow.renderMandelbrot();
        }
    }

    private class optionChangeHandler implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if(!(e.getSource() instanceof JSpinner)) return;
            JSpinner spinner = (JSpinner)e.getSource();

            if(spinner == spinner_iterations){
                iterations = getIterations();
                iterationChange();
            } else if(spinner == spinner_scale) {
                scaleFactor = getScaleFactor();
                scaleChange();
            } else if(spinner == spinner_shiftX){
                xShift = getShiftX();
                xShiftChange();
            } else if(spinner == spinner_shiftY) {
                yShift = getShiftY();
                yShiftChange();
            }
            configChange();
        }
    }

    private class colourShiftChangeHandler extends AdvancedChangeAdapter {

        /**
         * Invoked when the target of the listener has finished changing its state.
         *
         * @param e a ChangeEvent object
         */
        @Override
        public void changeFinish(ChangeEvent e) {
            colourShift = getTint();
            colourShiftChange();
        }
    }

    private class openCLChangeHandler implements ChangeListener {
        /**
         * Invoked when the target of the listener has changed its state.
         *
         * @param e a ChangeEvent object
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            openCL = check_openCL.isSelected();
        }
    }

    //endregion
}

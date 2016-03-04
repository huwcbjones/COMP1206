package mandelbrot;

import mandelbrot.events.AdvancedChangeAdapter;
import mandelbrot.events.ConfigChangeListener;
import utils.Complex;
import utils.JSliderAdvanced;
import utils.Log;
import utils.SpringUtilities;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
    boolean useOpenCL = true;
    boolean isCacheDisabled = false;
    private JPanel panel_controls;
    private JPanel panel_singlets;
    private JPanel panel_colouring;
    private JPanel panel_advanced;
    private JSpinner spinner_iterations;

    private JLabel label_translateX;
    private JSpinner spinner_shiftX;

    private JLabel label_translateY;
    private JSpinner spinner_shiftY;

    private JLabel label_scale;
    private JSpinner spinner_scale;
    private JTabbedPane tabbedPane;
    //region Controls
    private JLabel label_iterations;

    private JLabel label_saturation;
    private JSliderAdvanced slider_saturation;

    private JLabel label_brightness;
    private JSliderAdvanced slider_brightness;
    //endregion
    //region Colouring
    private JLabel label_hue;
    private JCheckBox check_openCL;
    private JSliderAdvanced slider_hue;
    //endregion
    //region Advanced
    private JLabel label_openCL;
    private JLabel label_disableCache;
    //endregion

    // Variables
    double xShift;
    double yShift;
    double scaleFactor;
    int iterations;
    float hueShift;
    float saturation;
    float brightness;
    Complex selectedPoint;
    int escapeRadius = 2;
    private JCheckBox check_disableCache;
    //endregion
    //region Singlets
    private JButton btn_render;

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
        panel_config.setLayout(new BorderLayout());
        panel_config.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Controls"));

        tabbedPane = new JTabbedPane();
        panel_config.add(tabbedPane, BorderLayout.CENTER);

        panel_controls = new JPanel(new SpringLayout());
        panel_colouring = new JPanel(new SpringLayout());
        panel_advanced = new JPanel(new SpringLayout());

        initControlComponents();
        initColouringComponents();
        initAdvancedComponents();

        panel_singlets = new JPanel(new GridBagLayout());
        panel_config.add(panel_singlets, BorderLayout.PAGE_END);
        initSingletComponents();
    }

    private void initControlComponents() {

        // Iterations
        label_iterations = new JLabel("Iterations:", JLabel.TRAILING);
        panel_controls.add(label_iterations);

        spinner_iterations = new JSpinner(new SpinnerNumberModel(100, 1, Integer.MAX_VALUE, 1));
        spinner_iterations.addChangeListener(new optionChangeHandler());
        panel_controls.add(spinner_iterations);

        // Scale
        label_scale = new JLabel("Scale:", JLabel.TRAILING);
        panel_controls.add(label_scale);

        spinner_scale = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 0.1d));
        spinner_scale.addChangeListener(new optionChangeHandler());
        panel_controls.add(spinner_scale);

        // x shift
        label_translateX = new JLabel("X Shift:", JLabel.TRAILING);
        panel_controls.add(label_translateX);

        spinner_shiftX = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0.05));
        spinner_shiftX.addChangeListener(new optionChangeHandler());
        panel_controls.add(spinner_shiftX);

        // y shift
        label_translateY = new JLabel("Y Shift:", JLabel.TRAILING);
        panel_controls.add(label_translateY);

        spinner_shiftY = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0.05));
        spinner_shiftY.addChangeListener(new optionChangeHandler());
        panel_controls.add(spinner_shiftY);

        SpringUtilities.makeCompactGrid(panel_controls, 4, 2, 6, 6, 6, 6);
        tabbedPane.addTab("Controls", panel_controls);
    }

    private void initColouringComponents() {
        // Hue Shift
        label_hue = new JLabel("Hue Shift:", JLabel.TRAILING);
        panel_colouring.add(label_hue);

        slider_hue = new JSliderAdvanced(0, 360, 0);
        slider_hue.setMajorTickSpacing(36);
        slider_hue.setMinorTickSpacing(1);
        slider_hue.setPaintTicks(true);
        slider_hue.addAdvancedChangeListener(new colourShiftChangeHandler());
        panel_colouring.add(slider_hue);

        // Saturation
        label_saturation = new JLabel("Saturation:", JLabel.TRAILING);
        panel_colouring.add(label_saturation);

        slider_saturation = new JSliderAdvanced(0, 100, 100);
        slider_saturation.setMajorTickSpacing(10);
        slider_saturation.setMinorTickSpacing(1);
        slider_saturation.setPaintTicks(true);
        slider_saturation.addAdvancedChangeListener(new colourShiftChangeHandler());
        panel_colouring.add(slider_saturation);

        // Brightness
        label_brightness = new JLabel("Brightness:", JLabel.TRAILING);
        panel_colouring.add(label_brightness);

        slider_brightness = new JSliderAdvanced(0, 100, 100);
        slider_brightness.setMajorTickSpacing(10);
        slider_brightness.setMinorTickSpacing(1);
        slider_brightness.setPaintTicks(true);
        slider_brightness.addAdvancedChangeListener(new colourShiftChangeHandler());
        panel_colouring.add(slider_brightness);

        SpringUtilities.makeCompactGrid(panel_colouring, 3, 2, 6, 6, 6, 6);
        tabbedPane.addTab("Colouring", panel_colouring);
    }

    private void initAdvancedComponents() {
        // Use OpenCL
        label_openCL = new JLabel("Use OpenCL:", JLabel.TRAILING);
        panel_advanced.add(label_openCL);

        check_openCL = new JCheckBox();
        check_openCL.setSelected(true);
        check_openCL.addChangeListener(new openCLChangeHandler());
        panel_advanced.add(check_openCL);

        // Disable Cache
        label_disableCache = new JLabel("Disable Cache:", JLabel.TRAILING);
        panel_advanced.add(label_disableCache);

        check_disableCache = new JCheckBox();
        check_disableCache.setSelected(false);
        check_disableCache.addChangeListener(new disableCacheHandler());
        panel_advanced.add(check_disableCache);

        SpringUtilities.makeCompactGrid(panel_advanced, 2, 2, 6, 6, 6, 6);
        tabbedPane.addTab("Advanced", panel_advanced);
    }

    private void initSingletComponents(){
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.weightx = 1;

        btn_render = new JButton("Render");
        btn_render.setMnemonic(KeyEvent.VK_R);
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

    private void colourChange(){
        for(ConfigChangeListener l : listeners){
            l.colourChange(hueShift, saturation, brightness);
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

    public void setShiftX(double xShift, boolean triggerEvent){
        spinner_shiftX.setValue(xShift);
        this.xShift = xShift;
        if(triggerEvent){
            xShiftChange();
        }
    }

    public double getShiftY() {
        return (double) spinner_shiftY.getValue();
    }

    public void setShiftY(double yShift, boolean triggerEvent){
        spinner_shiftY.setValue(yShift);
        this.yShift = yShift;
        if(triggerEvent){
            yShiftChange();
        }
    }

    public int getIterations() {
        return (int) spinner_iterations.getValue();
    }

    public double getScaleFactor() {
        return (double) spinner_scale.getValue();
    }

    public void setScaleFactor(double scaleFactor, boolean triggerEvent){
        spinner_scale.setValue(scaleFactor);
        this.scaleFactor = scaleFactor;
        if(triggerEvent){
            scaleChange();
        }
    }

    public float getHue() {
        return (float) slider_hue.getValue() / 360f;
    }

    public float getSaturation() { return (float)slider_saturation.getValue() / 100f; }

    public float getBrightness() { return (float)slider_brightness.getValue() / 100f; }

    public int getEscapeRadiusSquared() { return escapeRadius * escapeRadius; }

    public Complex getSelectedPoint() {
        if(selectedPoint == null) return null;
        return selectedPoint.clone();
    }
    public void setSelectedPoint(Complex complex){
        this.selectedPoint = complex;
        selectedPointChange();
    }

    public void setUseOpenCL(boolean useOpenCL) {
        this.useOpenCL = useOpenCL;
    }

    public void disableOpenCL() {
        Log.Warning("OpenCL disabled!");
        this.useOpenCL = false;
        check_openCL.setSelected(false);
        check_openCL.setEnabled(false);
    }

    public boolean useOpenCL() {
        return useOpenCL;
    }

    public boolean isCacheDisabled() {
        return isCacheDisabled;
    }

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
            hueShift = getHue();
            saturation = getSaturation();
            brightness = getBrightness();
            colourChange();
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
            useOpenCL = check_openCL.isSelected();
        }
    }

    private class disableCacheHandler implements ChangeListener {
        /**
         * Invoked when the target of the listener has changed its state.
         *
         * @param e a ChangeEvent object
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            isCacheDisabled = check_disableCache.isSelected();
        }
    }

    //endregion
}

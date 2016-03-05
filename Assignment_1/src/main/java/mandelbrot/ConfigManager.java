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
    private JPanel panel_controls;
    private JPanel panel_singlets;
    private JPanel panel_colouring;
    private JPanel panel_advanced;
    private JTabbedPane tabbedPane;

    //region Variables
    private int escapeRadius = 2;
    private int iterations;
    private double scaleFactor;
    private double xShift;
    private double yShift;

    private float hueShift;
    private float saturation;
    private float brightness;

    private boolean displayJuliaMoveCursor = false;
    private boolean useOpenCL = true;
    private boolean useOpenCLDouble = false;
    private boolean isCacheDisabled = true;

    Complex selectedPoint;
    //endregion
    //region Controls
    private JLabel label_iterations;
    private JSpinner spinner_iterations;

    private JLabel label_translateX;
    private JSpinner spinner_shiftX;

    private JLabel label_translateY;
    private JSpinner spinner_shiftY;

    private JLabel label_scale;
    private JSpinner spinner_scale;
    //endregion
    //region Colouring
    private JLabel label_hue;
    private JSliderAdvanced slider_hue;

    private JLabel label_saturation;
    private JSliderAdvanced slider_saturation;

    private JLabel label_brightness;
    private JSliderAdvanced slider_brightness;
    //endregion
    //region Advanced
    private JLabel label_juliaCursor;
    private JCheckBox check_juliaCursor;

    private JLabel label_openCL;
    private JCheckBox check_openCL;

    private JLabel label_openCL_double;
    private JCheckBox check_openCLDouble;

    private JLabel label_disableCache;
    private JCheckBox check_disableCache;
    //endregion
    //region Singlets
    private JButton btn_render;
    private JButton btn_reset;
    //endregion
    private ArrayList<ConfigChangeListener> listeners;

    public ConfigManager(Main mainWindow){
        listeners = new ArrayList<>();
        this.mainWindow = mainWindow;
        initPanel();
    }

    /**
     * Adds a config change listener to this ConfigManager
     * @param listener Listener to add
     */
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
        // Display Julia Set under cursor
        label_juliaCursor = new JLabel("Display Julia on move:", JLabel.TRAILING);
        panel_advanced.add(label_juliaCursor);

        check_juliaCursor = new JCheckBox();
        check_juliaCursor.setSelected(false);
        check_juliaCursor.addChangeListener(new juliaCursorChangeHandler());
        panel_advanced.add(check_juliaCursor);

        // Use OpenCL
        label_openCL = new JLabel("Use OpenCL:", JLabel.TRAILING);
        panel_advanced.add(label_openCL);

        check_openCL = new JCheckBox();
        check_openCL.setSelected(true);
        check_openCL.addChangeListener(new openCLChangeHandler());
        panel_advanced.add(check_openCL);

        // Use OpenCL Double
        label_openCL_double = new JLabel("OpenCL use Double:", JLabel.TRAILING);
        panel_advanced.add(label_openCL_double);

        check_openCLDouble = new JCheckBox();
        check_openCLDouble.setSelected(false);
        check_openCLDouble.addChangeListener(new openCLDoubleChangeHandler());
        panel_advanced.add(check_openCLDouble);

        // Disable Cache
        label_disableCache = new JLabel("Disable Cache:", JLabel.TRAILING);
        panel_advanced.add(label_disableCache);

        check_disableCache = new JCheckBox();
        check_disableCache.setSelected(true);

        check_disableCache.addChangeListener(new disableCacheHandler());
        panel_advanced.add(check_disableCache);

        SpringUtilities.makeCompactGrid(panel_advanced, 4, 2, 6, 6, 6, 6);
        tabbedPane.addTab("Advanced", panel_advanced);
    }

    private void initSingletComponents(){
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(3, 0, 3, 0);
        constraints.weightx = 0.5;

        btn_render = new JButton("Render");
        btn_render.setMnemonic(KeyEvent.VK_R);
        btn_render.addActionListener(new renderHandler());
        panel_singlets.add(btn_render, constraints);

        constraints.gridy = 1;
        btn_reset = new JButton("Reset to Default");
        btn_reset.setMnemonic(KeyEvent.VK_T);
        btn_reset.addActionListener(new resetHandler());
        panel_singlets.add(btn_reset, constraints);
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

    /**
     * Gets the config panel
     * @return JPanel
     */
    public JPanel getConfigPanel(){
        return this.panel_config;
    }

    /**
     * Gets the shift in the x axis
     * @return x shift
     */
    public double getShiftX() {
        return (double) spinner_shiftX.getValue();
    }

    /**
     * Sets the x shift
     * @param xShift New X Shift
     * @param triggerEvent Trigger the xShiftChange event
     */
    public void setShiftX(double xShift, boolean triggerEvent){
        spinner_shiftX.setValue(xShift);
        this.xShift = xShift;
        if(triggerEvent){
            xShiftChange();
        }
    }

    /**
     * Gets the shift in the y axis
     * @return y shift
     */
    public double getShiftY() {
        return (double) spinner_shiftY.getValue();
    }

    /**
     * Sets the y shift
     * @param yShift New y shift
     * @param triggerEvent Trigger the yShiftChange event
     */
    public void setShiftY(double yShift, boolean triggerEvent){
        spinner_shiftY.setValue(yShift);
        this.yShift = yShift;
        if(triggerEvent){
            yShiftChange();
        }
    }

    /**
     * Gets number of iterations
     * @return iterations
     */
    public int getIterations() {
        return (int) spinner_iterations.getValue();
    }

    /**
     * Gets the scale factor
     * @return scale factor
     */
    public double getScaleFactor() {
        return (double) spinner_scale.getValue();
    }

    /**
     * Sets the scale factor
     * @param scaleFactor new scale factor
     * @param triggerEvent Trigger the scaleChange event
     */
    public void setScaleFactor(double scaleFactor, boolean triggerEvent){
        spinner_scale.setValue(scaleFactor);
        this.scaleFactor = scaleFactor;
        if(triggerEvent){
            scaleChange();
        }
    }

    /**
     * Gets the image hue
     * @return image hue
     */
    public float getHue() {
        return (float) slider_hue.getValue() / 360f;
    }
    /**
     * Gets the image saturation
     * @return image saturation
     */
    public float getSaturation() { return (float)slider_saturation.getValue() / 100f; }

    /**
     * Gets the image brightness
     * @return image brightness
     */
    public float getBrightness() { return (float)slider_brightness.getValue() / 100f; }

    /**
     * Gets the escape radius
     * @return escape radius squared
     */
    public int getEscapeRadiusSquared() { return escapeRadius * escapeRadius; }

    /**
     * Gets the currently selected complex on the Mandelbrot set
     * @return Complex, current selected point
     */
    public Complex getSelectedPoint() {
        if(selectedPoint == null) return null;
        return selectedPoint.clone();
    }

    /**
     * Sets the currently selected complex on the Mandelbrot set
     * @param complex new selected point
     */
    public void setSelectedPoint(Complex complex){
        this.selectedPoint = complex;
        selectedPointChange();
    }

    /**
     * Display the Julia set when the cursor is moved
     * @return true if when the cursor moves, the julia set should be updated
     */
    public boolean juliaDisplayOnMove() {
        return this.displayJuliaMoveCursor;
    }

    /**
     * Sets whether OpenCL should be used
     * @param useOpenCL
     */
    public void setUseOpenCL(boolean useOpenCL) {
        this.useOpenCL = useOpenCL;
    }

    /**
     * Disables OpenCL until the application is restarted
     */
    public void disableOpenCL() {
        Log.Warning("OpenCL disabled!");
        this.useOpenCL = false;
        check_openCL.setSelected(false);
        check_openCL.setEnabled(false);
    }

    /**
     * Disables OpenCL doubles until the application is restarted
     */
    public void disableOpenCL_double() {
        Log.Warning("OpenCL doubles disabled!");
        this.useOpenCLDouble = false;
        check_openCLDouble.setSelected(false);
        check_openCLDouble.setEnabled(false);
    }

    /**
     * Returns whether OpenCL should be used or not
     * @return true if openCL should be used
     */
    public boolean useOpenCL() {
        return useOpenCL;
    }

    /**
     * Returns whether OpenCL Doubles should be used or not
     * @return true if OpenCL Doubles should be used
     */
    public boolean useOpenCL_double() {
        return useOpenCLDouble;
    }

    /**
     * Returns true if the cache is disabled
     * @return boolean
     */
    public boolean isCacheDisabled() {
        return isCacheDisabled;
    }

    //endregion

    //region Event Handlers

    /**
     * Invoked when btn_render is activated
     */
    private class renderHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            mainWindow.renderMandelbrot();
            mainWindow.renderJulia();
        }
    }

    /**
     * Invoked when btn_reset is activated
     */
    private class resetHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Set the values back to defaults
            spinner_iterations.setValue(100);
            spinner_scale.setValue(1.0);
            spinner_shiftX.setValue(0.0);
            spinner_shiftY.setValue(0.0);
            slider_hue.setValue(0);
            slider_saturation.setValue(100);
            slider_brightness.setValue(100);

            mainWindow.renderMandelbrot();
            mainWindow.renderJulia();
        }
    }

    /**
     * Invoked when a spinner is changed
     */
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

    /**
     * Invoked when a slider is changed
     */
    private class colourShiftChangeHandler extends AdvancedChangeAdapter {
        @Override
        public void changeFinish(ChangeEvent e) {
            hueShift = getHue();
            saturation = getSaturation();
            brightness = getBrightness();
            colourChange();
        }
    }

    /**
     * Invoked when a Julia checkbox is changed
     */
    private class juliaCursorChangeHandler implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            displayJuliaMoveCursor = check_juliaCursor.isSelected();
        }
    }

    /**
     * Invoked when a OpenCL checkbox is changed
     */
    private class openCLChangeHandler implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            useOpenCL = check_openCL.isSelected();
        }
    }

    /**
     * Invoked when a OpenCL Double checkbox is changed
     */
    private class openCLDoubleChangeHandler implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            useOpenCLDouble = check_openCLDouble.isSelected();
        }
    }

    /**
     * Invoked when a cache checkbox is changed
     */
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

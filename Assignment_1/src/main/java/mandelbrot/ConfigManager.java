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
    private double rangeX = 4.0;
    private double rangeY = 4.0;

    private String fractal = "Mandelbrot";
    private double escapeRadiusSquared = 9;
    private int iterations = 100;
    private double scaleFactor = 1.0;
    private double xShift = 0;
    private double yShift = 0;

    private float hue = 0f;
    private float saturation = 1f;
    private float brightness = 1f;

    private boolean displayJuliaMoveCursor = false;
    private boolean useOpenCL = true;
    private boolean useOpenCLDouble = false;
    private boolean isCacheDisabled = true;
    private boolean zoomAnimationEnabled = false;

    Complex selectedPoint;
    //endregion
    //region Controls
    private JLabel label_fractal;
    private JComboBox<String> combo_fractal;

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
    private JLabel label_escapeRadius;
    private JSpinner spinner_escapeRadius;

    private JLabel label_juliaCursor;
    private JCheckBox check_juliaCursor;

    private JLabel label_openCL;
    private JCheckBox check_openCL;

    private JLabel label_openCL_double;
    private JCheckBox check_openCLDouble;

    private JLabel label_disableCache;
    private JCheckBox check_disableCache;

    private JLabel label_zoomAnimation;
    private JCheckBox check_zoomAnimation;

    //endregion
    //region Singlets
    private JButton btn_render;
    private JButton btn_reset;
    //endregion
    private ArrayList<ConfigChangeListener> listeners;

    public ConfigManager (Main mainWindow) {
        this.listeners = new ArrayList<>();
        this.mainWindow = mainWindow;
        this.initPanel();
    }

    /**
     * Adds a config change listener to this ConfigManager
     *
     * @param listener Listener to add
     */
    public void addConfigChangeListener (ConfigChangeListener listener) {
        this.listeners.add(listener);
    }

    //region Initialise Components
    private void initPanel () {
        this.panel_config = new JPanel();
        this.panel_config.setLayout(new BorderLayout());
        this.panel_config.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Controls"));

        this.tabbedPane = new JTabbedPane();
        this.panel_config.add(this.tabbedPane, BorderLayout.CENTER);

        this.panel_controls = new JPanel(new SpringLayout());
        this.panel_colouring = new JPanel(new SpringLayout());
        this.panel_advanced = new JPanel(new SpringLayout());

        this.initControlComponents();
        this.initColouringComponents();
        this.initAdvancedComponents();

        this.panel_singlets = new JPanel(new GridBagLayout());
        this.panel_config.add(this.panel_singlets, BorderLayout.PAGE_END);
        this.initSingletComponents();
    }

    private void initControlComponents () {
        // Fractal
        this.label_fractal = new JLabel("Fractal:", JLabel.TRAILING);
        this.panel_controls.add(this.label_fractal);

        this.combo_fractal = new JComboBox<>();
        this.combo_fractal.addActionListener(new fractalHandler());
        this.panel_controls.add(this.combo_fractal);

        // Iterations
        this.label_iterations = new JLabel("Iterations:", JLabel.TRAILING);
        this.panel_controls.add(this.label_iterations);

        this.spinner_iterations = new JSpinner(new SpinnerNumberModel(100, 1, Integer.MAX_VALUE, 1));
        this.spinner_iterations.addChangeListener(new optionChangeHandler());
        this.panel_controls.add(this.spinner_iterations);

        // Scale
        this.label_scale = new JLabel("Scale:", JLabel.TRAILING);
        this.panel_controls.add(this.label_scale);

        this.spinner_scale = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 0.1d));
        this.spinner_scale.addChangeListener(new optionChangeHandler());
        this.panel_controls.add(this.spinner_scale);

        // x shift
        this.label_translateX = new JLabel("X Shift:", JLabel.TRAILING);
        this.panel_controls.add(this.label_translateX);

        this.spinner_shiftX = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0.05));
        this.spinner_shiftX.addChangeListener(new optionChangeHandler());
        this.panel_controls.add(this.spinner_shiftX);

        // y shift
        this.label_translateY = new JLabel("Y Shift:", JLabel.TRAILING);
        this.panel_controls.add(this.label_translateY);

        this.spinner_shiftY = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0.05));
        this.spinner_shiftY.addChangeListener(new optionChangeHandler());
        this.panel_controls.add(this.spinner_shiftY);

        SpringUtilities.makeCompactGrid(this.panel_controls, 5, 2, 6, 6, 6, 6);
        this.tabbedPane.addTab("Controls", this.panel_controls);
    }

    private void initColouringComponents () {
        // Hue Shift
        this.label_hue = new JLabel("Hue Shift:", JLabel.TRAILING);
        this.panel_colouring.add(this.label_hue);

        this.slider_hue = new JSliderAdvanced(0, 360, 0);
        this.slider_hue.setMajorTickSpacing(36);
        this.slider_hue.setMinorTickSpacing(1);
        this.slider_hue.setPaintTicks(true);
        this.slider_hue.addAdvancedChangeListener(new colourShiftChangeHandler());
        this.panel_colouring.add(this.slider_hue);

        // Saturation
        this.label_saturation = new JLabel("Saturation:", JLabel.TRAILING);
        this.panel_colouring.add(this.label_saturation);

        this.slider_saturation = new JSliderAdvanced(0, 100, 100);
        this.slider_saturation.setMajorTickSpacing(10);
        this.slider_saturation.setMinorTickSpacing(1);
        this.slider_saturation.setPaintTicks(true);
        this.slider_saturation.addAdvancedChangeListener(new colourShiftChangeHandler());
        this.panel_colouring.add(this.slider_saturation);

        // Brightness
        this.label_brightness = new JLabel("Brightness:", JLabel.TRAILING);
        this.panel_colouring.add(this.label_brightness);

        this.slider_brightness = new JSliderAdvanced(0, 100, 100);
        this.slider_brightness.setMajorTickSpacing(10);
        this.slider_brightness.setMinorTickSpacing(1);
        this.slider_brightness.setPaintTicks(true);
        this.slider_brightness.addAdvancedChangeListener(new colourShiftChangeHandler());
        this.panel_colouring.add(this.slider_brightness);

        SpringUtilities.makeCompactGrid(this.panel_colouring, 3, 2, 6, 6, 6, 6);
        this.tabbedPane.addTab("Colouring", this.panel_colouring);
    }

    private void initAdvancedComponents () {
        // Escape Radius
        this.label_escapeRadius = new JLabel("Escape Radius:", JLabel.TRAILING);
        this.panel_advanced.add(this.label_escapeRadius);

        this.spinner_escapeRadius = new JSpinner(new SpinnerNumberModel(3, 0, 100, 0.1));
        this.spinner_escapeRadius.addChangeListener(new optionChangeHandler());
        this.panel_advanced.add(this.spinner_escapeRadius);

        // Display Julia Set under cursor
        this.label_juliaCursor = new JLabel("Display Julia on move:", JLabel.TRAILING);
        this.panel_advanced.add(this.label_juliaCursor);

        this.check_juliaCursor = new JCheckBox();
        this.check_juliaCursor.setSelected(displayJuliaMoveCursor);
        this.check_juliaCursor.addChangeListener(new checkChangeHanlder());
        this.panel_advanced.add(this.check_juliaCursor);

        // Use OpenCL
        this.label_openCL = new JLabel("Use OpenCL:", JLabel.TRAILING);
        this.panel_advanced.add(this.label_openCL);

        this.check_openCL = new JCheckBox();
        this.check_openCL.setSelected(useOpenCL);
        this.check_openCL.addChangeListener(new checkChangeHanlder());
        this.panel_advanced.add(this.check_openCL);

        // Use OpenCL Double
        this.label_openCL_double = new JLabel("OpenCL use Double:", JLabel.TRAILING);
        this.panel_advanced.add(this.label_openCL_double);

        this.check_openCLDouble = new JCheckBox();
        this.check_openCLDouble.setSelected(useOpenCLDouble);
        this.check_openCLDouble.addChangeListener(new checkChangeHanlder());
        this.panel_advanced.add(this.check_openCLDouble);

        // Disable Cache
        this.label_disableCache = new JLabel("Disable Cache:", JLabel.TRAILING);
        this.panel_advanced.add(this.label_disableCache);

        this.check_disableCache = new JCheckBox();
        this.check_disableCache.setSelected(isCacheDisabled);

        this.check_disableCache.addChangeListener(new checkChangeHanlder());
        this.panel_advanced.add(this.check_disableCache);

        // Enable Zoom Animations
        this.label_zoomAnimation = new JLabel("Enable Zoom Animation:", JLabel.TRAILING);
        this.panel_advanced.add(this.label_zoomAnimation);

        this.check_zoomAnimation = new JCheckBox();
        this.check_zoomAnimation.setSelected(zoomAnimationEnabled);
        this.check_zoomAnimation.addChangeListener(new checkChangeHanlder());
        this.panel_advanced.add(this.check_zoomAnimation);

        SpringUtilities.makeCompactGrid(this.panel_advanced, 6, 2, 6, 6, 6, 6);
        this.tabbedPane.addTab("Advanced", this.panel_advanced);
    }

    private void initSingletComponents () {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(3, 0, 3, 0);
        constraints.weightx = 0.5;

        this.btn_render = new JButton("Render");
        this.btn_render.setMnemonic(KeyEvent.VK_R);
        this.btn_render.addActionListener(new renderHandler());
        this.panel_singlets.add(this.btn_render, constraints);

        constraints.gridy = 1;
        this.btn_reset = new JButton("Reset to Default");
        this.btn_reset.setMnemonic(KeyEvent.VK_T);
        this.btn_reset.addActionListener(new resetHandler());
        this.panel_singlets.add(this.btn_reset, constraints);
    }
    //endregion

    //region Event Triggers
    private void fractalChanged () {
        for (ConfigChangeListener l : this.listeners) {
            l.fractalChange(this.fractal);
        }
    }

    private void escapeRadiusChanged () {
        for (ConfigChangeListener l : this.listeners) {
            l.escapeRadiusSquaredChange(this.escapeRadiusSquared);
        }
    }

    private void xShiftChange () {
        for (ConfigChangeListener l : this.listeners) {
            l.xShiftChange(this.xShift);
        }
    }

    private void yShiftChange () {
        for (ConfigChangeListener l : this.listeners) {
            l.yShiftChange(this.yShift);
        }
    }

    private void iterationChange () {
        for (ConfigChangeListener l : this.listeners) {
            l.iterationChange(this.iterations);
        }
    }

    private void scaleChange () {
        for (ConfigChangeListener l : this.listeners) {
            l.scaleChange(this.scaleFactor);
        }
    }

    private void colourChange () {
        for (ConfigChangeListener l : this.listeners) {
            l.colourChange(this.hue, this.saturation, this.brightness);
        }
    }

    private void selectedPointChange () {
        for (ConfigChangeListener l : this.listeners) {
            l.selectedPointChange(this.selectedPoint);
        }
    }
    //endregion

    //region Get/Set Methods

    /**
     * Gets range of axis in x direction
     *
     * @return double, x range
     */
    public double getRangeX () {return this.rangeX;}

    /**
     * Gets range of axis in y direction
     *
     * @return double, y range
     */
    public double getRangeY () {return this.rangeY;}

    /**
     * Adds an item to the fractal selection
     *
     * @param fractal fractal string to add
     */
    public void addFractal (String fractal) {
        this.combo_fractal.addItem(fractal);
    }

    /**
     * Sets the currently selected fractal to the argument
     *
     * @param fractal Fractal to set
     * @return true if fractal was set
     */
    public boolean setFractal (String fractal) {
        boolean hasItem = false;
        int index = 0;
        for (int i = 0; i < this.combo_fractal.getItemCount(); i++) {
            if (this.combo_fractal.getItemAt(i).equals(fractal)) {
                index = i;
                hasItem = true;
                break;
            }
        }
        if (!hasItem) return false;
        this.combo_fractal.setSelectedIndex(index);
        return true;
    }

    /**
     * Gets the currently selected fractal
     *
     * @return string, fractal
     */
    public String getFractal () { return this.fractal; }

    /**
     * Gets the config panel
     *
     * @return JPanel
     */
    public JPanel getConfigPanel () {
        return this.panel_config;
    }

    /**
     * Gets the shift in the x axis
     *
     * @return x shift
     */
    public double getShiftX () {
        return this.xShift;
    }

    /**
     * Sets the x shift
     *
     * @param xShift       New X Shift
     * @param triggerEvent Trigger the xShiftChange event
     */
    public void setShiftX (double xShift, boolean triggerEvent) {
        this.xShift = xShift;
        this.spinner_shiftX.setValue(xShift);
        if (triggerEvent) {
            this.xShiftChange();
        }
    }

    /**
     * Gets the shift in the y axis
     *
     * @return y shift
     */
    public double getShiftY () {
        return this.yShift;
    }

    /**
     * Sets the y shift
     *
     * @param yShift       New y shift
     * @param triggerEvent Trigger the yShiftChange event
     */
    public void setShiftY (double yShift, boolean triggerEvent) {
        this.yShift = yShift;
        this.spinner_shiftY.setValue(yShift);
        if (triggerEvent) {
            this.yShiftChange();
        }
    }

    /**
     * Gets number of iterations
     *
     * @return iterations
     */
    public int getIterations () {
        return this.iterations;
    }

    /**
     * Gets the scale factor
     *
     * @return scale factor
     */
    public double getScaleFactor () {
        return this.scaleFactor;
    }

    /**
     * Sets the scale factor
     *
     * @param scaleFactor  new scale factor
     * @param triggerEvent Trigger the scaleChange event
     */
    public void setScaleFactor (double scaleFactor, boolean triggerEvent) {
        this.scaleFactor = scaleFactor;
        this.spinner_scale.setValue(scaleFactor);
        if (triggerEvent) {
            this.scaleChange();
        }
    }

    /**
     * Gets the image hue
     *
     * @return image hue
     */
    public float getHue () {
        return this.hue;
    }

    /**
     * Gets the image saturation
     *
     * @return image saturation
     */
    public float getSaturation () { return this.saturation; }

    /**
     * Gets the image brightness
     *
     * @return image brightness
     */
    public float getBrightness () { return this.brightness; }

    /**
     * Gets the escape radius squared
     *
     * @return escape radius squared
     */
    public double getEscapeRadiusSquared () {
        return this.escapeRadiusSquared;
    }

    /**
     * Gets the currently selected complex on the Mandelbrot set
     *
     * @return Complex, current selected point
     */
    public Complex getSelectedPoint () {
        if (this.selectedPoint == null) return null;
        return this.selectedPoint.clone();
    }

    /**
     * Sets the currently selected complex on the Mandelbrot set
     *
     * @param complex new selected point
     */
    public void setSelectedPoint (Complex complex) {
        this.selectedPoint = complex;
        this.selectedPointChange();
    }

    /**
     * Display the Julia set when the cursor is moved
     *
     * @return true if when the cursor moves, the julia set should be updated
     */
    public boolean juliaDisplayOnMove () {
        return this.displayJuliaMoveCursor;
    }

    /**
     * Sets whether OpenCL should be used
     *
     * @param useOpenCL
     */
    public void setUseOpenCL (boolean useOpenCL) {
        this.useOpenCL = useOpenCL;
    }

    /**
     * Disables OpenCL until the application is restarted
     */
    public void disableOpenCL () {
        Log.Warning("OpenCL disabled!");
        this.useOpenCL = false;
        this.check_openCL.setSelected(false);
        this.check_openCL.setEnabled(false);
    }

    /**
     * Disables OpenCL doubles until the application is restarted
     */
    public void disableOpenCL_double () {
        Log.Warning("OpenCL doubles disabled!");
        this.useOpenCLDouble = false;
        this.check_openCLDouble.setSelected(false);
        this.check_openCLDouble.setEnabled(false);
    }

    /**
     * Returns whether OpenCL should be used or not
     *
     * @return true if openCL should be used
     */
    public boolean useOpenCL () {
        return this.useOpenCL;
    }

    /**
     * Returns whether OpenCL Doubles should be used or not
     *
     * @return true if OpenCL Doubles should be used
     */
    public boolean useOpenCL_double () {
        return this.useOpenCLDouble;
    }

    /**
     * Returns true if the cache is disabled
     *
     * @return boolean
     */
    public boolean isCacheDisabled () {
        return this.isCacheDisabled;
    }

    /**
     * Returns true if the zoom is to be animated
     *
     * @return boolean
     */
    public boolean isAnimateZoom () {return this.zoomAnimationEnabled; }
    //endregion

    //region Event Handlers

    /**
     * Invoked when selected fractal changes
     */
    private class fractalHandler implements ActionListener {

        @Override
        public void actionPerformed (ActionEvent e) {
            ConfigManager.this.fractal = (String) ConfigManager.this.combo_fractal.getSelectedItem();
            ConfigManager.this.fractalChanged();
        }
    }

    /**
     * Invoked when btn_render is activated
     */
    private class renderHandler implements ActionListener {

        @Override
        public void actionPerformed (ActionEvent e) {
            ConfigManager.this.mainWindow.renderMainPanel();
            ConfigManager.this.mainWindow.renderJulia();
        }
    }

    /**
     * Invoked when btn_reset is activated
     */
    private class resetHandler implements ActionListener {

        @Override
        public void actionPerformed (ActionEvent e) {
            // Set the values back to defaults
            ConfigManager.this.spinner_iterations.setValue(100);
            ConfigManager.this.spinner_scale.setValue(1.0);
            ConfigManager.this.spinner_shiftX.setValue(0.0);
            ConfigManager.this.spinner_shiftY.setValue(0.0);
            ConfigManager.this.slider_hue.setValue(0);
            ConfigManager.this.slider_saturation.setValue(100);
            ConfigManager.this.slider_brightness.setValue(100);

            ConfigManager.this.mainWindow.renderMainPanel();
            ConfigManager.this.mainWindow.renderJulia();
        }
    }

    /**
     * Invoked when a spinner is changed
     */
    private class optionChangeHandler implements ChangeListener {

        @Override
        public void stateChanged (ChangeEvent e) {
            if (!( e.getSource() instanceof JSpinner )) return;
            JSpinner spinner = (JSpinner) e.getSource();

            if (spinner == ConfigManager.this.spinner_iterations) {
                if (ConfigManager.this.iterations == (int) ConfigManager.this.spinner_iterations.getValue()) return;
                ConfigManager.this.iterations = (int) ConfigManager.this.spinner_iterations.getValue();
                ConfigManager.this.iterationChange();
            } else if (spinner == ConfigManager.this.spinner_scale) {
                if (ConfigManager.this.scaleFactor == (double) ConfigManager.this.spinner_scale.getValue()) return;
                ConfigManager.this.scaleFactor = (double) ConfigManager.this.spinner_scale.getValue();
                ConfigManager.this.scaleChange();
            } else if (spinner == ConfigManager.this.spinner_shiftX) {
                if (ConfigManager.this.xShift == (double) ConfigManager.this.spinner_shiftX.getValue()) return;
                ConfigManager.this.xShift = (double) ConfigManager.this.spinner_shiftX.getValue();
                ConfigManager.this.xShiftChange();
            } else if (spinner == ConfigManager.this.spinner_shiftY) {
                if (ConfigManager.this.yShift == (double) ConfigManager.this.spinner_shiftY.getValue()) return;
                ConfigManager.this.yShift = (double) ConfigManager.this.spinner_shiftY.getValue();
                ConfigManager.this.yShiftChange();
            } else if (spinner == ConfigManager.this.spinner_escapeRadius) {


                double escapeRadius = (double) ConfigManager.this.spinner_escapeRadius.getValue();
                if (ConfigManager.this.escapeRadiusSquared == escapeRadius * escapeRadius) return;
                ConfigManager.this.escapeRadiusSquared = escapeRadius * escapeRadius;
                ConfigManager.this.escapeRadiusChanged();
            }
        }
    }

    /**
     * Invoked when a slider is changed
     */
    private class colourShiftChangeHandler extends AdvancedChangeAdapter {
        @Override
        public void changeFinish (ChangeEvent e) {
            ConfigManager.this.hue = (float) ConfigManager.this.slider_hue.getValue() / 360f;
            ConfigManager.this.saturation = (float) ConfigManager.this.slider_saturation.getValue() / 100f;
            ConfigManager.this.brightness = (float) ConfigManager.this.slider_brightness.getValue() / 100f;
            ConfigManager.this.colourChange();
        }
    }

    private class checkChangeHanlder implements ChangeListener {

        @Override
        public void stateChanged (ChangeEvent e) {
            if (!( e.getSource() instanceof JCheckBox )) return;
            JCheckBox check = (JCheckBox) e.getSource();
            if (check == ConfigManager.this.check_openCL) {
                ConfigManager.this.useOpenCL = check.isSelected();
            } else if (check == ConfigManager.this.check_openCLDouble) {
                ConfigManager.this.useOpenCLDouble = check.isSelected();
            } else if (check == ConfigManager.this.check_disableCache) {
                ConfigManager.this.isCacheDisabled = check.isSelected();
            } else if (check == ConfigManager.this.check_juliaCursor) {
                ConfigManager.this.displayJuliaMoveCursor = check.isSelected();
            } else if (check == ConfigManager.this.check_zoomAnimation) {
                ConfigManager.this.zoomAnimationEnabled = check.isSelected();
            }
        }
    }


    //endregion
}

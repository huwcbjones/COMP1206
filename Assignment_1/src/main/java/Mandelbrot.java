import mandelbrot.Main;

import javax.swing.*;

/**
 * Bootstrapper for application
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class Mandelbrot {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Main gui = new Main();
            }
        });
    }

}

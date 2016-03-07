package mandelbrot.management;

import junit.framework.TestCase;
import mandelbrot.ConfigManager;
import mandelbrot.Main;
import mandelbrot.events.RenderListener;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 06/03/2016
 */
public class MandelbrotRenderManagementThreadTest{

    static long endTime;
    static Main main;

    public static void main(String[] args){

        main = new Main();
        main.setSize(1024, 768);
        ConfigManager config = main.getConfigManager();
       // config.disableOpenCL();
        MandelbrotRenderManagementThread t = main.mandelbrotRenderer;
        t.addRenderListener(new renderHandler());
        t.numberThreads = Runtime.getRuntime().availableProcessors() * 2;
        synchronized (main){
            try {
                main.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long startTime;
        for(int i = 1; i < t.imgWidth; i ++) {
            t.numberStrips = i;
            System.out.print(t.numberStrips);
            for(int j = 0; j < 50; j++){
                startTime = System.nanoTime();
                t.render();
                synchronized (main){
                    try {
                        main.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.print("," + (endTime - startTime));
                }
            }
            System.out.print("\n");
        }

        main.dispose();
    }

    private static class renderHandler implements RenderListener {

        @Override
        public void renderComplete() {
            synchronized (main){
                endTime = System.nanoTime();
                main.notify();
            }
        }
    }


}
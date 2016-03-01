package mandelbrot.management;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static com.nativelibs4java.opencl.JavaCL.createBestContext;

/**
 * Render's images using OpenCL bindings
 *
 * @author Huw Jones
 * @since 01/03/2016
 */
public class OpenClRenderThread extends Thread {
    private boolean isAvailable = true;
    private CLContext context;
    private CLQueue queue;

    private HashMap<String, CLProgram> programs;

    public OpenClRenderThread(){
        super("OpenCL_Render_Thread");
        init();
        programs = new HashMap<>();
    }

    private void init(){
        try {
            context = createBestContext();
            queue = context.createDefaultQueue();

            CLPlatform plat = context.getPlatform();
            System.out.println("********************************************************************************");
            System.out.println("* OPEN CL SUPPORT                                                              *");
            System.out.println("********************************************************************************");
            System.out.println("* Vendor: " + plat.getBestDevice().getVendor());
            System.out.println("* Name: " + plat.getName());
            System.out.println("* Device: " + plat.getBestDevice().getName());
            System.out.println("* Version: " + plat.getBestDevice().getVersion());
            System.out.println("* Driver Version: " + plat.getBestDevice().getDriverVersion());
            System.out.println("* OpenCL Version: " + plat.getBestDevice().getOpenCLCVersion());
            System.out.println("* Compute Units: " + plat.getBestDevice().getMaxComputeUnits());
            System.out.println("* Samplers: " + plat.getBestDevice().getMaxSamplers());
            System.out.println("* Work Group: " + plat.getBestDevice().getMaxWorkGroupSize());
            System.out.println("* Address Bits: " + plat.getBestDevice().getAddressBits());
            System.out.println("* GPU Memory: " + (plat.getBestDevice().getGlobalMemSize() / 1024 / 1024) + "MB");
            System.out.println("* Profile: " + plat.getBestDevice().getProfile());
            System.out.print("* Extensions: \n");
            for (String s : plat.getExtensions()) {
                System.out.print("*\t- " + s + "\n");
            }
            System.out.println("********************************************************************************");
        } catch (Exception ex) {
            isAvailable = false;
            System.err.println("OpenCL support unavailable.");
            System.err.println(ex.getCause().toString());
        }
    }

    public boolean loadProgram(String programName, String source){
        try {
            CLProgram program = context.createProgram(source);
            programs.put(programName, program);
        } catch (Exception ex){
            return false;
        }
        return true;
    }

    public boolean loadProgram(String programName, InputStream stream){
        try {
            String source = IOUtils.readText(stream);
            return loadProgram(programName, source);
        } catch (IOException ex){
            return false;
        }
    }

    public CLProgram getProgram(String programName){
        return programs.get(programName);
    }

    public CLQueue getQueue() {
        return queue;
    }
    public CLContext getContext() {
        return context;
    }
    /**
     * Gets a boolean representing whether OpenCL rendering is available
     * @return True if OpenCL is available.
     */
    public boolean isAvailable() {
        return isAvailable;
    }

}

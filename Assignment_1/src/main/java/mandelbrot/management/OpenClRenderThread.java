package mandelbrot.management;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.util.IOUtils;
import utils.Log;

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
        Log.Information("Loading OpenCL Render Thread...");
        init();
        programs = new HashMap<>();
    }

    private void init(){
        try {
            context = createBestContext();
            queue = context.createDefaultQueue();

            CLPlatform plat = context.getPlatform();
            Log.Information("********************************************************************************");
            Log.Information("* OPEN CL SUPPORT                                                              *");
            Log.Information("********************************************************************************");
            Log.Information("* Vendor:\t\t\t" + plat.getBestDevice().getVendor());
            Log.Information("* Name:\t\t\t\t" + plat.getName());
            Log.Information("* Device:\t\t\t" + plat.getBestDevice().getName());
            Log.Information("* Version:\t\t\t" + plat.getBestDevice().getVersion());
            Log.Information("* Driver Version:\t" + plat.getBestDevice().getDriverVersion());
            Log.Information("* OpenCL Version:\t" + plat.getBestDevice().getOpenCLCVersion());
            Log.Information("* Compute Units:\t" + plat.getBestDevice().getMaxComputeUnits());
            Log.Information("* Samplers:\t\t\t" + plat.getBestDevice().getMaxSamplers());
            Log.Information("* Work Group:\t\t" + plat.getBestDevice().getMaxWorkGroupSize());
            Log.Information("* Address Bits:\t\t" + plat.getBestDevice().getAddressBits());
            Log.Information("* GPU Memory:\t\t" + (plat.getBestDevice().getGlobalMemSize() / 1024 / 1024) + "MB");
            Log.Information("* Profile:\t\t\t" + plat.getBestDevice().getProfile());
            Log.Information("* Extensions:");
            for (String s : plat.getExtensions()) {
                Log.Information("*\t- " + s);
            }
            Log.Information("********************************************************************************");
            Log.Warning("OpenCL support available!");
        } catch (Exception ex) {
            isAvailable = false;
            Log.Warning("OpenCL support unavailable.");
            Log.Warning(ex.getCause().toString());
        }
    }

    public boolean loadProgram(String programName, String source){
        try {
            CLProgram program = context.createProgram(source);
            Log.Information("Loaded OpenCL program, " + programName + ".");
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

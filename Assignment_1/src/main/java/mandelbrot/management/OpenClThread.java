package mandelbrot.management;

import com.nativelibs4java.opencl.*;
import com.nativelibs4java.util.IOUtils;
import utils.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.nativelibs4java.opencl.JavaCL.createBestContext;

/**
 * Render's images using OpenCL bindings
 *
 * @author Huw Jones
 * @since 01/03/2016
 */
public class OpenClThread extends Thread {
    private boolean isAvailable = true;
    private boolean useDouble = false;
    private CLContext context;
    private CLQueue queue;

    private HashMap<String, CLProgram> programs;

    public OpenClThread(){
        super("OpenCL_Render_Thread");
        Log.Information("Loading OpenCL Render Thread...");
        init();
        programs = new HashMap<>();
    }

    private void init(){
        try {
            Log.Information("********************************************************************************");
            Log.Information("* OPEN CL SUPPORT                                                              *");
            Log.Information("********************************************************************************");
            Log.Information("* AVAILABLE PLATFORMS                                                          *");
            Log.Information("********************************************************************************");

            // List available platforms
            // If multiple platforms, could load balance the work across them (for another day, or I need another GPU)
            CLPlatform[] platforms = JavaCL.listPlatforms();
            CLDevice[] devices;
            for(CLPlatform platform: platforms){
                devices = platform.listAllDevices(false);
                for(CLDevice device : devices){
                    Log.Information("*");
                    Log.Information("* Vendor:\t\t\t" + device.getVendor());
                    Log.Information("* Name:\t\t\t\t" + device.getName());
                }
            }
            Log.Information("*");

            // Create best context, then get the queue
            context = createBestContext();
            queue = context.createDefaultQueue();

            CLPlatform plat = context.getPlatform();
            useDouble = plat.getBestDevice().isDoubleSupported();

            // Print debug info
            Log.Information("********************************************************************************");
            Log.Information("* SELECTED PLATFORM                                                            *");
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
            Log.Information("* Double Support:\t" + (useDouble ? "Enabled" : "Disabled"));
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

    /**
     * Loads an OpenCL program to memory.
     * @param programName Program Name
     * @param source Source Code for program
     * @return true if program was loaded successfully
     */
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

    /**
     * Loads an OpenCL program to memory.
     * @param programName Program Name
     * @param stream Input Stream for program
     * @return true if program was loaded successfully
     */
    public boolean loadProgram(String programName, InputStream stream){
        try {
            String source = IOUtils.readText(stream);
            return loadProgram(programName, source);
        } catch (IOException ex){
            return false;
        }
    }

    /**
     * Returns the OpenCL with name
     * @param programName Name of program to return
     * @return The program.
     */
    public CLProgram getProgram(String programName){
        return programs.get(programName);
    }

    /**
     * Gets the current CLQueue
     * @return CL Queue
     */
    public CLQueue getQueue() {
        return queue;
    }

    /**
     * Gets the current context
     * @return CL Context
     */
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

    /**
     * Returns true if the OpenCL device supports doubles
     *
     * @return boolean
     */
    public boolean useDouble() {
        return useDouble;
    }

}

import com.nativelibs4java.opencl.*;
import org.bridj.Pointer;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.nativelibs4java.opencl.JavaCL.createBestContext;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 06/03/2016
 */
public class HotspotCrash {

    public static void main(String[] args) {
        CLContext context = createBestContext();
        CLQueue queue = context.createDefaultQueue();
        CLProgram program = context.createProgram(
            "__kernel void mandelbrot(\n"+
                "const float2 dimensions,\n"+
                "const float2 scale,\n"+
                "__global int* outputi\n"+
            ") {\n"+
                "float2 coords = convert_float2((int2)(get_global_id(0), get_global_id(1)));\n"+
                "int pixelID = convert_int(coords.y * dimensions.x + coords.x);\n"+
                "float2 complex = ((coords - dimensions / 2.0f) * scale) / 1;\n"+
                "float2 z_square = complex * complex;\n"+
                "float2 z = complex;\n"+
                "int iterationNum = 0;\n"+
                "while ( (z_square.x + z_square.y <= 4) && (iterationNum < 100)) {\n"+
                    "z.y = z.x * z.y;\n"+
                    "z.y += z.y;\n"+
                    "z.x = z_square.x - z_square.y;\n"+
                    "z += complex;\n"+
                    "z_square = z * z;\n"+
                    "iterationNum++;\n"+
                "}\n"+

                "int colour = 0xFF000000;\n"+

                "if(iterationNum < 100 ) {\n"+
                    "colour = 0xFFFFFFFF;\n" +
                "}\n"+
                "outputi[pixelID] = colour;\n" +
            "}\n"
        );
        double width = 3840;
        double height = 2160;
        double aspectRatio = width / height;
        double xRange = 4.0;
        double yRange = 2.6;

        if (aspectRatio * yRange < 4) {
            yRange = xRange / aspectRatio;
        } else {
            xRange = yRange * aspectRatio;
        }

        BufferedImage image = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_RGB);
        int[] pixels;
        Pointer<Integer> results = Pointer.allocateInts((int)width * (int)height);
        CLBuffer<Integer> resultsBuffer = context.createIntBuffer(CLMem.Usage.Output, results, false);
        float[] scale = new float[]{(float)(xRange / width), (float)(yRange / height)};
        float[] dimensions = new float[]{(float)width, (float)height};
        while (true) {
            System.out.println("starting");
            CLKernel kernel = program.createKernel(
                    "mandelbrot",
                    dimensions,
                    scale,
                    resultsBuffer
            );

            kernel.enqueueNDRange(queue, new int[]{(int)width, (int)height}, new int[]{1, 1});
            queue.finish();
            System.out.println("finished");

            results = resultsBuffer.read(queue);
            pixels = results.getInts();
            image.setRGB(0, 0, (int)width, (int)height, pixels, 0, (int)width);
            File outputfile = new File("mandelbrot.png");
        }
    }
}

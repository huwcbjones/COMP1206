package shared.utils;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Utility Class for converting UUID to other formats and back
 *
 * @author Huw Jones
 * @since 10/04/2016
 */
public class UUIDUtils {

    public static UUID BytesToUUID(byte[] bytes){
        if(bytes.length != 16){
            return null;
        }
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();

        long msb = buffer.getLong(0);
        long lsb = buffer.getLong(8);

        return new UUID(msb, lsb);
    }

    public static byte[] UUIDToBytes(UUID uuid){
        if(uuid == null){
            return new byte[0];
        }
        ByteBuffer buffer = ByteBuffer.allocate(16);
        return buffer.putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).array();
    }

    public static String UUIDToBase64String(UUID uuid){
        return Base64.encode(UUIDToBytes(uuid));
    }

}

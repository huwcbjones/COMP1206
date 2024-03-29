package shared.utils;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 11/04/2016
 */
public class UUIDUtilsTest {
    @Test
    public void UUIDConversionTest() throws Exception {
        UUID uuid = UUID.randomUUID();
        byte[] uuid_bytes = UUIDUtils.UUIDToBytes(uuid);
        assertEquals(uuid, UUIDUtils.BytesToUUID(uuid_bytes));
    }

    @Test
    public void base64Check() throws Exception{
        UUID uuid = UUID.randomUUID();
        System.out.println(uuid);
        System.out.println(UUIDUtils.UUIDToBase64String(uuid));
        System.out.println(UUIDUtils.UUIDToBase64String(uuid).length());
    }
}
package shared;

import org.junit.Test;
import shared.utils.UUIDUtils;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 10/04/2016
 */
public class UserTest {
    @Test
    public void getUniqueByteID() throws Exception {
        UUID id = UUID.randomUUID();
        byte[] bytes = UUIDUtils.UUIDToBytes(id);

        assertEquals(id, UUIDUtils.BytesToUUID(bytes));
    }

}
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import java.nio.ByteBuffer;

public class ByteTest {
    private static final int BYTE_SIZE_BOOL = 1;
    private static final int BYTE_SIZE_BYTE = 1;
    private static final int BYTE_SIZE_CHAR = 2;
    private static final int BYTE_SIZE_SHORT = 2;
    private static final int BYTE_SIZE_INT = 4;
    private static final int BYTE_SIZE_FLOAT = 4;
    private static final int BYTE_SIZE_LONG = 8;
    private static final int BYTE_SIZE_DOUBLE = 8;

    @Test
    public void conversionOfLong() {
        final long testValue = 12345L;

        ByteBuffer buffer = ByteBuffer.allocate(BYTE_SIZE_LONG);
        buffer.putLong(testValue);
        byte[] bytes = buffer.array();

        assertThat(byteArrayToHex(bytes), is("00 00 00 00 00 00 30 39 "));

        // 변환한 byteArray를 다시 double 값으로 변환
        assertThat(ByteBuffer.wrap(bytes).getLong(), is(testValue));
    }
    @Test
    public void conversionOfDouble() {
        final double testValue = 12345.0;

        ByteBuffer buffer = ByteBuffer.allocate(BYTE_SIZE_DOUBLE);
        buffer.putDouble(testValue);
        byte[] bytes = buffer.array();

        assertThat(byteArrayToHex(bytes), is("40 c8 1c 80 00 00 00 00 "));

        // 변환한 byteArray를 다시 double 값으로 변환
        assertThat(ByteBuffer.wrap(bytes).getDouble(), is(testValue));
    }
    @Test
    public void conversionOfInt() {
        final int testValue = 12345;

        ByteBuffer buffer = ByteBuffer.allocate(BYTE_SIZE_INT);
        buffer.putInt(testValue);

        byte[] bytes = buffer.array();
        assertThat(byteArrayToHex(bytes), is("00 00 30 39 "));

        // 변환한 byteArray를 다시 int 값으로 변환
        assertThat(ByteBuffer.wrap(bytes).getInt(), is(testValue));
    }
    @Test
    public void conversionOfShort() {
        final short testValue = (short) 12345;

        ByteBuffer buffer = ByteBuffer.allocate(BYTE_SIZE_SHORT);
        buffer.putShort(testValue);

        byte[] bytes = buffer.array();
        assertThat(byteArrayToHex(bytes), is("30 39 "));

        // 변환한 byteArray를 다시 int 값으로 변환
        assertThat(ByteBuffer.wrap(bytes).getShort(), is(testValue));
    }
    private String byteArrayToHex(byte[] bytes)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte currentByte : bytes)
        {
            stringBuilder.append(String.format("%02x ", currentByte&0xff));
        }
        return stringBuilder.toString();
    }
}

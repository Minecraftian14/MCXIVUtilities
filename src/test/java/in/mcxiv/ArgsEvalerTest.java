package in.mcxiv;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

class ArgsEvalerTest {

    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
    }

    @Test
    void testArgsEvaler() {
        ArgsEvaler parser = new ArgsEvaler.ArgsEvalerBuilder()
                .addIndexed("name_a")
                .addIndexed("name_b")
                .addIndexed("some_int_a", int.class)
                .addIndexed("some_float_a", float.class)
                .addIndexed("big_int", BigInteger.class)
                .addNamed("value")
                .addNamed("path", File.class)
                .addNamed("bytes", ByteBuffer.class)
                .addTagged("--tag")
                .addResolver(ByteBuffer.class, (c, s) -> ByteBuffer.wrap(s.getBytes()))
                .build();

        HashMap<String, Object> map = parser.parse("path=this/lol", "hello", "world", "bytes=someBytes", "135", "--tag", "a tagged value", "67.0", "123456787654321", "value=something");

        Assertions.assertEquals("hello", map.get("name_a"));
        Assertions.assertEquals("world", map.get("name_b"));
        Assertions.assertEquals(135, map.get("some_int_a"));
        Assertions.assertEquals(67f, map.get("some_float_a"));
        Assertions.assertEquals(new BigInteger("123456787654321"), map.get("big_int"));

        Assertions.assertEquals("something", map.get("value"));
        Assertions.assertEquals(new File("this/lol"), map.get("path"));
        Assertions.assertTrue(map.get("bytes") instanceof ByteBuffer);
        Assertions.assertEquals(9, ((ByteBuffer) map.get("bytes")).array().length);

        Assertions.assertEquals("a tagged value", map.get("--tag"));

        System.out.println(map);

        // Just curious
        System.out.println(((File) map.get("path")).getAbsolutePath());

        ArgsEvaler.ResultMap resultMap = parser.parseToResultMap("ooof", "ooof", "1114");
        int name = resultMap.get("some_int_a");
        Assertions.assertEquals(1114, name);
    }
}
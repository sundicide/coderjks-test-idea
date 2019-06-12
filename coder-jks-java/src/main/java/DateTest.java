import org.junit.Test;

import java.util.Date;

public class DateTest {

    @Test
    public void newDateTest()
    {
        System.out.println(new Date().getTime());
        System.out.println((double)(new Date().getTime()));
    }
}

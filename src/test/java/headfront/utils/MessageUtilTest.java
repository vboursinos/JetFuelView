package headfront.utils;

import static org.junit.Assert.assertEquals;

/**
 * Created by Deepak on 06/04/2016.
 */
public class MessageUtilTest {
    @org.junit.Test
    public void removeHtml() throws Exception {
        String input = "<html>\n      LDN-BACKUP</html>\n";
        String output = MessageUtil.removeHtml(input);
        assertEquals("LDN-BACKUP", output);

    }

}
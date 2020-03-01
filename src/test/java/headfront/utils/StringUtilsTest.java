package headfront.utils;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;

/**
 * Created by Deepak on 19/08/2016.
 */
public class StringUtilsTest {

    @Test
    public void getConfigUrl() throws Exception {
        String[] input = {"tcp://192.168.56.101:8001/amps/json", "192.168.56.101:8001/amps/json"}; //, "tcp://userName:Password@192.168.56.101:8001/amps/json"};
        String output = "http://192.168.56.101:8085/amps/instance/config.xml";

        for (int i = 0; i < input.length; i++) {
            String adminUrl = StringUtils.getConfigUrl(input[i], "8085", false);
            assertEquals(output, adminUrl);
        }

    }

    @Test
    public void getConfigUrlSecure() throws Exception {
        String[] input = {"tcp://192.168.56.101:8001/amps/json", "192.168.56.101:8001/amps/json"}; //, "tcp://userName:Password@192.168.56.101:8001/amps/json"};
        String output = "https://192.168.56.101:8085/amps/instance/config.xml";

        for (int i = 0; i < input.length; i++) {
            String adminUrl = StringUtils.getConfigUrl(input[i], "8085", true);
            assertEquals(output, adminUrl);
        }

    }

    @Test
    public void removePasswordsFromUrl() throws Exception {
        String[] input = {"http://192.168.56.101:8085/amps/instance/config.xml",
                "http://this:test@192.168.56.101:8085/amps/instance/config.xml"};
        String output = "http://192.168.56.101:8085/amps/instance/config.xml";

        for (int i = 0; i < input.length; i++) {
            String adminUrl = StringUtils.removePasswordsFromUrl(input[i]);
            assertEquals(output, adminUrl);
        }

    }

    @Test
    public void getServerAndPortFromUrl() throws Exception {
        String[] input = {"http://192.168.56.101:8001/amps/instance/config.xml",
                "http://this:test@192.168.56.101:8001/amps/instance/config.xml",
                "https://this:test@192.168.56.101:8001/amps.json",
                "tcp://192.168.56.101:8001/amps/json",
                "192.168.56.101:8001/amps/json"};
        String output = "192.168.56.101:8001";

        for (int i = 0; i < input.length; i++) {
            String adminUrl = StringUtils.getServerAndPortFromUrl(input[i]);
            assertEquals(output, adminUrl);
        }
    }

    @Test
    public void getServerFromUrl() throws Exception {
        String[] input = {"http://192.168.56.101:8001/amps/instance/config.xml",
                "http://this:test@192.168.56.101:8001/amps/instance/config.xml",
                "https://this:test@192.168.56.101:8001/amps.json",
                "tcp://192.168.56.101:8001/amps/json",
                "192.168.56.101:8001/amps/json"};
        String output = "192.168.56.101";

        for (int i = 0; i < input.length; i++) {
            String adminUrl = StringUtils.getServerFromUrl(input[i]);
            assertEquals(output, adminUrl);
        }
    }

    @Test
    public void getShortServerAndPortFromUrl() throws Exception {
        String[] input = {"http://192.168.56.101:8001/amps/instance/config.xml",
                "http://this:test@192.168.56.101:8001/amps/instance/config.xml",
                "https://this:test@192.168.56.101:8001/amps.json",
                "tcp://192.168.56.101:8001/amps/json",
                "192.168.56.101:8001/amps/json"};
        String output = "192.168.56.101:8001";

        for (int i = 0; i < input.length; i++) {
            String adminUrl = StringUtils.getShortServerAndPortFromUrl(input[i]);
            assertEquals(output, adminUrl);
        }
    }

    @Test
    public void getServerFromHostnameUrl() throws Exception {
        String[] input = {"http://london.google.co.uk:8001/amps/instance/config.xml",
                "http://this:test@london.google.co.uk:8001/amps/instance/config.xml",
                "https://this:test@london.google.co.uk:8001/amps.json",
                "tcp://london.bbc.co.uk:8001/amps/json",
                "london.google.co.uk:8001/amps/json"};
        String output = "london";

        for (int i = 0; i < input.length; i++) {
            String adminUrl = StringUtils.getServerFromUrl(input[i]);
            assertEquals(output, adminUrl);
        }
    }

    @Test
    public void getShortServerAndPortFromHostnameUrl() throws Exception {
        String[] input = {"http://london.google.co.uk:8001/amps/instance/config.xml",
                "http://this:test@london.google.co.uk:8001/amps/instance/config.xml",
                "https://this:test@london.google.co.uk:8001/amps.json",
                "tcp://london.bbc.co.uk:8001/amps/json",
                "london.google.co.uk:8001/amps/json"};
        String output = "london:8001";

        for (int i = 0; i < input.length; i++) {
            String adminUrl = StringUtils.getShortServerAndPortFromUrl(input[i]);
            assertEquals(output, adminUrl);
        }
    }

// This test is invalid !!!
    @Test
    public void getAmpsJsonConnectionString() throws Exception {
        String[] input = {"http://london.google.co.uk:8001/amps/instance/config.xml",
                "http://this:test@london.google.co.uk:8001/amps/instance/config.xml",
                "https://this:test@london.google.co.uk:8001/amps.json",
                "tcp://london.google.co.uk:8001/amps/json"};
        String[] output ={ "http://deepak:DOne123@london.google.co.uk:8999/amps/json",
                "http://deepak:DOne123@london.google.co.uk:8999/amps/json",
                "https://deepak:DOne123@london.google.co.uk:8999/amps/json",
                "tcp://deepak:DOne123@london.google.co.uk:8999/amps/json"};

        for (int i = 0; i < input.length; i++) {
            String adminUrl = StringUtils.getAmpsJsonConnectionString(input[i], "deepak", "DOne123", "8999");
            assertEquals(output[i], adminUrl);
        }
    }

    // This test is invalid!!!!
    @Test
    public void getAmpsJsonConnectionStringNullCredential() throws Exception {
        String[] input = {"http://london.google.co.uk:8001/amps/instance/config.xml",
                "http://this:test@london.google.co.uk:8001/amps/instance/config.xml",
                "https://this:test@london.google.co.uk:8001/amps.json",
                "tcp://london.google.co.uk:8001/amps/json",
                "tcps://london.google.co.uk:8001/amps/json"};
        String[] output ={ "http://london.google.co.uk:8999/amps/json",
                "http://london.google.co.uk:8999/amps/json",
                "https://london.google.co.uk:8999/amps/json",
                "tcp://london.google.co.uk:8999/amps/json",
                "tcps://london.google.co.uk:8999/amps/json"};

        for (int i = 0; i < input.length; i++) {
            String adminUrl = StringUtils.getAmpsJsonConnectionString(input[i], null, null, "8999");
            assertEquals(output[i], adminUrl);
        }
    }

    @Test
    public void getAmpsJsonConnectionStringIP() throws Exception {
        String[] input = {"http://192.168.56.101:8001/amps/instance/config.xml",
                "http://this:test@192.168.56.101:8001/amps/instance/config.xml",
                "https://this:test@192.168.56.101:8001/amps.json",
                "tcp://192.168.56.101:8001/amps/json"};
        String[] output = { "http://deepak:DOne123@192.168.56.101:8999/amps/json",
                "http://deepak:DOne123@192.168.56.101:8999/amps/json",
            "https://deepak:DOne123@192.168.56.101:8999/amps/json",
                    "tcp://deepak:DOne123@192.168.56.101:8999/amps/json"};


        for (int i = 0; i < input.length; i++) {
            String adminUrl = StringUtils.getAmpsJsonConnectionString(input[i], "deepak", "DOne123", "8999");
            assertEquals(output[i], adminUrl);
        }
    }

    @Test
    public void getAmpsJsonConnectionStringNullCredentialIP() throws Exception {
        String[] input = {"http://192.168.56.101:8001/amps/instance/config.xml",
                "http://this:test@192.168.56.101:8001/amps/instance/config.xml",
                "https://this:test@192.168.56.101:8001/amps.json",
                "tcp://192.168.56.101:8001/amps/json",
                "tcps://192.168.56.101:8001/amps/json"};
        String[] output = {"http://192.168.56.101:8999/amps/json",
                "http://192.168.56.101:8999/amps/json",
                "https://192.168.56.101:8999/amps/json",
                "tcp://192.168.56.101:8999/amps/json",
                "tcps://192.168.56.101:8999/amps/json"};

        for (int i = 0; i < input.length; i++) {
            String adminUrl = StringUtils.getAmpsJsonConnectionString(input[i], null, null, "8999");
            assertEquals(output[i], adminUrl);
        }
    }

    @Test
    public void getAmpsJsonConnectionStringWithNoCredentials() throws Exception {
        String[] input = {"tcp://192.168.56.101:8001/amps/json"};
        String output = "tcp://192.168.56.101:8001/amps/json";

        for (int i = 0; i < input.length; i++) {
            String adminUrl = StringUtils.getAmpsJsonConnectionStringWithCredentials(input[i], null, null);
            assertEquals(output, adminUrl);
        }
    }

    @Test
    public void getAmpsJsonConnectionStringWithCredentials() throws Exception {
        String[] input = {"tcp://192.168.56.101:8001/amps/json"};
        String output = "tcp://Deepak:newSarah@192.168.56.101:8001/amps/json";

        for (int i = 0; i < input.length; i++) {
            String adminUrl = StringUtils.getAmpsJsonConnectionStringWithCredentials(input[i], "Deepak", "newSarah");
            assertEquals(output, adminUrl);
        }
    }


    @Test
    public void getAmpsUrlWithCredential() throws Exception {
        String[] input = {"tcp://192.168.56.101:8001/amps/json"};
        String output = "http://holiday:beach@192.168.56.101:8085/amps.json";

        for (int i = 0; i < input.length; i++) {
            String adminUrl = StringUtils.getAmpsAdminUrlWithCredential(input[i], "8085", "holiday", "beach", false);
            assertEquals(output, adminUrl);
        }

    }

    @Test
    public void getAmpsUrlWithCredentialSecure() throws Exception {
        String[] input = {"tcp://192.168.56.101:8001/amps/json"};
        String output = "https://holiday:beach@192.168.56.101:8085/amps.json";

        for (int i = 0; i < input.length; i++) {
            String adminUrl = StringUtils.getAmpsAdminUrlWithCredential(input[i], "8085", "holiday", "beach", true);
            assertEquals(output, adminUrl);
        }

    }

    @Test
    public void getFieldNamel() throws Exception {
        String[] input = {"[ORDERS_TOPIC]./Tick", "[ORDERS_TOPIC]./oldTick AS /Tick", "/Tick"};
        String output = "/Tick";
        for (int i = 0; i < input.length; i++) {
            String out = StringUtils.getFieldName(input[i]);
            assertEquals(output, out);
        }
    }

    @Test
    public void testInvalidCharRemovePassword() {
        String[] input = {"Deepak \n test", "Deepak \n :nan", "Deepak \n test\n", "\nDeepak  test"};
        String[] output = {"Deepak  test", "Deepak  :null", "Deepak  test", "Deepak  test"};
        for (int i = 0; i < input.length; i++) {
            String fixed = StringUtils.removeInvalidCharFromJson(input[i]);
            assertEquals(output[i], fixed);
        }
    }


    @Test
    public void testRemovePassword() {
        String[] input = {"tcp://192.168.56.101:8001/amps/json", "tcp://userName:Password@192.168.56.101:8001/amps/json",
                "{haClient.timeout=10000, client.username=null, client.uri=tcp://userName:Password@192.168.56.101:8001/amps/json, client.name=AmpsClient_Deepak}"};
        String[] output = {"tcp://192.168.56.101:8001/amps/json", "tcp://userName:*****@192.168.56.101:8001/amps/json",
                "{haClient.timeout=10000, client.username=null, client.uri=tcp://userName:*****@192.168.56.101:8001/amps/json, client.name=AmpsClient_Deepak}"};

        for (int i = 0; i < input.length; i++) {
            String removedPassword = StringUtils.removePassword(input[i]);
            assertEquals(output[i], removedPassword);
        }
    }

    @Test
    public void testNumberFormat() {
        Number[] input = {123123.099, 123, 1234, 123456, 1234567, 1231231231};
        String[] output = {"123,123", "123", "1,234", "123,456", "1,234,567", "1,231,231,231"};

        for (int i = 0; i < input.length; i++) {
            String result = StringUtils.formatNumber(input[i]);
            assertEquals(output[i], result);
        }
    }

    @Test
    public void testPad() {
        String paddedString = StringUtils.getPaddedString("3", 10, "0");
        assertEquals("0000000003", paddedString);
        paddedString = StringUtils.getPaddedString("3a2", 10, "0");
        assertEquals("00000003a2", paddedString);
        paddedString = StringUtils.getPaddedString("100", 10, "-");
        assertEquals("-------100", paddedString);
    }

    @Test
    public void testTimeFormat() {
        DateTimeFormatter outPutDateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
// = new DateTimeFormatterBuilder()
//                .parseCaseInsensitive()
//                .append(ISO_LOCAL_DATE_TIME)
//                .appendOffsetId()
//                .toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);

//        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnnZ");

//        String formatedDateTime = outPutDateFormatter.format(LocalDateTime.now());
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnnnnn")));

    }


    @Test
    public void testAnonymiseData() {
        String[] input = {"{\"ID\":\"DE0001754\",\"region\":\"AB\",\"message\":\"Calculated using Benchmark\",\"BID\":10,\"OFFER\":9449.652} ",
                "{\"ID\":\"Deepak3\",\"Tick\":\"IBM\",\"BID\":10,\"OFFER\":99.652,\"Shares\":4}"};
        String[] output = {"{\"ID\":\"TE0001750\",\"region\":\"ae\",\"message\":\"Calculated using Benchmarr\",\"BID\":12,\"OFFER\":1449.652} ",
                "{\"ID\":\"Teepak1\",\"Tick\":\"wBr\",\"BID\":12,\"OFFER\":19.652,\"Shares\":2}"};

        for (int i = 0; i < input.length; i++) {
            String result = StringUtils.anonymiseData(input[i]);
            assertEquals(output[i], result);
        }
    }

    @Test
    public void testFormatToReadableDate() {
        String s = StringUtils.formatToReadableDate("20170608T205425.465569Z");
        assertEquals("2017/06/08 20:54:25-465569", s);
    }

    @Test
    public void testStringNumberFormat() {
        String[] input = {"123123.099", "123", "1234", "123456", "1234567", "1231231231"};
        String[] output = {"123,123", "123", "1,234", "123,456", "1,234,567", "1,231,231,231"};

        for (int i = 0; i < input.length; i++) {
            String result = StringUtils.formatNumber(input[i]);
            assertEquals(output[i], result);
        }
    }

}
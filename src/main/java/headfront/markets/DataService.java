package headfront.markets;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Created by Deepak on 09/07/2016.
 */
public class DataService {

    private static final String COMPANY_STR = "Company";
    private final List<String> companiesList = Arrays.asList("HSBC", "Barclays", "JP Morgan",
            "Walmart", "Amazon", "Pepsi", "GE Electric", "Ford");

    private static final String COUNTRY_STR = "Country";
    private final List<String> countryList = Arrays.asList("China", "France", "New Zealand",
            "United States", "Germany", "Canada");

    private static final String CLIENT_STR = "Client";
    private final List<String> personList = Arrays.asList("John Smith", "Stacey Tipp", "Sarah Newby",
            "Mariah Carey", "Joe Smiller", "Hannah Light");

    private static final String CURRENCY_STR = "Currency";
    private final List<String> ccyList = Arrays.asList("USD", "GBP", "EUR", "JPY",
            "RUB", "PLN", "AUD");


    private final List<String> instrumentList = new ArrayList<>();
    private final List<String> randomStrings = new ArrayList<>();

    private final Set<String> ordersDoubleFields = createSet(new String[]{"Price", "Yeild", "Quantity"});
    private final Set<String> ordersStringFields = createSet(new String[]{"Desc", COMPANY_STR, COUNTRY_STR, CURRENCY_STR, CLIENT_STR});
    private final Set<String> ordersIntegerFields = createSet(new String[]{"State", "ClientID"});
    private final Set<String> ordersBooleanFields = createSet(new String[]{"Done", "Booked"});

    private final Set<String> tradesDoubleFields = createSet(new String[]{"Price", "Yeild", "Quantity"}); // Double
    private final Set<String> tradesStringFields = createSet(new String[]{"Desc", COMPANY_STR, COUNTRY_STR, CURRENCY_STR, CLIENT_STR});
    private final Set<String> tradesIntegerFields = createSet(new String[]{"State", "ClientID"}); // Integer
    private final Set<String> tradesBooleanFields = createSet(new String[]{"Done", "Booked"}); // Boolean

    private final Set<String> rfqsDoubleFields = createSet(new String[]{"BankPrice", "OurPrice", "Quantity"}); // Double
    private final Set<String> rfqsStringFields = createSet(new String[]{"Desc", COMPANY_STR, COUNTRY_STR, CURRENCY_STR, CLIENT_STR}); // String
    private final Set<String> rfqsIntegerFields = createSet(new String[]{"RFQState", "ClientID"}); // Integer
    private final Set<String> rfqsBooleanFields = createSet(new String[]{"Done", "Cover"}); // Boolean


    private final Set<String> instrumentsDoubleFields = createSet(new String[]{"Coupon", "IssueAmpunt", "DV01"}); // Double
    private final Set<String> instrumentsStringFields = createSet(new String[]{"Desc", COMPANY_STR, COUNTRY_STR, CURRENCY_STR}); // String
    private final Set<String> instrumentsIntegerFields = createSet(new String[]{"NoOfIssue", "Outstanding"}); // Integer
    private final Set<String> instrumentsBooleanFields = createSet(new String[]{"Active", "IsJunk"}); // Boolean

    private final Set<String> statusDoubleFields = createSet(new String[]{}); // Double
    private final Set<String> statusStringFields = createSet(new String[]{"Trader", "Status"}); // String
    private final Set<String> statusIntegerFields = createSet(new String[]{"TraderStatus", "TraderID"}); // Integer
    private final Set<String> statusBooleanFields = createSet(new String[]{"LoggedON", "AutoNegOn"}); // Boolean

    private final Set<String> quotesDoubleFields = createSet(new String[]{"Bid", "Offer", "BidQty", "OfferQty"}); // Double
    private final Set<String> quotesStringFields = createSet(new String[]{"Trader", "Status"}); // String
    private final Set<String> quotesIntegerFields = createSet(new String[]{"Status", "TraderID"}); // Integer
    private final Set<String> quotesBooleanFields = createSet(new String[]{"QuoteOn", "AutoNegOn"}); // Boolean


    private Random random = new Random();
    private int noOfRecords;
    private int count = 1;

    public DataService(int noOfRecords) {
        this.noOfRecords = noOfRecords;
        IntStream.rangeClosed(1, 1000).forEach(i -> {
            int id = 15635668 + i;
            instrumentList.add("DE" + id);
        });
        createRandomStrings();
    }

    private String getRandomInstrument() {
        return instrumentList.get(random.nextInt(instrumentList.size()));
    }

    private String getRandomCompany() {
        return companiesList.get(random.nextInt(companiesList.size()));
    }

    private String getRandomPerson() {
        return personList.get(random.nextInt(personList.size()));
    }

    private String getRandomCountry() {
        return countryList.get(random.nextInt(countryList.size()));
    }

    private String getRandomccy() {
        return ccyList.get(random.nextInt(ccyList.size()));
    }

    public Map<String, Object> getRandomOrder(String topic) {
        return createRecord(ordersDoubleFields, ordersStringFields, ordersIntegerFields, ordersBooleanFields,
                topic, null);
    }

    public Map<String, Object> getRandomTrade(String topic) {
        return createRecord(tradesDoubleFields, tradesStringFields, tradesIntegerFields, tradesBooleanFields,
                topic, null);
    }

    public Map<String, Object> getRandomRFQ(String topic) {
        return createRecord(rfqsDoubleFields, rfqsStringFields, rfqsIntegerFields, rfqsBooleanFields,
                topic, null);
    }

    public Map<String, Object> getRandomInstruments(String topic) {
        return createRecord(instrumentsDoubleFields, instrumentsStringFields, instrumentsIntegerFields, instrumentsBooleanFields,
                topic, null);
    }

    public Map<String, Object> getRandomQuotes(String topic) {
        return createRecord(quotesDoubleFields, quotesStringFields, quotesIntegerFields, quotesBooleanFields,
                topic, null);
    }

    public Map<String, Object> getRandomStatus(String topic) {
        return createRecord(statusDoubleFields, statusStringFields, statusIntegerFields, statusBooleanFields,
                topic, null);
    }

    private Map<String, Object> createRecord(final Set<String> doubleFields, final Set<String> StrFields,
                                             final Set<String> intFields, final Set<String> booleanFields,
                                             final String topic, String recordPrefix) {
        Map<String, Object> record = new HashMap<>();
        doubleFields.forEach(field -> {
            record.put(field, createRandomDouble());
        });
        StrFields.forEach(field -> {
            record.put(field, createRandomString(field));
        });
        intFields.forEach(field -> {
            record.put(field, createRandomInt());
        });
        booleanFields.forEach(field -> {
            record.put(field, createRandomBoolean());
        });
        record.put("Count", count++);
        record.put("Topic", topic);
        if (recordPrefix == null) {
            recordPrefix = topic;
        }
        String recordID = recordPrefix;
        recordID = recordID + "_" + random.nextInt(noOfRecords);
        record.put("ID", recordID);
        record.put("RecordPrefix", recordPrefix);
        return record;
    }

    private double createRandomDouble() {
        double randomValue = (random.nextInt(100) + random.nextDouble());
        return (double) Math.round(randomValue * 100) / 100;
    }

    private int createRandomInt() {
        return random.nextInt(100);
    }

    private boolean createRandomBoolean() {
        return random.nextDouble() < 0.5;
    }

    private String createRandomString(String field) {
        switch (field) {
            case CURRENCY_STR:
                return getRandomccy();
            case CLIENT_STR:
                return getRandomPerson();
            case COMPANY_STR:
                return getRandomCompany();
            case COUNTRY_STR:
                return getRandomCountry();

        }
        return randomStrings.get(random.nextInt(randomStrings.size()));
    }

    private Set<String> createSet(String[] fields) {
        Set<String> set = new HashSet<>();
        Collections.addAll(set, fields);
        return set;
    }

    private void createRandomStrings() {
        randomStrings.add("Plus Care");
        randomStrings.add("Access Asia");
        randomStrings.add("Investments");
        randomStrings.add("Acuserv");
        randomStrings.add("Adapt");
        randomStrings.add("Adaptabiz");
        randomStrings.add("Adaptas");
        randomStrings.add("Adaptaz");
        randomStrings.add("Advansed");
        randomStrings.add("Teksyztems");
        randomStrings.add("Affinity  Group");
        randomStrings.add("Afforda");
        randomStrings.add("Services");
        randomStrings.add("Alert Alarm");
        randomStrings.add("Alladin Realty");
        randomStrings.add("Alladins Lamp");
        randomStrings.add("Architectural");
        randomStrings.add("Asian Answers");
        randomStrings.add("Asian Fusion");
        randomStrings.add("Asian Junction");
        randomStrings.add("Asian Plan");
        randomStrings.add("Asian Solutions");
        randomStrings.add("Asiatic Solutions");
        randomStrings.add("Atlas Designs");
        randomStrings.add("Atlas Realty");
        randomStrings.add("Avant Garde");
        randomStrings.add("Avant Appraisal");
        randomStrings.add("Avant Interior");
        randomStrings.add("Awthentikz");
    }

}

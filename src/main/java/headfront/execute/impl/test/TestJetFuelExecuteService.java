package headfront.execute.impl.test;

import headfront.jetfuel.execute.FunctionAccessType;
import headfront.jetfuel.execute.FunctionExecutionType;
import headfront.jetfuel.execute.functions.FunctionParameter;
import headfront.jetfuel.execute.functions.JetFuelFunction;
import headfront.jetfuel.execute.impl.DefaultJetFuelExecuteService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Deepak on 21/05/2017.
 */
public class TestJetFuelExecuteService extends DefaultJetFuelExecuteService {


    public TestJetFuelExecuteService() {
        createAverageFunction();
        createDateTimeFunction();
        createUpdateBankStatusFunction();
        createUpdateQuoteStatusFunction();
        createPricePublisherFunction();
    }

    private void createAverageFunction() {
        FunctionParameter paramA = new FunctionParameter("firstParam", Integer.class, "First Number to Add");
        FunctionParameter paramB = new FunctionParameter("secondParam", Integer.class, "Second Number to Add");
        List<FunctionParameter> parameters = new ArrayList<>();
        parameters.add(paramA);
        parameters.add(paramB);
        JetFuelFunction function = new JetFuelFunction("Average", "Calculates Average of two numbers", parameters,
                Integer.class, "Returns the average of the two numbers", new AverageCalc(), FunctionAccessType.Refresh,
                FunctionExecutionType.RequestResponse);
        function.setTransientFunctionDetatils("MockFunctionPublisher", "DeepakHomeMac", new Date().toString());
        publishFunction(function);
    }

    private void createPricePublisherFunction() {
        FunctionParameter paramA = new FunctionParameter("instID", String.class, "Instrument ID");
        List<FunctionParameter> parameters = new ArrayList<>();
        parameters.add(paramA);
        final PricePublisher<Object> objectPricePublisher = new PricePublisher<>();
        JetFuelFunction function = new JetFuelFunction("PriceUpdates", "Sends Price updates for Instruments", parameters,
                String.class, "Returns the lastPrice", objectPricePublisher, FunctionAccessType.Refresh,
                FunctionExecutionType.Subscription);
        objectPricePublisher.setActiveSubscriptionFactory(getSubscriptionRegistry());
        function.setTransientFunctionDetatils("MockFunctionPublisher", "DeepakHomeMac", new Date().toString());
        publishFunction(function);
    }

    private void createDateTimeFunction() {
        List<FunctionParameter> parameters = new ArrayList<>();
        JetFuelFunction function = new JetFuelFunction("GetDateTime", "Calculates and returns Date and Time from server", parameters,
                String.class, "Returns the Date and Time", new ReturnDateTime(), FunctionAccessType.Refresh,
                FunctionExecutionType.RequestResponse);
        function.setTransientFunctionDetatils("MockFunctionPublisher", "DeepakHomeMac", new Date().toString());
        publishFunction(function);
    }

    private void createUpdateBankStatusFunction() {
        FunctionParameter paramA = new FunctionParameter("traderName", String.class, "Trader who will turn on Bank Status");
        FunctionParameter paramB = new FunctionParameter("status", Boolean.class, "Bank status, True is on and false is off");
        List<FunctionParameter> parameters = new ArrayList<>();
        parameters.add(paramA);
        parameters.add(paramB);
        JetFuelFunction function = new JetFuelFunction("UpdateBankStatus", "Update Bank Status for a given trader.", parameters,
                Integer.class, "Returns true if sucessfull, otherwise false", new TurnBankOn(), FunctionAccessType.Read,
                FunctionExecutionType.RequestResponse);
        function.setTransientFunctionDetatils("MockFunctionPublisher", "DeepakHomeMac", new Date().toString());
        publishFunction(function);
    }

    private void createUpdateQuoteStatusFunction() {
        FunctionParameter paramA = new FunctionParameter("instrument", String.class, "Instrument id of the quotes to be updated");
        FunctionParameter paramB = new FunctionParameter("status", Boolean.class, "Quote status, True is on and false is off");
        FunctionParameter paramC = new FunctionParameter("bidQty", Double.class, "New bid quantity");
        FunctionParameter paramD = new FunctionParameter("offerQty", Double.class, "New offer quantity");
        FunctionParameter paramE = new FunctionParameter("bidPrice", Double.class, "New bid price");
        FunctionParameter paramF = new FunctionParameter("offerSize", Double.class, "New offer price");
        FunctionParameter paramG = new FunctionParameter("options", String.class, "Use any number of the following options \n '-switchOffOnHit' will switch off the quote when hit automatically. \n '-noTraderName' will keep the identity of the trader secret if the market supports it.");
        List<FunctionParameter> parameters = new ArrayList<>();
        parameters.add(paramA);
        parameters.add(paramB);
        parameters.add(paramC);
        parameters.add(paramD);
        parameters.add(paramE);
        parameters.add(paramF);
        parameters.add(paramG);
        JetFuelFunction function = new JetFuelFunction("UpdateQuoteStatus", "Update Quote Status for a given instruments.", parameters,
                Integer.class, "Returns true if sucessfull, otherwise false", new UpdateQuoteStatus(), FunctionAccessType.Write,
                FunctionExecutionType.RequestResponse);
        function.setTransientFunctionDetatils("MockFunctionPublisher", "DeepakHomeMac", new Date().toString());
        publishFunction(function);
    }
}


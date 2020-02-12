package headfront.execute.impl.test;

import headfront.jetfuel.execute.ActiveSubscriptionRegistry;
import headfront.jetfuel.execute.functions.FunctionParameter;
import headfront.jetfuel.execute.functions.FunctionProcessor;
import headfront.jetfuel.execute.functions.FunctionResponseListener;

import java.util.List;
import java.util.Map;
//bad implementation as this is only a test class

public class UpdateQuoteStatus<T> implements FunctionProcessor {

    @Override
    public void validateAndExecuteFunction(String s, List<FunctionParameter> var2, List<Object> list, Map<String, Object> request, FunctionResponseListener functionPublisherResult) {
        functionPublisherResult.onCompleted(s, "Not Implemented", "This function is not implemented in this version");
    }
    @Override
    public void setActiveSubscriptionFactory(ActiveSubscriptionRegistry activeSubscriptionRegistry) {

    }

}

package headfront.execute.impl.test;

import headfront.jetfuel.execute.ActiveSubscriptionRegistry;
import headfront.jetfuel.execute.functions.FunctionParameter;
import headfront.jetfuel.execute.functions.FunctionProcessor;
import headfront.jetfuel.execute.functions.FunctionResponseListener;

import java.util.List;
import java.util.Map;
//bad implementation as this is only a test class

public class TurnBankOn<T> implements FunctionProcessor {

    @Override
    public void validateAndExecuteFunction(String s, List<FunctionParameter> var2, List<Object> list, Map<String, Object> request, FunctionResponseListener functionPublisherResult) {
        if (list.size() == 2) {
            Object param = list.get(0);
            if (param != null) {
                Object param2 = list.get(1);
                if (isBoolean(param2)) {
                    boolean b = Boolean.parseBoolean(param2.toString());
                    if (b) {
                        functionPublisherResult.onCompleted(s, "Success", "Bank is turned ON by trader " + param);
                    } else {
                        functionPublisherResult.onCompleted(s, "Success", "Bank is turned OFF by trader " + param);
                    }
                } else {
                    functionPublisherResult.onError(s, "param2 needs to none null and a boolean so either 'true' or 'false', we got " + param2.getClass().getSimpleName(), param2);
                }
            } else {
                functionPublisherResult.onError(s, "param1 needs to none null and a String, we got " + param.getClass().getSimpleName(), param);
            }
        } else {
            functionPublisherResult.onError(s, "Expected 2 values and got " + list, null);
        }
    }

    public static boolean isBoolean(Object s) {
        return ((s != null) && (s.toString().equalsIgnoreCase("true") || s.toString().equalsIgnoreCase("false")));
    }

    @Override
    public void setActiveSubscriptionFactory(ActiveSubscriptionRegistry activeSubscriptionRegistry) {

    }
}

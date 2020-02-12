package headfront.newexplorer;

/**
 * Created by Deepak on 20/09/2017.
 */
public class TempPrice {
    String inst;
    double bid;
    double ask;
    String desc;
    String active;

    public TempPrice(String inst, double bid, double ask, String desc, String active) {
        this.inst = inst;
        this.bid = bid;
        this.ask = ask;
        this.desc = desc;
        this.active = active;
    }

    public String getInst() {
        return inst;
    }

    public double getBid() {
        return bid;
    }

    public double getAsk() {
        return ask;
    }

    public String getDesc() {
        return desc;
    }

    public String getActive() {
        return active;
    }
}

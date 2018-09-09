package headfront.jetfuelview.util;

/**
 * Created by Deepak on 09/09/2018.
 */
public class ObjectHolder {

    private Object[] data;

    public ObjectHolder(Object... data){
        this.data = data;
    }

    public Object get(int i){
        return data[i];
    }
}

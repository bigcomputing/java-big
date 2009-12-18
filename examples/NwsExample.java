package examples;
import com.sca.nws.NetWorkSpace;
import com.sca.nws.NwsException;
import com.sca.nws.NwsVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NwsExample {
    public static void main(String[] args) throws NwsException {
        NetWorkSpace ws = new NetWorkSpace("java shop");

        System.out.println("connected, listing contents of netWorkSpace " +
                           "(should be nothing there).");
        System.out.println(ws.listVars());

        NwsVariable<Object> x = ws.variable("x");
        x.store(1);
        System.out.println("should now see x.");
        System.out.println(ws.listVars());

        System.out.println("find (but don't consume) x.");
        System.out.println(x.find());
        System.out.println("check that it is still there.");
        System.out.println(ws.listVars());

        System.out.println("associate another value with x.");
        x.store(2);
        System.out.println(ws.listVars()); 

        System.out.println("consume values for x, should see them in order saved.");
        System.out.println(x.fetch());
        System.out.println(x.fetch());
        System.out.println("no more values for x... .");
        System.out.println(ws.listVars());           

        System.out.println("so try to fetch and see what happens ... .");
        System.out.println(x.fetchTry("no go"));

        System.out.println("create a single-value variable.");
        NwsVariable<Double> pi = ws.variable("pi");
        pi.declare(NetWorkSpace.SINGLE);
        System.out.println(ws.listVars());

        System.out.println("get rid of x.");
        x.delete();
        System.out.println(ws.listVars());

        System.out.println("try to store two values to pi.");
        pi.store(2.171828182);
        pi.store(3.141592654);
        System.out.println(ws.listVars()); 

        System.out.println("check that the right one was kept.");
        System.out.println(pi.find());

        System.out.println("store a Map");
        Map<String, String> m = new HashMap<String, String>();
        m.put("foo", "spam");
        m.put("bar", "eggs");
        NwsVariable<Map<String, String>> map = ws.variable("map");
        map.store(m);
        System.out.println(map.find());

        System.out.println("store a List");
        NwsVariable<List<Integer>> list = ws.variable("list");
        list.store(Arrays.asList(6, 7, 42));
        System.out.println(list.find());

        System.out.println("what about the rest of the world?");
        System.out.println(ws.getNwsServer().listWss()); 

        ws.close();
    }
}

package examples;
import com.sca.nws.NetWorkSpace;
import com.sca.nws.NwsException;
import com.sca.nws.NwsVariable;

public class Variable {
    public static void main(String[] args) throws NwsException {
        NetWorkSpace nws = new NetWorkSpace("test");
        NwsVariable<Integer> x = nws.variable("x", Integer.class);
        x.declare(NetWorkSpace.LIFO);

        x.store(7);
        x.store(6);
        x.store(2);
        x.store(1);

        x.store(x.fetch() + x.fetch());  // 1 + 2 = 3
        System.out.println("1 + 2 = " + x.fetch());

        x.store(x.fetch() * x.fetch());  // 6 * 7 = 42
        System.out.println("6 * 7 = " + x.fetch());

    }
}

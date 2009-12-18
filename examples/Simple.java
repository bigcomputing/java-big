package examples;
import com.sca.nws.NetWorkSpace;
import com.sca.nws.NwsException;
import com.sca.nws.NwsVariable;

public class Simple {
    public static void main(String[] args) throws NwsException {
        NetWorkSpace ws = new NetWorkSpace("test");
        NwsVariable<Integer> x = ws.variable("x");

        for (int i = 0; i < 10; i++)
            x.store(i);

        for (int i: x)
            System.out.println(i);
    }
}

package examples;
import com.sca.nws.NetWorkSpace;
import com.sca.nws.NwsException;
import com.sca.nws.NwsVariable;

public class PubSubTry {
    public static void main(String[] args) throws NwsException {
        // Publish the data
        NetWorkSpace pubws = new NetWorkSpace("test");
        for (int i = 0; i < 10; i++)
            pubws.store("x", i);

        // Start three subscriber threads
        for (int i = 0; i < 3; i++) {
            NetWorkSpace subws = new NetWorkSpace("test");
            Subscriber sub = new Subscriber("Sub_" + i,
                                            subws.ifindTry("x", int.class));
            sub.start();
        }
    }

    static class Subscriber extends Thread {
        NwsVariable<Integer> x;

        Subscriber(String name, NwsVariable<Integer> x_) {
            super(name);
            x = x_;
        }

        public void run() {
            for (int i: x)
                System.out.println(getName() + ": got " + i);
        }
    }
}

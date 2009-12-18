package examples;
import com.sca.nws.NetWorkSpace;
import com.sca.nws.NwsException;
import com.sca.nws.NwsVariable;

public class PubSub {
    public static void main(String[] args) throws NwsException {
        // Start three subscriber threads
        for (int i = 0; i < 3; i++) {
            NetWorkSpace subws = new NetWorkSpace("test");
            Subscriber sub = new Subscriber("Sub_" + i,
                                            subws.ifind("x", Integer.class));
            sub.start();
        }

        // Publish the data
        NetWorkSpace pubws = new NetWorkSpace("test");
        NwsVariable<Integer> x = pubws.variable("x");
        for (int i = 0; i < 10; i++)
            x.store(i);

        // Poison pill indicates that no more data coming
        x.store(-1);
    }

    static class Subscriber extends Thread {
        NwsVariable<Integer> x;

        Subscriber(String name, NwsVariable<Integer> x_) {
            super(name);
            x = x_;
        }

        public void run() {
            for (int i: x) {
                if (i == -1) break;
                System.out.println(getName() + ": got " + i);
            }
        }
    }
}

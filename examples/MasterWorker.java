package examples;
import com.sca.nws.NetWorkSpace;
import com.sca.nws.NwsException;
import com.sca.nws.NwsOptions;
import com.sca.nws.NwsServer;
import com.sca.nws.NwsVariable;

public class MasterWorker {
    public static void main(String[] args) throws NwsException {
        NwsOptions opts = new NwsOptions();
        opts.host = NwsServer.DEFAULT_HOSTNAME;
        opts.port = NwsServer.DEFAULT_PORT;
        NetWorkSpace ws = new NetWorkSpace("test", opts);
        NwsVariable<Integer> task = ws.variable("task");
        NwsVariable<Object[]> result = ws.variable("result");

        // Start three worker threads
        for (int i = 0; i < 3; i++) {
            Worker w = new Worker("Worker_" + i, ws.getWsName(), opts);
            w.setDaemon(true);
            w.start();
        }

        final int NUM_TASKS = 30;

        // Submit the tasks
        for (int i = 0; i < NUM_TASKS; i++)
            task.store(i);

        // Wait for the results
        for (int i = 0; i < NUM_TASKS; i++) {
            Object[] ia = result.fetch();
            System.out.println(ia[0] + ": " + ia[1] + " squared is " + ia[2]);
        }
    }

    static class Worker extends Thread {
        NwsVariable<Integer> task;
        NwsVariable<Object[]> result;

        Worker(String name, String wsName, NwsOptions opts) throws NwsException {
            super(name);
            NetWorkSpace ws = new NetWorkSpace(wsName, opts);
            task = ws.ifetch("task");
            result = ws.variable("result");
        }

        public void run() {
            try {
                for (int i: task)
                    result.store(new Object[] {getName(), i, i * i});
            } catch (NwsException e) {
                e.printStackTrace();
            }
        }
    }
}

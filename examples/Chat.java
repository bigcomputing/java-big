package examples;
import com.sca.nws.NetWorkSpace;
import com.sca.nws.NwsConnectException;
import com.sca.nws.NwsException;
import com.sca.nws.NwsOptions;
import com.sca.nws.NwsVariable;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Chat extends WindowAdapter implements ActionListener {
    static final String user = System.getProperty("user.name");
    private final JTextArea text = new JTextArea(24, 50);
    private final JTextField msg = new JTextField();
    private final NwsVariable<byte[]> chat;

    Chat(NwsOptions opt) throws NwsException {
        chat = createChatVariable(opt);
    }

    Component createComponents() {
        text.setEditable(false);
        msg.addActionListener(this);

        JPanel pane = new JPanel(new BorderLayout());
        JScrollPane spane = new JScrollPane(text);
        spane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        pane.add(spane, BorderLayout.CENTER);
        pane.add(msg, BorderLayout.SOUTH);
        pane.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        return pane;
    }

    void displayMessage(byte[] t) {
        text.append(new String(t) + "\n");
    }

    public void actionPerformed(ActionEvent e) {
        String s = msg.getText().trim();
        msg.setText("");
        if (s.length() > 0)
            try {
                // Send our message to the chat variable with our name prefixed
                chat.store((user + ": " + s).getBytes());
            } catch (NwsException ex) {
            }
    }

    public void windowOpened(WindowEvent e) {
        msg.requestFocus();
    }

    private static NwsVariable<byte[]> createChatVariable(NwsOptions opt)
                throws NwsException {
        NwsVariable<byte[]> chat;
        opt.persistent = true;
        NetWorkSpace ws = new NetWorkSpace("chatroom", opt);
        chat = ws.ifind("chat");
        chat.declare(NetWorkSpace.FIFO);
        return chat;
    }

    private static void createAndShowGUI(Chat app) {
        JFrame frame = new JFrame("NWS Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Component contents = app.createComponents();
        frame.getContentPane().add(contents, BorderLayout.CENTER);
        frame.addWindowListener(app);
        frame.pack();
        frame.setVisible(true);
    }

    private static NwsOptions getNwsOptions(String[] args) {
        NwsOptions opt = new NwsOptions();

        for (int i = 0; i < args.length; i++) {
            try {
                if (args[i].equals("-h")) {
                    opt.host = args[i + 1];
                    i++;
                } else if (args[i].equals("-p")) {
                    opt.port = Integer.parseInt(args[i + 1]);
                    i++;
                } else {
                    System.err.println("error: unrecognized option: " +
                                       args[i]);
                    System.exit(1);
                }
            } catch (NumberFormatException nfe) {
                System.err.println("error: the " + args[i] +
                                   " option takes an integer argument");
                System.exit(1);
            } catch (ArrayIndexOutOfBoundsException ae) {
                System.err.println("error: the " + args[i] +
                                   " option takes a required argument");
                System.exit(1);
            }
        }

        return opt;
    }

    public static void main(String[] args) throws NwsException {
        NwsOptions opt = getNwsOptions(args);

        try {
            final Chat app = new Chat(opt);

            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createAndShowGUI(app);
                }
            });

            NwsVariable<byte[]> chat = createChatVariable(opt);

            // Display messages coming in from all chat clients
            for (final byte[] t: chat) {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        app.displayMessage(t);
                    }
                });
            }
        } catch (NwsConnectException e) {
            System.out.println("Error: unable to connect to NWS server on " +
                               opt.host + ":" + opt.port);
        }
    }
}

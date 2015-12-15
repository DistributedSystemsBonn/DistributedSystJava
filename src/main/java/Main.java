import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws Exception {
        //25.95.123.198 - Maxim
        Node node = new Node("25.95.123.198:9178");
        PdsServiceImpl.setNode(node);

        new Thread() {
            public void run() {
                try {
                    WebServer webServer = new WebServer(9178);
                    XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
                    PropertyHandlerMapping phm = new PropertyHandlerMapping();
                    phm.setVoidMethodEnabled(true);
                    phm.addHandler("Host", PdsServiceImpl.class);
                    xmlRpcServer.setHandlerMapping(phm);

                    XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl)
                            xmlRpcServer.getConfig();

                    serverConfig.setEnabledForExtensions(true);
                    serverConfig.setContentLengthOptional(false);
                    webServer.start();

                } catch (Exception ex) {
                    System.out.println("Something wrong with server");
                    System.exit(1);
                }
            }
        }.start();

        System.out.println("The Distributed System");
        System.out.println("Select an operation:");
        System.out.println("join ip:port");
        System.out.println("exit");

        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("> ");
            String input = br.readLine();

            if (input.equals("exit")) {
                System.exit(0);

            } else if (input.substring(0, 4).equals("join")) {
                System.out.println("joining...");
                String ipPort = input.substring(5);
                node.join(ipPort);
                System.out.println("joined to " + ipPort);

            } else if (input.equals("gethosts")) {
                String[] ipPorts = node.getIpPorts();
                for (String ipPort : ipPorts) {
                    System.out.println(ipPort);
                }

            } else {
                System.out.println("Unknown command");
            }
        }
    }
}
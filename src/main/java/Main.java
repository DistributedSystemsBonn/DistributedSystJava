import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class Main {
    public static void main(String[] args) throws Exception { //

        String HamachiIpPort = null; //
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while(e.hasMoreElements()) {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            if (n.getDisplayName().equals("LogMeIn Hamachi Virtual Ethernet Adapter")) {
                Enumeration ee = n.getInetAddresses();
                if (!ee.hasMoreElements()) {
                    throw new Exception("Smth wrong with Enumeration ee = n.getInetAddresses()");
                }
                InetAddress i = (InetAddress) ee.nextElement();
                HamachiIpPort = i.getHostAddress();
            }
        }

        HamachiIpPort = "25.124.17.178:9178";
        //HamachiIpPort += ":" + "9178";
        //HamachiIpPort = "25.95.123.198:9178";
        System.out.println("ip:port - " + HamachiIpPort);
        Node node = new Node(HamachiIpPort);
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
        System.out.println(" - join ip:port");
        System.out.println(" - gethosts");
        System.out.println(" - sign off");
        System.out.println(" - start_ct");
        System.out.println(" - start_ra");
        System.out.println(" - exit");

        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("> ");
            String input = br.readLine();

            if (input.equals("exit")) {
                System.exit(0);

            } else if (input.length() >= 4 && input.substring(0, 4).equals("join")) {
                System.out.println("joining...");
                int flg = 1;
                for (int i = 0; i < node.getDictionary().size(); i++) {
                    if (input.substring(5).equals(node.getDictionary().get(i).getIp())) {
                        flg = 0;
                        break;
                    }
                }
                if (input.substring(5).equals(node.getSelf().getIp()) || flg == 0) {
                    System.out.println("error -- cannot connect to this ip");
                } else {
                    String ipPort = input.substring(5);
                    node.join(ipPort);
                }
            } else if (input.equals("gethosts")) {
                String[] ipPorts = node.getIpPorts();
                for (String ipPort : ipPorts) {
                    System.out.println(ipPort);
                }
            } else if (input.equals("sign off")) {
                node.signOff();
                System.out.println("signed off the network");
            } else if (input.equals("start_ct")) {
                if (node.getDictionary().size() > 0) {
                    node.start(false);
                } else {
                    System.out.println("error -- node is not in the network");
                }
            }  else if (input.equals("start_ra")){
                if (node.getDictionary().size() > 0) {
                    node.start(true);
                } else {
                    System.out.println("error -- node is not in the network");
                }
            } else {
                System.out.println("error -- unknown command");
            }
        }
    }
}
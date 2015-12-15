import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.URL;

public class ClientFactoryPDS {
    public Host getClient(String ipPort) {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        try {
            config.setServerURL(new URL("http://" + ipPort + "/xmlrpc"));
        } catch (Exception ex) {
            System.out.println("Some problem with ip in ClientFactoryPDS class");
        }
        config.setEnabledForExtensions(true);
        config.setConnectionTimeout(60 * 1000);
        config.setReplyTimeout(60 * 1000);
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        org.apache.xmlrpc.client.util.ClientFactory factory = new org.apache.xmlrpc.client.util.ClientFactory(client);
        return (Host) factory.newInstance(Host.class);
    }
}

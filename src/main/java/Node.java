import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;

public class Node {
    private NodeInfo self;

    private NodeInfo masterNode;

    private List<NodeInfo> dictionary;

    public Node(String ip) {
        self = new NodeInfo();
        self.setId(UUID.randomUUID());
        self.setIp(ip);

        dictionary = new ArrayList<NodeInfo>();
        // Принимает
        // join
        // sign off
        // start
    }

    public List<NodeInfo> join(String ipPort) {
        // запрос
        List<NodeInfo> receivedDictionary;
        try {
/*            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(ipPort));

            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            // handleJoin прибавляет к своему dictionary полученный.
            // handleJoin рассылает словарь всей известной сети (и сторой, и новой).
            // данные
            receivedDictionary = (List<NodeInfo>) client.execute("Calculator.handleJoin", dictionary);*/

            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL("http://" + ipPort + "/xmlrpc"));
            config.setEnabledForExtensions(true);
            config.setConnectionTimeout(60 * 1000);
            config.setReplyTimeout(60 * 1000);
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);
            ClientFactory factory = new ClientFactory(client);
            Host pds = (Host) factory.newInstance(Host.class);

            Object[] ipPorts = pds.getHosts(self.getIp());

            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setIp(ipPort);
            dictionary.add(nodeInfo);

            for (Object s : ipPorts) {
                nodeInfo = new NodeInfo();
                nodeInfo.setIp((String) s);
                dictionary.add(nodeInfo);

                config = new XmlRpcClientConfigImpl();
                config.setServerURL(new URL("http://" + nodeInfo.getIp() + "/xmlrpc"));
                config.setEnabledForExtensions(true);
                config.setConnectionTimeout(60 * 1000);
                config.setReplyTimeout(60 * 1000);
                client = new XmlRpcClient();
                client.setConfig(config);
                factory = new ClientFactory(client);
                pds = (Host) factory.newInstance(Host.class);

                pds.addNewHost(self.getIp());
            }

           // System.out.println(pds.echo("Hello"));

        } catch (Exception ex) {
            System.out.println(ex);
        }

/*
        for (NodeInfo nodeInfo : receivedDictionary) {
            if (nodeInfo.getIp().equals(self.getIp())) {
                receivedDictionary.remove(nodeInfo);
                break;
            }
        }*/
/*
        NodeInfo o = new NodeInfo();
        for (NodeInfo nodeInfo : receivedDictionary) {
            if (nodeInfo.getIp().equals(self.getIp())) {
                o = nodeInfo;
                break;
            }
        }
        receivedDictionary.remove(o);*/

        return dictionary;
    }

    public void signOff() {
        // от всех
    }

    public void start() {

    }

    protected NodeInfo electMasterNode() {
        List<UUID> nodeIDs = new ArrayList<UUID>();
        for (NodeInfo nodeInfo : dictionary) {
            nodeIDs.add(nodeInfo.getId());
        }

        UUID[] nodeIDsArray = (UUID[]) nodeIDs.toArray();
        Arrays.sort(nodeIDsArray);
        UUID masterNodeId = nodeIDsArray[nodeIDsArray.length - 1];
        for (NodeInfo nodeInfo : dictionary) {
            if (nodeInfo.getId().equals(masterNodeId)) {
                masterNode = nodeInfo;
                break;
            }
        }

        return masterNode;
    }

    public NodeInfo getSelf() {
        return self;
    }

    public void setSelf(NodeInfo self) {
        this.self = self;
    }

    public void setDictionary(List<NodeInfo> dictionary) {
        this.dictionary = dictionary;
    }

    public List<NodeInfo> getDictionary() {
        return dictionary;
    }

    public String[] getIpPorts() {
        String[] ipPorts = new String[dictionary.size()];

        for (int i = 0; i < dictionary.size(); i++) {
            ipPorts[i] = dictionary.get(i).getIp();
        }

        return ipPorts;
    }
}

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class Node {
    private NodeInfo self;

    private NodeInfo masterNode;

    private List<NodeInfo> dictionary;

    public Node() {
        self = new NodeInfo();
        self.setId(UUID.randomUUID());
        self.setIp("0.0.0.0:8080");
    }

    public List<NodeInfo> join(String ip) {
        // запрос
        List<NodeInfo> receivedDictionary;
        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(ip));

            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            // handleJoin прибавляет к своему dictionary полученный.
            // handleJoin рассылает словарь всей известной сети (и сторой, и новой).
            // данные
            receivedDictionary = (List<NodeInfo>) client.execute("Calculator.handleJoin", dictionary);

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
}

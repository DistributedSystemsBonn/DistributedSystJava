import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;

public class Node {
    private NodeInfo self;

    private NodeInfo masterNode;

    private List<NodeInfo> dictionary;

    private ClientFactoryPDS clientFactoryPDS;

    public Node(String ip) {
        self = new NodeInfo();
        self.setId(UUID.randomUUID());
        self.setIp(ip);
        masterNode = new NodeInfo();
        dictionary = new ArrayList<NodeInfo>();
        // Принимает
        // join
        // sign off
        // start

        clientFactoryPDS = new ClientFactoryPDS();
    }

    public List<NodeInfo> join(String ipPort) {
        // запрос
        try {
            Host pds = clientFactoryPDS.getClient(ipPort);
            Object[] IpIdPorts = pds.getHosts(self.getIp(), self.getId());

            Object interimNodeInfo[];
            NodeInfo nodeInfo;

            for (int i = 0; i < IpIdPorts.length; i++) {
                nodeInfo = new NodeInfo();
                interimNodeInfo = (Object[]) IpIdPorts[i];
                nodeInfo.setIp((String) interimNodeInfo[0]);
                nodeInfo.setId((UUID) interimNodeInfo[1]);
                dictionary.add(nodeInfo);
                if (i > 0) {
                    pds = clientFactoryPDS.getClient(nodeInfo.getIp());
                    pds.addNewHost(self.getIp(), self.getId());
                }
            }
            System.out.println("joined to " + ipPort);
            // System.out.println(pds.echo("Hello"));

        } catch (Exception ex) {
            //System.out.println(ex);
            System.out.println("error -- ip is not valid");

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
        try {
            for (int i = 0; i < dictionary.size(); i++) {
                Host pds = clientFactoryPDS.getClient(dictionary.get(i).getIp());
                pds.DelHost(self.getIp());
            }
            dictionary.clear();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public void start() {
        System.out.println("master node election...");
        NodeInfo MasterNode;
        MasterNode = electMasterNode();
    }

    protected NodeInfo electMasterNode() {
        List<UUID> nodeIDs = new ArrayList<UUID>();
        for (NodeInfo nodeInfo : dictionary) {
            if (getSelf().getId().compareTo(nodeInfo.getId()) == -1) {
                nodeIDs.add(nodeInfo.getId());
            }
        }
        UUID[] nodeIDsArray = new UUID[nodeIDs.size()];
        nodeIDs.toArray(nodeIDsArray);
        Arrays.sort(nodeIDsArray);
        boolean isMaster = true;
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        if (nodeIDsArray.length != 0) {
            while (masterNode.getIp() == null) {
                for (int i = nodeIDsArray.length - 1; i >= 0; i--) {
                    for (NodeInfo nodeInfo : dictionary) {
                        if (nodeInfo.getId().equals(nodeIDsArray[i])) {
                            try {
                                Host pds = clientFactoryPDS.getClient(nodeInfo.getIp());
                                Date t = new Date();
                                System.out.println(t.getTime() + " connecting to " + nodeInfo.getIp());
                                if (pds.isAlive().equals("Ok")) {
                                    t = new Date();
                                    System.out.println(t.getTime() + " " + nodeInfo.getIp() + " says Ok");
                                    isMaster = false;
                                }
                            } catch (Exception ex) {
                                System.out.println(ex);
                            }
                        }
                    }
                }
                if (isMaster) {
                    masterNode = getSelf();
                    System.out.println("master node is " + masterNode.getIp());
                    for (NodeInfo node: dictionary) {
                        Host pds = clientFactoryPDS.getClient(node.getIp());
                        pds.masterMessage(self.getIp(), self.getId());
                    }
                    break;
                }
            }
            try {
                Thread.sleep(1000);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } else {
            masterNode = getSelf();
            System.out.println("master node is " + masterNode.getIp());
            for (NodeInfo node: dictionary) {
                try {
                    Host pds = clientFactoryPDS.getClient(node.getIp());
                    pds.masterMessage(self.getIp(), self.getId());
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
        }
        return masterNode;
    }

    public void CME() {
        System.out.println("Centralised Mutual Exclusion used for connecting to " + getMasterNode().getIp());
        for (NodeInfo node: dictionary) {
            try {
                Host pds = clientFactoryPDS.getClient(node.getIp());
                pds.loop();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    public void tram(){
        try {
            Host pds = clientFactoryPDS.getClient(getMasterNode().getIp());
            Boolean isAccess;
            String masterStr = pds.getString();
            masterStr += self.getIp();
            pds = clientFactoryPDS.getClient(getMasterNode().getIp());
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public NodeInfo getSelf() {
        return self;
    }

    public void setSelf(NodeInfo self) {
        this.self = self;
    }

    public NodeInfo getMasterNode() {
        return masterNode;
    }

    public void setMasterNode(NodeInfo masterNode) {
        this.masterNode = masterNode;
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
    public UUID[] getIds() {
        UUID[] ids = new UUID[dictionary.size()];

        for (int i = 0; i < dictionary.size(); i++) {
            ids[i] = dictionary.get(i).getId();
        }

        return ids;
    }
}
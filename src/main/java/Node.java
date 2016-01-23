import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;

public class Node {
    private NodeInfo self;

    private String masterNode;

    private List<NodeInfo> dictionary;

    private ClientFactoryPDS clientFactoryPDS;

    public Node(String ip) {
        self = new NodeInfo(); // consist of IP (method getIp()) and id (method getId()) of the node
        self.setIp(ip);
        dictionary = new ArrayList<NodeInfo>(); // consist of information about all nodes in the network
        clientFactoryPDS = new ClientFactoryPDS();
    }

    public List<NodeInfo> join(String ipPort) {
        try {
            Host pds = clientFactoryPDS.getClient(ipPort);
            Object[] ipPorts = pds.getHosts(self.getIp()); // getting IPs of all nodes in the network

            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setIp(ipPort);
            dictionary.add(nodeInfo);

            for (Object s : ipPorts) {
                nodeInfo = new NodeInfo();
                nodeInfo.setIp((String) s);
                dictionary.add(nodeInfo);
                pds = clientFactoryPDS.getClient(nodeInfo.getIp());
                pds.addNewHost(self.getIp()); // sending to the node s notification about self connection
            }
        } catch (Exception ex) {
            System.out.println(ex); // if cannot access the node output an error
        }
        System.out.println("joined to " + ipPort);
        return dictionary;
    }

    public void signOff() {
        try {
            int i = 0;
            while (i < dictionary.size()) {
                Host pds = clientFactoryPDS.getClient(dictionary.get(i).getIp());
                pds.signOff(self.getIp()); // sending node i notification about signing self off the network
                i++;
            }
            dictionary.clear();
        } catch (Exception ex) {
            System.out.println(ex); // if cannot access the node output an error
        }
    }

    public void start(boolean isRicart) {
        boolean isMasterElected = false;
        while (!isMasterElected) {
            startBullyElection(); // start of the master node election
            for (NodeInfo nodeInfo : dictionary) {
                Host pds = clientFactoryPDS.getClient(nodeInfo.getIp());
                pds.getStartMsg(isRicart);
            }
            if (isRicart) {
                isMasterElected = startRicartAgrawala();
            } else {
                isMasterElected = startCentralMutualExclusion();
            }
        }
    }

    public void startBullyElection() {
        // Creating an array of IDs that are bigger then this one
        List<String> nodeIDs = new ArrayList<String>();
        for (NodeInfo nodeInfo : dictionary) {
            if (getSelf().getId().compareTo(nodeInfo.getId()) == -1) { // isolation the nodes with higher id
                nodeIDs.add(nodeInfo.getId()); //
            }
        }
        boolean _isElectionFinished = false;
        if (nodeIDs.size() != 0) {
            String[] nodeIDsArray = new String[nodeIDs.size()];
            nodeIDs.toArray(nodeIDsArray);
            Arrays.sort(nodeIDsArray);
            // end of creating
            boolean flag = false;
            boolean msg;
            for (String id : nodeIDsArray) {
                msg = sendElectionMsg(getIpPortById(id));
                try {
                    Thread.sleep(1000); // 1000 milliseconds is one second.
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                flag |= msg; // computing if there any node with higher id online
            }
            if (!flag) {
                _isElectionFinished = true;
            }
        } else {
            _isElectionFinished = true;
        }
        if (_isElectionFinished) {
            System.out.println("This node is a master node");
            for (NodeInfo nodeInfo : dictionary) {
                Host pds = clientFactoryPDS.getClient(nodeInfo.getIp());
                pds.setMasterNode(self.getIp()); // sending a notification to all nodes that it is a master
            }
        }
    }

    public boolean sendElectionMsg(String ipPort) {
        boolean msg = false;
        try {
            Host pds = clientFactoryPDS.getClient(ipPort); // call proxy.ReceiveElectionMsg() method of target host.
            msg = pds.receiveElectionMsg(self.getIp()); // the receiver starts "election algorithm" thread and return true;
        } catch (Exception ex) {
            System.out.println("Cannot connect " + ipPort);
        }
        return msg;
    }


    public boolean startRicartAgrawala() {
        try {
            Thread.sleep(1000); // 1000 milliseconds is one second.
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        try {
            Host pds = clientFactoryPDS.getClient(masterNode);
            String masterString = pds.readResource(self.getIp());
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public boolean startCentralMutualExclusion() {
        try {
            Thread.sleep(1000); // 1000 milliseconds is one second.
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        try {
            Host pds = clientFactoryPDS.getClient(masterNode);
            String masterString = pds.readResource(self.getIp());
            masterString += self.getId();
            pds.updateResource(masterString, self.getIp());
        } catch (Exception ex) {
            return false;
        }
        return true;
    }


    public NodeInfo getSelf() {
        return self;
    }

    public void setSelf(NodeInfo self) {
        this.self = self;
    }

    public String getMasterNode() {
        return masterNode;
    }

    public void setMasterNode(String masterNode) {
        System.out.println("Master node is " + masterNode);
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

    protected String getIpPortById(String id) {
        for (NodeInfo nodeInfo : dictionary) {
            if (nodeInfo.getId().equals(id)) {
                return nodeInfo.getIp();
            }
        }

        return "";
    }
}
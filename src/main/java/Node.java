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
        self = new NodeInfo();
        self.setIp(ip);
        dictionary = new ArrayList<NodeInfo>();
        clientFactoryPDS = new ClientFactoryPDS();
    }

    public List<NodeInfo> join(String ipPort) {
        try {
            Host pds = clientFactoryPDS.getClient(ipPort);
            Object[] ipPorts = pds.getHosts(self.getIp());

            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setIp(ipPort);
            dictionary.add(nodeInfo);

            for (Object s : ipPorts) {
                nodeInfo = new NodeInfo();
                nodeInfo.setIp((String) s);
                dictionary.add(nodeInfo);
                pds = clientFactoryPDS.getClient(nodeInfo.getIp());
                pds.addNewHost(self.getIp());
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        System.out.println("joined to " + ipPort);
        return dictionary;
    }

    public void signOff() {
        try {
            int i = 0;
            while (i < dictionary.size()) {
                Host pds = clientFactoryPDS.getClient(dictionary.get(i).getIp());
                pds.signOff(self.getIp());
                i++;
            }
            dictionary.clear();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public void start(boolean isRicart) {
        startBullyElection();
        for (NodeInfo nodeInfo : dictionary) {
            Host pds = clientFactoryPDS.getClient(nodeInfo.getIp());
            pds.getStartMsg(isRicart);
        }
        if (isRicart) {
            startRicartAgrawala();
        } else {
            startCentralMutualExclusion();
        }
    }

    public void startBullyElection() {
        // Creating an array of IDs that are bigger then this one
        List<String> nodeIDs = new ArrayList<String>();
        for (NodeInfo nodeInfo : dictionary) {
            if (getSelf().getId().compareTo(nodeInfo.getId()) == -1) {
                nodeIDs.add(nodeInfo.getId());
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
                    Thread.sleep(1000);                 //1000 milliseconds is one second.
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                flag |= msg;
            }
            if (!flag) {
                _isElectionFinished = true;
            }
        } else {
            _isElectionFinished = true;
        }
        if (_isElectionFinished) {
            System.out.println("HA-HA-HA I AM MASTER NODE!!!");
            for (NodeInfo nodeInfo : dictionary) {
                Host pds = clientFactoryPDS.getClient(nodeInfo.getIp());
                pds.setMasterNode(self.getIp());
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


    public void startRicartAgrawala() {
        try {
            Thread.sleep(1000);                 //1000 milliseconds is one second.
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

    }

    public void startCentralMutualExclusion() {
        try {
            Thread.sleep(1000);                 //1000 milliseconds is one second.
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        Host pds = clientFactoryPDS.getClient(masterNode);
        String masterString = pds.readResource(self.getIp());
        masterString += self.getId();
        pds.updateResource(masterString, self.getIp());
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
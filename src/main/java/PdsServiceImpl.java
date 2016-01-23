import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PdsServiceImpl {
    private static Node node;

    public static void setNode(Node _node) {
        node = _node;
    }

    public Object[] getHosts(String ipAndPort) {
        Object[] ipPorts = node.getIpPorts();
        NodeInfo newNode = new NodeInfo();
        newNode.setIp(ipAndPort);
        node.getDictionary().add(newNode);

        System.out.println("joining request from " + ipAndPort);
        return ipPorts;
    }

    public void addNewHost(String ipAndPort) {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setIp(ipAndPort);
        node.getDictionary().add(nodeInfo);
    }

    public boolean receiveElectionMsg(String Ip) {
        node.startBullyElection();
        return true;
    }

    public void setMasterNode(String ipAndPort) {
        node.setMasterNode(ipAndPort);
    }

    public boolean signOff(String ipPort) {
        List<NodeInfo> nodeInfos = node.getDictionary();
        for (NodeInfo nodeInfo : nodeInfos) {
            if (nodeInfo.getIp().equals(ipPort)) {
                nodeInfos.remove(nodeInfo);
                System.out.println(ipPort + " signed off");
                return true;
            }
        }

        return false;
    }
}
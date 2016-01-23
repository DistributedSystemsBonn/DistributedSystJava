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

    public boolean signOff (String DelIpPort) {
        String[] ipPorts = node.getIpPorts();
        int DelIpIndex = 0;
        for (String ipPort : ipPorts) {
            if (ipPort.equals(DelIpPort)) {
                break;
            }
            DelIpIndex = DelIpIndex + 1;
        }
        node.getDictionary().remove(DelIpIndex);

        System.out.println(DelIpPort + " was signed off");
        return true;
    }


    public boolean receiveElectionMsg(String Ip) {
        node.startBullyElection();
        return true;
    }

    public void setMasterNode(String ipAndPort) {
        node.setMasterNode(ipAndPort);
    }
}
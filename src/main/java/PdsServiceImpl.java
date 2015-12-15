import java.util.ArrayList;
import java.util.List;

public class PdsServiceImpl {
    private static Node node;

    public static void setNode(Node _node) {
        node = _node;
    }

    public Object[] getHosts(String ipPort) {
        String[] ipPorts = node.getIpPorts();

        NodeInfo newNode = new NodeInfo();
        newNode.setIp(ipPort);
        node.getDictionary().add(newNode);

        System.out.println("joining request from " + ipPort);

        return ipPorts;
    }

    public String echo(String echo) {
        System.out.println(echo);
        return echo;
    }

    public void addNewHost(String ipAndPort) {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setIp(ipAndPort);
        node.getDictionary().add(nodeInfo);
    }
}

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PdsServiceImpl {
    private static Node node;

    public static void setNode(Node _node) {
        node = _node;
    }

    public Object[] getHosts(String ipAndPort) {
        //String[] ipPorts = node.getIpPorts();
        Object[] ipPorts = node.getIpPorts();

        NodeInfo newNode = new NodeInfo();
        newNode.setIp(ipAndPort);
        node.getDictionary().add(newNode);

        System.out.println("joining request from " + ipAndPort);

        return ipPorts;
    }

    public String echo() {
        node.start();
        //System.out.println(echo);
        String response = "Ok";
        return response;
    }

    public void addNewHost(String ipAndPort) {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setIp(ipAndPort);
        node.getDictionary().add(nodeInfo);
    }

    public void DelHost(String DelIpPort) {
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
    }

    public String isAlive() {
        node.start();
        String response = "Ok";
        return response;
    }

    public void masterMessage(String ipPort) {
        NodeInfo masterNodeInfo = new NodeInfo();
        masterNodeInfo.setIp(ipPort);
        node.setMasterNode(masterNodeInfo);
        System.out.println("master node is " + masterNodeInfo.getIp());
    }

    public void loop() {
        try {
            Thread.sleep(1000);                 //1000 milliseconds is one second.
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        node.tram();
    }

    public String getString() {
        String masterStr = "wwwww";
        return masterStr;
    }

    public boolean receiveElectionMsg(String Ip) {
        node.startBullyElection();
        return true;
    }
}

    public void setMasterNode(String ipAndPort) {
        node.setMasterNode(ipAndPort);
    }
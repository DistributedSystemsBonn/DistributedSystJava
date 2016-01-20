import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PdsServiceImpl {
    private static Node node;

    public static void setNode(Node _node) {
        node = _node;
    }

    public Object[] getHosts(String ipPort, UUID newId) {
        //String[] ipPorts = node.getIpPorts();
        //UUID[] ids = node.getIds();

        Object[] backInfo = new Object[node.getDictionary().size()+1];
        Object[] interimNodeInfo = new Object[2];
        interimNodeInfo[0] = node.getSelf().getIp();
        interimNodeInfo[1] = node.getSelf().getId();
        backInfo[0] = interimNodeInfo;

        for (int i = 0; i < node.getDictionary().size(); i++) {
            interimNodeInfo = new Object[2];
            interimNodeInfo[0] = node.getDictionary().get(i).getIp();
            interimNodeInfo[1] = node.getDictionary().get(i).getId();
            backInfo[i+1] = interimNodeInfo;
        }

        NodeInfo newNode = new NodeInfo();
        newNode.setIp(ipPort);
        newNode.setId(newId);
        node.getDictionary().add(newNode);

        System.out.println("joining request from " + ipPort);

        return backInfo;
    }

    public String echo() {
        node.start();
        //System.out.println(echo);
        String response = "Ok";
        return response;
    }

    public void addNewHost(String ipAndPort, UUID id) {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setIp(ipAndPort);
        nodeInfo.setId(id);
        node.getDictionary().add(nodeInfo);
    }

    public void DelHost(String DelIpPort) {
        String[] ipPorts = node.getIpPorts();
        int DelIpIndex = 0;
        for (String ipPort : ipPorts) {
            if (ipPort.equals(DelIpPort)) {
                break;
            }
            DelIpIndex=DelIpIndex+1;
        }
        node.getDictionary().remove(DelIpIndex);

        System.out.println(DelIpPort + " was signed off");
    }

    public String IsAlive () {
        node.start();
        String response = "Ok";
        return response;
    }

    public void masterMessage(String ipPort, UUID id) {
        NodeInfo masterNodeInfo = new NodeInfo();
        masterNodeInfo.setIp(ipPort);
        masterNodeInfo.setId(id);
        node.setMasterNode(masterNodeInfo);
        System.out.println("master node is " + masterNodeInfo.getIp());
    }

    public void loop() {
        try {
            Thread.sleep(1000);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        node.tram();
    }

    public String getString() {
        String masterStr = "wwwww";
        return masterStr;
    }
}
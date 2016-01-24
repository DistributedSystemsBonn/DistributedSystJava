import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.*;

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

    public void getStartMsg(final boolean isRicart) {
        new Thread() {
            public void run() {
                node.startProcess(isRicart);
            }
        }.start();
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

    public String readResource(String ipPort) {
        System.out.println("\nRead resource from: " + ipPort);
        System.out.println("::Current String:: " + node.getResource());
        return node.getResource();
    }

    public void updateResource(String updateStr, String ipPort) {
        node.setResource(updateStr);
        System.out.println("\nUpdate resource from: " + ipPort);
        System.out.println("::String:: " + node.getResource());
    }

    public void getSyncRequestCT(long id, String ipPort) {
        Request request = new Request(0, 0, id, ipPort);

        System.out.println("Master: get request from " + id);
        if (node.state == Node.State.Released) {
            sendAcceptResponse(ipPort);
        } else if (node.state == Node.State.Held) {
            node.addRequest(request);
        }
    }

    protected void sendAcceptResponse(String ipPort) {
        Host pds = node.clientFactoryPDS.getClient(ipPort);
        node.state = Node.State.Held;
        pds.getAcceptResponseCT();
    }

    public void getReleasedMsgCT(long id, String fromIpAndPort) {
        /*
        * LogHelper.WriteStatus("Master: Released from " + fromIpAndPort);
            _module.State = AccessState.Released;

            var next = _module.PopRequest();
            if (next != null)
            {
                SendAcceptResponse(next.IpAndPort);
            }
        * */

        System.out.println("Master: Released from " + fromIpAndPort);
        node.state = Node.State.Released;
        Request next = node.popRequest();
        if (next != null) {
            sendAcceptResponse(next.getIpPort());
        }
    }

    public void getAcceptResponseCT() {
        System.out.println("Accepted for resource");
        node.isAllowedCT.set();
    }

    public void getSyncRequestRA(int timestamp, long id, String ipAndPort)
    {
        System.out.println("SERVER: RECV " + node.getSelf().getIp() + " FROM: " + ipAndPort + " TIME: " + timestamp);
        // create request object
        Request request = new Request(node.getSelf().getId(), timestamp, id, ipAndPort);

        if (node.state != Node.State.Held && !node.isInterested())
        {
            //Send accept msg to callee
            sendAcceptResponse_RA(ipAndPort);
        }
        else if (node.state == Node.State.Held)
        {
            node.addRequest(request);
        }
        else if (node.isInterested())
        {
            if (node.clock.compareTime(timestamp, id))
            {   // request timestamp is smaller than this node's timestamp.
                //Send accept msg to callee
                sendAcceptResponse_RA(ipAndPort);
            }
            else
            {
                node.addRequest(request);
            }
        }
        node.clock.receiveEventHandle(timestamp);
    }

    public void getAcceptResponseRA(String fromIpAndPort, int timestamp) {
        String myIp = node.getSelf().getIp();

        node.removeFromAcceptList(fromIpAndPort);

        //check if all accept messages received. if yes, start accessing to resource
        if (node.isGotAllOk()) {
            System.out.println("RESET. GOT ALL AT: " + myIp);
            node.hasGotAllMessagesBack.set();
        }

        //Clock: recive handle
        node.clock.receiveEventHandle(timestamp);
    }

    public void sendAcceptResponse_RA(String ipAndPort)
    {
        //Send event in clock
        node.clock.sendEventHandle();
        String myIp = node.getSelf().getIp();
        System.out.println("SERVER: " + myIp + " SEND OK TO: " + ipAndPort);

        synchronized (Shared.SendLock) {
            Host pds = node.clientFactoryPDS.getClient(ipAndPort);
            //send accept response with parameter which describes our host
            pds.getAcceptResponseRA(node.getSelf().getIp(), node.clock.Value);
        }
    }
}
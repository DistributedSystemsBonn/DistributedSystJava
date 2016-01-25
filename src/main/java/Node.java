import java.net.URL;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.log4j.net.SyslogAppender;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;

public class Node {

    private NodeInfo self;

    private String masterNode;

    private List<NodeInfo> dictionary;

    public ClientFactoryPDS clientFactoryPDS;

    private String resource;

    private Queue<Request> masterQueue;

    private long startTime;

    private static final long MAX_DURATION = 20000;

    private Object lock = new Object();

    public boolean isInterested() {
        return isInterested;
    }

    public void setInterested(boolean interested) {
        clock.localEventHandle();
        isInterested = interested;
    }

    public enum State {Released, Requested, Held}

    public State state;

    public List<String> acceptList;

    public ManualResetEvent isAllowedCT = new ManualResetEvent(false);
    public ManualResetEvent isElectionFinished = new ManualResetEvent(false);
    public ManualResetEvent hasGotAllMessagesBack = new ManualResetEvent(false);

    boolean _isElectionFinishedBully;

    public ExtendedLamportClock clock;

    private boolean isInterested;

    public Node(String ip) {
        self = new NodeInfo(); // consist of IP (method getIp()) and id (method getId()) of the node
        self.setIp(ip);
        dictionary = new ArrayList<NodeInfo>(); // consist of information about all nodes in the network
        clientFactoryPDS = new ClientFactoryPDS();
        resource = "";
        masterQueue = new LinkedList<Request>();

        state = State.Released;

        acceptList = new ArrayList<String>();

        clock = new ExtendedLamportClock(self.getId());

        isInterested = false;
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
            System.out.println("joined to " + ipPort);
        } catch (Exception ex) {
            System.out.println("error - cannot access the node"); // if cannot access the node output an error
        }
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

    public void start(final boolean isRicart) {
        boolean isMasterElected = false;
        while (!isMasterElected) {
            for (NodeInfo nodeInfo : dictionary) {
                Host pds = clientFactoryPDS.getClient(nodeInfo.getIp());
                pds.getStartMsg(isRicart);
            }
            isMasterElected = true;

            new Thread() {
                public void run() {
                    startProcess(isRicart);
                }
            }.start();

            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {

            }

            new Thread() {
                public void run() {
                    startBullyElection(); // start of the master node election
                }
            }.start();
        }
    }

    public void startBullyElection() {
        // Creating an array of IDs that are bigger then this one
        List<Long> nodeIDs = new ArrayList<Long>();
        for (NodeInfo nodeInfo : dictionary) {
            if (getSelf().getId() < nodeInfo.getId()) { // isolation the nodes with higher id
                nodeIDs.add(nodeInfo.getId());
            }
        }
        _isElectionFinishedBully = false;
        if (nodeIDs.size() != 0) {
            Long[] nodeIDsArray = new Long[nodeIDs.size()];
            nodeIDs.toArray(nodeIDsArray);
            Arrays.sort(nodeIDsArray);
            // end of creating
            boolean flag = false;
            boolean msg;
            for (long id : nodeIDsArray) {
                msg = sendElectionMsg(getIpPortById(id));
                try {
                    Thread.sleep(1000); // 1000 milliseconds is one second.
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                flag |= msg; // computing if there any node with higher id online
            }
            if (!flag) {
                _isElectionFinishedBully = true;
            }
        } else {
            _isElectionFinishedBully = true;
        }
        if (_isElectionFinishedBully) {
            System.out.println("This node is a master node");
            setMasterNode(self.getIp());
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

    protected void waitRandomTime() {
        Random r = new Random();
        try {
            Thread.sleep(r.nextInt(2000) + 2001); //wait from 2s to 4s
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    protected boolean isMasterNode() {
        return masterNode.equals(self.getIp());
    }

    public void startProcess(boolean isRicart) {

        System.out.println("##### Start process using " + (isRicart ? "Ricart & Agrawala " : "Centralized ")
                + "Algorithm.");
        System.out.println("# 0. Waiting for the Master Election #");

        isElectionFinished.reset();

        // wait the end of master election - which will be done by other thread.

        if (!_isElectionFinishedBully) {
            try {
                isElectionFinished.waitOne();
            } catch (Exception ex) {
                System.out.println("Some problem with isElectionFinished.waitOne()");
            }
        }

        System.out.println("# Master Election ended. Now Start Read & Update Process.");

        if (isMasterNode()) {
            return;
        }

        int count = 0;

        startTime = System.currentTimeMillis();

        while (true) {
            count++;
            waitRandomTime();

            processResourceFromMasterNode(isRicart);
            long executeTime = System.currentTimeMillis();

            if (executeTime - startTime > MAX_DURATION) {
                if (isRicart && !isMasterNode()) {
                    releaseRA();
                }
                System.out.println("Exited loop with " + (executeTime - startTime));
                break;
            }
        }

        String finalString = null;
        try {
            Thread.sleep(5000);
            finalString = readFromMasterNode();
        } catch (Exception ignored) {

        }
        System.out.println("\n===============================");
        System.out.println("Final string: " + finalString);
        System.out.println("===============================");

//        try {
//            Host pds = clientFactoryPDS.getClient(masterNode);
//            String masterString = pds.readResource(self.getIp());
//            masterString += self.getId();
//            pds.updateResource(masterString, self.getIp());
//        } catch (Exception ex) {
//            return false;
//        }
//        return true;
    }

    private void sendSyncMsg(NodeInfo toNode) {

        int logicClockTs = clock.sendEventHandle();

        acceptList.add(toNode.getIp());

        synchronized (Shared.SendLock) {
            Host pds = clientFactoryPDS.getClient(toNode.getIp());
            pds.getSyncRequestRA(logicClockTs, self.getId(), self.getIp());
        }
    }

    public void processResourceFromMasterNode(boolean isRicart) {

        if (isRicart) {
            List<NodeInfo> hostListWithoutMaster = new ArrayList<NodeInfo>();
            for (NodeInfo nodeInfo : dictionary) {
                if (nodeInfo.getIp().equals(masterNode)) continue;
                hostListWithoutMaster.add(nodeInfo);
            }

            hasGotAllMessagesBack.reset();
            state = State.Requested;
            isInterested = true;

            System.out.println("Client: [" + self.getIp() + "] Current timestamp: "/* + _module.Clock.Value*/);
            System.out.println("Client: [" + self.getIp() + "] Capacity: " + hostListWithoutMaster.size());

            for (final NodeInfo nodeInfo : hostListWithoutMaster) {
                System.out.println(" CLIENT: SEND REQ FROM: [" + self.getIp() + "] TO: [" +
                        nodeInfo.getId() + "]");
                new Thread() {
                    public void run() {
                        sendSyncMsg(nodeInfo);
                    }
                }.start();
            }

            //wait until receive all messages. .Set() method is called in RicartSyncAlgServer
            try {
                hasGotAllMessagesBack.waitOne();
            } catch (Exception ignored) {

            }

            System.out.println("CLIENT: RECV ALL ACCEPT MESSAGES AT: [" + self.getIp() + "]");
            state = State.Held;

        } else {

            isAllowedCT.reset();

            new Thread() {
                public void run() {
                    Host pds = clientFactoryPDS.getClient(masterNode);
                    pds.getSyncRequestCT(Long.toString(self.getId()), self.getIp());
                }
            }.run();

            try {
                isAllowedCT.waitOne();
            } catch (Exception ex) {
                System.out.println("some problem with isAllowedCT.waitOne()");
            }
        }

        long executeTime = System.currentTimeMillis();
        String fromMaster = "";

        if (executeTime - startTime < MAX_DURATION) {
            try {
                fromMaster = readFromMasterNode();
            } catch (Exception ex) {
                System.out.println("Cannot READ from the same node");
            }

            String fruit = getRandomFruitAndNumber();

            String concatenated = fromMaster.concat(fruit);

            try {
                updateMasterNodeResource(concatenated);
            } catch (Exception ex) {
                System.out.println("Cannot UPDATE from the same node");
            }
        }

        if (isRicart) {
            releaseRA();
        } else {
            Host pds = clientFactoryPDS.getClient(masterNode);
            pds.getReleasedMsgCT(Long.toString(self.getId()), self.getIp());
        }
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

        isElectionFinished.set();
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

    protected String getIpPortById(long id) {
        for (NodeInfo nodeInfo : dictionary) {
            if (nodeInfo.getId() == id) {
                return nodeInfo.getIp();
            }
        }

        return "";
    }

    protected String readFromMasterNode() throws Exception {
        if (isMasterNode()) throw new Exception();

        Host pds = clientFactoryPDS.getClient(masterNode);

        return pds.readResource(self.getIp());
    }

    protected String getRandomFruitAndNumber() {
        Random r = new Random();
        String[] fruits = {"apple", "mango", "papaya", "banana", "guava", "pineapple"};
        return fruits[r.nextInt(fruits.length)] + r.nextInt();
    }

    protected void updateMasterNodeResource(String str) throws Exception {
        if (isMasterNode()) throw new Exception();
        synchronized (this) {
            Host pds = clientFactoryPDS.getClient(masterNode);
            pds.updateResource(str, self.getIp());
        }

        System.out.println("Updated string To Master Node with IP: " + masterNode);
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void addRequest(Request request) {
        masterQueue.add(request);
    }

    public Request popRequest() {
        Request firstNode = null;
        if (masterQueue.size() > 0) {
            firstNode = masterQueue.poll();
            System.out.println("Server: Remove request from queue: " + firstNode.getCallerId());
        }
        return firstNode;
    }

    public List<Request> QueueRA = new ArrayList<Request>();

    public void removeFromAcceptList(String ipPort) {
        System.out.println("SERVER: " + self.getId() + " REMOVE IP: " + ipPort);

        try {
            if (!acceptList.remove(ipPort)) {
                throw new Exception("Element in accept list doesnt exist: " + ipPort);
            }
        } catch (Exception ex) {
            System.out.println("Element in accept list doesnt exist: " + ipPort);
        }
    }

    public boolean isGotAllOk() {
        return acceptList.size() == 0;
    }

    public void releaseRA() {

        state = State.Released;
        isInterested = false;

        for (Request request : QueueRA) {
            sendAcceptResponse(request.getIpPort());
        }

        QueueRA.clear();

        System.out.println("Client: Released resource at [" + self.getIp() + "]");
    }


    public void sendAcceptResponse(String ipAndPort) {

        clock.sendEventHandle();
        System.out.println("SERVER: " + self.getIp() + " SEND OK TO: " + ipAndPort);
        synchronized (Shared.SendLock) {
            Host pds = clientFactoryPDS.getClient(ipAndPort);
            pds.getAcceptResponseRA(self.getIp(), clock.Value);
        }
    }
}
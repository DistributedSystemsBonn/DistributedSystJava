import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface Host {
    public Object[] getHosts(String ipAndPort); // get information about the nodes in the network
    public void addNewHost(String ipAndPort); // add to some node in the network information about this node
    public boolean signOff(String ipPort); // delete from some node in the network information about this node

    void getStartMsg(boolean isRicartAlgorithm);
    boolean receiveElectionMsg(String id); // start election algorithm on some node in the network
    void setMasterNode(String ipAndPort);

    String readResource(String ipAndPort);  // read the resource of the master node
    void updateResource(String updateStr, String ipAndPort);  // read the resource of the master node

    void getSyncRequestCT(String id, String ipAndPort);
    void getReleasedMsgCT(String id, String fromIpAndPort);
    void getAcceptResponseCT();

    void getSyncRequestRA(String timestamp, String id, String ipAndPort);
    void getAcceptResponseRA(String fromIpAndPort, String timestamp);
}
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface Host {
    public Object[] getHosts(String ipAndPort);
    public void addNewHost(String ipAndPort);
    public boolean signOff(String ipPort);

    void getStartMsg(boolean isRicartAlgorithm);
    boolean receiveElectionMsg(String id);
    void setMasterNode(String ipAndPort);

    String readResource(String ipAndPort);
    void updateResource(String updateStr, String ipAndPort);

    void getSyncRequestCT(long id, String ipAndPort);
    void getReleasedMsgCT(long id, String fromIpAndPort);
    void getAcceptResponseCT();

    void getSyncRequestRA(int timestamp, long id, String ipAndPort);
    void getAcceptResponseRA(String fromIpAndPort, int timestamp);
}
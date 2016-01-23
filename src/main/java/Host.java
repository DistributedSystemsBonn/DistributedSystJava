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
}
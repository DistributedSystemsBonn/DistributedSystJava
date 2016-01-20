import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface Host {
    //public Object[] getHosts(String ipAndPort, UUID newId);
    public Object[] getHosts(String ipAndPort);
    public String echo();
    public void addNewHost(String ipAndPort);
    public void DelHost(String ipPort);
    public String isAlive();
    public void masterMessage(String ipPort, UUID id);
    public void loop();
    public String getString();
}
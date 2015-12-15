import java.util.List;

public interface Host {
    public Object[] getHosts(String ipAndPort);
    public String echo(String echo);
    public void addNewHost(String ipAndPort);
}

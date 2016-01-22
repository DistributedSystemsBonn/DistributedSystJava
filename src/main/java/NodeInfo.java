import java.util.UUID;

public class NodeInfo {
    private String ip;
    private String id;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
        String[] parts = ip.split(":");
        this.initId(parts[0], Integer.parseInt(parts[1]));
    }

    public void initId(String ip, int port) {
        String[] parts = ip.split("\\.");
        String id = "";

        for (String part : parts) id += part;
        id += Integer.toString(port);

        this.id = id;
    }

    public String getId() {
        return id;
    }
}

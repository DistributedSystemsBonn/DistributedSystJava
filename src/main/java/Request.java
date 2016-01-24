public class Request {
    private long id;
    private long time;
    private long callerId;
    private String ipPort;

    public Request(long id, long time, long callerId, String ipPort) {
        this.id = id;
        this.time = time;
        this.callerId = callerId;
        this.ipPort = ipPort;
    }

    public long getCallerId() {
        return callerId;
    }

    public void setCallerId(long callerId) {
        this.callerId = callerId;
    }

    public String getIpPort() {
        return ipPort;
    }

    public void setIpPort(String ipPort) {
        this.ipPort = ipPort;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}

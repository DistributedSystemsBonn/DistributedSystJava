public class EchoServiceImpl implements EchoService {
    private volatile int index = 1;

    public void echo(String msg) {
        System.out.println(index+": "+msg);
        index++;
    }
}
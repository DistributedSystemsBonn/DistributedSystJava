public class ExtendedLamportClock {
    private long _localId;          // Machine ID
    public int Value; // Logical Clock

    public ExtendedLamportClock(long localId) {
        _localId = localId;
    }

    private void Increment() {
        ++Value;
    }

    public int SendEventHandle() {
        Increment();
        return Value;
    }

    public int LocalEventHandle() {
        Increment();
        return Value;
    }

    public int ReceiveEventHandle(int candidateValue) {
        if (candidateValue > Value) {
            Value = candidateValue;
        }
        Increment();
        System.out.println("UPDATE CLOCK TO: " + Value);
        return Value;
    }

    public boolean CompareTime(int requestLamportClock, long remoteId) {
        if (remoteId == _localId) {
            System.out.println("*** remoteID and localID is same:: " + remoteId);
        }
        if (requestLamportClock < Value) {
            return true;
        }
        if (requestLamportClock > Value) {
            return false;
        }
        return remoteId < _localId;
    }
}
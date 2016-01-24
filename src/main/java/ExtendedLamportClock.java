public class ExtendedLamportClock {
    private long _localId;  // Machine ID
    public int Value; // Logical Clock

    public ExtendedLamportClock(long localId) {
        _localId = localId;
    }

    public int sendEventHandle() {
        ++Value;
        return Value;
    }

    public int localEventHandle() {
        ++Value;
        return Value;
    }

    public int receiveEventHandle(int candidateValue) {
        if (candidateValue > Value) {
            Value = candidateValue;
        }
        ++Value;
        System.out.println("UPDATE CLOCK TO: " + Value);
        return Value;
    }

    public boolean compareTime(int requestLamportClock, long remoteId) {
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
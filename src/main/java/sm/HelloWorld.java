package sm;

import net.e6tech.elements.common.resources.Initializable;
import net.e6tech.elements.common.resources.Resources;

public class HelloWorld implements Initializable {

    private Thread busyThread;
    private String message;
    private long waitTime;

    @Override
    public void initialize(Resources resources) {
        if (busyThread != null) busyThread.interrupt();
        busyThread = new Thread(() -> {
            try {
                while (true) {
                    System.out.println(message);
                    Thread.sleep(waitTime);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        busyThread.start();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }
}

package io.github.cadenceoss.iwf.spring;

public class SingletonWorkerService {
    private static Worker worker;

    public static void startWorkerIfNotUp() {
        if (worker == null) {
            worker = new Worker();
            worker.start();
        }
    }
}

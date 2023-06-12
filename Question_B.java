package PROJECT;

public class Question_B {
    static class SharedVariables {
        int A1;
        int B1;
        int B2;
        int A2;
        int B3;
        int A3;
    }

    static class ThreadA extends Thread {
        private final SharedVariables sharedVariables;
        private final Object lockObject;

        public ThreadA(SharedVariables sharedVariables, Object lockObject) {
            this.sharedVariables = sharedVariables;
            this.lockObject = lockObject;
        }

        @Override
        public void run() {
            synchronized (lockObject) {
                sharedVariables.A1 = Utility.sumUpToN(100);
                lockObject.notifyAll();
                try {
                    lockObject.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sharedVariables.A2 = sharedVariables.B2 + Utility.sumUpToN(400);
                lockObject.notifyAll();
                try {
                    lockObject.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sharedVariables.A3 = sharedVariables.B3 + Utility.sumUpToN(600);
                lockObject.notifyAll();
            }
        }
    }

    static class ThreadB extends Thread {
        private final SharedVariables sharedVariables;
        private final Object lockObject;

        public ThreadB(SharedVariables sharedVariables, Object lockObject) {
            this.sharedVariables = sharedVariables;
            this.lockObject = lockObject;
        }

        @Override
        public void run() {
            synchronized (lockObject) {
                try {
                    lockObject.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sharedVariables.B1 = sharedVariables.A1 + Utility.sumUpToN(200);
                lockObject.notifyAll();
                try {
                    lockObject.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sharedVariables.B2 = Utility.sumUpToN(300);
                lockObject.notifyAll();
                try {
                    lockObject.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sharedVariables.B3 = sharedVariables.A2 + Utility.sumUpToN(500);
                lockObject.notifyAll();
            }
        }
    }

    static class Utility {
        public static int sumUpToN(int n) {
            return n * (n + 1) / 2;
        }
    }

    public static void main(String[] args) {
        SharedVariables sharedVariables = new SharedVariables();
        Object lockObject = new Object();

        ThreadA threadA = new ThreadA(sharedVariables, lockObject);
        ThreadB threadB = new ThreadB(sharedVariables, lockObject);

        threadA.start();
        threadB.start();

        try {
            threadA.join();
            threadB.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Final values of shared variables:");
        System.out.println("A1: " + sharedVariables.A1);
        System.out.println("B1: " + sharedVariables.B1);
        System.out.println("B2: " + sharedVariables.B2);
        System.out.println("A2: " + sharedVariables.A2);
        System.out.println("B3: " + sharedVariables.B3);
        System.out.println("A3: " + sharedVariables.A3);
    }
}

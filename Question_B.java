package PROJECT;

public class Question_B {

    static class SharedVariables {
        int a1 = 0;
        int b1 = 0;
        int b2 = 0;
        int a2 = 0;
        int b3 = 0;
        int a3 = 0;
    }

    public static void main(String[] args) {
        SharedVariables sharedVariables = new SharedVariables();
        Object lockObject = new Object();

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lockObject) {
                    // calculate a1
                    sharedVariables.a1 = sumUpToN(100);
                    
                    // calculate b1 using a1
                    sharedVariables.b1 = sharedVariables.a1 + sumUpToN(200);
                    lockObject.notify(); // notify thread2 to execute b2
                    
                    try {
                        lockObject.wait(); // wait for thread2 to finish executing b2
                        
                        // calculate a2 using b2
                        sharedVariables.a2 = sharedVariables.b2 + sumUpToN(400);
                        lockObject.notify(); // notify thread2 to execute b3
                        
                        lockObject.wait(); // wait for thread2 to finish executing b3
                        
                        // calculate a3 using b3
                        sharedVariables.a3 = sharedVariables.b3 + sumUpToN(600);
                        lockObject.notify(); // notify thread2 to finish execution
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lockObject) {
                    try {
                        lockObject.wait(); // wait for thread1 to execute a1
                        
                        // calculate b2
                        sharedVariables.b2 = sumUpToN(300);
                        lockObject.notify(); // notify thread1 to continue and execute b3
                        
                        lockObject.wait(); // wait for thread1 to execute b3
                        
                        // calculate b3 using a2
                        sharedVariables.b3 = sharedVariables.a2 + sumUpToN(500);
                        lockObject.notify(); // notify thread1 to finish execution
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // display final values of shared variables
        System.out.println("final values of shared variables:");
        System.out.println("a1: " + sharedVariables.a1);
        System.out.println("b1: " + sharedVariables.b1);
        System.out.println("b2: " + sharedVariables.b2);
        System.out.println("a2: " + sharedVariables.a2);
        System.out.println("b3: " + sharedVariables.b3);
        System.out.println("a3: " + sharedVariables.a3);
    }

    // utility method to calculate the sum of numbers up to N
    public static int sumUpToN(int n) {
        return n * (n + 1) / 2;
    }
}

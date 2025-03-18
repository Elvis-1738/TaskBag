import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * Worker process that retrieves and executes a single task from the TaskBag.
 * Each worker runs independently and processes exactly one task before exiting.
 */
public class Worker {
    public static void main(String[] args) {
        try {
            // Connect to the RMI registry to retrieve the TaskBag remote object
            Registry registry = LocateRegistry.getRegistry("localhost", 2099);
            TaskBag taskBag = (TaskBag) registry.lookup("TaskBag");

            System.out.println("Worker Client connected to TaskBag on port 2099");

            // Check how many tasks are available
            //This prevents workers from waiting indefinitely for tasks that don't exist.
            int availableTasks = taskBag.getTaskCount("Task");
            System.out.println("Available tasks in TaskBag: " + availableTasks);

            // If no tasks are left, notify the worker and exit
            if (availableTasks == 0) {
                System.out.println("No more tasks left for processing. Worker is exiting...");
                return; // Exit worker process immediately
            }

            // Retrieve a single available task
            List<Integer> range = taskBag.pairIn("Task");

            // Double-check if the task retrieval failed (shouldn't happen, but added for safety)
            if (range == null || range.isEmpty()) {
                System.out.println("No more tasks left for processing. Worker is exiting...");
                return; // Exit worker process
            }

            // Display the task batch assigned to this worker
            System.out.println("Worker processing task from " + range);

            // Process the numbers to identify prime numbers
            List<Integer> primes = findPrimes(range);

            // If no primes found, notify and exit
            if (primes.isEmpty()) {
                System.out.println("Worker found no primes in this task.");
            } else {
                // Send computed prime numbers back to TaskBag
                taskBag.pairOut("Primes", primes);
                System.out.println("Worker has sent primes to TaskBag: " + primes);
            }

            // Worker exits after processing one task
            System.out.println("Worker has completed processing and is exiting...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Identifies prime numbers within a given list.
     * @param range List of integers to check for primality.
     * @return List of prime numbers found in the given range.
     */
    private static List<Integer> findPrimes(List<Integer> range) {
        List<Integer> primes = new java.util.ArrayList<>();
        for (int num : range) {
            if (isPrime(num)) primes.add(num);
        }
        return primes;
    }

    /**
     * Checks if a number is prime.
     * @param num The number to be checked.
     * @return True if the number is prime, false otherwise.
     */
    private static boolean isPrime(int num) {
        if (num < 2) return false;// This line handles the base cases. 2 is the first prime number, anything else is false.
        for (int i = 2; i * i <= num; i++) {
            if (num % i == 0) return false;
        }
        return true;
    }
}

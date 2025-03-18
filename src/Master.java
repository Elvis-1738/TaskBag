import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Master process that distributes tasks to workers through the TaskBag using Java RMI.
 * This class sets task parameters, assigns tasks to the TaskBag, and collects results.
 */
public class Master {
    public static void main(String[] args) {
        try {
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            int MAX, GRANULARITY;

            // Prompt user to enter the maximum value for task distribution
            while (true) {
                System.out.print("Enter the maximum value for task distribution: ");
                if (scanner.hasNextInt()) {
                    MAX = scanner.nextInt();
                    if (MAX > 0) break;
                }
                System.out.println("Error: Please enter a valid positive integer.");
                scanner.nextLine();
            }

            // Prompt user to enter the granularity (task batch size)
            while (true) {
                System.out.print("Enter the granularity (number of tasks per batch): ");
                if (scanner.hasNextInt()) {
                    GRANULARITY = scanner.nextInt();
                    if (GRANULARITY > 0) break;
                }
                System.out.println("Error: Please enter a valid granularity.");
                scanner.nextLine();
            }

            scanner.close(); // Close scanner to prevent resource leak

            System.out.println("Using MAX = " + MAX + ", GRANULARITY = " + GRANULARITY);
            
            // Connect to the RMI registry and locate the TaskBag remote object
            Registry registry = LocateRegistry.getRegistry("localhost", 2099);
            TaskBag taskBag = (TaskBag) registry.lookup("TaskBag");//retrieves the remote TaskBag object from the registry using the name "TaskBag".

            System.out.println("BSE1 Master Client connected to TaskBag on port 2099");

            // **Set task parameters in TaskBag** to ensure all workers get the correct values
            //This ensures that all worker processes have the same task configuration.
            taskBag.setTaskParameters(MAX, GRANULARITY);

            // Distribute tasks in batches of GRANULARITY size
            for (int i = 0; i < MAX; i += GRANULARITY) {
                List<Integer> range = new ArrayList<>();//holds all the numbers for the current batch.
                // Math.min(i + GRANULARITY, MAX) ensures that the loop doesn't go beyond MAX in the last batch.
                for (int j = i; j < Math.min(i + GRANULARITY, MAX); j++) {
                    range.add(j);
                }
                taskBag.pairOut("Task", range); // Send task batch to TaskBag
            }

            System.out.println("GroupBSE1 Master has distributed tasks up to: " + MAX);
            System.out.println("Waiting for workers to complete...");

            List<Integer> primes = new ArrayList<>(); //creates an empty array list named 'primes'
            int totalBatches = (MAX + GRANULARITY - 1) / GRANULARITY; //calculates the total number of task batches that will be sent to the worker processes.
            int retries = 20; // Number of times to check for results before exiting

            // Continuously retrieve results from TaskBag
            //This code block is responsible for continuously checking if the worker processes have finished their tasks and returned the prime numbers they found.
            //It implements a retry mechanism to avoid waiting indefinitely if the workers are slow.
            while (retries > 0) {
                int availablePrimes = taskBag.getTaskCount("Primes"); // Check if results exist
                if (availablePrimes == 0) {
                    System.out.println("No results yet, waiting...");
                    TimeUnit.SECONDS.sleep(2);// waiting for response per 2 seconds
                    retries--;
                    continue;
                }
                
                //This for loop is responsible for collecting the prime number results from the TaskBag after the worker processes have completed their calculations.
                for (int i = 0; i < totalBatches; i++) {
                    List<Integer> primesBatch = taskBag.pairIn("Primes");

                    if (primesBatch != null && !primesBatch.isEmpty()) {
                        primes.addAll(primesBatch);
                        System.out.println("GroupBSE1 Master received primes batch: " + primesBatch);
                    }
                }

                if (!primes.isEmpty()) break; // Exit loop if at least one result is received

                TimeUnit.SECONDS.sleep(2);
                retries--;
            }

            System.out.println("Final List of Primes found: " + primes);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

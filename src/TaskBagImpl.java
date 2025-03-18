import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the TaskBag interface.
 * This class serves as a shared task repository for distributed processing for both the Master and Worker processes.
 * Master prepares tasks and puts them here. Workers fetch tasks, process them, and store results using this implementation.
 */
public class TaskBagImpl extends UnicastRemoteObject implements TaskBag {
    private final Map<String, LinkedBlockingQueue<List<Integer>>> taskBag; // Stores tasks as key-value pairs
    private int nextTask = 0; // Keeps track of the next available task index
    private int MAX = 100;  // Default max value, will be updated by Master
    private int GRANULARITY = 10; // Default granularity, will be updated by Master

    /**
     * Constructor to initialize the TaskBag.
     * Uses a ConcurrentHashMap to store tasks in a thread-safe manner.
     */
    protected TaskBagImpl() throws RemoteException {
        super();
        taskBag = new ConcurrentHashMap<>();//Initializes the taskBag map with a thread-safe ConcurrentHashMap.
    }

    /**
     * Adds a task to the Task Bag.
     * @param key Identifier for the task
     * @param value List of integers representing the task
     */
    @SuppressWarnings("unused")
    @Override
    public void pairOut(String key, List<Integer> value) throws RemoteException {
        taskBag.computeIfAbsent(key, v -> new LinkedBlockingQueue<>()).offer(value);
        System.out.println("GroupBSE1 TaskBag: Added" + value + " under key '" + key + "'");
    }

    /**
     * Retrieves and removes a task from the Task Bag.
     * If no task is available, it waits for a short period before checking again.
     * @param key Identifier for the task
     * @return List of integers representing the retrieved task
     */
    @Override
    public List<Integer> pairIn(String key) throws RemoteException {
        try {
            LinkedBlockingQueue<List<Integer>> queue = taskBag.getOrDefault(key, new LinkedBlockingQueue<>());
            while (true) {
                List<Integer> task = queue.poll(1000, TimeUnit.MILLISECONDS); // Wait 1 sec for new tasks
                if (task != null) {
                    System.out.println("TaskBag: Retrieved " + task + " under key '" + key + "'");
                    return task;
                }
                if (nextTask >= MAX / GRANULARITY) { // Check if all tasks are completed
                    System.out.println("All tasks completed.");
                    return null;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * Reads a task from the Task Bag without removing it.
     * @param key Identifier for the task
     * @return List of integers representing the task
     */
    @Override
    public List<Integer> readPair(String key) throws RemoteException {
        return taskBag.getOrDefault(key, new LinkedBlockingQueue<>()).peek();
    }

    /**
     * Returns the number of tasks currently in the Task Bag.
     * @param key Identifier for the task
     * @return Number of tasks available
     */
    @Override
    public int getTaskCount(String key) throws RemoteException {
        return taskBag.getOrDefault(key, new LinkedBlockingQueue<>()).size();
    }

    /**
     * Retrieves the next available task index.
     * @return The next task index
     */
    @Override
    public synchronized int getNextTask() throws RemoteException {
        return nextTask;
    }

    /**
     * Increments the next task index when a worker picks up a task.
     */
    @Override
    public synchronized void updateNextTask() throws RemoteException {
        nextTask++;
    }

    /**
     * Returns the current MAX value set by the Master.
     * @return MAX value
     */
    @Override
    public synchronized int getMaxValue() throws RemoteException {
        return MAX;
    }

    /**
     * Returns the current GRANULARITY value set by the Master.
     * @return GRANULARITY value
     */
    @Override
    public synchronized int getGranularity() throws RemoteException {
        return GRANULARITY;
    }

    /**
     * Allows the Master to dynamically set MAX and GRANULARITY values.
     * @param max        The maximum value for task distribution
     * @param granularity The number of tasks per batch
     */
    @Override
    public synchronized void setTaskParameters(int max, int granularity) throws RemoteException {
        this.MAX = max;
        this.GRANULARITY = granularity;
        System.out.println("Updated TaskBag parameters: MAX = " + MAX + ", GRANULARITY = " + GRANULARITY);
    }

    /**
     * Starts the TaskBag service and binds it to the RMI registry.
     */
    public static void main(String[] args) {
        try {
            TaskBagImpl taskBag = new TaskBagImpl();
            TaskBag stub = taskBag;
            
            Registry registry = LocateRegistry.createRegistry(2099);
            registry.rebind("TaskBag", stub);

            System.out.println("GroupBSE1 TaskBagImplementer is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


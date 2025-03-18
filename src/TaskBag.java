// TaskBag.java (Remote Interface)
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote interface for TaskBag used in Java RMI.
 * This defines the methods that Master and Worker processes use to interact with the Task Bag.
 */

 public interface TaskBag extends Remote {

    /**
     * Adds a task to the Task Bag.
     * @param key Identifier for the task.
     * @param value List of integers representing the task.
     * @throws RemoteException If an RMI error occurs.
     */
    void pairOut(String key, List<Integer> value) throws RemoteException;

    /**
     * Retrieves and removes a task from the Task Bag.
     * @param key Identifier for the task.
     * @return List of integers representing the task.
     * @throws RemoteException If an RMI error occurs.
     */
    List<Integer> pairIn(String key) throws RemoteException;

    /**
     * Reads a task from the Task Bag without removing it.
     * @param key Identifier for the task.
     * @return List of integers representing the task.
     * @throws RemoteException If an RMI error occurs.
     */
    List<Integer> readPair(String key) throws RemoteException;

    /**
     * Returns the number of tasks currently in the Task Bag.
     * @param key Identifier for the tasks.
     * @return Number of tasks available.
     * @throws RemoteException If an RMI error occurs.
     */
    int getTaskCount(String key) throws RemoteException;


//Methods for Task Numbering
    /**
     * Gets the next available task index for workers.
     * @return The index of the next task.
     * @throws RemoteException If an RMI error occurs.
     */
    int getNextTask() throws RemoteException;

    /**
     * Increments the next task index.
     * This ensures each worker gets a unique task.
     * @throws RemoteException If an RMI error occurs.
     */
    void updateNextTask() throws RemoteException;

    /**
     * Retrieves the MAX value set by the Master.
     * @return The maximum range of numbers for prime calculation.
     * @throws RemoteException If an RMI error occurs.
     */
    int getMaxValue() throws RemoteException;

    /**
     * Retrieves the GRANULARITY value set by the Master.
     * @return The size of each task batch.
     * @throws RemoteException If an RMI error occurs.
     */
    int getGranularity() throws RemoteException;

// method to allow Master to set the task distribution parameters
    /**
     * 
     * Allows the Master to set task parameters (MAX and GRANULARITY).
     * @param max The maximum range of numbers for processing.
     * @param granularity The number of tasks per batch.
     * @throws RemoteException If an RMI error occurs.
     */
    void setTaskParameters(int max, int granularity) throws RemoteException;
}
package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FCFSScheduler {
	private Queue<Process> readyQueue;
	private List<Process> completedProcesses;
	private Process currentRunningProcess;

	public FCFSScheduler() {
		readyQueue = new ConcurrentLinkedQueue<>();
		completedProcesses = new ArrayList<>();
	}

	public void addProcess(Process process) {
		readyQueue.add(process);
	}

	public List<Process> getProcessesInQueue() {
		return new ArrayList<>(readyQueue);
	}

	public Process getCurrentRunningProcess() {
		return currentRunningProcess;
	}

	public void runScheduler() {
		while (true) {
			if (!readyQueue.isEmpty()) {
				Process currentProcess = readyQueue.poll();
				currentRunningProcess = currentProcess;

				while (!currentProcess.isCompleted()) {
					currentProcess.execute(1);
					System.out.println(currentProcess.getName());

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				System.out.println(currentProcess.getName() + "DONZO");

				completedProcesses.add(currentProcess);
				currentRunningProcess = null;
			}
		}
	}

	public List<Process> getCompletedProcesses() {
		return completedProcesses;
	}
}

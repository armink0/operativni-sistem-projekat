package application;

public class Process {
	private String name;
	private int executionTime;
	private int memoryRequirement;
	private State state;

	public enum State {
		WAITING, RUNNING, DONE
	}

	public Process(String name, int executionTime, int memoryRequirement) {
		this.name = name;
		this.executionTime = executionTime;
		this.memoryRequirement = memoryRequirement;
		this.state = State.WAITING;
	}

	public String getName() {
		return name;
	}

	public int getExecutionTime() {
		return executionTime;
	}

	public int getMemoryRequirement() {
		return memoryRequirement;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public void execute(int time) {
		executionTime -= time;
		
		if (executionTime <= 0) {
			state = State.DONE;
		} else {
			state = State.RUNNING;
		}
	}

	public boolean isCompleted() {
		return state == State.DONE;
	}
}

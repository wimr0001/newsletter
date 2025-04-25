package wr.newsletter;

import java.util.ArrayList;

public class ProcessResult {

    private static ProcessResult single_instance = null;
    private ArrayList<Integer> lineNumbers = new ArrayList<Integer>(10);
    private ArrayList<String> messages = new ArrayList<String>(10);
    private boolean actionExecuted = false;
    private int createdItems = 0;
    private int sequence = 0;
    private InputData inputData;

    public static synchronized ProcessResult getInstance() {
        if (single_instance == null) {
            single_instance = new ProcessResult();
        }
        return single_instance;
    }
    private ProcessResult() {}

    public void addNotification(int i, String msg) {
        lineNumbers.add(Integer.valueOf(i));
        messages.add(msg);
    }

    public ArrayList<Integer> getLineNumbers() {
        return lineNumbers;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    public boolean hasResultErrors() {
        return lineNumbers.size() > 0;
    }

    public String getMessage(int n) {
        if (n < messages.size()) {
            return messages.get(n);
        }
        return "Onbekend";
    }

	public boolean isActionExecuted() {
		return actionExecuted;
	}

	public void setActionExecuted(boolean actionExecuted) {
		this.actionExecuted = actionExecuted;
	}

	public int getCreatedItems() {
		return createdItems;
	}

	public void setCreatedItems(int createdItems) {
		this.createdItems = createdItems;
	}
	public void increaseCreatedItems() {
		this.createdItems++;
	}
	
    public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public void increaseSequence() {
		this.sequence++;
	}
	
	public InputData getInputData() {
		return inputData;
	}
	public void setInputData(InputData inputData) {
		this.inputData = inputData;
	}
	public void cleanup() {
    	this.createdItems = 0;
    	this.actionExecuted = false;
        lineNumbers = new ArrayList<Integer>(10);
        messages = new ArrayList<String>(10);
    }
}

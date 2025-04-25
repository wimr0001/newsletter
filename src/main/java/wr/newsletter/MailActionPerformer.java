package wr.newsletter;

public class MailActionPerformer implements Runnable {

	public void run() {
		MailAction action = MailAction.getInstance();
		ProcessResult processResult = action.getMailer().process(action.getSequence()+1);
		action.setSequence(processResult.getSequence());
		processResult.setActionExecuted(true);
		action.getMailFrame().processNext(processResult, action.getSequence());
	}
}

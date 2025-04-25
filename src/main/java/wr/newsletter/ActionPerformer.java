package wr.newsletter;

public class ActionPerformer implements Runnable {

	public void run() {
		PdfAction action = PdfAction.getInstance();
		ProcessResult processResult = action.getPdfCreator().process(action.getSequence() + 1);
		action.setSequence(processResult.getSequence());
		if (processResult.hasResultErrors()) {
			processResult.setActionExecuted(false);
		} else {
			processResult.setActionExecuted(true);
		}
		action.getPdfFrame().processNext(processResult, action.getSequence());
	}
}

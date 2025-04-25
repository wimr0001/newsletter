package wr.newsletter;

public class MailAction {

    private static MailAction single_instance;
    // 0 = init mail
    // 1 = next mail

    private int sequence = 0;
    private Mailer mailer;
    private MailFrame mailFrame;

    public static synchronized MailAction getInstance() {
        if (single_instance == null) {
            single_instance = new MailAction();
        }
        return single_instance;
    }

    public Mailer getMailer() {
        return mailer;
    }

    public void setMailer(Mailer m) {
        this.mailer = m;
    }

    public MailFrame getMailFrame() {
        return mailFrame;
    }

    public void setMailFrame(MailFrame frame) {
        this.mailFrame = frame;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
    public void addSequence() {
    	sequence++;
    }
}

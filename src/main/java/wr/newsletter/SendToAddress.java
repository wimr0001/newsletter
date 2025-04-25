package wr.newsletter;

public class SendToAddress {
    private String mailAddress;
    private String personalName;

    public SendToAddress(String aMailAddress, String aPersonalName ) {
        this.mailAddress = aMailAddress;
        this.personalName = aPersonalName;
    }

    public String getMailAddress() {
        return mailAddress;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    public String getPersonalName() {
        return personalName;
    }

    public void setPersonalName(String personalName) {
        this.personalName = personalName;
    }
}

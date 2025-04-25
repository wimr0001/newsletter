package wr.newsletter;

public class PdfAction {

	private static PdfAction single_instance;
	private int sequence = 0;
	private PdfCreator pdfCreator;
	private PdfFrame pdfFrame;

	public static synchronized PdfAction getInstance() {
		if (single_instance == null) {
			single_instance = new PdfAction();
		}
		return single_instance;
	}

	public PdfCreator getPdfCreator() {
		return pdfCreator;
	}

	public void setPdfCreator(PdfCreator pc) {
		this.pdfCreator = pc;
	}

	public PdfFrame getPdfFrame() {
		return pdfFrame;
	}

	public void setPdfFrame(PdfFrame pdfFrame) {
		this.pdfFrame = pdfFrame;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public void addSequence() {
		this.sequence++;
	}
}

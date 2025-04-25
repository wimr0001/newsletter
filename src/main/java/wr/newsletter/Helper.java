package wr.newsletter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.SimpleHash;
import wr.freemarker.Formatter;

public abstract class Helper {
	public static final String COLNAME_PDF = "pdf";
	public static final String COLNAME_SEQUENCE = "volgnr";
	public static final String COLNAME_MAIL = "email";


	protected static String formatContent(Formatter formatter, ArrayList<String> headers, ArrayList<String> cols,
			String templateName) throws Exception {
		return Helper.formatContent(formatter, headers, cols, templateName, null);

	}

	protected static String formatContent(Formatter formatter, ArrayList<String> headers, ArrayList<String> cols,
			String template, String cacheName) throws Exception {
		String personalizedContent = "";
		SimpleDateFormat simpleFormat = new SimpleDateFormat("dd-MM-yyyy");
		String dateStr = simpleFormat.format(new Date());
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("userdir", System.getProperty("user.dir"));
		map.put("actual_year", dateStr.substring(6));
		map.put("actual_date", dateStr);
		map.put("myutils", new wr.freemarker.FreemarkerUtil());
		int i = 0;
		for (String header : headers) {
			if (i < cols.size()) {
				map.put(header, cols.get(i));
			}
			i++;
		}
		DefaultObjectWrapper wrapper = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_33).build();
		if (cacheName == null) {
			personalizedContent = formatter.format(template, new SimpleHash(map, wrapper), wrapper);
		} else {
			personalizedContent = formatter.format(template, new SimpleHash(map, wrapper), wrapper, true, cacheName);
		}
		return personalizedContent;
	}

	protected static void html2pdf(String html, String output) throws Exception {
		File pdfDest = new File(output);
		// pdfHTML specific code
		ConverterProperties converterProperties = new ConverterProperties();
		HtmlConverter.convertToPdf(html, new FileOutputStream(pdfDest), converterProperties);
	}

	protected static void expandCols(InputData inputData, int size, int startSequence, boolean mailWithAttachment)
			throws Exception {
		int s = size;
		int len = 1;
		String leadingZero = "";
		int n = 0;
		if (startSequence == 0) {
			inputData.getHeaders().add(COLNAME_SEQUENCE);
			inputData.getHeaders().add(COLNAME_PDF);
			inputData.getHeaders().add(COLNAME_MAIL);
		}
		for (ArrayList<String> cols : inputData.getDataLines()) {
			if (n < startSequence) {
				n++;
				continue;
			}
			n++;
			len = Math.max(2, Integer.valueOf(size).toString().length());
			leadingZero = String.format("%0" + Integer.valueOf(len).toString() + "d", n);
			cols.add(leadingZero);
			if (mailWithAttachment) {
				cols.add("N");
			} else {
				cols.add("NA");
			}
			cols.add("N");
		}
	}
}

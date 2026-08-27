//package com.stream.application.pdfconvert;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.StringReader;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.List;
//
//import org.docx4j.fonts.IdentityPlusMapper;
//import org.docx4j.fonts.Mapper;
//import org.docx4j.fonts.PhysicalFont;
//import org.docx4j.fonts.PhysicalFonts;
//import org.docx4j.model.structure.SectionWrapper;
//import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
//
//import com.itextpdf.text.BaseColor;
//import com.itextpdf.text.Chunk;
//import com.itextpdf.text.Document;
//import com.itextpdf.text.DocumentException;
//import com.itextpdf.text.Font;
//import com.itextpdf.text.FontFactory;
//import com.itextpdf.text.pdf.PdfWriter;
//import com.stream.application.wordreplace.ThaiTextUtil;
//
//public class DocxToPdfConverter {
//	public static void main(String[]s) throws Exception {
//		Document document = new Document();
//		//Prepare data with ICU4J
//  		String content = new String(Files.readAllBytes(Paths.get("D:\\text.txt")), StandardCharsets.UTF_8);
//  		StringBuffer strBuffer = new StringBuffer();
//  		
//  		try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
//    		String line;
//    	    while ((line = reader.readLine()) != null) {
//    	        
//    	        strBuffer.append(ThaiTextUtil.addThaiWordBreakPreserveNewLine(line));
//    	        strBuffer.append("\n");
//    	    }
//    	} catch (IOException e) {
//    	    e.printStackTrace();
//    	}
//    	
//		
//		
//		PdfWriter.getInstance(document, new FileOutputStream("D://iTextHelloWorld.pdf"));
//
//		document.open();
//		Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
//		Chunk chunk = new Chunk(strBuffer.toString(), font);
//
//		document.add(chunk);
//		document.close();	
//	}
//}
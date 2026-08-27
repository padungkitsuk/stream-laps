package com.stream.application;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import com.stream.application.wordreplace.ThaiTextUtil;
import com.stream.application.wordreplace.WordReplaceUtil;


public class TestWordReplaceApp {
    
	public static void main(String[]s) {
//		System.out.println(ThaiTextUtil.addThaiWordBreakPreserveNewLine("test"));
		System.out.println("TETS....");
	}
	
  	public static void main2(String[]s) throws Exception {
  		
  		//Prepare data with ICU4J
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



    	System.out.println("======= Read Data:\n"+ThaiTextUtil.addThaiWordBreakPreserveNewLine("test"));
    	
    	
//    	WordReplaceUtil wordReplaceUtil = new WordReplaceUtil();
//
//    	//สิ่งที่พบ
////    	XWPFDocument doc = new XWPFDocument(new FileInputStream("D:\\template\\suggestion-th-template-001-02.docx"));
////    	wordReplaceUtil.replaceTextWithNewLine(doc, "Param1", "บ. ซินนามอน จก.");
////    	wordReplaceUtil.replaceTextWithNewLine(doc, "Param2", "A-2026-03  การใช้งานอุปกรณ์ป้องกันการย้อนกลับของไฟ (Flashback Arrestor)");
////    	wordReplaceUtil.replaceTextWithNewLine(doc, "Param3", "ประเภท:  Class A");
////    	wordReplaceUtil.replaceTextWithNewLine(doc, "Param4", ThaiTextUtil.addThaiWordBreakPreserveNewLine(strBuffer.toString()));
//////    	WordReplaceUtil.replaceImageInTable(doc, 	"Param5", "D:\\risknova.png", 300, 200);	//1 image
////    	WordReplaceUtil.replaceImageInTable(doc, "Param5", "D:\\risknova.png", 230, 150);	//2 image
////    	WordReplaceUtil.replaceImageInTable(doc, "Param6", "D:\\risknova.png", 230, 150);	//2 image
//
//    	//อ้างอิง
//    	XWPFDocument doc = new XWPFDocument(new FileInputStream("D:\\template\\suggestion-th-template-003-02.docx"));
//    	wordReplaceUtil.replaceTextWithNewLine(doc, "Param1", "บ. ซินนามอน จก.");
//    	wordReplaceUtil.replaceTextWithNewLine(doc, "Param2", ThaiTextUtil.addThaiWordBreakPreserveNewLine(strBuffer.toString()));
////    	wordReplaceUtil.replaceImageInTable(doc, 	"Param3", "D:\\risknova.png", 300, 200);	//1 image
//    	wordReplaceUtil.replaceImageInTable(doc, "Param3", "D:\\risknova.png", 230, 150);	//2 image
//    	wordReplaceUtil.replaceImageInTable(doc, "Param4", "D:\\risknova.png", 230, 150);	//2 image
//    	wordReplaceUtil.replaceTextWithNewLine(doc, "Param5", "ตัวอย่างตำแหน่งติดตั้งเซนเซอร์บนบนชุดโซ่ลำเลียง (Drag Chain Conveyor Sensor)");
//    	wordReplaceUtil.replaceTextWithNewLine(doc, "Param6", "ตัวอย่างตำแหน่งติดตั้งเซนเซอร์บนบนชุดโซ่ลำเลียง (Drag Chain Conveyor Sensor)");
//
//    	System.out.println("======= Word Replace Parameter Success!!!");
//
//
//  		FileOutputStream out = new FileOutputStream("D:\\output-002-02.docx");
//  		doc.write(out);
//  		out.close();
//  		doc.close();
//  		System.out.println("======= Word File Generate Success!!!");
//
//  		DocxToPdfConverter.convertDocxToPdf(
//  				"D:\\output-002-02.docx",
//                "D:\\output-002-02.docx.pdf",
//                "THSarabunNew.ttf"
//        );
//
//        System.out.println("======= PDF generated.");

  	}
}

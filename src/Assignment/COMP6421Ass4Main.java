package Assignment;


import java.io.IOException;



import LexicalAnalyzer.InputLoader;
import LexicalAnalyzer.LexicalAnalyzer;
import LexicalAnalyzer.StateMachineDriver;
import SemanticActions.SymbolTable;

import SyntacticAnalyzer.SyntacticAnalyzer;

import utils.SysLogger;


public class COMP6421Ass4Main {
	
	public static void main(String[] args) {
		// create and initialize the logger
		SysLogger.init();
		
		// show greetings
		System.out.println("Wellcome to COMP 6421 Project.");
		System.out.println("Please read $ThisProgram\\readme.pdf first.");
		System.out.println(
				"Please put all the test files under the root directoy of $ThisProgram\\input\\, which already has some sample files.");
		System.out.println(
				"The result of the progrom will be stored accordingly in the files under $ThisProgram\\output\\ \n");

		
//		try {
//			System.in.read();
//		} catch (IOException e) {
//			
//		}
	
		// load all test input files
		InputLoader testFilesLoader = new InputLoader();
		String path = System.getProperty("user.dir");
		
		if (testFilesLoader.loadTextFiles(path) != 0) {
			return;
		}

		// create state machine table driver
		if (StateMachineDriver.init() != 0) {
			return;
		}

		// begin to lexical analyze for each file
		for (int i = 0; i < testFilesLoader.lstFiles.size(); i++) {
			SymbolTable st = null;
			for (int j = 0; j < 2; j++) {
				if (j == 0) {
					SysLogger.enableLog(false);
				} else {
					SysLogger.enableLog(true);
					//create output file first
			    	SysLogger.setOutputFilenames(testFilesLoader.lstResultFiles.get(i), 
			    			testFilesLoader.lstErrFiles.get(i));
			    	
			    	SysLogger.setASMFilenames(testFilesLoader.lstASMFiles.get(i));
			    	
					// write time stamp to output files
	

					//SysLogger.info(datetimeNow + "\nThe following is the result:");
					String tabNote = "Note: If the length of TAB of your editor is not 4, please accordingly modify LexicalAnalyzer.java at line 26 and run again.\n";
					SysLogger.info(tabNote);
					SysLogger.err(tabNote);
					SysLogger.log("--------------------------------------------------");
					SysLogger.log("Start to analyze: " + testFilesLoader.lstFiles.get(i));
					SysLogger.log("--------------------------------------------------");
				}
				
				// create a lexical analyzer
				LexicalAnalyzer scanner = new LexicalAnalyzer();
				
				if (scanner.init(testFilesLoader.lstFiles.get(i)) != 0) {
					return;
				}
				
				// create a syntax analyzer
				SyntacticAnalyzer parser = new SyntacticAnalyzer();
				
				if (parser.init(scanner) != 0) {
					return;
				}
				
				if (parser.parseEx(st)) {
					st = (SymbolTable) parser.getSmActions().getStHead().clone();
					
					//scanner.getAllTokens();
					parser.getSmActions().printAll();
				} else {
					continue;
				}
			}
		}

		System.out.println("\nThe program ends successfully!");
		System.out.println("The result of the progrom has been stored in the files under $ThisProgram\\output\\ \n");


	}
}

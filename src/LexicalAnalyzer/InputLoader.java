
package LexicalAnalyzer;

import java.io.File;
import java.util.ArrayList;

import utils.SysLogger;

public class InputLoader {
	public static final int MAX_FILES = 50;
	public static final int MAX_FILE_SIZE = 2 * 1024 * 1024; // 2M bytes

	public ArrayList<String> lstFiles = new ArrayList<String>(); // input files
	public ArrayList<String> lstResultFiles = new ArrayList<String>(); // result files
	public ArrayList<String> lstErrFiles = new ArrayList<String>(); // error msg files
	public ArrayList<String> lstASMFiles = new ArrayList<String>(); // ASM files
	public ArrayList<String> lstdervFiles = new ArrayList<String>(); // output derivation:files
	// load all files under a directory
	public int loadTextFiles(String dirPath) {
		
		String inputDir = dirPath + "/input";
		String outputDir = dirPath + "/output";
		
	
		System.out.println("inputDir:"+inputDir);
		System.out.println("outputDir"+outputDir);
		
		File filesDir = new File(inputDir);
		File list[] = filesDir.listFiles();

		if (list.length > MAX_FILES) {
			SysLogger.err("Too many files under directory: " + inputDir);
			return -1;
		}

		for (int i = 0; i < list.length; i++) {
			if (list[i].isFile()) {
				SysLogger.log("load a file:" + inputDir + list[i].getName());

				if (list[i].length() > MAX_FILE_SIZE) {
					SysLogger.err("File " + list[i].getName() + " is too large: " + list[i].length());
					return -1;
				}

				lstFiles.add(inputDir + "/"+ list[i].getName());

				String name = list[i].getName();
				lstResultFiles.add(outputDir + "/"+	name.substring(0, name.length() - 4) + "_result.txt");
				lstErrFiles.add(outputDir + "/"+	name.substring(0, name.length() - 4) + "_error.txt");
				lstdervFiles.add(outputDir + "/"+	name.substring(0, name.length() - 4) + "_derivation.txt");
				lstASMFiles.add(outputDir + "/"+	name.substring(0, name.length() - 4) + "_asm.m");
				
			}
		}
		return 0;
	}

	public static void deleteFilesWithExt(String dirPath){
		
		String outputDir = dirPath + "/output";
		File outputDirectoryFile = new File(outputDir);
		
		
		for (File f : outputDirectoryFile.listFiles()) {
		    if (f.getName().endsWith(".txt")) {
		        f.delete(); // may fail mysteriously - returns boolean you may want to check
		    }
		}
	}
}

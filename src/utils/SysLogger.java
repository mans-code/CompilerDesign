
package utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

class LoggerTextFormat extends Formatter { 
    @Override 
    public String format(LogRecord record) { 
    	return record.getLevel() + ": " + record.getMessage() + "\r\n"; 
    } 
}

class OutputTextFormat extends Formatter { 
    @Override 
    public String format(LogRecord record) { 
    	return record.getMessage() + "\r\n"; 
    } 
}

public class SysLogger {
	public static Logger log = Logger.getLogger("COMP6421Project");
	public static Logger result = Logger.getLogger("COMP6421ProjectResult");
	public static Logger err = Logger.getLogger("COMP6421ProjectError");
	public static Logger asm = Logger.getLogger("COMP6421ProjectASM");
	public static Logger derv = Logger.getLogger("COMP6421ProjectDerv");
	public static boolean bLexicalAnalyzer = false;
	
	private static FileHandler fhLast = null;
	private static FileHandler fhErrLast = null;
	private static FileHandler fhASMLast = null;
	private static FileHandler fhdervast = null;
	private static boolean logEnable = true;
	
	public static void enableLog(boolean b) {
		logEnable = b;
	}
		
	// initialization: e.g. create log files
	public static void init() {
		// disable console logging
		log.setUseParentHandlers(false);
		result.setUseParentHandlers(false);
		err.setUseParentHandlers(false);
		asm.setUseParentHandlers(false);
		derv.setUseParentHandlers(false);
		// add a file handler
		String path = System.getProperty("user.dir") + "\\logs\\log.txt";
		FileHandler fileHandle;
		try {
			fileHandle = new FileHandler(path);
			fileHandle.setLevel(Level.ALL);
			fileHandle.setFormatter(new LoggerTextFormat());
			log.addHandler(fileHandle);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setOutputFilenames(String filename, String errFilename) {
		// create a logger to show the results of the program.
		String path = filename;
		
		FileHandler fileHandle;
		try {
			fileHandle = new FileHandler(path);
			fileHandle.setLevel(Level.ALL);
			fileHandle.setFormatter(new OutputTextFormat());
			if (fhLast != null) {
				fhLast.flush();
				fhLast.close();
				result.removeHandler(fhLast);
			}
			result.addHandler(fileHandle);
			fhLast = fileHandle;
		} catch (IOException e) {

			e.printStackTrace();
		}	
		
		
		path = errFilename;
		try {
			fileHandle = new FileHandler(path);
			fileHandle.setLevel(Level.ALL);
			fileHandle.setFormatter(new OutputTextFormat());
			if (fhErrLast != null) {
				fhErrLast.flush();
				fhErrLast.close();
				err.removeHandler(fhErrLast);
			}
			err.addHandler(fileHandle);
			fhErrLast = fileHandle;
		} catch (IOException e) {
			e.printStackTrace();
		}	
		

	}
	public static void setdervFilenames(String filename) {
		// create a logger to show the results of the program.\
		String path = filename;
		FileHandler fileHandle;
		try {
			
			fileHandle = new FileHandler(path );
			fileHandle.setLevel(Level.ALL);
			fileHandle.setFormatter(new OutputTextFormat());
			if (fhdervast != null) {
				fhdervast.flush();
				fhdervast.close();
				derv.removeHandler(fhdervast);
			}
			derv.addHandler(fileHandle);
			fhdervast = fileHandle;
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static void setASMFilenames(String filename) {
		// create a logger to show the results of the program.
		String path = filename;
		FileHandler fileHandle;
		try {
			fileHandle = new FileHandler(path);
			fileHandle.setLevel(Level.ALL);
			fileHandle.setFormatter(new OutputTextFormat());
			if (fhASMLast != null) {
				fhASMLast.flush();
				fhASMLast.close();
				asm.removeHandler(fhASMLast);
			}
			asm.addHandler(fileHandle);
			fhASMLast = fileHandle;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void log(String msg) {
		if (logEnable) {
			log.info(msg);
		}
	}
	
	public static void der(String msg) {
		if (logEnable) {
			log.severe(msg);
			derv.info(msg);;
		}
	}
	
	public static void info(String msg) {
		if (logEnable) {
			log.info(msg);
			result.info(msg);
		}
	}
	
	public static void err(String msg) {
		if (logEnable) {
			log.severe(msg);
			err.info(msg);
		}
	}
	public static void asm(String msg) {
		if (logEnable) {
			log.severe(msg);
			asm.info(msg);
		}
	}
}


	
	
	
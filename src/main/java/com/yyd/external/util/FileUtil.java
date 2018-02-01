package com.yyd.external.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

//import org.apache.log4j.Logger;
public class FileUtil {	
	public static String getResourcePath() {
//		try {
//			return ResourceUtils.getURL("classpath:").getPath();
//		} catch (FileNotFoundException e) {
//		}
		return ClassLoader.getSystemResource("").getPath();
	}

	public static Properties buildProperties(String path) throws Exception {
		String propertiesPath = getResourcePath() + path;
		Properties properties = new Properties();
		FileInputStream fis = new FileInputStream(propertiesPath);
		properties.load(fis);
		fis.close();
		return properties;
	}
	
	//private static final Logger logger = Logger.getLogger(FileUtil.class);
	public static String readFileByLines(String fileName) throws IOException {
		String content = "";
        File file = new File(fileName);
        BufferedReader reader = null;      
        try{
	        reader = new BufferedReader(new FileReader(file));
	        String tempString = null;
	        while ((tempString = reader.readLine()) != null) {
	            content += tempString +"\n";
	        }     
	    }catch(Exception e){
	    	//logger.error("", e);
	    }finally{
	    	if( reader != null ){
	    		reader.close(); 
	    	}
	    }        
        return content;
    }
	
	public static ArrayList<String> readFileGetListByLines(String fileName){
		ArrayList<String> list = new ArrayList<>();
		if(StringTool.isEmpty(fileName))
			return list;
        File file = new File(fileName);
        if(!file.exists())
        	return list;
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
            	list.add(tempString);
            }
        }catch(Exception e){
        	//logger.error("", e);
        }finally{
        	if( reader != null ){
        		try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				} 
        	}
        }             
        return list;
    }
	
	public static byte[] toByteArray(String filePath) {
		
        ByteArrayOutputStream bos=null;
        BufferedInputStream in = null;
        try {
            File f = new File(filePath);
            if(f.exists()){
                in = new BufferedInputStream(new FileInputStream(f));
                bos = new ByteArrayOutputStream((int) f.length());
                int buf_size = 1024;
                byte[] buffer = new byte[buf_size];
                int len = 0;
                while (-1 != (len = in.read(buffer, 0, buf_size))) {
                    bos.write(buffer, 0, len);
                }
            }
            else
            {
            	return null;
            }

        } catch (IOException e) {
        	//logger.error("toByteArray() Exception", e);
        } finally {
            try {
                in.close();
                bos.close();
            } catch (IOException e) {
            	//logger.error("toByteArray() Exception",e);
            }
        }
        return bos.toByteArray();
    }
	
	
	
}

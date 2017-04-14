package sam.utils;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import org.apache.commons.io.FileUtils;

public class Base64Factory {

	/**
	 * Encode a byte array to base64-String
	 * 
	 * @param array byte[]
	 * @return String in base64
	 */
	public static String encodeByteArray64(byte[] array){
		return Base64.getEncoder().encodeToString(array);
	}
	
	/**
	 * Convert a file into a byte array
	 * 
	 * @param filePath String path to image file
	 * @return byte[] array of bytes
	 */
	public static byte[] imageToByteArray(String filePath){
		byte[] out = null;
		try {
			out = FileUtils.readFileToByteArray(new File(filePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out;
	}
	
	public static String encodeImageToBase64(String imagePath){
		return Base64Factory.encodeByteArray64(Base64Factory.imageToByteArray(imagePath));
	}
	
	public static void main(String[] args) {
		
		System.out.println(encodeImageToBase64(args[0]));
		
	}
	
}

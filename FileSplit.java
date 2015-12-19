import java.io.*;
import java.util.Scanner;

/**
 * Created by ameya on 11/13/15.
 */
public class FileSplit {
	int partCounter = 1;
	int counter=0;
	public void splitFile(File f) throws IOException {

		int sizeOfFiles = 102400;// 100KB
		byte[] buffer = new byte[sizeOfFiles];

		try (BufferedInputStream bis = new BufferedInputStream(
				new FileInputStream(f))) {//try-with-resources to ensure closing stream
			String name = f.getName();

			String dir = System.getProperty("user.dir").toString()+"\\Server";
			int tmp = 0;
			while ((tmp = bis.read(buffer)) > 0) {
				//write each chunk of data into separate file with different number in name
				File newFile = new File(dir, name + "." +"chunk"+ String.format("%02d", partCounter++));
				counter++;
				try (FileOutputStream out = new FileOutputStream(newFile)) {
					out.write(buffer, 0, tmp);//tmp is chunk size
				}
			}
		}
	}
	public static void joinFile(String baseFilename, String folderName)
	{
		int numberParts;
		try {
			numberParts = getNumberParts(folderName+"/"+baseFilename);

			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(folderName+"/"+baseFilename));
			for (int part = 1; part < numberParts+1; part++)
			{
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(folderName+"/"+baseFilename + ".chunk" + String.format("%02d", part)));

				int b;
				while ( (b = in.read()) != -1 )
					out.write(b);

				in.close();
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * find out how many chunks there are to the base filename
	 */
	private static int getNumberParts(String baseFilename) throws IOException
	{
		// list all files in the same directory
		File directory = new File(baseFilename).getAbsoluteFile().getParentFile();
		final String justFilename = new File(baseFilename).getName();
		String[] matchingFiles = directory.list(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.startsWith(justFilename) && name.substring(justFilename.length()).matches("^\\.chunk\\d+$");
			}
		});
		return matchingFiles.length;
	}
}
//    public static void main(String[] args) throws IOException {
//        Scanner scanner = new Scanner(System.in);
//        System.out.print("Enter a file name: ");
//        System.out.flush();
//        String filename = scanner.nextLine();
//        File file = new File(filename);
//        splitFile(file);
//    }

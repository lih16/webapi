/*********************************************************************/
/** Class Name:    PMCdownload                                      **/
/** Description:   We download pdf files based on three strategies  **/
/**                                                                 **/
/**                                                                 **/
/** Author:        Dianwei Han                                      **/
/**                Department of Genetics&Genomic Sci, Mount Sinai  **/
/** Copyright:     2017, Mount Sinai, Medicine School               **/
/*********************************************************************/

package downloadPDFFile;
import downloadPDFFile.ParseMissingPMID;

import java.io.*;
import java.lang.CharSequence;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.net.URISyntaxException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ThreadLocalRandom;

public class PMCdownload {
    
    static String cookieValue="";
    static HashMap<String, String> map = new HashMap<String,String>();
    static HashMap<String, String> ftpPath = new HashMap<String,String>();

    public static void readMapFile(String infile) {     

        String strLine = null;
        ArrayList<String> rtn = new ArrayList<String>();

        try {

            FileInputStream fstream = new FileInputStream(infile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream, "UTF-16"));
            //BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            while ((strLine = br.readLine()) != null)   {
                  //System.out.println("string MAP [" + strLine + "]");
                  String[] parts = strLine.split("#");
                  //System.out.println("leng " + parts.length);
                  if (parts.length > 1 ) {
                      map.put(parts[1],parts[0].trim());       
                  } 
             
            }
        } catch (Exception e){
               System.err.println(e.getMessage());
        }
        
    }

    public static void readftpMap(String infile) {     

        String strLine = null;
        ArrayList<String> rtn = new ArrayList<String>();

        try {

            FileInputStream fstream = new FileInputStream(infile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream, "UTF-16"));
            while ((strLine = br.readLine()) != null)   {
                  //System.out.println("string MAP [" + strLine + "]");
                  String[] parts = strLine.split("#");
                  //System.out.println("leng " + parts.length);
                  if (parts.length > 1 ) {
                      ftpPath.put(parts[1],parts[0].trim());       
                  } 
             
            }
        } catch (Exception e){
               System.err.println(e.getMessage());
        }
        
    }

    public static ArrayList<String> readListFile(String inf) throws FileNotFoundException {

        String strLine = null;
        ArrayList<String> rtn = new ArrayList<String>();

        try {

            FileInputStream fstream = new FileInputStream(inf);
            // if UTF-16 encoding, use UTF-16, otherwise, don't use it
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream,"UTF-16"));
            //BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            while ((strLine = br.readLine()) != null)   {
                  System.out.println("readListFile:[" + strLine +"]");
                     rtn.add(strLine);
            }
        } catch (Exception e){
            System.err.println(e.getMessage());
        }
        return rtn;

    }

    public static int pmcAPIFetchPDF(String pmcID, String pmid) {
        //System.out.println("Entrez.PMC_fetchPDF(" + pmcID + "): download PMC paper in PDF format");

        String strLine = null;
        StringBuilder sb = new StringBuilder();

        try {
            URLConnection connection = new java.net.URL("http://www.pubmedcentral.nih.gov/picrender.fcgi?tool=pmcentrez&artid=" + pmcID + "&blobtype=pdf").openConnection();
            String redirect = connection.getHeaderField("Location");
            if (redirect != null) {
              
            System.out.println("redirect is true " );
                connection = new URL(redirect).openConnection();
            }
            connection.setRequestProperty("User-Agent","Safari/12603.1.30.0.34");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            connection.setReadTimeout(15*1000);
            connection.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));

            java.io.BufferedInputStream  bin  = new java.io.BufferedInputStream(connection.getInputStream());

            java.io.FileOutputStream     fos  = new java.io.FileOutputStream("downloadPDFFile/pmcAPI_Dir/" + pmid + ".pdf");
            java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
            int length = -1;

            InputStream in = connection.getInputStream();
            Files.copy(in, Paths.get("downloadPDFFile/pmcAPI_Dir/"+pmid+".pdf"), StandardCopyOption.REPLACE_EXISTING);

            bout.close(); 
            fos.close(); 
            bin.close();
            return 1;
            
        }
        catch(EOFException eof) {System.out.println("EOFException: " + eof.getMessage());} 
        catch(IOException  ioe) {System.out.println("IOException: "  + ioe.getMessage());} 
        catch(Exception    e)   {System.out.println("Exception: "    + e.getMessage());  }
        return -1;
    }


    public static int ftpFetchPackage(String path, String pmid) {

         int flg = -1;


         try {
                String command = "wget -O downloadPDFFile/ftp_Dir/" + pmid + ".tar.gz ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/" + path;
                //String command = "wget -O downloadPDFFile/ftp_Dir/  ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/" + path;
                System.out.println(command);
                Process p = Runtime.getRuntime().exec(command);
                flg = 1;
                //return flg;
         } catch (IOException ioe) {
            System.out.println(ioe);
            
         }

         return flg; 

    }

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {

          String s ="https://login.eresources.mssm.edu/login";
          //dir="";
          //targetDir=args[1];
          URL url1 = new URL(s);
          System.out.println(url1.getHost());
          URL url = new URL("https://login.eresources.mssm.edu/login");
          HttpURLConnection connection = (HttpURLConnection) url.openConnection();
          connection.setDoOutput(true);
          connection.setDoInput(true);
          connection.setRequestMethod("POST");
          connection.setInstanceFollowRedirects(false);
          connection.connect();
          OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "utf-8");
          out.write("user=hand10&pass=486A#asd");
          out.flush();
          out.close();

          cookieValue = connection.getHeaderField("Set-Cookie");
          System.out.println("cookieVal "+ cookieValue);
          connection.disconnect();
          HttpURLConnection resumeConnection = (HttpURLConnection) url1
                     .openConnection();
          if (cookieValue != null) {
                   resumeConnection.setRequestProperty("Cookie", cookieValue);
          }
          resumeConnection.connect();


        ArrayList<String> al = readListFile(args[0]);
        readMapFile(args[1]);
        readftpMap(args[2]);

        int cnt_API = 0;
        int cnt_whole = 0;

        System.out.println("LENGTH of Map "+ map.size());
        System.out.println("LENGTH of ftpPath "+ ftpPath.size());

        for (Map.Entry entry : map.entrySet()){
          //   System.out.println("["+entry.getKey()+"]:["+entry.getValue()+"]");
          ;
        }

        int rtnftpFetch = 0;
        int rtnAPIFetch = 0;
        int rtnCodeFetch = 0;

        System.out.println("length of input list[" + al.size() + "]");
        BufferedWriter writer = null;
        File logFile = new File("downloadPDFFile/log.txt");

    try {
         writer = new BufferedWriter(new FileWriter(logFile, true));
 
        for (String str : al) {

                      //fetch Random time to sleep 
                      int rand;
                      rand = ThreadLocalRandom.current().nextInt(1,9);
                      System.out.println("JavaCode fetch ["+ str + "] random:[" + rand + "]" );
                      try {
                         Thread.sleep(rand * 6239 + 3); 
                      } catch (Exception e) {
                         System.out.println(e);
                      }

             if (map.containsKey(str) == true) {
     
                System.out.println("API fetch ["+ str + "]");
                cnt_API = cnt_API + 1;
                rtnAPIFetch = pmcAPIFetchPDF(map.get(str), str);
                System.out.println(" rtnAPIFetch ["+ rtnAPIFetch + "]");
                if (rtnAPIFetch == 1) {
                      writer.write(str + " is fetched by API");
                      writer.newLine();
                      writer.flush();
                      continue;
                }
             } 


             if (ftpPath.containsKey(str) == true) {
           
                 System.out.println("FTP fetch ["+ str + "]");
                 rtnftpFetch = ftpFetchPackage(ftpPath.get(str), str);
                 System.out.println(" rtnftpFetch ["+ rtnftpFetch + "]");
                 if (rtnftpFetch == 1) {
                      writer.write(str + " is fetched by ftp");
                      writer.newLine();
                      writer.flush();
                      continue;
                 }
             } 


                      System.out.println("JavaCode fetch ["+ str + "] random:[" + rand + "]" );
                      ParseMissingPMID codesolution = new ParseMissingPMID();
                      rtnCodeFetch = codesolution.fetchPDF(str, cookieValue);
                      if (rtnCodeFetch  == 1 ) {
                      writer.write(str + " is fetched by Java Code");
                      writer.newLine();
                      writer.flush();
                         
                      } else {
                      writer.write(str + " should be fetched by Java Code, but failed ...");
                      writer.newLine();
                      writer.flush();
                      }
                      System.out.println(" rtnCodeFetch ["+ rtnCodeFetch + "]");
           
        }
        System.out.println(" input LEN :" + al.size() + " API num :[" + cnt_API + "]");
        System.out.println("LENGTH of Map "+ map.size());
        } catch (Exception e) {
            System.out.println(e);
        }
        writer.close();
    }
}

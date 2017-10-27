/*********************************************************************/
/** Class Name:    ParseMissingPMID                                 **/
/** Description:   We download pdf files based on pmid              **/ 
/**                                                                 **/
/** Author:        Dianwei Han                                      **/
/**                Department of Genetics&Genomic Sci, Mount Sinai  **/
/** Copyright:     2017, Mount Sinai, Medicine School               **/
/*********************************************************************/

package downloadPDFFile;
import java.io.*;
import java.lang.CharSequence;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Scanner;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpHost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.commons.io.FileUtils;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ParseMissingPMID {
     
    static int type;

    static ArrayList<String> fieldNames = new ArrayList<String>();

    public ParseMissingPMID(){}

    public static boolean containsAMP(String str)  {
      
         if (str == null ) {
             return false;
         }
         
         CharSequence matchStr = "amp;"; 
         if (str.contains(matchStr) == true){
              return true;
         } else {
             return false;
         }    
    }
    
    public static String removeAMP(String str) {

        return str.replaceAll("amp;","");
    }

    public static ArrayList<String> readListFile(String inf) throws FileNotFoundException {
       
            String strLine = null;
            ArrayList<String> rtn = new ArrayList<String>();

            try {

                FileInputStream fstream = new FileInputStream(inf);
               BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                //Read File Line By Line
                while ((strLine = br.readLine()) != null)   {
                     rtn.add(strLine);
                }
           } catch (Exception e){
               System.err.println(e.getMessage());
           }
           return rtn;

    }

    public static String processPDFFile(String pdfFile, String cookieVal) throws IOException, FileNotFoundException, URISyntaxException, InterruptedException {


        if (pdfFile == null) {
           return "NA";
        } 

        String strLine = null;
        StringBuilder sb = new StringBuilder();
        URL url;
        BufferedReader br;


        if (pdfFile == null) {
           return "NA";
        }
System.out.println("processPDF file [" + pdfFile + "]");
        CloseableHttpClient httpClient = HttpClients.custom().setRedirectStrategy(new LaxRedirectStrategy()).build();

        try {

              HttpClientContext context = HttpClientContext.create();
              HttpGet httpGet = new HttpGet(pdfFile);
              if (cookieVal != null ) {
                  httpGet.setHeader("Cookie", cookieVal);
              }
System.out.println("executing request "+ httpGet.getRequestLine() );
              httpClient.execute(httpGet, context);
              HttpHost target = context.getTargetHost();
              List<URI> redirectLocations = context.getRedirectLocations();
              URI location = URIUtils.resolve(httpGet.getURI(), target, redirectLocations);
System.out.println("final location  ["+ location.toASCIIString() + "]");
               httpGet = new HttpGet(location.toASCIIString());
                         httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:34.0) Gecko/20100101 Firefox/34.0");
                         httpGet.addHeader("Referer", "https://www.google.com");
                          if (cookieVal != null ) {
                           httpGet.setHeader("Cookie", cookieVal);
                          }
                          CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
                          HttpEntity fileEntity = httpResponse.getEntity();
              br = new BufferedReader(new InputStreamReader(fileEntity.getContent(), Charset.forName("UTF-8")));

            //Read File Line By Line

            Thread.sleep(5000);

            while ((strLine = br.readLine()) != null)   {
                sb.append(strLine);
            }


        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException e){
            System.err.println(e.getMessage());
        } catch (InterruptedException ie) {
            System.out.println(ie);
        }



System.out.println("CONTENT "+ sb.toString());

        // extract pdf link according to different types
        if (type == 1)  { // pmcid type
            Pattern pattern = Pattern.compile("/pmc/articles/PMC\\d+/pdf/(.+)pdf");
            Matcher matcher = pattern.matcher(sb.toString());


            if (matcher.find() == false){

                type = 100;
               System.out.println("BAD PMC PDF header ");
               return "NA";
            }

            String fullStr = matcher.group(1);

//System.out.println("fullStr "+ fullStr);
           String[] smallStr = fullStr.split(" ");
           String tempStr = smallStr[0].replaceAll("\"", ""); 

            sb.setLength(0); 
            sb = sb.append(pdfFile + "pdf/" + tempStr);
            return sb.toString(); 
        }


            
            String urlF = StringUtils.substringBetween(sb.toString(), "pdfurl=","queryStr");
System.out.println("urlF [" + urlF + "]");
            if (urlF != null) {
                String tempStr = StringUtils.substringBetween(urlF.trim(), "\"", "\"");
                if (tempStr != null) {
                    return tempStr;
                }
            }
            // strength rule1

System.out.println("rule1");
            if (sb.toString().contains("citation_pdf_url")) {
            Pattern pattern = Pattern.compile("citation_pdf_url\"\\s+content=(.+)\"");
            Matcher matcher = pattern.matcher(sb.toString());
           
            if (matcher.find() == false){
               ;
              /*
                type = 100;
               System.out.println("BAD type 2 PDF header ");
               return "NA";
               */
            } else {
               String fullStr = matcher.group(1);
               String fullURL = StringUtils.substringBetween(fullStr, "\"", "\"");
               return fullURL; 
            }
            }



System.out.println("rule2");
            // strength rule2 nature
            if (sb.toString().contains("www.nature.com.eresources.mssm.edu/favicon")) {
                 Pattern pattern = Pattern.compile("download-pdf\"\\s+href=(.+)\"");
                 Matcher matcher = pattern.matcher(sb.toString());
                 if (matcher.find() == false){
                    type = 100;
                    System.out.println("BAD type 2 PDF header ");
                    return "NA";
                 } else {
                    String fullStr = matcher.group(1);
                    String fullURL = StringUtils.substringBetween(fullStr, "\"", "\"");
                    return (new String("http://www.nature.com.eresources.mssm.edu"+fullURL)); 
                 }
             }
            
System.out.println("rule8");
            // strengthened rule8
            if (sb.toString().contains("online.liebertpub.com")) {
System.out.println(" rule 8  enter ") ;
          
                 String urlRaw = StringUtils.substringBetween(sb.toString(), "\"pdfLink\"", "target=");
                 String fullStr = StringUtils.substringBetween(urlRaw, "\"", "\"");
 
                 if (fullStr == null){
                    type = 100;
                    System.out.println("BAD type 2 PDF header ");
                    return "NA";
                 } else {
                    return (new String("http://online.liebertpub.com"+fullStr)); 
                 }
             }

System.out.println("rule3");
             // strengthen rule3 Tlsevier
            if (sb.toString().contains("pdfLink")) {
                 Pattern pattern = Pattern.compile("pdfLink\"\\s+href=(.+)\"");
                 //Pattern pattern = Pattern.compile("pdfLink\"\\s+href=(.+)\"");
                 Matcher matcher = pattern.matcher(sb.toString());
                 if (matcher.find() == false){
                    type = 100;
                    System.out.println("BAD type 2 PDF header ");
                    return "NA";
                 } else {
                   String fullStr = matcher.group(1);
                   String fullURL = StringUtils.substringBetween(fullStr, "\"", "\"");
                   return fullURL; 
                }
            }

System.out.println("rule6");
            // strengthed rule6 science
            if (sb.toString().contains("www.sciencedirect.com/science/")) {
                 Pattern pattern = Pattern.compile("pdf-download-btn-link\"\\s+href=(.+)\"");
                 Matcher matcher = pattern.matcher(sb.toString());
                 if (matcher.find() == false){
                    type = 100;
                    System.out.println("BAD type 2 PDF header ");
                    return "NA";
                 } else {
                    String fullStr = matcher.group(1);
                    String fullURL = StringUtils.substringBetween(fullStr, "\"", "\"");
                    return (new String("http://www.sciencedirect.com"+fullURL)); 
                 }
             }
 
            // strengthened rule9
            if (sb.toString().contains("citation_pdf_url")) {
             String rStr = StringUtils.substringBetween(sb.toString(), "\"citation_fulltext_html_url\"", "\"citation_pdf_url\"");
                 System.out.println("H "+ rStr);
                 if (rStr != null ) {
                 return (StringUtils.substringBetween(rStr,"\"", "\""));
                 } else {
                   type = 100;
                   return "NA";
                 }
            }
            // strengthened rule10
            if (sb.toString().contains("www.sagepub.com")) {
System.out.println("rule10e");
               String fullURL = StringUtils.substringBetween(sb.toString(), "\"show-references\"", "\"show-pdf\"");
               String fullStr = StringUtils.substringBetween(fullURL,"href", "Cited");
               String rtnStr =  StringUtils.substringBetween(fullStr,"\"", "\"");
               if (rtnStr != null) {
System.out.println("rule10e" + fullStr);
                   return (new String("http://www.sagepub.com.eresources.mssm.edu"+rtnStr)); 
               } else {
                 type = 100;
                  return "NA";
               }
 
            }

        return "NA";
    }

    public static String readWebPageFile(String inf, String cookieVal) throws IOException, FileNotFoundException {
         
        String strLine = null;
        StringBuilder sb = new StringBuilder();
        URL url;
        HttpURLConnection resumeConnection;
        InputStream is = null;
        BufferedReader br; 

        try {

            StringBuilder urlSB = new StringBuilder();
            urlSB = urlSB.append("https://www-ncbi-nlm-nih-gov.eresources.mssm.edu/pubmed?term="+inf);
            url = new URL(urlSB.toString());
            resumeConnection = (HttpURLConnection) url.openConnection();
            if (cookieVal != null) {
                 resumeConnection.setRequestProperty("Cookie", cookieVal);
            }
            resumeConnection.connect();

System.out.println("PMID " + inf);
            is = resumeConnection.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            //Read File Line By Line
            while ((strLine = br.readLine()) != null)   {
                sb.append(strLine);
            }
            br.close();
        }
        catch (MalformedURLException mue) {
            mue.printStackTrace();
        }
        catch (IOException e){
            System.err.println(e.getMessage());
        }
        return sb.toString();

    }

    public static ArrayList<String> extractStringBetween(String str, String start, String end) {
   
        ArrayList<String> result = new ArrayList<String>();
        String[] firstStr = str.split("\\"+end);
        for (String fstr : firstStr) {
            String[] secondStr = fstr.split("\\"+start);
            result.add(secondStr[1]);
        } 

        return result;
        
    }


    public static String parseData(String str) {
     
        /* extract field names */
 
        String thead = StringUtils.substringBetween(str, "PMCID:", "pmcid");
        if (thead != null) { 
            String urlPMC = StringUtils.substringBetween(thead, "href=", "ref"); 
            String tempStr = StringUtils.substringBetween(urlPMC.trim(), "\"", "\"");
            StringBuilder sb = new StringBuilder("https://www.ncbi.nlm.nih.gov");
            sb = sb.append(tempStr);
            System.out.println("TYPE 1 PMCID ["+sb.toString()+"]");
            type = 1;
            return sb.toString();
          
        } else {
               
  System.out.println(" ParseData [" + str + "]");
            String urlRaw = StringUtils.substringBetween(str, "Full text links", " ref=");


  System.out.println(" fullStr [" + urlRaw + "]");
            String fullURL = StringUtils.substringBetween(urlRaw, "\"", "\"");
  System.out.println(" fullStr [" + fullURL + "]");
               
            if ( containsAMP(fullURL) == false) {

                type = 2;
                System.out.println("TYPE 2 " + fullURL);
                return fullURL;
            } else {
                String newFullURL = removeAMP(fullURL);
                System.out.println("TYPE 3 " + newFullURL);
                type = 3;
                return newFullURL;
            }
        }
        
    }

    public static int fetchPDF(String pmid, String cookieValue) throws IOException, URISyntaxException, InterruptedException {

        
        try {
            File file = new File("codePDF/");
            FileOutputStream is = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(is);
            BufferedWriter w = new BufferedWriter(osw);


                // read pubmed file
                String rawData = readWebPageFile(pmid, cookieValue);
                if (rawData != null) {
                   ;
                   //System.out.println("rawData" + rawData );
                }
                String rtnFromParser = parseData(rawData);
                System.out.println("AFTER parseData TYPE " + type);

                // process "Not Available CASE"
                if (type != 100) {


                    String writeURL = new String();
                    writeURL = processPDFFile(rtnFromParser, cookieValue);
                    System.out.println("Return writeURL "+ writeURL);
                
                    //String writeURL=new String("https://academic.oup.com/jcem/article-pdf/84/2/405/13122083/jcem0405.pdf");
                    // writeURL=new String("https://academic.oup.com/jcem/article-pdf/84/2/405/13122083/jcem0405.pdf");
                    //writeURL="http://www.neurology.org/content/70/8/617.full.pdf";
                    if (type != 100) { 

                      CloseableHttpClient httpClient = HttpClients.custom().setRedirectStrategy(new LaxRedirectStrategy()).build();

                       HttpClientContext context = HttpClientContext.create();
                       HttpGet httpGet = new HttpGet(writeURL);
                       //HttpGet httpGet = new HttpGet("http://www.google.com");
                       if (cookieValue != null ) {
                              httpGet.setHeader("Cookie", cookieValue);
                       }
System.out.println("executing request "+ httpGet.getRequestLine() );
                       httpClient.execute(httpGet, context);
                       HttpHost target = context.getTargetHost();
                       List<URI> redirectLocations = context.getRedirectLocations();
                       URI location = URIUtils.resolve(httpGet.getURI(), target, redirectLocations);
System.out.println("final location  ["+ location.toASCIIString() + "]");
                        HttpGet httpet = new HttpGet(location.toASCIIString());
                         httpet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:34.0) Gecko/20100101 Firefox/34.0");
                         httpet.addHeader("Referer", "https://www.google.com");
                          if (cookieValue != null ) {
                           httpet.setHeader("Cookie", cookieValue);
                          }
                          HttpClient httpc = HttpClientBuilder.create().build();
                          HttpResponse httpResponse = httpc.execute(httpet);
                          HttpEntity fileEntity = httpResponse.getEntity();
System.out.println("PDF length " + fileEntity.getContent().toString());

                          /*

                        String strLine="";
                        StringBuilder sb = new StringBuilder();

                     Thread.sleep(5000);
            
            while ((strLine = br.readLine()) != null)   {
System.out.println("strLine " + strLine);
                sb.append(strLine);
            }

                          */

                            if (fileEntity != null) {
                              FileUtils.copyInputStreamToFile(fileEntity.getContent(), new File("downloadPDFFile/javaCode_Dir/"+pmid+".pdf"));
                              }

                        return 1;

                    }
                    w.write("curl \"" + writeURL + "\" > output/" + pmid + ".pdf");
                    w.newLine();
             } 
        } catch (IOException e) {
            System.out.println("Problem writing to outputExcutable");
            return -1;
        //} catch (InterruptedException ie) {
       //     System.out.println(ie);
        } 

        return -1;
    }
}

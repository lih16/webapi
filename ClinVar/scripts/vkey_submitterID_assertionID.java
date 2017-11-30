import java.io.*;
import java.lang.CharSequence;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class vkey_submitterID_assertionID {

   static          HashMap<String,String> submitterMAP = new HashMap<String,String>(); 
   static          HashMap<String,Integer> typeMAP = new HashMap<String, Integer>();

   public static class CLINVAR {
        String vkey;
        String chr;
        String pos;
        String ref;
        String alt;
        String submitterID;
        String assertionID;
        String rcv;
        String clinical_significance;
        String alltraits;
        String comment;
    }

    public static void  read_submitterMapFile(String infile) {

        String strLine = null;
        ArrayList<String> rtn = new ArrayList<String>();

        try {

            FileInputStream fstream = new FileInputStream(infile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            while ((strLine = br.readLine()) != null)   {
                  //System.out.println("string MAP [" + strLine + "]");
                  String[] parts = strLine.split("\\t");
                  if (parts.length > 1 ) {
                      submitterMAP.put(parts[0],parts[1].trim());
                  }

             }
          } catch (Exception e){
               System.err.println(e.getMessage());
          }

    } 
           
    public static void main(String[] args) throws FileNotFoundException {
       
            String strLine = null;


            ArrayList<CLINVAR> clinvar = new ArrayList<CLINVAR>();

            read_submitterMapFile(args[0]);

            try {

                FileInputStream fstream1 = new FileInputStream(args[1]);
                BufferedReader br1 = new BufferedReader(new InputStreamReader(fstream1));


                FileOutputStream outstream = new FileOutputStream(args[2]);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outstream));

  
 
                int indx=0;
                //Read File Line By Line
                while ((strLine = br1.readLine()) != null)   {
                     
                     String[] arrAtt = strLine.split("\\t");
                     System.out.println("all submitters " + arrAtt[27] + "  assertionID "+ arrAtt[49]);

                     // remove the header
                     if (arrAtt[0].contains("chrom")) continue;

                     // case 1: assertionID does not contain "||"
                     if (arrAtt[49].contains("||") == false) {
                         CLINVAR cl = new CLINVAR();
                         cl.chr = arrAtt[0];
                         cl.pos = arrAtt[1];
                         cl.ref = arrAtt[2];
                         cl.alt = arrAtt[3];

                         String tempStr = arrAtt[27];
                         StringBuilder tempsb = new StringBuilder();
                         for (int i=0; i <tempStr.length(); i++) {
                           char c = tempStr.charAt(i);
                           int ascii = (int)c;
                           if (c == 8217) {
                             tempsb.append("'");
                             continue;
                           }
                           if (c == 8211) {
                             tempsb.append("-");
                             continue;
                           } else {
                             tempsb.append(c);
                             continue;
                           }
                           //System.out.println(tempStr + " " + ascii);
                         }

                         if (submitterMAP.containsKey(StringUtils.stripAccents(tempsb.toString()))) {
                            cl.submitterID = submitterMAP.get(StringUtils.stripAccents(tempsb.toString()));
                         } else {
                            System.out.println("COULD NOT FIND !!!" + " submitter " + arrAtt[27] );
                         }
                         cl.assertionID = arrAtt[49];
                         clinvar.add(cl);
                         cl = null;

                      }
                    // case 2: assertionID contains "||"
                    if (arrAtt[49].contains("||") == true) {     

                       String[] inArr = arrAtt[49].split(Pattern.quote("||"));
                       String[] sub = arrAtt[27].split(Pattern.quote("||"));

                       for (int j=0; j< inArr.length; j++) {

                         CLINVAR cl = new CLINVAR();
                         cl.chr = arrAtt[0];
                         cl.pos = arrAtt[1];
                         cl.ref = arrAtt[2];
                         cl.alt = arrAtt[3];

                         String tempStr = sub[j];
                         StringBuilder tempsb = new StringBuilder();
                         for (int i=0; i <tempStr.length(); i++) {
                           char c = tempStr.charAt(i);
                           int ascii = (int)c;
                           if (c == 8217) {
                             tempsb.append("'");
                             continue;
                           }
                           if (c == 8211) {
                             tempsb.append("-");
                             continue;
                           } else {
                             tempsb.append(c);
                             continue;
                           }
                           //System.out.println(tempStr + " " + ascii);
                         }

                         if (submitterMAP.containsKey(StringUtils.stripAccents(tempsb.toString()))) {
                            cl.submitterID = submitterMAP.get(StringUtils.stripAccents(tempsb.toString()));
                         } else {
                            System.out.println("COULD NOT FIND !!!" + " submitter " + arrAtt[27] );
                         }
                         cl.assertionID = inArr[j];
                         clinvar.add(cl);
                         cl = null;


                       }

                    }
                 }
               

                 System.out.println("LENGTH of Map "+ submitterMAP.size());
                 
                 StringBuilder sb = new StringBuilder();
                 sb.append("chr" + "\t" + "pos" + "\t" + "ref" + "\t" + "alt" + "\t" + "submitterID" + "\t" + "assertionID");
                 bw.write(sb.toString());
                 bw.newLine();
                 sb.setLength(0);

                 for (int k=0; k < clinvar.size() ; k++){
                       
                       sb.append(clinvar.get(k).chr + "\t" + clinvar.get(k).pos + "\t" + clinvar.get(k).ref + "\t" + clinvar.get(k).alt + "\t" + clinvar.get(k).submitterID + "\t" + clinvar.get(k).assertionID);

                       bw.write(sb.toString());
                       bw.newLine();
                       sb.setLength(0);
            
                 }

                 bw.close();


           } catch (Exception e){
               System.err.println(e.getMessage());
           }

    }
}

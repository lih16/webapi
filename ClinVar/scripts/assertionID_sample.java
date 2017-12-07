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

public class assertionID_sample {

   public static class CLINVAR {

        String assertionID="";
        String sample_num;
        String affected_status="";
        String method_type="";
        String age="";
        String ethnicity="";
        String species="";
        String num_of_families="";
        String num_of_familieswithvariant="";
        String num_of_familieswithsegregation="";
        String was_segregationobserved="";
        String SingleHeterozygote="";
        String VariantAlleles="";
        String Homozygotes=""; 
        String VariantChromosomes="";
        String CompoundHeterozygote="";
        String Hemizygote="";
        String NumberMosaic="";
        String AlleleFrequency="";
        String IndependentObservations="";
        String SecondaryFinding="";
        String SampleLocalID="";
    }

    public static String convertPercentageToRealNumber(String str) {

             String[] parts = str.split(Pattern.quote("%"));
             double f = Double.parseDouble(parts[0]) * 0.01;
System.out.println(" convert percentage " + f);
             return Double.toString(f); 
    }
    public static void main(String[] args) throws FileNotFoundException {
       
            String strLine = null;


            ArrayList<CLINVAR> clinvar = new ArrayList<CLINVAR>();


            try {

                FileInputStream fstream1 = new FileInputStream(args[0]);
                BufferedReader br1 = new BufferedReader(new InputStreamReader(fstream1));


                FileOutputStream outstream = new FileOutputStream(args[1]);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outstream));

  
 
                int indx=0;
                //Read File Line By Line
                while ((strLine = br1.readLine()) != null)   {
                     
                     String[] arrAtt = strLine.split("\\t");
                  System.out.println("indx " + indx );

                     
                     // remove the header
                     if (arrAtt[0].contains("chrom")) continue;

                System.out.println("p start OK submitters " + arrAtt[27] + "  assertionID ["+ arrAtt[49] + "] type [" + arrAtt[38] + "]");

                     if (arrAtt[49].contains("||") == false) {
                     System.out.println("p0 OK submitters " + arrAtt[27] + "  assertionID "+ arrAtt[49] + " type " + arrAtt[38]);

                             if (arrAtt[38].contains("::") == false) {

                     System.out.println("p1 OK submitters " + arrAtt[38] + "  assertionID "+ arrAtt[49] + " obdata " + arrAtt[47]);

                                    if ( arrAtt[47].contains("##") == false ) {
                                          
                                           CLINVAR cl = new CLINVAR();
                                           cl.assertionID = arrAtt[49];
                                           cl.sample_num = "1";
                                           String[] status_pair = arrAtt[37].split("=");
                                           cl.affected_status = status_pair[1];
                                           
                                           String[] type_pair = arrAtt[38].split("=");
                                           cl.method_type = type_pair[1];
                                           String[] age_pair = arrAtt[39].split("=");
                                           cl.age = age_pair[1];
                                          if (arrAtt[40].contains("=")) {
                                           String[] eth_pair = arrAtt[40].split("=");
                                           cl.ethnicity = eth_pair[1]; 
                                           } else {
                                            cl.ethnicity="";
                                           }
                                           String[] species_pair = arrAtt[41].split("=");
                                           cl.species = species_pair[1];
                                            String[] n_f_pair = arrAtt[42].split("=");
                                           cl.num_of_families = n_f_pair[1];
                                            String[] nfv_pair = arrAtt[44].split("=");
                                           cl.num_of_familieswithvariant = nfv_pair[1];
                                           String[] nfs_pair = arrAtt[43].split("="); 
                                           cl.num_of_familieswithsegregation = nfs_pair[1];
                                           String[] was_pair = arrAtt[45].split("="); 
                                           cl.was_segregationobserved = was_pair[1];

                                           if (cl.affected_status.equals("NA"))
                                               cl.affected_status = "";
                                           if (cl.method_type.equals("NA"))
                                               cl.method_type = "";
                                           if (cl.age.equals("NA"))
                                               cl.age = "";
                                           if (cl.ethnicity.equals("NA"))
                                               cl.ethnicity = "";
                                           if (cl.species.equals("NA"))
                                               cl.species = "";
                                           if (cl.num_of_families.equals("NA"))
                                               cl.num_of_families = "";
                                           if (cl.num_of_familieswithvariant.equals("NA"))
                                               cl.num_of_familieswithvariant = "";
                                           if (cl.num_of_familieswithsegregation.equals("NA"))
                                               cl.num_of_familieswithsegregation = "";
                                           if (cl.was_segregationobserved.equals("NA"))
                                               cl.was_segregationobserved = "";
                                            

                                         if (arrAtt[47].contains("=")) { 
                                             String[] sample_obdata_comment_pair = arrAtt[47].split("=");

                                             if (sample_obdata_comment_pair[1].contains("->")) {
                                               String[] keyvalue_pair = sample_obdata_comment_pair[1].split("->");

                                               switch(keyvalue_pair[0]) {
                                                 case "SingleHeterozygote": 
                                                     cl.SingleHeterozygote = keyvalue_pair[1]; break;
                                                 case "VariantAlleles":
                                                     cl.VariantAlleles = keyvalue_pair[1]; break;
                                                 case "Homozygotes":
                                                     cl.Homozygotes = keyvalue_pair[1]; break;
                                                 case "VariantChromosomes":
                                                     cl.VariantChromosomes = keyvalue_pair[1]; break;
                                                 case "CompoundHeterozygote":
                                                     cl.CompoundHeterozygote = keyvalue_pair[1]; break;
                                                 case "Hemizygote":
                                                     cl.Hemizygote = keyvalue_pair[1]; break;
                                                 case "NumberMosaic":
                                                     cl.NumberMosaic = keyvalue_pair[1]; break;
                                                 case "AlleleFrequency":
                                                     cl.AlleleFrequency = keyvalue_pair[1]; 
                                                     if (cl.AlleleFrequency.contains("%")) {
                                                           cl.AlleleFrequency = convertPercentageToRealNumber(cl.AlleleFrequency);
                                                     }
                                                     break;
                                                 case "IndependentObservations":
                                                     cl.IndependentObservations = keyvalue_pair[1]; break;
                                                 case "SecondaryFinding":
                                                     cl.SecondaryFinding = keyvalue_pair[1]; break;
                                                 case "SampleLocalID":
                                                     cl.SampleLocalID = keyvalue_pair[1]; break;
                                                     default:
                                                     break;
                                                 }

                                              }
                                           } 
                                           clinvar.add(cl);
                                           cl = null;
                                            
               System.out.println("pass 1"); 

                                    }
               System.out.println("pass 2"); 
                                    // observed_data level
                                    if ( arrAtt[47].contains("##") == true) {

                                           String[] sample_obdata_comment_pair = arrAtt[47].split("=");

                                           CLINVAR cl = new CLINVAR();
                                           cl.assertionID = arrAtt[49];
                                           cl.sample_num = "1";
               System.out.println("pass 0"); 
                                           String[] status_pair = arrAtt[37].split("=");
                                           cl.affected_status = status_pair[1];
               System.out.println("pass 0"); 
                                           String[] type_pair = arrAtt[38].split("=");
                                           cl.method_type = type_pair[1];
               System.out.println("pass 0"); 
                                           String[] age_pair = arrAtt[39].split("=");
                                           cl.age = age_pair[1];
               System.out.println("pass 0"); 

                                           if (arrAtt[40].contains("=")) {
                                            String[] eth_pair = arrAtt[40].split("=");
                                            cl.ethnicity = eth_pair[1];
                                            } else {
                                             cl.ethnicity="";
                                            }

               System.out.println("pass 0"); 
                                           String[] species_pair = arrAtt[41].split("=");
                                           cl.species = species_pair[1];
               System.out.println("pass 0"); 
                                            String[] n_f_pair = arrAtt[42].split("=");
                                           cl.num_of_families = n_f_pair[1];
               System.out.println("pass 0"); 
                                            String[] nfv_pair = arrAtt[44].split("=");
                                           cl.num_of_familieswithvariant = nfv_pair[1];
               System.out.println("pass 0");String[] nfs_pair = arrAtt[43].split("="); 
                                           cl.num_of_familieswithsegregation = nfs_pair[1];
               System.out.println("pass 0");String[] was_pair = arrAtt[45].split("="); 
                                           cl.was_segregationobserved = was_pair[1];
/*
                                           cl.affected_status = arrAtt[37];
                                           cl.method_type = arrAtt[38];
                                           cl.age = arrAtt[39];
                                           cl.ethnicity = arrAtt[40]; 
                                           cl.species = arrAtt[41];
                                           cl.num_of_families = arrAtt[42];
                                           cl.num_of_familieswithvariant = arrAtt[44];
                                           cl.num_of_familieswithsegregation = arrAtt[43];
                                           cl.was_segregationobserved = arrAtt[45];
*/

                                           if (cl.affected_status.equals("NA"))
                                               cl.affected_status = "";
                                           if (cl.method_type.equals("NA"))
                                               cl.method_type = "";
                                           if (cl.age.equals("NA"))
                                               cl.age = "";
                                           if (cl.ethnicity.equals("NA"))
                                               cl.ethnicity = "";
                                           if (cl.species.equals("NA"))
                                               cl.species = "";
                                           if (cl.num_of_families.equals("NA"))
                                               cl.num_of_families = "";
                                           if (cl.num_of_familieswithvariant.equals("NA"))
                                               cl.num_of_familieswithvariant = "";
                                           if (cl.num_of_familieswithsegregation.equals("NA"))
                                               cl.num_of_familieswithsegregation = "";
                                           if (cl.was_segregationobserved.equals("NA"))
                                               cl.was_segregationobserved = "";

                                        String[] sample_obdata_comment = sample_obdata_comment_pair[1].split(Pattern.quote("##"));
                                        for (int k = 0; k < sample_obdata_comment.length; k++) {

                     System.out.println("p2 OK submitters " + arrAtt[38] + "  assertionID "+ arrAtt[49] + "observedDAta " + sample_obdata_comment[k]);
                                           if (sample_obdata_comment[k].contains("->")) {
                                               String[] keyvalue_pair = sample_obdata_comment[k].split("->");

                                               switch(keyvalue_pair[0]) {
                                                 case "SingleHeterozygote": 
                                                     cl.SingleHeterozygote = keyvalue_pair[1]; break;
                                                 case "VariantAlleles":
                                                     cl.VariantAlleles = keyvalue_pair[1]; break;
                                                 case "Homozygotes":
                                                     cl.Homozygotes = keyvalue_pair[1]; break;
                                                 case "VariantChromosomes":
                                                     cl.VariantChromosomes = keyvalue_pair[1]; break;
                                                 case "CompoundHeterozygote":
                                                     cl.CompoundHeterozygote = keyvalue_pair[1]; break;
                                                 case "Hemizygote":
                                                     cl.Hemizygote = keyvalue_pair[1]; break;
                                                 case "NumberMosaic":
                                                     cl.NumberMosaic = keyvalue_pair[1]; break;
                                                 case "AlleleFrequency":
                                                     cl.AlleleFrequency = keyvalue_pair[1]; 
                                                     if (cl.AlleleFrequency.contains("%")) {
                                                           cl.AlleleFrequency = convertPercentageToRealNumber(cl.AlleleFrequency);
                                                     }
                                                     break;
                                                 case "IndependentObservations":
                                                     cl.IndependentObservations = keyvalue_pair[1]; break;
                                                 case "SecondaryFinding":
                                                     cl.SecondaryFinding = keyvalue_pair[1]; break;
                                                 case "SampleLocalID":
                                                     cl.SampleLocalID = keyvalue_pair[1]; break;
                                                     default:
                                                     break;
                                                 }   

                                           }   


                                        }
                                          clinvar.add(cl);
                                          cl = null;
                                   
                                    }
                            
                             }
                             
                             //sample level
                             if (arrAtt[38].contains("::") == true) {

                                
                                //String[] sampleethnicity  = arrAtt[40].split(Pattern.quote("::"));

                                String[] status_pair = arrAtt[37].split("=");
                                String[] samplestatus = status_pair[1].split(Pattern.quote("::"));

                                String[] type_pair = arrAtt[38].split("=");
                                String[] sampletype = type_pair[1].split(Pattern.quote("::"));

                                String[] age_pair = arrAtt[39].split("=");
                                String[] sampleage = age_pair[1].split(Pattern.quote("::"));

                                String[] sampleethnicity = {};

                                 int ethflg = 0;
                                 if (arrAtt[40].contains("=")) {
                                     String[] ethnicity_pair = arrAtt[40].split("=");
                                     sampleethnicity = ethnicity_pair[1].split(Pattern.quote("::"));
                                 } else {
                                     ethflg = 1;
                                 }

                                String[] species_pair = arrAtt[41].split("=");
                                String[] samplespecies = species_pair[1].split(Pattern.quote("::"));

                                String[] numfamilies_pair = arrAtt[42].split("=");
                                String[] samplenumfamilies = numfamilies_pair[1].split(Pattern.quote("::"));

                                String[] numfamilieswithvariant_pair = arrAtt[44].split("=");
                                String[] samplenumfamilieswithvariant = numfamilieswithvariant_pair[1].split(Pattern.quote("::"));

                                String[] numfamilieswithsegregation_pair = arrAtt[43].split("=");
                                String[] samplenumfamilieswithsegregation = numfamilieswithsegregation_pair[1].split(Pattern.quote("::"));

                                String[] segregation_pair = arrAtt[45].split("=");
                                String[] samplesegregation = segregation_pair[1].split(Pattern.quote("::"));
                                String[] samplect;

                                if (arrAtt[47].contains("NA")) {
                                    samplect=arrAtt[47].split(Pattern.quote("::"));
                                } else {
                                    String[] ct_pair = arrAtt[47].split("=");
                                    samplect = ct_pair[1].split(Pattern.quote("::"));
                                }



                                for (int j = 0; j < sampletype.length; j++) {
                                    if ( samplect[j].contains("##") == false) {
                                           //String[] sample_obdata_comment_pair = samplecomment[j].split("=");

                                           CLINVAR cl = new CLINVAR();
                                           cl.assertionID = arrAtt[49];
                                           cl.sample_num = Integer.toString(j+1);
                                           cl.affected_status = samplestatus[j];
                                           cl.method_type = sampletype[j];
System.out.println("HHH method " + cl.method_type);
                                           cl.age = sampleage[j];
                                           if (sampleethnicity.length > j && ethflg == 0) {
                                               cl.ethnicity = sampleethnicity[j]; 
                                           } else {
                                               cl.ethnicity = "";
                                           }
System.out.println("pass me 0 ");
                                           cl.species = samplespecies[j];
                                           cl.num_of_families = samplenumfamilies[j];
                                           cl.num_of_familieswithvariant = samplenumfamilieswithvariant[j];
System.out.println("pass me 21 ");
                                           cl.num_of_familieswithsegregation = samplenumfamilieswithsegregation[j];
                                           cl.was_segregationobserved = samplesegregation[j];

                                           if (cl.affected_status.equals("NA"))
                                               cl.affected_status = "";
                                           if (cl.method_type.equals("NA"))
                                               cl.method_type = "";
                                           if (cl.age.equals("NA"))
                                               cl.age = "";
                                           if (cl.ethnicity.equals("NA"))
                                               cl.ethnicity = "";
                                           if (cl.species.equals("NA"))
                                               cl.species = "";
                                           if (cl.num_of_families.equals("NA"))
                                               cl.num_of_families = "";
                                           if (cl.num_of_familieswithvariant.equals("NA"))
                                               cl.num_of_familieswithvariant = "";
                                           if (cl.num_of_familieswithsegregation.equals("NA"))
                                               cl.num_of_familieswithsegregation = "";
                                           if (cl.was_segregationobserved.equals("NA"))
                                               cl.was_segregationobserved = "";

//System.out.println("pass me 22 " + samplecomment[j]);
                                           if (samplect[j].contains("->")) {
System.out.println("pass me 23 ");
                                               String[] keyvalue_pair = samplect[j].split("->");
System.out.println("pass me 24 ");

                                               switch(keyvalue_pair[0]) {
                                                 case "SingleHeterozygote": 
                                                     cl.SingleHeterozygote = keyvalue_pair[1]; break;
                                                 case "VariantAlleles":
                                                     cl.VariantAlleles = keyvalue_pair[1]; break;
                                                 case "Homozygotes":
                                                     cl.Homozygotes = keyvalue_pair[1]; break;
                                                 case "VariantChromosomes":
                                                     cl.VariantChromosomes = keyvalue_pair[1]; break;
                                                 case "CompoundHeterozygote":
                                                     cl.CompoundHeterozygote = keyvalue_pair[1]; break;
                                                 case "Hemizygote":
                                                     cl.Hemizygote = keyvalue_pair[1]; break;
                                                 case "NumberMosaic":
                                                     cl.NumberMosaic = keyvalue_pair[1]; break;
                                                 case "AlleleFrequency":
                                                     cl.AlleleFrequency = keyvalue_pair[1]; 
                                                     if (cl.AlleleFrequency.contains("%")) {
                                                           cl.AlleleFrequency = convertPercentageToRealNumber(cl.AlleleFrequency);
                                                     }
                                                     break;
                                                 case "IndependentObservations":
                                                     cl.IndependentObservations = keyvalue_pair[1]; break;
                                                 case "SecondaryFinding":
                                                     cl.SecondaryFinding = keyvalue_pair[1]; break;
                                                 case "SampleLocalID":
                                                     cl.SampleLocalID = keyvalue_pair[1]; break;
                                                     default:
                                                     break;
                                                 }

                                           }
                                           clinvar.add(cl);
                                           cl = null;
System.out.println("pass me 1 ");


                                    }
                                    // observed_data level
                                    if (samplect[j].contains("##") == true) {


                                           CLINVAR cl = new CLINVAR();
                                           cl.assertionID = arrAtt[49];
                                           cl.sample_num = Integer.toString(j+1);
                                           cl.affected_status = samplestatus[j];
                                           cl.method_type = sampletype[j];
                                           cl.age = sampleage[j];

                                           if (sampleethnicity.length > j && ethflg == 0) {
                                               cl.ethnicity = sampleethnicity[j]; 
                                           } else {
                                               cl.ethnicity = "";
                                           }

                                           cl.species = samplespecies[j];
                                           cl.num_of_families = samplenumfamilies[j];
                                           cl.num_of_familieswithvariant = samplenumfamilieswithvariant[j];
                                           cl.num_of_familieswithsegregation = samplenumfamilieswithsegregation[j];
                                           cl.was_segregationobserved = samplesegregation[j];

                                           if (cl.affected_status.equals("NA"))
                                               cl.affected_status = "";
                                           if (cl.method_type.equals("NA"))
                                               cl.method_type = "";
                                           if (cl.age.equals("NA"))
                                               cl.age = "";
                                           if (cl.ethnicity.equals("NA"))
                                               cl.ethnicity = "";
                                           if (cl.species.equals("NA"))
                                               cl.species = "";
                                           if (cl.num_of_families.equals("NA"))
                                               cl.num_of_families = "";
                                           if (cl.num_of_familieswithvariant.equals("NA"))
                                               cl.num_of_familieswithvariant = "";
                                           if (cl.num_of_familieswithsegregation.equals("NA"))
                                               cl.num_of_familieswithsegregation = "";
                                           if (cl.was_segregationobserved.equals("NA"))
                                               cl.was_segregationobserved = "";

                                        String[] sample_obdata_comment = samplect[j].split(Pattern.quote("##"));
                                        for (int k = 0; k < sample_obdata_comment.length; k++) {

                                           if (sample_obdata_comment[k].contains("->")) {
                                               String[] keyvalue_pair = sample_obdata_comment[k].split("->");

                                               switch(keyvalue_pair[0]) {
                                                 case "SingleHeterozygote": 
                                                     cl.SingleHeterozygote = keyvalue_pair[1]; break;
                                                 case "VariantAlleles":
                                                     cl.VariantAlleles = keyvalue_pair[1]; break;
                                                 case "Homozygotes":
                                                     cl.Homozygotes = keyvalue_pair[1]; break;
                                                 case "VariantChromosomes":
                                                     cl.VariantChromosomes = keyvalue_pair[1]; break;
                                                 case "CompoundHeterozygote":
                                                     cl.CompoundHeterozygote = keyvalue_pair[1]; break;
                                                 case "Hemizygote":
                                                     cl.Hemizygote = keyvalue_pair[1]; break;
                                                 case "NumberMosaic":
                                                     cl.NumberMosaic = keyvalue_pair[1]; break;
                                                 case "AlleleFrequency":
                                                     cl.AlleleFrequency = keyvalue_pair[1]; 
                                                     if (cl.AlleleFrequency.contains("%")) {
                                                           cl.AlleleFrequency = convertPercentageToRealNumber(cl.AlleleFrequency);
                                                     }
                                                     break;
                                                 case "IndependentObservations":
                                                     cl.IndependentObservations = keyvalue_pair[1]; break;
                                                 case "SecondaryFinding":
                                                     cl.SecondaryFinding = keyvalue_pair[1]; break;
                                                 case "SampleLocalID":
                                                     cl.SampleLocalID = keyvalue_pair[1]; break;
                                                     default:
                                                     break;
                                                 }   

                                           }   
                      

                                        }
                                        clinvar.add(cl);
                                        cl = null;
                                    }
                                }
                            
                             }

System.out.println(" pass 32");


                     }

System.out.println(" pass 4" + " 49 " + arrAtt[49]);

                    // case 2: assertionID contains "||", submitter level
                    if (arrAtt[49].contains("||") == true) {

System.out.println(" Hello 4");

                       String[] inArr = arrAtt[49].split(Pattern.quote("||"));
                       String[] status = arrAtt[37].split(Pattern.quote("||"));
                       String[] type = arrAtt[38].split(Pattern.quote("||"));
                       String[] age = arrAtt[39].split(Pattern.quote("||"));
                       String[] ethnicity  = arrAtt[40].split(Pattern.quote("||"));
                       String[] species  = arrAtt[41].split(Pattern.quote("||"));
                       String[] numfamilies  = arrAtt[42].split(Pattern.quote("||"));
                       String[] numfamilieswithvariant  = arrAtt[44].split(Pattern.quote("||"));
                       String[] numfamilieswithsegregation  = arrAtt[43].split(Pattern.quote("||"));
                       String[] segregation  = arrAtt[45].split(Pattern.quote("||"));
                       String[] ct = arrAtt[47].split(Pattern.quote("||"));
                       

System.out.println(" HHHH 4" + " type [" + type[0] + "]");

                       for (int i=0; i<inArr.length; i++) {

System.out.println(" i " + i + " type.length [" + type.length + "]");
System.out.println(" i 1" + i + "ct[i] [" +  ct[i] + "] type[i] [" + type[i] + "]");

                             if (type[i].contains("::") == false) {

                                    if (ct[i].contains("##") == false) {

System.out.println(" i 2" + i);


                                           CLINVAR cl = new CLINVAR();
                                           cl.assertionID = inArr[i];
                                           cl.sample_num = Integer.toString(i + 1);

                                           String[] st_pair = status[i].split("=");
                                           cl.affected_status = st_pair[1];
                                           //cl.affected_status = status[i];
                                           String[] type_pair = type[i].split("=");
                                           cl.method_type = type_pair[1];
                                           //cl.method_type = type[i];
System.out.println(" pass 11 00" + " age size " + age.length + "ethnicity size [" + ethnicity.length + "] " + arrAtt[40] + "HH " + arrAtt[41] +  " i [" + i + "]");
                                           String[] age_pair = age[i].split("=");
                                           cl.age = age_pair[1];
                                           //cl.age = age[i];
System.out.println(" pass 11 1");
                                           if (ethnicity.length > i) {
                                               String[] eth_pair = ethnicity[i].split("=");
                                               if (eth_pair.length > 1)
                                                  cl.ethnicity = eth_pair[1]; 
System.out.println(" pass 11 2");
                                           } else {
                                               cl.ethnicity ="";
                                           }
System.out.println(" pass 11 2");
                                           String[] s_pair = species[i].split("=");
                                           cl.species = s_pair[1];

                                           //cl.species = species[i];

                                           String[] nf_pair = numfamilies[i].split("=");
                                           cl.num_of_families = nf_pair[1];
                                           String[] nfwv_pair = numfamilieswithvariant[i].split("=");
                                           cl.num_of_familieswithvariant =nfwv_pair[1]; 

                                           String[] nfws = numfamilieswithsegregation[i].split("=");
                                           cl.num_of_familieswithsegregation = nfws[1];
                                           String[] seg = segregation[i].split("=");
                                           cl.was_segregationobserved = seg[1];

                                           if (cl.affected_status.equals("NA"))
                                               cl.affected_status = "";
                                           if (cl.method_type.equals("NA"))
                                               cl.method_type = "";
                                           if (cl.age.equals("NA"))
                                               cl.age = "";
                                           if (cl.ethnicity.equals("NA"))
                                               cl.ethnicity = "";
                                           if (cl.species.equals("NA"))
                                               cl.species = "";
                                           if (cl.num_of_families.equals("NA"))
                                               cl.num_of_families = "";
                                           if (cl.num_of_familieswithvariant.equals("NA"))
                                               cl.num_of_familieswithvariant = "";
                                           if (cl.num_of_familieswithsegregation.equals("NA"))
                                               cl.num_of_familieswithsegregation = "";
                                           if (cl.was_segregationobserved.equals("NA"))
                                               cl.was_segregationobserved = "";
        
                                           if (ct[i].contains("=") == false) continue;

                                           String[] sample_obdata_comment_pair = ct[i].split("="); 

                                           if (sample_obdata_comment_pair[1].contains("->")) {
                                               String[] keyvalue_pair = sample_obdata_comment_pair[1].split("->");

                                               switch(keyvalue_pair[0]) {
                                                 case "SingleHeterozygote": 
                                                     cl.SingleHeterozygote = keyvalue_pair[1]; break;
                                                 case "VariantAlleles":
                                                     cl.VariantAlleles = keyvalue_pair[1]; break;
                                                 case "Homozygotes":
                                                     cl.Homozygotes = keyvalue_pair[1]; break;
                                                 case "VariantChromosomes":
                                                     cl.VariantChromosomes = keyvalue_pair[1]; break;
                                                 case "CompoundHeterozygote":
                                                     cl.CompoundHeterozygote = keyvalue_pair[1]; break;
                                                 case "Hemizygote":
                                                     cl.Hemizygote = keyvalue_pair[1]; break;
                                                 case "NumberMosaic":
                                                     cl.NumberMosaic = keyvalue_pair[1]; break;
                                                 case "AlleleFrequency":
                                                     cl.AlleleFrequency = keyvalue_pair[1]; break;
                                                 case "IndependentObservations":
                                                     cl.IndependentObservations = keyvalue_pair[1]; break;
                                                 case "SecondaryFinding":
                                                     cl.SecondaryFinding = keyvalue_pair[1]; break;
                                                 case "SampleLocalID":
                                                     cl.SampleLocalID = keyvalue_pair[1]; break;
                                                     default:
                                                     break;
                                                 }

                                           }
                                           clinvar.add(cl);
                                           cl = null;


                                    }
                                    // observed_data level
                                    if (ct[i].contains("##") == true) {
                                           String[] sample_obdata_comment_pair = ct[i].split("="); 


                                           CLINVAR cl = new CLINVAR();
                                           cl.assertionID = inArr[i];
                                           cl.sample_num =  Integer.toString(i + 1);

                                           String[] st_pair = status[i].split("=");
                                           cl.affected_status = st_pair[1];
                                           //cl.affected_status = status[i];
                                           String[] type_pair = type[i].split("=");
                                           cl.method_type = type_pair[1];
                                           //cl.method_type = type[i];
System.out.println(" pass 11 00" + " age size " + age.length + "ethnicity size [" + ethnicity.length + "] " + arrAtt[40] + "HH" + arrAtt[41] +  " i [" + i + "]");
                                           String[] age_pair = age[i].split("=");
                                           cl.age = age_pair[1];
                                           //cl.age = age[i];
System.out.println(" pass 11 [1" + arrAtt[40] + "] i" + i);
                                           if (ethnicity.length > i) {
                                               String[] eth_pair = ethnicity[i].split("=");
System.out.println(" pass 11 2");
                                               if (eth_pair.length > 1)
                                               cl.ethnicity = eth_pair[1]; 
                                           } else {
                                               cl.ethnicity ="";
                                           }
System.out.println(" pass 11 2");

                                           String[] s_pair = species[i].split("=");
                                           cl.species = s_pair[1];


                                           String[] nf_pair = numfamilies[i].split("=");
                                           cl.num_of_families = nf_pair[1];
                                           String[] nfwv_pair = numfamilieswithvariant[i].split("=");
                                           cl.num_of_familieswithvariant =nfwv_pair[1]; 

                                           String[] nfws = numfamilieswithsegregation[i].split("=");
                                           cl.num_of_familieswithsegregation = nfws[1];
                                           String[] seg = segregation[i].split("=");
                                           cl.was_segregationobserved = seg[1];

                                           if (cl.affected_status.equals("NA"))
                                               cl.affected_status = "";
                                           if (cl.method_type.equals("NA"))
                                               cl.method_type = "";
                                           if (cl.age.equals("NA"))
                                               cl.age = "";
                                           if (cl.ethnicity.equals("NA"))
                                               cl.ethnicity = "";
                                           if (cl.species.equals("NA"))
                                               cl.species = "";
                                           if (cl.num_of_families.equals("NA"))
                                               cl.num_of_families = "";
                                           if (cl.num_of_familieswithvariant.equals("NA"))
                                               cl.num_of_familieswithvariant = "";
                                           if (cl.num_of_familieswithsegregation.equals("NA"))
                                               cl.num_of_familieswithsegregation = "";
                                           if (cl.was_segregationobserved.equals("NA"))
                                               cl.was_segregationobserved = "";
                                           /*
                                           cl.affected_status = status[i];
                                           cl.method_type = type[i];
System.out.println(" pass 11");
                                           cl.age = age[i];
                                           cl.ethnicity = ethnicity[i]; 
                                           cl.species = species[i];
                                           cl.num_of_families = numfamilies[i];
                                           cl.num_of_familieswithvariant = numfamilieswithvariant[i];
                                           cl.num_of_familieswithsegregation = numfamilieswithsegregation[i];

                                           cl.was_segregationobserved = segregation[i];
                                            */

                                        String[] sample_obdata_comment = sample_obdata_comment_pair[1].split(Pattern.quote("##"));
                                        for (int k = 0; k < sample_obdata_comment.length; k++) {
System.out.println(" pass 22 11");

                                           if (sample_obdata_comment[k].contains("->")) {
                                               String[] keyvalue_pair = sample_obdata_comment[k].split("->");

                                               switch(keyvalue_pair[0]) {
                                                 case "SingleHeterozygote": 
                                                     cl.SingleHeterozygote = keyvalue_pair[1]; break;
                                                 case "VariantAlleles":
                                                     cl.VariantAlleles = keyvalue_pair[1]; break;
                                                 case "Homozygotes":
                                                     cl.Homozygotes = keyvalue_pair[1]; break;
                                                 case "VariantChromosomes":
                                                     cl.VariantChromosomes = keyvalue_pair[1]; break;
                                                 case "CompoundHeterozygote":
                                                     cl.CompoundHeterozygote = keyvalue_pair[1]; break;
                                                 case "Hemizygote":
                                                     cl.Hemizygote = keyvalue_pair[1]; break;
                                                 case "NumberMosaic":
                                                     cl.NumberMosaic = keyvalue_pair[1]; break;
                                                 case "AlleleFrequency":
                                                     cl.AlleleFrequency = keyvalue_pair[1]; break;
                                                 case "IndependentObservations":
                                                     cl.IndependentObservations = keyvalue_pair[1]; break;
                                                 case "SecondaryFinding":
                                                     cl.SecondaryFinding = keyvalue_pair[1]; break;
                                                 case "SampleLocalID":
                                                     cl.SampleLocalID = keyvalue_pair[1]; break;
                                                     default:
                                                     break;
                                                 }   

                                           }   

                                        }
                                        clinvar.add(cl);
                                        cl = null;
                                    }
                            
                             }
                             
                             //sample level
                             if (type[i].contains("::") == true) {
//System.out.println(" pass 0000 status " + status[i] +  "type " + type[i] + " age " + age[i] + "eth " + ethnicity[i] + " species " + species[i] + " numfa" + numfamilies[i]  );
System.out.println(" pass 0000 status " + status[i] +  "type " + type[i] + " age " + age[i] + " [" + ethnicity.length + "] " + arrAtt[40]); // + "eth " + ethnicity[i] );//+ " species " + species[i] + " numfa" + numfamilies[i]  );
                                String[] status_pair = status[i].split("=");
                                String[] samplestatus = status_pair[1].split(Pattern.quote("::"));
                                String[] type_pair = type[i].split("=");
                                String[] sampletype = type_pair[1].split(Pattern.quote("::"));
                                String[] age_pair = age[i].split("=");
                                String[] sampleage = age_pair[1].split(Pattern.quote("::"));

System.out.println(" pass 00001");
                                //String[] ethnicity_pair = ethnicity[i].split("=");
                                //String[] sampleethnicity = ethnicity_pair[1].split(Pattern.quote("::"));
                                String[] sampleethnicity = {};

                                 int ethflg = 0;
                                 if ( ethnicity.length <= i) {
                                    ethflg = 1;
                                 } else if (ethnicity[i].contains("=")) {
                                     String[] ethnicity_pair = ethnicity[i].split("=");
                                     sampleethnicity = ethnicity_pair[1].split(Pattern.quote("::"));
                                 } else {
                                     ethflg = 1; 
                                 }

System.out.println(" pass 00002");
                                String[] species_pair = species[i].split("=");
                                String[] samplespecies = species_pair[1].split(Pattern.quote("::"));
                                String[] numfamilies_pair = numfamilies[i].split("=");
                                String[] samplenumfamilies = numfamilies_pair[1].split(Pattern.quote("::"));
                                String[] numfamilieswithvariant_pair = numfamilieswithvariant[i].split("=");
                                String[] samplenumfamilieswithvariant = numfamilieswithvariant_pair[1].split(Pattern.quote("::"));
                                String[] numfamilieswithsegregation_pair = numfamilieswithsegregation[i].split("=");
                                String[] samplenumfamilieswithsegregation = numfamilieswithsegregation_pair[1].split(Pattern.quote("::"));
                                String[] segregation_pair = segregation[i].split("=");
                                String[] samplesegregation = segregation_pair[1].split(Pattern.quote("::"));
                                String[] samplect;

System.out.println(" pass 000a03h");
                                if (ct[i].contains("NA")) {
                                    samplect=ct[i].split(Pattern.quote("::"));
                                } else {
                                    String[] ct_pair = ct[i].split("=");
                                    samplect = ct_pair[1].split(Pattern.quote("::"));
                                }


System.out.println(" type [" + type[i] + "] ct [" + ct[i] + "]");

                                for (int j = 0; j < sampletype.length; j++) {
                                    if  (samplect[j].contains("##") == false) {
System.out.println(" p00 [" + samplect[j] +"]");

                                         //String[] sample_obdata_comment_pair = samplect[j].split("=");
                                           CLINVAR cl = new CLINVAR();
                                           cl.assertionID = inArr[i];
                                           cl.sample_num = Integer.toString(i*j + 1);
                                           cl.affected_status = samplestatus[j];
System.out.println(" p00 [" + samplect[j] +"]");
                                           cl.method_type = sampletype[j];
System.out.println(" p00 [" + samplect[j] +"]");
                                           cl.age = sampleage[j];

System.out.println(" p00 [" + samplect[j] +"] j = " +  j +" len " + sampleethnicity.length);
                                           if (ethflg == 0  && sampleethnicity.length > j) {
                                              cl.ethnicity = sampleethnicity[j]; 
System.out.println(" p00 [" + samplect[j] +"]");
                                           } else {
                                              cl.ethnicity = "";
                                           }
                                           
System.out.println(" p00 3");
                                           cl.species = samplespecies[j];
                                           cl.num_of_families = samplenumfamilies[j];
                                           cl.num_of_familieswithvariant = samplenumfamilieswithvariant[j];
                                           cl.num_of_familieswithsegregation = samplenumfamilieswithsegregation[j];
                                           cl.was_segregationobserved = samplesegregation[j];

                                           if (cl.affected_status.equals("NA"))
                                               cl.affected_status = "";
                                           if (cl.method_type.equals("NA"))
                                               cl.method_type = "";
                                           if (cl.age.equals("NA"))
                                               cl.age = "";
                                           if (cl.ethnicity.equals("NA"))
                                               cl.ethnicity = "";
                                           if (cl.species.equals("NA"))
                                               cl.species = "";
                                           if (cl.num_of_families.equals("NA"))
                                               cl.num_of_families = "";
                                           if (cl.num_of_familieswithvariant.equals("NA"))
                                               cl.num_of_familieswithvariant = "";
                                           if (cl.num_of_familieswithsegregation.equals("NA"))
                                               cl.num_of_familieswithsegregation = "";
                                           if (cl.was_segregationobserved.equals("NA"))
                                               cl.was_segregationobserved = "";

                                           if (samplect[j].contains("->")) {
                                               String[] keyvalue_pair = samplect[j].split("->");

                                               switch(keyvalue_pair[0]) {
                                                 case "SingleHeterozygote": 
                                                     cl.SingleHeterozygote = keyvalue_pair[1]; break;
                                                 case "VariantAlleles":
                                                     cl.VariantAlleles = keyvalue_pair[1]; break;
                                                 case "Homozygotes":
                                                     cl.Homozygotes = keyvalue_pair[1]; break;
                                                 case "VariantChromosomes":
                                                     cl.VariantChromosomes = keyvalue_pair[1]; break;
                                                 case "CompoundHeterozygote":
                                                     cl.CompoundHeterozygote = keyvalue_pair[1]; break;
                                                 case "Hemizygote":
                                                     cl.Hemizygote = keyvalue_pair[1]; break;
                                                 case "NumberMosaic":
                                                     cl.NumberMosaic = keyvalue_pair[1]; break;
                                                 case "AlleleFrequency":
                                                     cl.AlleleFrequency = keyvalue_pair[1]; break;
                                                 case "IndependentObservations":
                                                     cl.IndependentObservations = keyvalue_pair[1]; break;
                                                 case "SecondaryFinding":
                                                     cl.SecondaryFinding = keyvalue_pair[1]; break;
                                                 case "SampleLocalID":
                                                     cl.SampleLocalID = keyvalue_pair[1]; break;
                                                     default:
                                                     break;
                                                 }

                                           }
                                           clinvar.add(cl);
                                           cl = null;


                                    }
System.out.println(" p00 [over]");
                                    // observed_data level
                                    if ( samplect[j].contains("##") == true) {
System.out.println(" p11");

System.out.println("cl.status " + samplect[j] );
                                         //String[] sample_obdata_comment_pair = samplect[j].split("=");
                                           CLINVAR cl = new CLINVAR();
                                           cl.assertionID = inArr[i];
                                           cl.sample_num = Integer.toString(i*j + 1);
                                           cl.affected_status = samplestatus[j];
                                           cl.method_type = sampletype[j];
                                           cl.age = sampleage[j];
                                           cl.ethnicity = sampleethnicity[j]; 
                                           cl.species = samplespecies[j];
                                           cl.num_of_families = samplenumfamilies[j];
                                           cl.num_of_familieswithvariant = samplenumfamilieswithvariant[j];
System.out.println("pss 0 ");
                                           cl.num_of_familieswithsegregation = samplenumfamilieswithsegregation[j];
                                           cl.was_segregationobserved = samplesegregation[j];

                                           if (cl.affected_status.equals("NA"))
                                               cl.affected_status = "";
                                           if (cl.method_type.equals("NA"))
                                               cl.method_type = "";
                                           if (cl.age.equals("NA"))
                                               cl.age = "";
                                           if (cl.ethnicity.equals("NA"))
                                               cl.ethnicity = "";
                                           if (cl.species.equals("NA"))
                                               cl.species = "";
                                           if (cl.num_of_families.equals("NA"))
                                               cl.num_of_families = "";
                                           if (cl.num_of_familieswithvariant.equals("NA"))
                                               cl.num_of_familieswithvariant = "";
                                           if (cl.num_of_familieswithsegregation.equals("NA"))
                                               cl.num_of_familieswithsegregation = "";
                                           if (cl.was_segregationobserved.equals("NA"))
                                               cl.was_segregationobserved = "";
System.out.println("pss 1 ");


                                        //String[] sample_obdata_comment = sample_obdata_comment_pair[1].split(Pattern.quote("##"));
                                        String[] sample_obdata_comment = samplect[j].split(Pattern.quote("##"));
                                        for (int k = 0; k < sample_obdata_comment.length; k++) {

System.out.println(" pass 66 ");
                                           if (sample_obdata_comment[k].contains("->")) {
                                               String[] keyvalue_pair = sample_obdata_comment[k].split("->");

                                               switch(keyvalue_pair[0]) {
                                                 case "SingleHeterozygote": 
                                                     cl.SingleHeterozygote = keyvalue_pair[1]; break;
                                                 case "VariantAlleles":
                                                     cl.VariantAlleles = keyvalue_pair[1]; break;
                                                 case "Homozygotes":
                                                     cl.Homozygotes = keyvalue_pair[1]; break;
                                                 case "VariantChromosomes":
                                                     cl.VariantChromosomes = keyvalue_pair[1]; break;
                                                 case "CompoundHeterozygote":
                                                     cl.CompoundHeterozygote = keyvalue_pair[1]; break;
                                                 case "Hemizygote":
                                                     cl.Hemizygote = keyvalue_pair[1]; break;
                                                 case "NumberMosaic":
                                                     cl.NumberMosaic = keyvalue_pair[1]; break;
                                                 case "AlleleFrequency":
                                                     cl.AlleleFrequency = keyvalue_pair[1]; break;
                                                 case "IndependentObservations":
                                                     cl.IndependentObservations = keyvalue_pair[1]; break;
                                                 case "SecondaryFinding":
                                                     cl.SecondaryFinding = keyvalue_pair[1]; break;
                                                 case "SampleLocalID":
                                                     cl.SampleLocalID = keyvalue_pair[1]; break;
                                                     default:
                                                     break;
                                                 }   

                                            }
System.out.println(" pass 77 ");


                                        }
                                        clinvar.add(cl);
                                        cl = null;
System.out.println(" pass 88 ");
                                    }
                                }
                            
                             }

                           
System.out.println(" pass 4.1");
                       } 
                       
System.out.println(" pass 4.5");

                    }
System.out.println(" pass 5");
                    indx ++;

                 }
               

                 System.out.println("num of records "+ clinvar.size());
                 
                 StringBuilder sb = new StringBuilder();
                 sb.append("assertionID" + "\t" + "SampleNumber" + "\t" + "AffectedStatus" + "\t" + "MethodType" + "\t" + "Age" + "\t" + "Ethnicity" + "\t" + "Species" + "\t"+ "NumOfFamilies" + "\t" + 
                           "NumofFamiliesWithVariant" + "\t" + "NumOfFamiliesWithSegregation" + "\t" + "WasSegregationObserved?" + "\t" + "SingleHeterozygote" + "\t" + "Variant Alleles" + "\t" +
                           "Homozygotes" + "\t" + "VariantChromosomes" + "\t" + "CompoundHeterozygote" + "\t" + "Hemizygote" + "\t" + "NumberMosaic" + "\t" +
                           "AlleleFrequency" + "\t" + "IndependentObservations" + "\t" + "SecondaryFinding" + "\t" + "SampleLocalID");
                 bw.write(sb.toString());
                 bw.newLine();
                 sb.setLength(0);

                 for (int k=0; k < clinvar.size() ; k++){
                       
                       sb.append(clinvar.get(k).assertionID + "\t" + clinvar.get(k).sample_num + "\t" + clinvar.get(k).affected_status + "\t" + clinvar.get(k).method_type + "\t" +
                                 clinvar.get(k).age +"\t" + clinvar.get(k).ethnicity + "\t" + clinvar.get(k).species + "\t" + clinvar.get(k).num_of_families + "\t" +
                                 clinvar.get(k).num_of_familieswithvariant + "\t" + clinvar.get(k).num_of_familieswithsegregation + "\t" + clinvar.get(k).was_segregationobserved + "\t" +
                                 clinvar.get(k).SingleHeterozygote + "\t" + clinvar.get(k).VariantAlleles + "\t" + clinvar.get(k).Homozygotes + "\t" + clinvar.get(k).VariantChromosomes + "\t" +
                                 clinvar.get(k).CompoundHeterozygote + "\t" + clinvar.get(k).Hemizygote + "\t" + clinvar.get(k).NumberMosaic + "\t" + clinvar.get(k).AlleleFrequency + "\t" +
                                 clinvar.get(k).IndependentObservations + "\t" + clinvar.get(k).SecondaryFinding + "\t" + clinvar.get(k).SampleLocalID);

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

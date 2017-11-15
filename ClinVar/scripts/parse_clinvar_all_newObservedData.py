#!/usr/bin/env python

import re
import sys
import gzip
import argparse
from collections import defaultdict
import xml.etree.ElementTree as ET

# then sort it: cat clinvar_table.tsv | head -1 > clinvar_table_sorted.tsv; cat clinvar_table.tsv | tail -n +2 | sort  -k1,1 -k2,2n -k3,3 -k4,4 >> clinvar_table_sorted.tsv Reference on clinvar XML tag:
# ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/clinvar_submission.xsd Reference on clinvar XML tag:
# ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/tab_delimited/README

mentions_pubmed_regex = '(?:PubMed|PMID)(.*)'  # group(1) will be all the text after the word PubMed or PMID
extract_pubmed_id_regex = '[^0-9]+([0-9]+)[^0-9](.*)'  # group(1) will be the first PubMed ID, group(2) will be all remaining text

HEADER = ['chrom', 'pos', 'ref', 'alt', 'start', 'stop', 'strand', 'variation_type', 'variation_id', 'rcv', 'scv',
          'allele_id', 'symbol',
          'hgvs_c', 'hgvs_p', 'molecular_consequence',
          'clinical_significance', 'clinical_significance_ordered', 'pathogenic', 'likely_pathogenic',
          'uncertain_significance',
          'likely_benign', 'benign', 'review_status', 'review_status_ordered',
          'last_evaluated', 'all_submitters', 'submitters_ordered', 'all_traits',
          'all_pmids', 'inheritance_modes', 'age_of_onset', 'prevalence',
          'disease_mechanism', 'origin', 'xrefs', 'dates_ordered', 
          'sample_affected_status_ordered', 'sample_methodtype_ordered',
          'sample_age_ordered','sample_ethnicity_ordered', 'sample_species_ordered', 'sample_familydata_numfamilies_ordered',
          'sample_familydata_numfamilieswithsegregationobserved_ordered', 'sample_familydata_numfamilieswithvariant_ordered',
          'sample_familydata_segregationobserved_ordered', 'sample_familydata_familyhistory_ordered', 
          'sample_observeddata_ordered', 
          'phenotype_trait_clinvarassertion_observedin', 'phenotype_trait_clinvarassertion_traitset' ]



def replace_semicolons(s, replace_with=":"):
    return s.replace(";", replace_with)


def remove_newlines_and_tabs(s):
    return re.sub("[\t\n\r]", " ", s)


def parse_clinvar_tree(handle, dest=sys.stdout, multi=None, verbose=True, genome_build='GRCh37'):
    """Parse clinvar XML
    Args:
        handle: Open input file handle for reading the XML data
        dest: Open output file handle or stream for simple variants
        multi: Open output file handle or stream for complex non-single-variant clinvar records
            (eg. compound het, haplotypes, etc.)
        verbose: Whether to write extra stats to stderr
        genome_build: Either 'GRCh37' or 'GRCh38'
    """

    # variation -> rcv (one to many)

    dest.write(('\t'.join(HEADER) + '\n').encode('utf-8'))
    if multi is not None:
        multi.write(('\t'.join(HEADER) + '\n').encode('utf-8'))

    scounter = 0
    mcounter = 0
    skipped_counter = defaultdict(int)
    for event, elem in ET.iterparse(handle):
        if elem.tag != 'ClinVarSet' or event != 'end':
            continue

        # initialize all the fields
        current_row = {}
        current_row['rcv'] = ''
        current_row['variation_type'] = ''
        current_row['variation_id'] = ''
        current_row['allele_id'] = ''

        rcv = elem.find('./ReferenceClinVarAssertion/ClinVarAccession')
        if rcv.attrib.get('Type') != 'RCV':
            print("Error, not RCV record")
            break
        else:
            current_row['rcv'] = rcv.attrib.get('Acc')

        ReferenceClinVarAssertion = elem.findall(".//ReferenceClinVarAssertion")
        measureset = ReferenceClinVarAssertion[0].findall(".//MeasureSet")

        # only the ones with just one measure set can be recorded
        if len(measureset) > 1:
            print("A submission has more than one measure set." + elem.find('./Title').text)
            elem.clear()
            continue
        elif len(measureset) == 0:
            print("A submission has no measure set type" + measureset.attrib.get('ID'))
            elem.clear()
            continue

        measureset = measureset[0]

        measure = measureset.findall('.//Measure')

        current_row['variation_id'] = measureset.attrib.get('ID')
        current_row['variation_type'] = measureset.get('Type')

        # find all scv accession number
        scv_number = []
        for scv in elem.findall('.//ClinVarAssertion/ClinVarAccession'):
            if scv.attrib.get('Type') == "SCV":
                scv_number.append(scv.attrib.get('Acc'))

        current_row['scv'] = ';'.join(set(scv_number))

        # find all the Citation nodes, and get the PMIDs out of them
        pmids = []
        for citation in elem.findall('.//Citation'):
            pmids += [id_node.text for id_node in citation.findall('.//ID') if id_node.attrib.get('Source') == 'PubMed']

        # now find the Comment nodes, regex your way through the comments and extract anything that appears to be a PMID
        comment_pmids = []
        for comment in elem.findall('.//Comment'):
            mentions_pubmed = re.search(mentions_pubmed_regex, comment.text)
            if mentions_pubmed is not None and mentions_pubmed.group(1) is not None:
                remaining_text = mentions_pubmed.group(1)
                while True:
                    pubmed_id_extraction = re.search(extract_pubmed_id_regex, remaining_text)
                    if pubmed_id_extraction is None:
                        break
                    elif pubmed_id_extraction.group(1) is not None:
                        comment_pmids.append(pubmed_id_extraction.group(1))
                        if pubmed_id_extraction.group(2) is not None:
                            remaining_text = pubmed_id_extraction.group(2)

        current_row['all_pmids'] = ';'.join(sorted(set(pmids + comment_pmids)))

        # now find any/all submitters
        submitters_ordered = []
        for submitter_node in elem.findall('.//ClinVarSubmissionID'):
            if submitter_node.attrib is not None and submitter_node.attrib.has_key('submitter'):
                submitters_ordered.append(submitter_node.attrib['submitter'].replace(';', ','))

        # all_submitters will get deduplicated while submitters_ordered won't
        current_row['submitters_ordered'] = ';'.join(submitters_ordered)
        current_row['all_submitters'] = ";".join(set(submitters_ordered))

        # find the clincial significance and review status reported in RCV(aggregated from SCV)
        current_row['clinical_significance'] = []
        current_row['review_status'] = []

        clinical_significance = elem.find('.//ReferenceClinVarAssertion/ClinicalSignificance')
        if clinical_significance.find('.//ReviewStatus') is not None:
            current_row['review_status'] = clinical_significance.find('.//ReviewStatus').text;
        if clinical_significance.find('.//Description') is not None:
            current_row['clinical_significance'] = clinical_significance.find('.//Description').text

        current_row['last_evaluated'] = '0000-00-00'
        if clinical_significance.attrib.get('DateLastEvaluated') is not None:
            current_row['last_evaluated'] = clinical_significance.attrib.get('DateLastEvaluated', '0000-00-00')

        # match the order of the submitter list - edit 2/22/17
        current_row['review_status_ordered'] = ';'.join([
            x.text for x in elem.findall('.//ClinVarAssertion/ClinicalSignificance/ReviewStatus') if x is not None
        ])

        list_significance= [
            x.text.lower() for x in elem.findall('.//ClinVarAssertion/ClinicalSignificance/Description') if x is not None
        ]

        current_row['pathogenic'] = str(list_significance.count("pathogenic"))
        current_row['likely_pathogenic'] = str(list_significance.count("likely pathogenic"))
        current_row['uncertain_significance']=str(list_significance.count("uncertain significance"))
        current_row['benign']=str(list_significance.count("benign"))
        current_row['likely_benign']=str(list_significance.count("likely benign"))

        current_row['clinical_significance_ordered'] = ";".join(list_significance)

        current_row['dates_ordered'] = ';'.join([
            x.attrib.get('DateLastEvaluated', '0000-00-00')
            for x in elem.findall('.//ClinVarAssertion/ClinicalSignificance')
            if x is not None
        ])

        # init new fields
        for list_column in ('inheritance_modes', 'age_of_onset', 'prevalence', 'disease_mechanism', 'xrefs'):
            current_row[list_column] = set()


        # now find the sample components inside //ClinVarAssertion/ObservedIn

        status_arr = []
        methodtype_arr = []
        age_arr = []
        ethnicity_arr = []
        species_arr = []
        numfamilies_arr = []
        numfamilieswithsegregationobserved_arr = []
        numfamilieswithvariant_arr = []
        segregationobserved_arr = []
        history_arr = []
        obdata_arr = []

        one_submitter=''
        ClinVarAssertion = elem.findall(".//ClinVarAssertion")

        for i in range (len(ClinVarAssertion)):
            
            temp_submitter = ClinVarAssertion[i].find('.//ClinVarSubmissionID') 
            if temp_submitter.attrib is not None and temp_submitter.attrib.has_key('submitter'): 
                 one_submitter = temp_submitter.attrib.get('submitter')

            header_rec = one_submitter + "=" 

            sample_status_rec=''
            sample_methodtype_rec=''
            sample_age_rec=''
            sample_ethnicity_rec=''
            sample_species_rec=''
            sample_numfamilies_rec=''
            sample_numfamilieswithsegregationobserved_rec=''
            sample_numfamilieswihvariant_rec=''
            sample_segregationobserved_rec=''
            sample_history_rec=''
            sample_obdata_rec=''

            observedIn = ClinVarAssertion[i].findall('.//ObservedIn')
            for j in range (len(observedIn)):

                sample_affected = observedIn[j].find('.//Sample/AffectedStatus')
                if sample_affected.text is  not None:
                    
                    # initialize the variables 
                    sample_methodtypevalue = 'NA'
                    sample_agevalue = 'NA'
                    sample_ethnicityvalue = 'NA'
                    sample_speciesvalue = 'NA'
                    family_NumFamilies = 'NA'
                    family_NumFamiliesWithSegregationObserved = 'NA'
                    family_NumFamiliesWithVariant = 'NA'
                    family_SegregationObserved = 'NA'
                    family_history = 'NA'
                    sample_observeddatavalue = 'NA'


                    sample_methodtype = observedIn[j].find('.//Method/MethodType')
                    if sample_methodtype.text is not None:
                        sample_methodtypevalue = sample_methodtype.text
                    else:
                        sample_methodtypevalue = 'NA' 


                    sample_ages = observedIn[j].findall('.//Sample/Age')
                    for sample_age in sample_ages:
                       if sample_age is not None:
                           age_type = sample_age.attrib.get('Type')
                           if age_type == 'minimum':
                               sample_agevalue = sample_age.text + '-'
                           if age_type == 'maximum':
                               sample_agevalue = sample_agevalue + sample_age.text 
                       else:
                           sample_agevalue = 'NA' 

                    sample_observeddatas = observedIn[j].findall('.//ObservedData')
                    for sample_obdata in sample_observeddatas:
                       if sample_obdata is not None:
                           if sample_methodtype == 'literature only':
                               citation_id = sample_obdata.find('.//Citation/ID')
                               if citation_id is not None:
                                   id = citation_id.text
                               else:
                                   id = 'NA'
                               desc = sample_obdata.find('.//Attribute')
                               if desc.text is not None and desc.attribu.get('Type') == 'Description':
                                   desc_text = desc.text
                               if j == 0:
                                   sample_observeddatavalue = id + "=" + desc_text
                               else:
                                   sample_observeddatavalue = sample_observeddatavalue + "##" + id + "=" + desc_text


                           else: # type is in ['curation', 'clinical testing', 'research', 'reference population']
                               desc = sample_obdata.find('.//Attribute')
                               if desc.text is not None and desc.attrib.get('Type') == 'Description':
                                   desc_text = desc.text
                                   if j == 0:
                                        sample_observeddaavalue = "Description" + "=" + desc_text
                                   else:
                                        sample_observeddatavalue = sample_observeddatavalue + "##" + "Description" + "=" + desc_text
                               else:
                                   if desc.attrib.get('integerValue') is not None:
                                       if j == 0: 
                                           sample_observeddatavalue = desc.attrib.get('Type') + "=" + desc.attrib.get('integerValue')
                                       else:
                                           sample_observeddatavalue = sample_observeddatavalue + "##" + desc.attrib.get('Type') + "=" + desc.attrib.get('integerValue')
                              
                                   else: 
                                       if j == 0: 
                                           sample_observeddatavalue = desc.attrib.get('Type') + "=" + desc.text
                                       else:
                                           sample_observeddatavalue = sample_observeddatavalue + "##" + desc.attrib.get('Type') + "=" + desc.text

                       else:
                           sample_observeddatavalue = 'NA' 


                    sample_ethnicity = observedIn[j].find('.//Sample/Ethnicity')
                    if sample_ethnicity is not None:
                        sample_ethnicityvalue = sample_ethnicity.text
                    else:
                        sample_ethnicityvalue = 'NA' 
                        
                    sample_species = observedIn[j].find('.//Sample/Species')
                    if sample_species is not None:
                        sample_speciesvalue = sample_species.text
                    else:
                        sample_speciesvalue = 'NA' 

                    familydata = observedIn[j].find('.//Sample/FamilyData')

                    if familydata is not None and familydata.attrib.get('NumFamilies') is not None:
                        family_NumFamilies = familydata.attrib.get('NumFamilies')
                    else:
                        family_NumFamilies = 'NA'


                    
                    if familydata is not None and familydata.attrib.get('NumFamiliesWithSegregationObserved') is not None:
                        family_NumFamiliesWithSegregationObserved = familydata.attrib.get('NumFamiliesWithSegregationObserved')
                    else:
                        family_NumFamiliesWithSegregationObserved = 'NA' 


                    if familydata is not None and familydata.attrib.get('NumFamiliesWithVariant') is not None:
                        family_NumFamiliesWithVariant = familydata.attrib.get('NumFamiliesWithVariant')
                    else:
                        family_NumFamiliesWithVariant = 'NA' 


                    if familydata is not None and familydata.attrib.get('SegregationObserved') is not None:
                        family_SegregationObserved = familydata.attrib.get('SegregationObserved')
                    else:
                        family_SegregationObserved = 'NA'


                    if familydata is not None and familydata.find('FamilyHistory') is not None:
                        family_history =  familydata.find('FamilyHistory').text
                    else:
                        family_history = 'NA' 

                    
                    if j > 0: 

                        sample_status_rec = sample_status_rec + "::" + sample_affected.text 
                        sample_methodtype_rec = sample_methodtype_rec + "::" + sample_methodtypevalue
                        sample_age_rec = sample_age_rec + "::" + sample_agevalue
                        if sample_ethnicityvalue is not None:
                           sample_ethnicity_rec = sample_ethnicity_rec + "::" + sample_ethnicityvalue
                        sample_species_rec = sample_species_rec + "::" + sample_speciesvalue
                        sample_numfamilies_rec = sample_numfamilies_rec + "::"+ family_NumFamilies
                        sample_numfamilieswithsegregationobserved_rec = sample_numfamilieswithsegregationobserved_rec +  "::" + family_NumFamiliesWithSegregationObserved
                        sample_numfamilieswithvariant_rec = sample_numfamilieswithvariant_rec + "::"+ family_NumFamiliesWithVariant
                        sample_segregationobserved_rec = sample_segregationobserved_rec +  "::"+ family_SegregationObserved
                        sample_history_rec = sample_history_rec + "::" + family_history
                        sample_obdata_rec = sample_obdata_rec + "::" + sample_observeddatavalue


                    else:  

                        sample_status_rec = header_rec + sample_affected.text 
                        sample_methodtype_rec = header_rec + sample_methodtypevalue
                        sample_age_rec = header_rec + sample_agevalue
                        if sample_ethnicityvalue is not None:
                           sample_ethnicity_rec = header_rec + sample_ethnicityvalue
                        sample_species_rec = header_rec + sample_speciesvalue
                        sample_numfamilies_rec = header_rec + family_NumFamilies
                        sample_numfamilieswithsegregationobserved_rec = header_rec + family_NumFamiliesWithSegregationObserved
                        sample_numfamilieswithvariant_rec = header_rec + family_NumFamiliesWithVariant
                        sample_segregationobserved_rec = header_rec +  family_SegregationObserved
                        sample_history_rec = header_rec + family_history
                        sample_obdata_rec = header_rec + sample_observeddatavalue
                else:
                    continue

            status_arr.append(sample_status_rec) 
            methodtype_arr.append(sample_methodtype_rec) 
            ethnicity_arr.append(sample_ethnicity_rec) 
            age_arr.append(sample_age_rec) 
            species_arr.append(sample_species_rec) 
            numfamilies_arr.append(sample_numfamilies_rec) 
            numfamilieswithsegregationobserved_arr.append(sample_numfamilieswithsegregationobserved_rec) 
            numfamilieswithvariant_arr.append(sample_numfamilieswithvariant_rec) 
            segregationobserved_arr.append(sample_segregationobserved_rec) 
            history_arr.append(sample_history_rec) 
            obdata_arr.append(sample_obdata_rec)
            
        current_row['sample_affected_status_ordered'] = '||'.join(status_arr) 
        current_row['sample_methodtype_ordered'] = '||'.join(methodtype_arr) 
        current_row['sample_age_ordered'] = '||'.join(age_arr) 
        current_row['sample_ethnicity_ordered'] = '||'.join(ethnicity_arr) 
        current_row['sample_species_ordered'] = '||'.join(species_arr) 
        current_row['sample_familydata_numfamilies_ordered'] = '||'.join(numfamilies_arr) 
        current_row['sample_familydata_numfamilieswithsegregationobserved_ordered'] = '||'.join(numfamilieswithsegregationobserved_arr) 
        current_row['sample_familydata_numfamilieswithvariant_ordered'] = '||'.join(numfamilieswithvariant_arr) 
        current_row['sample_familydata_segregationobserved_ordered'] = '||'.join(segregationobserved_arr) 
        current_row['sample_familydata_familyhistory_ordered'] = '||'.join(history_arr) 
        current_row['sample_observeddata_ordered'] = '||'.join(obdata_arr)
       

        # now find the observeddata_attribute
        current_row['observeddata_attribute'] = "||".join([
            x.text.replace('\r',' ').replace('\n','') for x in elem.findall('.//ReferenceClinVarAssertion/ObservedIn/ObservedData/Attribute') if x is not None and x.text is not None
        ])


        # now find the phenotypes this variant is associated with
        current_row['phenotype_trait_clinvarassertion_observedin']= ''
        for traitset in elem.findall('.//ClinVarAssertion/ObservedIn/TraitSet/Trait'):
            if (traitset.attrib is not None and traitset.attrib.get('Type') == 'Finding'):
               pheno_name_node = traitset.find('.//Name/ElementValue')
               if pheno_name_node is not None:
                   current_row['phenotype_trait_clinvarassertion_observedin'] += pheno_name_node.text 
 
        # now find the phenotypes this variant is associated with
        current_row['phenotype_trait_clinvarassertion_traitset'] = ''
        for traitset in elem.findall('.//ClinVarAssertion/TraitSet/Trait'):
            if (traitset.attrib is not None and traitset.attrib.get('Type') == 'Disease'):
                pheno_name_node = traitset.find('.//Name/ElementValue')
                if pheno_name_node is not None:
                   current_row['phenotype_trait_clinvarassertion_traitset'] += pheno_name_node.text

 
 
        # now find the disease(s) this variant is associated with
        current_row['all_traits'] = []
        for traitset in elem.findall('.//TraitSet'):
            disease_name_nodes = traitset.findall('.//Name/ElementValue')
            trait_values = []
            for disease_name_node in disease_name_nodes:
                if disease_name_node.attrib is not None and disease_name_node.attrib.get('Type') == 'Preferred':
                    trait_values.append(disease_name_node.text)
            current_row['all_traits'] += trait_values

            for attribute_node in traitset.findall('.//AttributeSet/Attribute'):
                attribute_type = attribute_node.attrib.get('Type')
                if attribute_type in {'ModeOfInheritance', 'age of onset', 'prevalence', 'disease mechanism'}:
                    column_name = 'inheritance_modes' if attribute_type == 'ModeOfInheritance' else attribute_type.replace(
                        ' ', '_')
                    column_value = attribute_node.text.strip()
                    if column_value:
                        current_row[column_name].add(column_value)

                        # put all the cross references one column, it may contains NCBI gene ID, conditions ID in disease databases.
            for xref_node in traitset.findall('.//XRef'):
                xref_db = xref_node.attrib.get('DB')
                xref_id = xref_node.attrib.get('ID')
                current_row['xrefs'].add("%s:%s" % (xref_db, xref_id))

        current_row['origin'] = set()
        for origin in elem.findall('.//ReferenceClinVarAssertion/ObservedIn/Sample/Origin'):
            current_row['origin'].add(origin.text)

        for column_name in (
                'all_traits', 'inheritance_modes', 'age_of_onset', 'prevalence', 'disease_mechanism', 'origin',
                'xrefs'):
            column_value = current_row[column_name] if type(current_row[column_name]) == list else sorted(
                current_row[column_name])  # sort columns of type 'set' to get deterministic order
            current_row[column_name] = remove_newlines_and_tabs(';'.join(map(replace_semicolons, column_value)))

        current_row['symbol'] = ''
        var_name = measureset.find(".//Name/ElementValue").text
        if var_name is not None:
            match = re.search(r"\(([A-Za-z0-9]+)\)", var_name)
            if match is not None:
                genesymbol = match.group(1)
                current_row['symbol'] = genesymbol

        for i in range(len(measure)):

            if current_row['symbol'] is None:
                genesymbol = measure[i].findall('.//Symbol')
                if genesymbol is not None:
                    for symbol in genesymbol:
                        if (symbol.find('ElementValue').attrib.get('Type') == 'Preferred'):
                            current_row['symbol'] = symbol.find('ElementValue').text;
                            break

            # find the allele ID (//Measure/@ID)
            current_row['allele_id'] = measure[i].attrib.get('ID')
            # find the GRCh37 or GRCh38 VCF representation
            genomic_location = None

            for sequence_location in measure[i].findall(".//SequenceLocation"):
                if sequence_location.attrib.get('Assembly') == genome_build:
                    if all(sequence_location.attrib.get(key) is not None for key in
                           ('Chr', 'start', 'referenceAllele', 'alternateAllele')):
                        genomic_location = sequence_location
                        break
            # break after finding the first non-empty GRCh37 or GRCh38 location

            if genomic_location is None:
                skipped_counter['missing SequenceLocation'] += 1
                elem.clear()
                continue  # don't bother with variants that don't have a VCF location

            current_row['chrom'] = genomic_location.attrib['Chr']
            current_row['pos'] = genomic_location.attrib['start']
            current_row['ref'] = genomic_location.attrib['referenceAllele']
            current_row['alt'] = genomic_location.attrib['alternateAllele']
            current_row['start'] = genomic_location.attrib['start']
            current_row['stop'] = genomic_location.attrib['stop']
            current_row['strand'] = ''
            for measure_relationship in measure[i].findall(".//MeasureRelationship"):
                if current_row['symbol'] == measure_relationship.find(".//Symbol/ElementValue").text:
                    for sequence_location in measure_relationship.findall(".//SequenceLocation"):
                        if 'Strand' in sequence_location.attrib and genomic_location.attrib['Accession'] == sequence_location.attrib['Accession']:
                            current_row['strand'] = sequence_location.attrib['Strand']
                            break

            current_row['molecular_consequence'] = set()
            current_row['hgvs_c'] = ''
            current_row['hgvs_p'] = ''

            attributeset = measure[i].findall('./AttributeSet')
            for attribute_node in attributeset:
                attribute_type = attribute_node.find('./Attribute').attrib.get('Type')
                attribute_value = attribute_node.find('./Attribute').text;

                # find hgvs_c
                if (attribute_type == 'HGVS, coding, RefSeq' and "c." in attribute_value):
                    current_row['hgvs_c'] = attribute_value

                # find hgvs_p
                if (attribute_type == 'HGVS, protein, RefSeq' and "p." in attribute_value):
                    current_row['hgvs_p'] = attribute_value

                # aggregate all molecular consequences
                if (attribute_type == 'MolecularConsequence'):
                    for xref in attribute_node.findall('.//XRef'):
                        if xref.attrib.get('DB') == "RefSeq":
                            # print xref.attrib.get('ID'), attribute_value
                            current_row['molecular_consequence'].add(":".join([xref.attrib.get('ID'), attribute_value]))

            column_name = 'molecular_consequence'
            column_value = current_row[column_name] if type(current_row[column_name]) == list else sorted(
                current_row[column_name])  # sort columns of type 'set' to get deterministic order
            current_row[column_name] = remove_newlines_and_tabs(';'.join(map(replace_semicolons, column_value)))

            if len(measure) == 1:
                dest.write(('\t'.join([current_row[column] for column in HEADER]) + '\n').encode('utf-8'))
                scounter += 1
            else:
                if multi is not None:
                    multi.write(('\t'.join([current_row[column] for column in HEADER]) + '\n').encode('utf-8'))
                    mcounter += 1

            if scounter % 100 == 0:
                dest.flush()
            if mcounter % 100 == 0:
                if multi is not None:
                    multi.flush()

            counter = scounter + mcounter
            if verbose and counter % 100 == 0:
                sys.stderr.write("{0} entries completed, {1}, {2} total \r".format(
                    counter,
                    ', '.join('%s skipped due to %s' % (v, k) for k, v in skipped_counter.items()),
                    counter + sum(skipped_counter.values())
                ))
                sys.stderr.flush()

        # done parsing the xml for this one clinvar set.
        elem.clear()

    sys.stderr.write("Done\n")


def get_handle(path):
    if path[-3:] == '.gz':
        handle = gzip.open(path)
    else:
        handle = open(path)
    return handle


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Extract PMIDs from the ClinVar XML dump')
    parser.add_argument('-g', '--genome-build', choices=['GRCh37', 'GRCh38'],
                        help='Genome version (either GRCh37 or GRCh38)', required=True)
    parser.add_argument('-x', '--xml', dest='xml_path',
                        type=str, help='Path to the ClinVar XML dump', required=True)
    parser.add_argument('-o', '--out', nargs='?', type=argparse.FileType('w'), default=sys.stdout)
    parser.add_argument('-m', '--multi', help="Output file name for complex alleles")

    args = parser.parse_args()
    if args.multi is not None:
        f = open(args.multi, 'w')
        parse_clinvar_tree(get_handle(args.xml_path), dest=args.out, multi=f, genome_build=args.genome_build)
        f.close()
    else:
        parse_clinvar_tree(get_handle(args.xml_path), dest=args.out, genome_build=args.genome_build)


Download xml file:
	wget ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/xml/ClinVarFullRelease_00-latest.xml.gz
	then gunzip this gz file.

	or directly go to ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/xml/ for monthly xml files.
	place the downloaded file at /sc/orga/projects/AILUN/VISta/data/rawdata/ClinVar/historical_data/   


Export ClinVarSRC path:
	you can export ClinVarSRC in your .bashrc. 
	For example, export ClinVarSRC=/sc/orga/projects/AILUN/VISta/data/rawdata/ClinVar/historical_data


Run parser to extract fields from xml:
	We have two versions of parsers. parse_clinvar_all.py will be used if you want to extract all 37 
	fields covered by Mac Arthur lab plus the new 13 fields. However, parse_clinvar_xml_VISta.py would
	be used if you want to pull out 31 fields (18 covered by Mac Arthur lab plus 13 new fields).

        We suggest using shell script for running it.
	$ ./sh ClinVarFullRelease_2017-10.xml ClinVar_2017-10.csv [ENTER]
        The input file is ClinVarFullRelease_2017-10.xml, and the output is ClinVar_2017-10.csv.
	Of course, before you run it, make sure the input file is under the path of $ClinVarSRC.


Add vkey and vid to each variant:
	After we obtain parsed csv file, we need to add vkey and vid two columns. Nick, could you please 
	add necessary info in here?


Upload csv file to db2:
        Nick, please add something right here. ...
	... 


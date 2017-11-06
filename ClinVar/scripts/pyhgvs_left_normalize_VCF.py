#takes as arg1 a list of tab-delimited VCF-format variants("chr\tpos\tref\talt") and outputs the left-normalized version in the same format
#output file is arg2
#
##IMPORTANT: 
#pyhgvs does not handle adding anchor bases to blank alleles correctly so the input VCF should be preprocessed to add anchor bases (something like /hpc/users/soensz01/scripts/add_anchor_base_to_MVL_VCF.pl) if your input VCF is in the format: chr1 981259 A - (for a del)

#
import sys #command line argument passing
import pyhgvs as hgvs
import pyhgvs.utils as hgvs_utils
import pygr.seqdb
from pygr.seqdb import SequenceFileDB

hg19 = SequenceFileDB('/sc/orga/projects/chenr02a/KBase/download/hgdownload.cse.ucsc.edu/goldenPath/hg19.fa')

out = open(sys.argv[2], 'w')
with open (sys.argv[1]) as input:
	for variant in input:
		variant=variant.strip()
		#chr_original, pos_original, ref_original, alt_original = variant.split("\t")
                arr = variant.split("\t")
                chrom, pos, ref, alt = arr[0], int(arr[1]), arr[2], arr[3]
                chrom_norm = chrom if chrom.startswith('chr') else 'chr' + chrom
                # Because hg38 uses chrM and GRCh38 uses 'MT'
                if chrom_norm == 'chrMT':
                    chrom_norm = 'chrM'
                # Check for insertions with no anchor base:
                if ref in ['-', '.']:
                    anchor = str(hg19[chrom_norm][pos-1:pos])
                    ref = anchor
                    alt = anchor + alt
                # Check for deletions with no anchor base:
                elif alt in ['-', '.']:
                    anchor = str(hg19[chrom_norm][pos-2:pos-1])
                    ref = anchor + ref
                    alt = anchor
                    pos = pos - 1

		#one thing to be careful of if modifying is normalize_variant() expects the alt allele to be an array (in [])
		norm_var = hgvs.normalize_variant(str(chrom_norm), int(pos), ref, [alt], hg19)
                out.write(norm_var.position.chrom+"\t"+str(norm_var.position.chrom_start)+"\t"+norm_var.ref_allele+"\t"+norm_var.alt_alleles[0]+"\n")

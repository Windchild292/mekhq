# User Data Folder
Written 23-APR-2021
MekHQ version 0.49.0

This is a list of all supported files in the userdata directory. Any file not listed here has not been checked to ensure it works properly, and thus the userdata folder is not supported for them. Directories are especially likely to not work if you place them here.

All files, unless specified, are overwrite implemented. This means that the system will load the userdata version if it exists, and then fallback to using the default version.

## General Suite Directories/Files:

## MegaMek-specific Folders/Files:

## MegaMekLab-specific Directories/Files:

## MekHQ-specific Directories/Files:
### data/universe Directory
randomDeathCauses.xml: This contains the causes of an individual's random death based on the random death age ranges and the weighted personnel statuses.
ranks.xml: This contains custom rank files. This is merge implemented, so that is will be merged with the default rank systems in MekHQ on load.

# Summary

A program that allows it's user to break a file into smaller files and transforms them into future jobs to be processed (stemmed) and count the number of apparitions of a certain word.

# Detailed

The program can take an argument called "-source" that will start being parsed. If the file is bigger than the default "-filesize" than it will start creating a temporary file and will copy the first &filesize bytes from the source and upload it to the mongoDB gridFS with a modified metadata and with a chunkSize specified or default (see below). This process of reading and uploading is done on a secondary thread.

The main thread, after sending the file to be fragmented and uploaded interogates the mongoDB gridFS files for those files of which metadata has specified metadata.proccessed = false. If the number of files respecting this condition is 0, the thread sleeps for 2.5 minutes and repets the interogation up to 10 times. If a job file becomes present, the job consumer is called and the main thread will wait for it's finish state.

The job consumer is a third thread that will download a file uploaded to the mongoDB with the metadata.processed=false, will mark that file as metadata.processed=true, stem the file to find the unique words and update/upload the words and count to the Dictionary collection. Using the machine name will update the metadata.processer=machineID, and upload in the Worker collection a document with the worker name, job and status. This could be used in the future to see if a job file is blocked or marked as processed=true but didn't completly finished being processed. If the job is correctly finished then it's deleted along with it's chunks.

If the file source was succesfully uploaded and there are no more jobs in the mongoDB, the program will print the first &resultSize words with the MAX count and MIN count in the &result output specified.

# F.A.Q

## Efficiency - currently the program is fairly efficient but it's not very accurate. 

 1. Stemming is one of the big accuracy drowbacks but it's time efficient for this sample. If accuracy is what you are looking for, I recommend using a lemmatization. You can add it to the project and just modify the JobConsumer (see Wiki).

 2. Currently I have to say that the bigger the file you want to process, the smaller the -filesize should be set. Tested with a 65GB sourcefile and a 512MB &filesize will take longer than a 100MB &filesize, also if there is a second run on another server that only has a sourcefile of 12MB then it will help the first one to finish the jobs and if no source is specified, the program will continue to process jobs as long as they are present in the mongoDB. Also I don't recommend a smaller size than 10MB, the reason I went for gridFS is because normal collections would be too small to store.
 -TODO - the program could be modified in order to check the jobsize before deciding what to do. This way we can decide if a server is powerful enough to take some jobs at startup.

## Security - mongoDB
 -TODO - the program considers that it has all the rights to create / delete / upsert / find collections and documents as he pleases.

## Accuracy

 -TODO - make the results to be displayed in a proper order and saved in a proper format (currently sepparated by ";")
 -TODO - language specification as parrameter and propper lemmatization process to be set-up.


## Parameters supported

-source			  Input file to be processed and filepath("/usr/Documents/", "C:\Folder\filename.ext")

-mongo			  The ip and port adress of the mongoDB server
				      localhost, 192.168.100.1:99882 ...

-log			    The output method that the program will have
				      console/file/all/none (For file a file will be created at a relative path with the name /logs/TheFloow.yyyyMMdd.HH.mm.log)

-chunksize		Number of bytes of a chunk in mongodb for the subfiles file ex: 371234
				      Default:358400

-filesize		  Number of bytes of the subfiles created from the sourcefile in mongodb for the source file ex: 371234
				      Default:10870912

-result			  The display method of the rezults
				      console/file/all/none (For file a file will be created with the name /rez/TheFloow.yyyyMMdd.HH.mm.rez)

-resultSize		The number of MAX and MIN count entry of the results

## Example of ran

java –Xmx8192m -jar PlatformJob.jar –source dump.xml –mongo localhost -log file -chunksize 358400 -filesize 10870912 -result file -resultSize 25




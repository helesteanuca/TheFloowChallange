# Challange

  The main scope of the project is to create an java application that will take as optional input a file, will break it into smaller files and upload them as Jobs to be processed further or by other instances of the same application on other servers.

## The functional goal 
-of the challenge is to analyse a file with a large body of text and to count unique words so that the most common and least common words can beidentified. Your solution should also provide a means of viewing the results.

## The technical goal 
-of the challenge is to create a system that distributes the workloadand   scales   easily.   Your   solution   should   demonstrate   that  you   are   capable   ofengineering  a   system,   we   therefore   discourage   the   use   of   frameworks   that   willmanage the distribution in its entirely.You are required to produce a program that counts the words in a file and saves thecounts to a MongoDB server. The program will need to support execution on multipleservers that communicate via a common means (e.g. a MongoDB collection) andwork together to break down the workload.

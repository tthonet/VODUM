**VODUM: a Topic Model Unifying Viewpoint, Topic and Opinion Discovery**
======

## __Introduction__

This github repository provides the source code and data used in the paper *VODUM: a Topic Model Unifying Viewpoint, Topic and Opinion Discovery* by Thibaut Thonet, Guillaume Cabanac, Mohand Boughanem, and Karen Pinel-Sauvagnat, published at ECIR '16. More detail about this work can be found in the original paper (http://www.irit.fr/publis/SIG/2016_ECIR_TCBPS.pdf).

The source code presented here is the Java implementation of a collapsed Gibbs sampler for VODUM. It is based on the JGibbLDA implementation of collapsed Gibbs sampling for LDA (http://jgibblda.sourceforge.net/).
We provide as well a program to automatically evaluate the perplexity and viewpoint identification accuracy performance of a VODUM model learned through collapsed Gibbs sampling.
This repository also contains the preprocessed collection of documents used for the evaluation of VODUM in our paper. These documents come from the Bitterlemons e-zine (http://www.bitterlemons.net/), and were first introduced as a collection in *Which Side are You on? Identifying Perspectives at the Document and Sentence Levels* by Wei-Hao Lin, Theresa Wilson, Janyce Wiebe, and Alexander Hauptmann, published at CoNLL '06 (http://aclweb.org/anthology/W/W06/W06-2915.pdf). The original collection is available at https://sites.google.com/site/weihaolinatcmu/data. We preprocessed this collection using LingPipe (http://alias-i.com/lingpipe/) according to the process described in our paper.

## __Content__

In this section, we describe in detail the content of this github repository.

* The directory **bin** contains the runnable jar files **vodum.jar** and **vodum-evaluation.jar** that can be executed to perform collapsed Gibbs sampling and model evaluation, respectively. The next sections detail how to use these jar files.

* The directory **data** contains a directory **bitterlemons** with the preprocessed data file of the Bitterlemons collection (**bitterlemons.dat**). The data file is organized as follows. The first line indicates the number of documents in the collection, every other line corresponds to a document. In a document, words, represented by strings, are separated with spaces and sentences are separated with pipes ("|"). Each word is provided with its part-of-speech-based category (separated from the word with a colon ":"): 0 for topical word category and 1 for opinion word category. The directory **data** also contains the ground truth file with the document-level viewpoint assignments of Bitterlemons (**bitterlemons.grt**). In the ground truth file, 0 denotes the Palestinian viewpoint and 1 denotes the Israeli viewpoint.

* The directory **lib** contains the libraries used by our program. It contains the files **args4j-2.0.6.jar** and **commons-io-2.4.jar** that correspond to the Args4j library (http://args4j.kohsuke.org/) and the Apache Commons IO library (http://commons.apache.org/io), respectively.

* The directory **src** contains the source code of our program, compressed in the **vodum-src.jar** jar file.

* The file **LICENCE.txt** describes the licence of our code, as well as that of software and libraries on which our program is based.

* The file **README.md** is the current file.


## __Collapsed Gibbs sampling for VODUM__

### __Parameter estimation__

This section details how to use the collapsed Gibbs sampler we implemented in order to perform parameter estimation on VODUM. VODUM's parameters are learned using the collection provided as input.

#### __Command line execution__

The parameters of a VODUM model can be learned through collapsed Gibbs sampling using the following command:
<pre><code>$ java -jar bin/vodum.jar -est [-alpha &lt;double&gt;] [-beta0 &lt;double&gt;] [-beta1 &lt;double&gt;] [-eta &lt;double&gt;] [-ntopics &lt;int&gt;] [-nviews &lt;int&gt;] [-nchains &lt;int&gt;] [-niters &lt;int&gt;] [-savestep &lt;int&gt;] [-topwords &lt;int&gt;] -dir &lt;string&gt; -dfile &lt;string&gt;</code></pre>

The semantic of each parameter is detailed below:

* ``-est``: Specifies that the program is run to estimate parameter (learning step).

* ``-alpha <double>``: Value of &alpha;, the symmetric Dirichlet prior for the &theta; distribution (distribution over topics).

* ``-beta0 <double>``: Value of &beta;<sub>0</sub>, the symmetric Dirichlet prior for the &phi;<sub>0</sub> distribution (distribution over topical words).

* ``-beta1 <double>``: Value of &beta;<sub>1</sub>, the symmetric Dirichlet prior for the &phi;<sub>1</sub> distribution (distribution over opinion words).

* ``-eta <double>``: Value of &eta;, the symmetric Dirichlet prior for the &pi; distribution (distribution over viewpoints).

* ``-ntopics <int>``: Number of topics (T).

* ``-nviews <int>``: Number of viewpoints (V).

* ``-nchains <int>``: Number of chains (independent executions of the program) to perform.

* ``-niters <int>``: Number of iterations to perform for each chain.

* ``-savestep <int>``: Number of steps (one step corresponds to one iteration) between samples to be saved. If the savestep is higher than the niters, only one sample (the sample for the last iteration) will be saved for each chain.

* ``-topwords <int>``: Number of top words (most likely words in &phi;<sub>0</sub> and &phi;<sub>1</sub>, for each viewpoint and topic) to save.

* ``-dir <string>``: Path of the directory containing the data file, and where the samples will be saved.

* ``-dfile <string>``: Name of the data file.

**Example:**
<pre><code>$ java -jar "bin/vodum.jar" -est -alpha 0.01 -beta0 0.01 -beta1 0.01 -eta 100 -ntopics 12 -nviews 2 -nchains 5 -niters 1000 -savestep 1000 -topwords 20 -dir "data/bitterlemons" -dfile "bitterlemons.dat"</code></pre>

#### __Output files__

The execution of the collapsed Gibbs sampler for parameter estimation outputs the following files for each model (sample) generated:

* **&lt;model name&gt;.assign**: This file contains the viewpoint and topic assignments. As in the data file used as input, words are separated with spaces and sentences with pipes ("|"), and part-of-speech categories are also separated with colons (":"). The differences are that the number of documents is not written in the first line, and words are represented by an index instead of a string. The matching between indices and strings is given in the wordmap file. The document-level viewpoint assignments are specified at the beginning of each line and separated with the first sentence with a pipe ("|"). The sentence-level topic assignments is provided at the end of each sentence, separated from the words with a semi-colon (";").

* **&lt;model name&gt;.others**: This file contains the value of the parameters (*alpha*, *beta0*, *beta1*, *eta*, *ntopics*, *nviews*) used in the model. It also specifies the number of documents in the collection (*ndocs*), the size of the vocabulary (*nwords*), the number of different topical and opinion words (*ntopwords* and *nopwords*, respectively), and the perplexity of the model (*perplexity*).

* **&lt;model name&gt;.phi0**: This file contains the distributions over topical words &phi;<sub>0</sub>. The file is composed of as many lines as the number of topics. Each line corresponds to the distribution over words for the corresponding topic. The probability of words ordered according to their wordmap index are separated with spaces.

* **&lt;model name&gt;.phi1**: This file contains the distributions over opinion words &phi;<sub>1</sub>. The file is composed of as many blocks as the number of viewpoints. Blocks are separated with an empty line. Each block is composed of as many lines as the number of topics. Each line corresponds to the distribution over words for the corresponding topic and the viewpoint corresponding to the block. The probability of words ordered according to their wordmap index are separated with spaces.

* **&lt;model name&gt;.pi**: This file contains the distribution over viewpoints &pi;. The probability of viewpoints are separated with spaces.

* **&lt;model name&gt;.theta**: This file contains the distribution over topics &theta;. The file is composed of as many lines as the number of viewpoints. Each line corresponds to the distribution over topics for the corresponding viewpoint. The probability of topics are separated with spaces.

* **&lt;model name&gt;.twords**: This file contains the most likely words in the distribution over topical words &phi;<sub>0</sub>. It provides for each topic the words (represented by strings) with the highest probability and its probability in &phi;<sub>0</sub>. 

* **&lt;model name&gt;.vtwords**: This file contains the most likely words in the distribution over opinion words &phi;<sub>1</sub>. It provides for each topic-viewpoint pair the words (represented by strings) with the highest probability and its probability in &phi;<sub>0</sub>. 

* **&lt;model name&gt;.wordmap**: This file contains the matching between the string representation and the index representation of the words in the vocabulary. The first line corresponds to the number of different words in the vocabulary.

### __Inference__

This section details how to use the collapsed Gibbs sampler we implemented in order to perform inference on VODUM. This requires that a model was learned beforehand on a collection of documents. Inference is then performed by using the learned parameters to infer the new parameters for held out documents and the updated model.

#### __Command line execution__

The inference on VODUM can be performed using the following command:
<pre><code>$ java -jar bin/vodum.jar -inf [-niters &lt;int&gt;] [-topwords &lt;int&gt;] -dir &lt;string&gt; -dfile &lt;string&gt; -model &lt;string&gt;</code></pre>

The semantic of each parameter is detailed below:

* ``-inf``: Specifies that the program is run to perform inference (testing step).

* ``-niters <int>``: Number of iterations to perform.

* ``-topwords <int>``: Number of top words (most likely words in &phi;<sub>0</sub> and &phi;<sub>1</sub>, for each viewpoint and topic) to save.

* ``-dir <string>``: Path of the directory containing the held out data file and the model learned beforehand, and where the inference samples will be saved.

* ``-dfile <string>``: Name of the held out data file.

* ``-model <string>``: Name of the model learned beforehand and that will be used to perform inference on held out data.

**Example:** 
<pre><code>$ java -jar "bin/vodum.jar" -inf -niters 1000 -topwords 20 -dir "data/bitterlemons" -dfile "bitterlemons-test.dat" -model "model-01-final"</code></pre>

#### __Output files__

The files generated for the inference are the same as the files ouput by parameter estimation. The difference is that the prefix of all files, which was given by the model name in parameter estimation, is now the name of the model learned beforehand, to which is appended "-inference": **&lt;model name&gt;-inference**. The information provided in those files is related only to the held out documents and not the documents of the original data file on which a model was trained.

## __Model evaluation__

#### __Command line execution__

A model learned by the collapsed Gibbs sampler can be evaluated using the following command:
<pre><code>$ java -jar "bin/vodum-evaluation.jar" &lt;ground truth-file-path&gt; &lt;model directory path&gt; &lt;output file path></code></pre>

The semantic of each parameter is detailed below:

* ``<ground truth file path>``: Path of the file containing the viewpoint ground truth judgments of the collection on which a model was learned. The file has to contain one document-level viewpoint assignment per line, with as many lines as the number of documents in the collection, and in the same order as documents are organized in the collection.

* ``<model directory path>``: Path of the directory containing the model (samples) learned on the collection.

* ``<output file path>``: Path of the output file, where the results of the evaluation are written. The summary of the evaluation contains information about the perplexity and viewpoint identification accuracy performance for each sample evaluated, as well as the minimal, maximal, and average performance for these two metrics.

**Example:**
<pre><code>$ java -jar "bin/vodum-evaluation.jar" "data/bitterlemons.grt" "data/bitterlemons" "eval/bitterlemons.txt"</code></pre>

#### __Output file__

The model evaluation ouputs a log file that summarizes the results of the evaluation. The first lines of the file contain the individual results for all samples contained in the model directory, indicating their performance in terms of perplexity and accuracy. The last lines of the file provides the overall performance, indicating the minimal, average, and maximal perplexity and accuracy over all samples.


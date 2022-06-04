# Representative Subset Selection #

A Java implementation of the algorithms in our TKDE paper "Efficient Representative Subset Selection over Sliding Windows", which is available on [IEEE Xplore](https://ieeexplore.ieee.org/document/8410031/) and [arXiv](https://arxiv.org/abs/1706.04764).

## Requirements ##

JDK 8+

## Dependencies ##

- Apache Commons Math 3.6.1, downloaded from <https://mvnrepository.com/artifact/org.apache.commons/commons-math3/3.6.1>  
- Fastutil 7.0.11, downloaded from <https://mvnrepository.com/artifact/it.unimi.dsi/fastutil/7.0.11>

## Usage ##

### 1. Input Format ###

For active set selection, the format of an element is:  

```csv
    <id>:<feature_1,feature_2,......,feature_l>:<cost_1,cost_2,......,cost_d>  
```

In the Yahoo! Webscope dataset, each feature vector has 5 dimensions; each element is assigned with 5 costs.  
The feature vector is L1-normalized; the cost is linearly scaled, with the average value to be 1.  
An example element is like:  

```csv
    2:0.088932,0.003915,0.592269,0.314084,0.000801:0.976,1.078,1.36,1.252,0.472  
```

We provide a sample of the Yahoo! Webscope dataset we use in our experiments, namely "yahoo-sample".  
  
For social stream summarization, the format of an element is:  

```csv
    <id>:<word_1,weight_1 word_2,weight_2 ...... word_l,weight_l>:<cost_1,cost_2,......,cost_d>  
```

In the Twitter dataset, each element is assigned with 5 costs.  
The weight of a word is linearly scaled, with the minimum value to be 1; the cost is also linearly scaled, with the average value to be 1.  
An example element is like:  

```csv
    1:58391,1.74 20167,7.3 82163,40.36 39179,169.55 46142,1.18:1.348,0.95,1.02,0.6,0.708  
```

We provide a sample of the Twitter dataset we use in our experiments, namely "twitter-sample".  

### 2. How to Run the Code ###

To run the KnapWindow (KW) algorithm for active set selection:  

```shell
    $ java -jar activeset-kw.jar <dataset_dir> <W> <d> <c> <lambda>  
    dataset_dir:    String, the path of dataset file  
    W:    int, the window size  
    d:    int, the dimension of knapsacks (costs)  
    c:    float, the average cost  
    lambda:    float, the parameter lambda in KW  
```

For example,  

```shell
    java -jar activeset-kw.jar yahoo-sample 10000 2 0.04 0.01  
```

To run the KnapWindowPlus (KW<sup>+</sup>) algorithm for active set selection:  

```shell
    $ java -jar activeset-kwplus.jar <dataset_dir> <W> <d> <c> <lambda> <beta>  
    dataset_dir:    String, the path of dataset file  
    W:    int, the window size  
    d:    int, the dimension of knapsacks (costs)  
    c:    float, the average cost  
    lambda:    float, the parameter lambda in KW+  
    beta:    float, the parameter beta in KW+  
```

For example,  

```shell
    java -jar activeset-kwplus.jar yahoo-sample 10000 2 0.04 0.01 0.01  
```

To run the KnapWindow (KW) algorithm for social stream summarization:  

```shell
    java -jar summarization-kw.jar <dataset_dir> <W> <d> <c> <lambda>  
```

The parameter setting is the same as active set selection.  
For example,  

```shell
    java -jar summarization-kw.jar twitter-sample 10000 2 0.04 0.01  
```

To run the KnapWindowPlus (KW<sup>+</sup>) algorithm for social stream summarization:  

```shell
    java -jar summarization-kwplus.jar <dataset_dir> <W> <d> <c> <lambda> <beta>  
```

The parameter setting is the same as active set selection.  
For example,  

```shell
    java -jar summarization-kwplus.jar twitter-sample 10000 2 0.04 0.01 0.01  
```

### 3. Output Format ###

The name of the result file is "`<dataset_dir>-KW-<W>-<d>-<c>-<lambda>.csv`" for KW or "`<dataset_dir>-KWPLUS-<W>-<d>-<c>-<lambda>-<beta>.csv`" for KW<sup>+</sup>.  
  
For KW, each line of the result file is like:  

```csv
    <cur_id>,<function_value>,<stream_CPU_time>,<post_CPU_time>  
    cur_id:    the ID of the most recent element in the stream  
    function_value:    the utility function value of the result returned by KW  
    stream_CPU_time:    the CPU time elapsed for stream processing  
    post_CPU_time:    the CPU time elapsed for post-processing  
```

For KW<sup>+</sup>, each line of the result file is like:  

```csv
    <cur_id>,<function_value>,<stream_CPU_time>,<post_CPU_time>,<num_elements>,<num_checkpoints>  
    cur_id:    the ID of the most recent element in the stream  
    function_value:    the utility function value of the result returned by KW+  
    stream_CPU_time:    the CPU time elapsed for stream processing  
    post_CPU_time:    the CPU time elapsed for post-processing  
    num_elements:    the number of elements maintained by KW+  
    num_checkpoints:    the number of checkpoints maintained by KW+  
```

## Contact ##

If there is any question, feel free to contact: [Yanhao Wang](mailto:yhwang@dase.ecnu.edu.cn).

# Overview

This is a simple (Smart) Url Shorter built mainly to gain more experience with several aspects of Web Development.
The project consists of the following major parts:

* Backend: built by [Ayhem18](https://github.com/ayhem18)
* FrontEnd: TODO
* Deployment /devops: TODO  

# Usage

TODO

# Features

the site mimics a service that offers url shortening features to companies. Each company is associated with 
a unique identifier and site and a subscription. The subscription determines the scale and quality of the service 
provided.

## The url shortening algorithm
### Naive Idea

1. create the following two endpoints: 
    * api/url/shorten: basically map any (valid) url to some sort of hash
    * api/redirect/{url_hash}: basically redirects the hashed url to the original one
2. save each (url, url_hashed) pairs

This approach, although simple and easy to implement, takes a lot of resources as the memory constraints is 
$O(N)$ where $N$ is the total number of urls ever received.  


### Implemented algorithm
To my understanding of urls, a given url can be divided into levels separated by the "/" delimiter. 
Each level is composed of the following elements: 

1. level name
2. path variable
3. query parameter name(s)
4. query parameter value(s)

My simple algorithm is based on the observation above. For a given company, save the data in question for each level 
which gives the possibility to encode and decode exponentially more urls than the naive approach.

For example, given a GitHub as a client to encode (and decode) the following url: 


https://github.com/ayhem18?tab=overview&from=2025-01-01&to=2025-01-13

the backend saves the following document: 

{
* site: www.github.com

* level1: {
    * path variables : {ayhem18 : 'a'}

    * queryParameterNames: {tab: 'a', from: 'b', to : 'c'}
  
    * queryParameterValues: { 2025-01-01: 'a', '2025-01-13': 'b'}
  
  }
}

for the following url: 

https://github.com/ayhem18/urlShortener/issues

the backend saves the following document: 

{
* site: www.github.com

* level1:
  {
  * path variables : {ayhem18 : 'a'} 
  
  }

* level2:
  {
  * path variables : {urlShortener : 'a'}
  
  }
  
* level3: 
   { 
   * levelName: { issues: 'a'}
   
   }
}



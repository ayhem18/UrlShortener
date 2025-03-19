# The url shortening algorithm
## Naive Idea

1. create the following two endpoints: 
    * api/url/shorten: basically map any (valid) url to some sort of hash
    * api/redirect/{url_hash}: basically redirects the hashed url to the original one
2. save each (url, url_hashed) pairs

This approach, although simple and easy to implement, takes a lot of resources as the memory constraints is 
$O(N)$ where $N$ is the total number of urls ever received.  


## Suggested algorithm 
The first observation is that the url can be divided into levels separated by the "/" delimiter. 
Each level is composed of the following elements: 

1. level name
2. path variable
3. query parameter name(s)
4. query parameter value(s)

Level Names and query parameter names can be considered similary in the sense that, for a given domain, level names and query parameters are common across many urls. 

path variables and query parameter values on the other hand can vary a lot. 

It might not be as useful to save query parameter values / path variables as it is to save the query parameter names / level names.

The algorithm is based on these observations. 

For a given company, which is associated with a given domain, we can encode the data per level. The minimum length to encode a level name and query parameter name should be lower than that of the query parameter values / path variables.  

1. Given a url, extract the level information for each level 
2. for each level, using the type of the string, encode the data if it is longer than the minimum length associated with the type. For example, if the string is a path variable, and its length is less than 10, it won't be encoded. Otherwise, it will. 
3. For each company, we save only the data encoded per level (without type information)





My simple algorithm is based on the observation above. For a given company, save the dataEncoded in question for each level 
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


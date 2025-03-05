# Overview

This is a simple (Smart) Url Shorter built mainly to gain more experience with several aspects of Web Development.


* Backend: built by [Ayhem18](https://github.com/ayhem18)
* FrontEnd: TODO
* Deployment /devops: TODO  


# Usage

## Todo


# Functionality

The site offers URL shorterning service to companies. Each user is associated with a company. 
The service revolves around the following entities: 

* Company
* User
* Role
* Subscription
* Url

Each company is registed with a subscription that affects the scale of the service provided. 

Each user has a role in their company that determines the actions they can perform. The company is not user-specific. It shared across the users of the same company. 

Each company is created / registerd by a special user referred to as the `Owner`. Upon the company addition, the service shares special tokens specific to the company. A new user is verified using the tokens in question.

For better performance, the Url shorterning algorithm is not universal to all urls, but incorporates the company information in the decoding and encoding processes.


More details can be found in the [backend/documentation/ReadMe.md](backend/documentation/ReadMe.md) file.



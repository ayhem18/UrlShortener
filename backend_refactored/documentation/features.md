# Overview

This markdown file contains the list of features implemented in the backend and their implementation / explanation

# Company endpoints

## Register a new company

### Request

POST /api/auth/register/company

### Request Body

```json
{
    "companyId": "string": unique,
    "topLevelDomain": "string": unique,
    "subscriptionType": "string": non-null,
    "mailDomain": "string": if non-null, all users will be forced to use this domain (and it can't be a standard mail domain like gmail.com, yahoo.com, etc)
    "owner_email": "string": email of the owner of the company 
}
```
### Implementation

1. checks for uniqueness, id and topLevelDomain must be unique 
2. save a Company object in the database 
3. create a token in the database for the owner of the company  
4. returns the company serialized 

TAKE a look at register a new user endpoints for completness 

## VerifyCompany

POST /api/auth/verify-company

### Request Body    

```json
{
    "companyId": "string",
    "token": "string",
    "email": "string"
}
``` 
### Implementation 

1. the company must exist, and be unverified. 
2. there should be only one token object with the same companyId in the database.
3. the passed token must match the token in the database.
4. the email must match the owner_email in the company object.  
5. update the company object to set the verified field to true. 
6. Link the owner to the token. 
7. return the company serialized. 


# User endpoints

## Register a new user

### Request

POST /api/auth/register/user

### Request Body

```json
{
    "companyId": "string",
    "username": "string": unique,
    "email": "string": unique, 
    "password": "string",
    "role": "string": non-null,
    "roleToken": "string": can be null if the role is the owner. 
}
```

### Implementation 

1. if the company does not exist, return a 400 error 

2. at this point, we know the company exists.
    - if the company is verified, the role cannot be `owner`. The token must exist, valid and cannot be linked to another user. 
    - otherwise, the role must be `owner` and the email must match the owner_email in the company object. If these conditions are not met, raise an error.
        otherwise, send an email to the owner with the token to verify the company.

if the user is not an owner:

    3. create a user object and save it in the database.  

    4. create a userTokenLink object and save it to the database.

    5. return the user serialized.




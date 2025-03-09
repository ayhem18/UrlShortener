# 📄 Get started here

This is the documentation for the different endpoints of the URL Shortener project.

## Authentication

Currently, the only authentication method is basic authentication.


## Company endpoints

| Endpoint | HTTP Method | Purpose | Expected Input | Possible Errors | tested ? |
|----------|-------------|---------|----------------|-----------------|-----------------|
| /api/auth/register/company | POST | Register a new company | JSON body with company id, site, and subscription type | **400**: Company already exists with given id<br>**400**: Company already exists with given site | Yes |
| /api/company/{companyId} | DELETE | Delete a company and all its users | Company ID in path parameter, Owner authentication required | **400**: Company not found<br>**401**: Authentication failed<br>**403**: User not authorized (not owner) | Yes |
| /api/company/{companyId}/details | GET | Retrieve company details | Company ID in path parameter | **400**: Company not found | Yes 



## User endpoints   

| Endpoint | HTTP Method | Purpose | Expected Input | Possible Errors | tested ? |
|----------|-------------|---------|----------------|-----------------|----------|
| /api/auth/register/user | POST | Register a new user for a company | JSON body with:<br>- companyId (8-16 chars)<br>- username (2-16 chars, alphanumeric + '_', starts with letter)<br>- password<br>- role<br>- roleToken | **400**: Company not found<br>**400**: Invalid role<br>**403**: Username already exists<br>**403**: Incorrect role token<br>**403**: Cannot create user before owner<br>**403**: Cannot create multiple owners | Yes |



## Url endpoints 

| Endpoint | HTTP Method | Purpose | Expected Input | Possible Errors | tested ? |
|----------|-------------|---------|----------------|-----------------|----------|
| /api/url/encode/{url} | GET | Encode a URL for shortening | URL as path parameter<br>Requires authenticated user | **400**: Invalid URL format<br>**400**: URL site doesn't match company site<br>**400**: Exceeds subscription level limits<br>**400**: Exceeds maximum URL levels allowed<br>**401**: Authentication required | No |




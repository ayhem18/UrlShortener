# Overview

This markdown file contains the documentation of the token endpoints. 

# Generate Token endpoint

## Request

GET /api/token/generate

## Request Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `role` | string | The role of the token to be generated |

## Implementation

1. only users with the authority CAN_WORK_WITH_TOKENS can access this endpoint: meaning only the owner and the admins
2. the role must be of lower priority that the user's role. In other words, an admin can generate a token for an employee, but an employee can't generate a token for an admin, and an admin can't generate a token for another admin. The token for the owner is generated once: when the company is registered.

3. The maximum number of active tokens cannot exceed the maximum number of users associated with the role. This limit is set by the company's subscription.
4. The token must be unique for a given company (regardless of the role)
5. Only a hash of the token is stored in the database, not the token itself.
6. create the token record in the database. (leave it as inactive)
7. return the token


# Revoke Token endpoint

## Request

GET /api/token/revoke

## Request Parameters

| Parameter | Type | Description | optional |
|-----------|------|-------------|----------|
| `userEmail` | string | The email of the user to revoke the token for | yes |

## Implementation   

1. only users with the authority CAN_WORK_WITH_TOKENS can access this endpoint: meaning only the owner and the admins
2. the role of the request sender must be higher than the role of the user to be revoked. In other words, an admin can revoke a token for an employee, but an employee can't revoke a token for an admin.
3. the token must be active.
4. the token must be associated with the user's email.

5. delete the token from the database.
6. remove the link between the user and the token (the tokenUserLink object) => the user will have to login again with a new regenerated token. 


# Get all tokens endpoint

## Request

GET /api/token/all

## Request Parameters

| Parameter | Type | Description |optional |
|-----------|------|-------------|----------|
| `role` | string | The role of the tokens to be retrieved | no |

## Implementation

1. only users with the authority CAN_WORK_WITH_TOKENS can access this endpoint: meaning only the owner and the admins
2. if the role is provided, it must be of lower priority than the request sender's role. 
3. if the role is not provided return  all the tokens of all users  to role of lower priority than the request sender's role. 
4. return a list of tokens (the serialization would not include the token's hash.)






# paypay-middleware-api
In this Restful API have the underlayer modules of the project and only validates as a bridge without having bussiness requierements.

## Database
For this project, you need to have at least a schema created (DBPAYPAY) on SQL Server to run this project.

## Properties
There are properties environmental properties inside the resources package, on this properies get sure you have the same sql port for the connection and a valid user/password credentials, for practical purposes I put the default port and a generic user/password. Also have the core uri that connects with paypay-core-api

## Maven
This project have maven dependencies, so is necessary to have JDK 1.8 to download ann build the artifacts.

### Maven Commands
The only command that is necessary to execute is: mvn clean install

![logo](../images/header_icon.png "VISIDA")

Files related to the REST API and SQL database are included in visida_backend. The API is developed with AspNet .NET.

## Installation
Open the .sln solution file in Visual Studio 2019 or higher. If the packages do not download and install automatically, they process can be run by opening the Package Manager console and running:
```bash
dotnet restore
```

## SQL Server
The API uses Entity Framework migrations to create the database schema.
Create an SQL database and update the connection string at visida_backend/VISIDA_API/Web.config to point at the SQL server.
```xml
  <connectionStrings>
    <add name="VISIDA_APIContext" connectionString="Data Source=<server-name>; Initial Catalog=<your-database-name>; 
         Integrated Security=True; MultipleActiveResultSets=True;" providerName="System.Data.SqlClient" />
  </connectionStrings>
```
    - "Initial Catalog" is used to reference the database name.
    - The name must remain as VISIDA_APIContext
 
Run the migrations to update the database by opening the Package Manager console and running:
```bash
Update-Database
```

SQL servers other than T-SQL will require the Entity Framework provider to be updated.

## Config
Running the migrations to create the database will insert a default LoginUser with username = admin and password = admin. It is advised to update this to a secure password, and create new users with lower permission levels through the CMS (when operational) for regular usage of the system. 

Use of Google Cloud Services will require setup and configuration of the appropriate APIs through the Google Cloud Platform. Once a .json key file has been created and downloaded for your project, you should update the GOOGLE_APPLICATION_CREDENTIALS environment variable to reference the key file’s folder location.

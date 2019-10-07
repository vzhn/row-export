
```
usage: row-export [options]
 -e,--expression <arg>   expression
 -h,--help               show help and exit
 -l,--login <arg>        jdbc connection login
 -p,--password <arg>     jdbc connection password
 -u,--url <arg>          jdbc connection url
```

Usage example for a [Northwind database](https://github.com/dshifflet/NorthwindOracle_DDL/blob/master/northwind_export.sql):
```
./row-export --url jdbc:oracle:thin:@localhost:1521:XE --login northwind --password northwind -e "ORDERS ORDER_ID:10440"
```

Row selection expression explained:
```
ORDERS ORDER_ID:10440
^      ^        ^ 
|      |        +------- primary key value
|      +---------------- primary key column
+----------------------- table name
```


Output:
```sql
INSERT INTO NORTHWIND.SHIPPERS(SHIPPER_ID,COMPANY_NAME,PHONE) VALUES 
  (2,'United Package','(503) 555-3199');
INSERT INTO NORTHWIND.EMPLOYEES(EMPLOYEE_ID,LASTNAME,FIRSTNAME,TITLE,TITLE_OF_COURTESY,BIRTHDATE,HIREDATE,ADDRESS,CITY,REGION,POSTAL_CODE,COUNTRY,HOME_PHONE,EXTENSION,PHOTO,NOTES) VALUES 
  (2,'Fuller','Andrew','Vice President, Sales','Dr.',to_date('19.02.1952 00:00:00','DD.MM.YYYY HH24:MI:SS'),to_date('14.08.1992 00:00:00','DD.MM.YYYY HH24:MI:SS'),'908 W. Capital Way','Tacoma','WA','98401','USA','(206) 555-9482','3457','andrew  .jpg','Andrew received his BTS commercial and a Ph.D. in international marketing from the University of Dallas.  He is fluent in French and Italian and reads German.  He joined the company as a sales representative, was promoted to sales manager and was then named vice president of sales.  Andrew is a member of the Sales Management Roundtable, the Seattle Chamber of Commerce, and the Pacific Rim Importers Association.');
INSERT INTO NORTHWIND.CUSTOMERS(CUSTOMER_ID,CUSTOMER_CODE,COMPANY_NAME,CONTACT_NAME,CONTACT_TITLE,ADDRESS,CITY,REGION,POSTAL_CODE,COUNTRY,PHONE) VALUES 
  (71,'SAVEA','Save-a-lot Markets','Jose Pavarotti','Sales Representative','187 Suffolk Ln.','Boise','ID','83720','USA','(208) 555-8097');
INSERT INTO NORTHWIND.EMPLOYEES(EMPLOYEE_ID,LASTNAME,FIRSTNAME,TITLE,TITLE_OF_COURTESY,BIRTHDATE,HIREDATE,ADDRESS,CITY,REGION,POSTAL_CODE,COUNTRY,HOME_PHONE,EXTENSION,PHOTO,NOTES,REPORTS_TO) VALUES 
  (4,'Peacock','Margaret','Sales Representative','Mrs.',to_date('19.09.1958 00:00:00','DD.MM.YYYY HH24:MI:SS'),to_date('03.05.1993 00:00:00','DD.MM.YYYY HH24:MI:SS'),'4110 Old Redmond Rd.','Redmond','WA','98052','USA','(206) 555-8122','5176','margaret.jpg','Margaret holds a BA in English literature from Concordia College and an MA from the American Institute of Culinary Arts. She was temporarily assigned to the London office before returning to her permanent post in Seattle.',2);
INSERT INTO NORTHWIND.ORDERS(ORDER_ID,CUSTOMER_ID,EMPLOYEE_ID,ORDER_DATE,REQUIRED_DATE,SHIPPED_DATE,SHIP_VIA,FREIGHT,SHIP_NAME,SHIP_ADDRESS,SHIP_CITY,SHIP_REGION,SHIP_POSTAL_CODE,SHIP_COUNTRY) VALUES 
  (10440,71,4,to_date('10.02.1997 00:00:00','DD.MM.YYYY HH24:MI:SS'),to_date('10.03.1997 00:00:00','DD.MM.YYYY HH24:MI:SS'),to_date('28.02.1997 00:00:00','DD.MM.YYYY HH24:MI:SS'),2,86.53,'Save-a-lot Markets','187 Suffolk Ln.','Boise','ID','83720','USA');
```

When topological sorting for selected rows is not possible you'll see ``cycles detected`` exception message 
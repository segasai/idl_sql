;+
; NAME
;   GET_COLINFO
; AUTHOR:
;	Stephane Beland
;	University of Colorado at Boulder
;   Email: sbeland@colorado.edu
;  
; PURPOSE:
;    Get the names and datatypes for the selected columns defined in the query
;
; CALLING SEQUENCE:
; RESULT = GET_COLINFO (sql_query, /DEBUG, /SIZE, USER=, 
;                    PASS=, URL=, PROTOCOL=, HOST= , PORT=, DB= ] )
;
; DESCRIPTION:
; GET_COLINFO uses the JDBC to connect to the database, execute the query
; and return the results
;
; INPUTS:
;     SQL_QUERY - The SQL query which you want to execute 
;                 "SELECT * FROM tablename" to teh names,datatypes of all columns
;                 "SELECT colname1,colname2, FROM tablename" for 2 columns only
;
; OPTIONAL INPUT KEYWORDS:
;     /SIZE  - The query will return an integer array with 
;              [number_of_dimensions, dimension1, dimesion2, sqlTypeNumber]
;     /DEBUG - The query will be outputted to the terminal 
;     USER - The username for the database connection (if not
;            specified the value is taken from the system variable
;            !_IDL_SQL_USER )
;     PASS - The password for the database connection (if not
;            specified the value is taken from the system variable 
;            !IDL_SQL_PASS )
;     URL - The JDBC url for the connection. For example
;           'jdbc:postgresql://localhost:5432/wsdb'. If specified,
;           there is no need to specify, PROTOCOL, HOST, PORT and DB parameters
;     PROTOCOL - The PROTOCOL part of the JDBC connection. For example 
;           'jdbc:postgresql://' or 'jdbc:sqlserver://'.
;     HOST - The hostname or IP adress where your DB server is
;            running. For example 'localhost'
;     PORT - The port number where your DB is listening (as string) 
;     DB - The database name to which you want to connect
;     DRIVER - The name of the class of the JDBC driver of your
;              database. For example: 'org.postgresql.Driver' or 
;              'oracle.jdbc.driver.OracleDriver'.
;
; OUTPUTS: 
;     RESULT - IDL 2D string array containing the names and datatypes.
;
; EXAMPLES:
; IDL> result = get_colinfo("SELECT * FROM tablename")
; IDL> result = get_colinfo("SELECT colname1,colname2, FROM tablename")
;
; IDL> get_sql_colarray,'select imbin from public.toto where name='+"'line4'",data,/twod,/byte 
; IDL> help,data
; DATA            BYTE      = Array[4, 5]
; IDL> print,data
; 10  11  12  13
; 14  15  16  17
; 18  19  20  21
; 22  23  24  25
; 26  27  28  29
; IDL> res=get_colinfo('SELECT imbin from public.toto where name='+"'line4'")
; IDL> print,res
; imbin _numeric int4 (4,5)
; IDL> res=get_colinfo('SELECT imbin from public.toto where name='+"'line4'",/size)
; IDL> print,res                                                                   
;          2           4           5           4
;
; The corresponding SQL columnType and columnTypeName are:
;
;      -7 	BIT
;      -6 	TINYINT
;      -5 	BIGINT
;      -4 	LONGVARBINARY 
;      -3 	VARBINARY
;      -2 	BINARY
;      -1 	LONGVARCHAR
;      0 	NULL
;      1 	CHAR
;      2 	NUMERIC
;      3 	DECIMAL
;      4 	INTEGER
;      5 	SMALLINT
;      6 	FLOAT
;      7 	REAL
;      8 	DOUBLE
;      12 	VARCHAR
;      91 	DATE
;      92 	TIME
;      93 	TIMESTAMP
;      203	ARRAY
;      1111 OTHER
;
;-

function get_colinfo, query, DB=db, HOST=host, PORT=port, USER=user, PASS=pass,$
                 size=size_flag0, URL=url, PROTOCOL=protocol, DRIVER=driver, DEBUG=debug_flag0
	on_error,1
	if keyword_set(debug_flag0) then message, query,/info
	get_sql_setup, user, pass, driver, url, protocol, host, port, db
	obj = OBJ_NEW('IDLJavaObject$idl_sql','idl_sql')
	
	CATCH, error_status  
	IF (error_status NE 0) THEN BEGIN
		print,!ERROR_STATE.MSG
		catch, /cancel
		obj_destroy, obj 
		message, get_sql_catch()
	ENDIF
	
   if keyword_set(size_flag0) then $ 
      arr= obj->get_colsize(query, url, user, pass, driver)  $
   else $
      arr= obj->get_colinfo(query, url, user, pass, driver) 

	obj_destroy,obj
   return,arr

end

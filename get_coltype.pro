;+
; NAME
;   GET_COLTYPE
; AUTHOR:
;	Stephane Beland
;	University of Colorado at Boulder
;   Email: sbeland@colorado.edu
;  
; PURPOSE:
;    Get the names of the datatypes for each column of a table
;
; CALLING SEQUENCE:
; RESULT = GET_COLTYPE (sql_query, /DEBUG, USER=, 
;                    PASS=, URL=, PROTOCOL=, HOST= , PORT=, DB= ] )
;
; DESCRIPTION:
; GET_COLTYPE uses the JDBC to connect to the database, execute the query
; and return the results
;
; INPUTS:
;     SQL_QUERY - The SQL query which you want to execute 
;
; OPTIONAL INPUT KEYWORDS:
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
;     RESULT - a string array containing the data type for each column 
;            selected in the query.
;
; EXAMPLES:
; IDL> result = get_coltype("SELECT * FROM tablename")
; IDL> result = get_coltype("SELECT colname1,colname2, FROM tablename")
;
;-

function get_coltype, query, DB=db, HOST=host, PORT=port, USER=user, PASS=pass,$
                 URL=url, PROTOCOL=protocol, DRIVER=driver, DEBUG=debug_flag0
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
	
   arr= obj->get_coltype(query, url, user, pass, driver) 

	obj_destroy,obj
   return,arr

end

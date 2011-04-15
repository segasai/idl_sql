;+
; NAME
;   SET_SQL_COLARRAY
; AUTHOR:
;  Stephane Beland
;  University of Colorado at Boulder
;  sbeland@colorado.edu
;
; PURPOSE:
;    Insert up to 4 IDL ARRAYS in one call in corresponding columns.
;    The 4 input arrays have to be of the same datatype and can be either:
;    STRARR, INTARR, LONARR, FLTARR, DBLARR or BYTARR
;
; CALLING SEQUENCE:
; SET_SQL_COLARRAY (sql_query, col1, [col2, col3, col4
;                    /DEBUG, USER=, PASS=, URL=, 
;                    PROTOCOL=, HOST=, PORT=, DB=, /BLOB] )
;
; DESCRIPTION:
; SET_SQL_COLARRAY uses the JDBC to connect to the database and execute the query
;
; INPUTS:
;     SQL_QUERY - The SQL query which you want to execute
;     COL1, COL2, COL3, ... are IDL byte arrays (BYTARR) to insert in the
;            columns defined by SQL_QUERY string
;
; OPTIONAL INPUT KEYWORDS:
;     /DEBUG - The query will be outputted to the terminal 
;     /BLOB  - Input arrays to be stored as BLOB (assumes columns are OID)
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
;     NONE
;
; EXAMPLES:
;
; IDL> set_sql_colarray,'INSERT imgbin INTO public.test VALUES (?)', BINDGEN(100) 
; IDL> val2="'test2'"
; IDL> set_sql_colarray,'UPDATE public.images SET img = ? data = ? WHERE imgname='+val2,dindgen(75), dindgen(20)
; 
; IDL> data=mrdfits(fitsname,0,h0)
; IDL> data=mrdfits(fitsname,1,h1)
; IDL> set_sql_colarray,'UPDATE public.images SET blob1 = ? blob2 = ? WHERE imgname='+val2,BYTE(h0),BYTE(h1), /blob
;
;
; IDL> outdata = bindgen(4,5)+10b
; IDL> help,outdata
; OUTDATA         BYTE      = Array[4, 5]
; IDL> print,outdata
; 10  11  12  13
; 14  15  16  17
; 18  19  20  21
; 22  23  24  25
; 26  27  28  29
; IDL> row = "line4"
; IDL> set_sql_colarray,'UPDATE public.toto set imbin=? where name='+row, outdata
; IDL> get_sql_colarray,'select imbin from public.toto where name='+row,data,/twod,/byte 
; IDL> help,data
; DATA            BYTE      = Array[4, 5]
; IDL> print,data
; 10  11  12  13
; 14  15  16  17
; 18  19  20  21
; 22  23  24  25
; 26  27  28  29
;
;
; NOTES:
;    Will insert up to 4 arrays OF THE SAME TYPE at one time. Different data types require
;    multiple calls. 
;    The underlying Java code will determine the routine to call depending on the datatype
;    of the IDL arrays passed as arguments (BYTARR, STRARR, INTARR, LONARR, FLTARR, DBLARR).
;
;
;-

pro set_sql_colarray, query, col0, col1, col2, col3, $
                 DB=db, HOST=host, PORT=port, USER=user, PASS=pass,$
                 URL=url, PROTOCOL=protocol, DRIVER=driver, BLOB=blob_flag0, $
                 DEBUG=debug_flag0
	on_error,2
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
	
   if keyword_set(blob_flag0) then begin
      if keyword_set(col3) then begin
         obj->put_blob, query, url, user, pass, driver, col0, col1, col2, col3
      endif else if keyword_set(col2) then begin
         obj->put_blob, query, url, user, pass, driver, col0, col1, col2
      endif else if keyword_set(col1) then begin
         obj->put_blob, query, url, user, pass, driver, col0, col1
      endif else if keyword_set(col0) then begin
         obj->put_blob, query, url, user, pass, driver, col0
      endif

   endif else begin

      if keyword_set(col3) then begin
         obj->put_array, query, url, user, pass, driver, col0, col1, col2, col3
      endif else if keyword_set(col2) then begin
         obj->put_array, query, url, user, pass, driver, col0, col1, col2
      endif else if keyword_set(col1) then begin
         obj->put_array, query, url, user, pass, driver, col0, col1
      endif else if keyword_set(col0) then begin
         obj->put_array, query, url, user, pass, driver, col0
      endif
   endelse

	obj_destroy,obj
	catch,/cancel

end

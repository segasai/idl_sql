;+
; NAME
;   GET_SQL_COLARRAY
; AUTHOR:
;  Stephane Beland
;  University of Colorado at Boulder
;  sbeland@colorado.edu
;
;  Based on Sergey Koposov get_sql_col
;
; PURPOSE:
;    Get the results of the SQL queries in IDL as 1D or 2D arrays or blob.
;
; CALLING SEQUENCE:
; arr = GET_SQL_COLARRAY (sql_query, col1, [col2, col3, col4, col5, col6,
;                    col7, col8, col9, col10, col11, col12, col13,
;                    col14, col15, col16, /STRING, /LONG, /BYTE, /BLOB
;                    /TWOD, /DEBUG, USER=, PASS=, URL=, PROTOCOL=, 
;                    HOST= , PORT=, DB= ] )
;
; DESCRIPTION:
;     GET_SQL_COLARRAY uses the JDBC to connect to the database, execute the query
;     and return the results of a array.
;
; INPUTS:
;     SQL_QUERY - The SQL query which you want to execute
;
; OPTIONAL INPUT KEYWORDS:
;     /STRING - By default the output array will have the double
;               precision type, and all the columns in the SQL output
;               will be casted (if possible) to double precision
;               type. If /STRING is specified the columns will be
;               casted to the string type and the output array will have
;               the string type.
;     /BYTE   - By default the output array will have the double
;               precision type, and all the columns in the SQL output
;               will be casted (if possible) to double precision
;               type. If /BYTE is specified the columns will be
;               casted to the byte type and the output array will have
;               the byte type.
;     /LONG   - By default the output array will have the double
;               precision type, and all the columns in the SQL output
;               will be casted (if possible) to double precision
;               type. If /LONG is specified the columns will be
;               casted to the long type and the output array will have
;               the long type.
;     /BLOB   - By default the output array will have the double
;               precision type, and all the columns in the SQL output
;               will be casted (if possible) to double precision
;               type. If /BLOB is specified the columns will be
;               casted to the byte type and the output array will have
;               the byte type.
;     /TWOD   - Extract the 2D data from the database. The 2D arrays
;               is not implemented for STRINGS or BLOBs.
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
;          COL1,COL2,COL3,...COL25 - IDL vectors to contain columns of 
;          data arrays.
;          Up to 16 columns may be read.  The default type is double,
;          but will change if /STRING, /BYTE, /INTEGER, /LONG or /FLOAT
;          option is specified (only one option should be set).
;
; NOTES: 
;          When specifying the returned datatype, the variables are converted
;          to the requested type with the associated Java truncation of precision.
;
; UPDATES:
;  Original by Stephane Beland, University of Colorado at Boulder April 2011
;           Adapted from Sergey Koposov get_sql_col code.
;
; EXAMPLES:
;
; IDL> get_sql_colarray,'select raster from public.myimages where fname='+val1,data,/long
; IDL> get_sql_colarray,'select header0, header1 from public.myimages where fname='+val1, h0, h1,/blob
; IDL> get_sql_colarray,'select image1 from public.myimages where fname='+"'line1'", h0, /twod
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
;-


pro get_sql_colarray, query, col0, col1, col2, col3, col4, col5, col6, col7, col8,$
                 col9, col10, col11, col12, col13, col14, col15,$
                 DB=db, HOST=host, PORT=port, USER=user, PASS=pass,$
                 URL=url, PROTOCOL=protocol, DRIVER=driver,$
                 STRING=string_flag0, BYTE=byte_flag0, LONG=long_flag0, $
                 BLOB=blob_flag0, TWOD=twod_flag0, DEBUG=debug_flag0
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
      arr= obj->get_blob(query, url, user, pass, driver) 
   endif else if keyword_set(string_flag0) then begin 
      ; only handle 1D string arrays
		arr= obj->get_sqlsar(query, url, user, pass, driver)
   endif else if NOT keyword_set(twod_flag0) then begin
      ; extract 1D arrays
      if keyword_set(byte_flag0) then begin
         arr= obj->get_sqlb(query, url, user, pass, driver) 
      endif else if keyword_set(long_flag0) then begin
         arr= obj->get_sqliar(query, url, user, pass, driver) 
      endif else begin
         arr= obj->get_sqlfar(query, url, user, pass, driver) 
      endelse
   endif else begin
      ; extract 2D arrays
      if keyword_set(byte_flag0) then begin
         arr= obj->get_sqlb2d(query, url, user, pass, driver) 
      endif else if keyword_set(long_flag0) then begin
         arr= obj->get_sqli2d(query, url, user, pass, driver) 
      endif else begin
         arr= obj->get_sqlf2d(query, url, user, pass, driver) 
      endelse
   endelse



	obj_destroy,obj
	catch,/cancel
	if not keyword_set(arr) then begin
		message,'No columns returned',/informational
		return
	end
	sz=size(arr)

	if sz[1] ne (N_PARAMS()-1) then begin
		message,'The number of columns in the SQL query is not equal to the number of arguments of the procedure',/infor
	end	

	for i = 0, sz[1]-1 do begin
		cur_i=string(i,format='(%"%d")')
		cur_col="col"+cur_i
      if sz[0] le 1 then begin
         len = 1
         tst=execute(cur_col+'=reform(arr['+cur_i+',*],len,/over)')
      endif else if sz[0] le 2 then begin
         len = sz[2]
         tst=execute(cur_col+'=reform(arr['+cur_i+',*],len,/over)')
      endif else if sz[0] le 3 then begin
         tst=execute(cur_col+'=reform(arr['+cur_i+',*,*],/over)')
      endif else if sz[0] le 4 then begin
         tst=execute(cur_col+'=reform(arr['+cur_i+',*,*,*],/over)')
      endif
	end
end

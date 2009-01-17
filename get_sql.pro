;+
; NAME
;   GET_SQL
; AUTHOR:
;	Sergey Koposov, Max Planck Institute of Astronomy/Institute of
;	Astronomy Cambridge
;   Email: math@sai.msu.ru WEB: http://lnfm1.sai.msu.ru/~math
;   UPDATED VERSIONs can be found that WEB PAGE:
;       http://code.google.com/p/idl-sql/
;  Copyright (C) 2005-2009 Sergey Koposov
;
; PURPOSE:
;    Get the results of the SQL queries in IDL
;
; CALLING SEQUENCE:
; arr = GET_SQL (sql_query, [ /STRING, /LONG, USER= , PASS=, URL=,
;                             PROTOCOL=, HOST= , PORT=, DB=, ] )
;
; DESCRIPTION:
; GET_SQL uses the JDBC to connect to the database, execute the query
; and return the results as a 2d array.
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
;     /LONG - By default the output array will have the double
;               precision type, and all the columns in the SQL output
;               will be casted (if possible) to double precision
;               type. If /LONG is specified the columns will be
;               casted to the long type and the output array will have
;               the long type.
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
;         Arr - The 2d array with the results. The default type will
;               be double but will change if /STRING or /LONG will be
;               specified.
;
; CONNECTION PARAMETERS:
; To connect to the database a lot of information is usually
; needed. While it is possible to always specify the parameters of the
; connection as parameters of your GET_SQL() invocation, I think
; that's not always reasonable. And it is better to use the
; preset parameters of the connection. 
; For that purpose the GET_SQL() command check the values of the set
; of special system variables:
; !_IDL_SQL_DRIVER
; !_IDL_SQL_USER
; !_IDL_SQL_PASS
; !_IDL_SQL_PROTOCOL
; !_IDL_SQL_HOST
; !_IDL_SQL_PORT
; !_IDL_SQL_DB
; ;_IDL_SQL_URL
; having the same meaning as the corresponding parameters of GET_SQL().
;
; So, if your IDL_STARTUP script for examle would contain something
; like that:
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; defsysv, '!_IDL_SQL_DRIVER','org.postgresql.Driver' 
; defsysv, '!_IDL_SQL_USER','math'
; defsysv, '!_IDL_SQL_PASS',''
; defsysv, '!_IDL_SQL_PROTOCOL','jdbc:postgresql://'
; defsysv, '!_IDL_SQL_HOST','localhost'
; defsysv, '!_IDL_SQL_PORT','5432'
; defsysv, '!_IDL_SQL_DB','wsdb'
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; then there is no need to specify any of the parameters of the
; connection when you call GET_SQL(). Example:

; IDL> a=get_sql('select ra,dec from rc3')
; But if you want to connect to other database on the same machine,
; you just specify the DB option and it will have higher priority than
; the system variable !IDL_SQL_DB
; IDL> a=get_sql('select ra,dec from rc3', DB='sdss')
;
; Probably, somebody would like to not deal with all DB/HOST/PORT
; options and would like to specify directly the JDBC URL of the
; connection. In that case, to setup the default connection you only
; need to define one system variable !_IDL_SQL_URL (and also
; !_IDL_SQL_USER and !_IDL_SQL_PASS)
; defsysv, '!_IDL_SQL_URL','jdbc:postgresql://localhost:5432/wsdb'
;
; or you can specify the URL as the option of GET_SQL() (in that case
; it will have higher priority than the value of the system variable
; !_IDL_SQL_URL. Example:
;
; IDL> a=get_sql('select ra,dec from rc3', URL='jdbc:postgresql://localhost:5432/wsdb')
;
;    This file is part of IDL_SQL
;
;    IDL_SQL is free software; you can redistribute it and/or modify
;    it under the terms of the GNU General Public License as published by
;    the Free Software Foundation; either version 2 of the License, or
;    (at your option) any later version.
;
;    IDL_SQL is distributed in the hope that it will be useful,
;    but WITHOUT ANY WARRANTY; without even the implied warranty of
;    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;    GNU General Public License for more details.
;
;    You should have received a copy of the GNU General Public License
;    along with IDL_SQL; if not, write to the Free Software
;    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
;-

function get_sql, query, DB=db, HOST=host, PORT=port, USER=user, PASS=pass,$
                  URL=url, PROTOCOL=protocol, DRIVER=driver,  $
                  STRING=string_flag0, LONG=long_flag0
	on_error,1
	type=2 ; means double	
	if keyword_set(string_flag0) then type=0
	if keyword_set(long_flag0) then type=1

	get_sql_setup, user, pass, driver, url, protocol, host, port, db
	obj = OBJ_NEW('IDLJavaObject$idl_sql','idl_sql')

	CATCH, error_status  
	IF (error_status NE 0) THEN BEGIN
		catch, /cancel
		obj_destroy, obj 
		message, get_sql_catch()
	ENDIF  
	if type eq 0 then $
		arr= obj->get_sqls(query, url, user, pass, driver) $
	else $
	if type eq 1 then $
		arr= obj->get_sqli(query, url, user, pass, driver) $
	else $
	if type eq 2 then $
		arr= obj->get_sqlf(query, url, user, pass, driver) 
	obj_destroy, obj
	return,arr
end
 


; You should have the right enviroment setup
;  export IDLJAVAB_LIB_LOCATION=~/j2sdk1.4.2_10/jre/lib/i386/client/
; (the path to libjvm.so)
;
;    to not crash on big result sets place in your ~/.idljavabrc
;     the following string 
;    JVM Option1 = -Xmx256m
;
; Also I recommend to put the followingoptions in the  ~/.idljavabrc
; JVM Classpath = $CLASSPATH:path_to_the current_package
;

; Copyright (C) 2005, Sergey Koposov
; This software is provided as is without any warranty whatsoever.
; Permission to use, copy, modify, and distribute modified or
; unmodified copies is granted, provided this copyright and disclaimer
; are included unchanged.


pro exec_sql, query, DB=db, HOST=host, PORT=port, USER=user, PASS=pass,$
							URL=url, PROTOCOL=protocol
	on_error,2
	get_sql_setup, user, pass, driver, url, protocol, host, port, db
	if n_elements(query) ne 1 then message, 'The query should be a scalar string'
	obj = OBJ_NEW('IDLJavaObject$idl_sql','idl_sql')

	CATCH, error_status  
	IF (error_status NE 0) THEN BEGIN
		catch, /cancel
		obj_destroy, obj 
		message, get_sql_catch()
	ENDIF  

	obj->exec_sql,query,url,user,pass,driver
	obj_destroy, obj
end
 


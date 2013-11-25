/*
       Copyright (C) 2005-2007 Sergey Koposov
   
    Author: Sergey Koposov, Max Planck Institute for Astronomy/Institute of Astronomy Cambridge
    Email: math@sai.msu.ru 
    http://lnfm1.sai.msu.ru/~math

    This file is part of IDL_SQL

    IDL_SQL is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Q3C is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with IDL_SQL; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

import java.sql.*;
import java.net.*;

import java.util.*;

public class idl_sql
{
	public idl_sql()
	{
		;
	}
	public double[][] get_sqlf(String in,String url, String user, String pass, String driver) throws Exception 
	{
		Connection conn = null; 
		ResultSet rs = null;
		Statement stmt = null;
		try
			{    
				Class.forName(driver); //"org.postgresql.Driver"
			}
		catch (ClassNotFoundException e)
			{
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try 
			{
				conn=DriverManager.getConnection(url, user, pass);
				stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery(in);
				ResultSetMetaData rsmd = rs.getMetaData ();
				int numberOfColumns = rsmd.getColumnCount ();
				rs.last();
				int numberOfRows = rs.getRow ();
				rs.beforeFirst();
				if (numberOfRows==0) return null;
				
				double arr0[][]=new double[numberOfColumns][numberOfRows];
				int j=0;
				while (rs.next())
					{
						for (int i = 1; i <= numberOfColumns; i++) 
							{
								arr0[i-1][j] = rs.getDouble(i);
							}
						j++;
					}
				return arr0;
			}
		catch (SQLException e)
			{
				//System.out.println("Caught the SQL exception:\n"+e);
				throw e;      
			}
		catch (Exception e)
			{
				//System.out.println("Caught the Exception:\n"+e);
				throw e;      
			}
		finally
			{
				try
				{
					rs.close();
				}
				catch(Exception e){}
				try
				{
					stmt.close();
				}
				catch(Exception e){}
				try
				{
					conn.close();
				}
				catch(Exception e){}
			}
	}
	
	
	public long[][] get_sqli(String in,String url, String user, String pass, String driver) throws Exception 
	{
		Connection conn = null;   
		ResultSet rs = null;
		Statement stmt = null;

		try
			{    
				Class.forName(driver); //"org.postgresql.Driver"
			}
		catch (ClassNotFoundException e)
			{
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try 
			{
				conn=DriverManager.getConnection(url, user, pass);
				stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery(in);
				ResultSetMetaData rsmd = rs.getMetaData ();
				int numberOfColumns = rsmd.getColumnCount ();
				rs.last();
				int numberOfRows = rs.getRow ();
				rs.beforeFirst();
				if (numberOfRows==0) return null;
				
				long arr0[][]=new long[numberOfColumns][numberOfRows];
				int j=0;
				while (rs.next())
					{
						for (int i = 1; i <= numberOfColumns; i++) 
							{
								arr0[i-1][j] = rs.getLong(i);
							}
						j++;
					}
				return arr0;
			}
		catch (SQLException e)
			{
				//System.out.println("Caught the SQL exception:\n"+e);
				throw e;      
			}
		catch (Exception e)
			{
				//System.out.println("Caught the Exception:\n"+e);
				throw e;      
			}
		finally
			{
				try
				{
					rs.close();
				}
				catch(Exception e){}
				try
				{
					stmt.close();
				}
				catch(Exception e){}
				try
				{
					conn.close();
				}
				catch(Exception e){}
			}
		
	}
	
	
	public String[][] get_sqls(String in,String url, String user, String pass, String driver) throws Exception 
	{
		Connection conn = null;
		ResultSet rs = null;  
		Statement stmt = null;
		try
			{    
				Class.forName(driver);//"org.postgresql.Driver");
			}
		catch (ClassNotFoundException e)
			{
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;      
			}
		try
			{    
				conn=DriverManager.getConnection(url,user,pass);
				stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery(in);
				ResultSetMetaData rsmd = rs.getMetaData ();
				int numberOfColumns = rsmd.getColumnCount ();
				rs.last();
				int numberOfRows = rs.getRow ();
				rs.beforeFirst();
				if (numberOfRows==0) return null;
				String arr0[][]=new String[numberOfColumns][numberOfRows];
				int j=0;
				while (rs.next())
					{
						for (int i = 1; i <= numberOfColumns; i++) 
							{
								arr0[i-1][j] = rs.getString(i);
							}
						j++;
					}
				return arr0;
			}
		catch (SQLException e)
			{
				//System.out.println("Caught the SQL exception:\n"+e);
				throw e;    
			}
		catch (Exception e)
			{
				//System.out.println("Caught the SQL exception:\n"+e);
				throw e;    
			}
		finally
			{
				try
				{
					rs.close();
				}
				catch(Exception e){}
				try
				{
					stmt.close();
				}
				catch(Exception e){}
				try
				{
					conn.close();
				}
				catch(Exception e){}
			}

	}
	
	public void exec_sql(String in,String url, String user, String pass, String driver) throws Exception 
	{
		Connection conn = null;                                          
		Statement stmt = null;

		try
			{    
				Class.forName(driver);
			}
		catch (ClassNotFoundException e)
			{
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try 
			{
				conn=DriverManager.getConnection(url,user,pass);
				stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				boolean result = stmt.execute(in);
			}
		catch (SQLException e)
			{
				//System.out.println("Caught the SQL exception:\n"+e);
				throw e;      
			}
		catch (Exception e)
			{
				//System.out.println("Caught the SQL exception:\n"+e);
				throw e;      
			}
		finally
			{
				try
				{
					stmt.close();
				}
				catch(Exception e){}
				try
				{
					conn.close();
				}
				catch(Exception e){}
			}
	}
	
}

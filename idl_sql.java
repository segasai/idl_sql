/*
       Copyright (C) 2005-2011 Sergey Koposov
       Copyright (C) 2011      Stephane Beland
   
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
 *
 * MODIFIED:  Stephane Beland, University of Colorado at Boulder, April 2011
 *    Added:  get_blob, get_sqlb, get_sqlb2d, get_sqlfar, get_sqlf2d, 
 *            get_sqliar, get_sqli2d, get_sqlsar, get_coltype, 
 *            get_colinfo, get_colsize, put_blob, put_array
 *         
*/

import java.sql.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import java.math.BigDecimal;
import org.postgresql.largeobject.*;


public class idl_sql
{
	public enum IdlSqlType {
		ISInt, ISDouble, ISString, ISByte;
		};

	public idl_sql()
	{
		;
	}
	public <T> Object get_sql(String in,String url, String user, String pass, String driver, T x) throws Exception 
	{
		Connection conn = null; 
		try
		{
			Class.forName(driver);
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
			throw e;
		}
		try {
			conn = DriverManager.getConnection(url, user, pass);
			Statement stmt = conn.createStatement(
							ResultSet.TYPE_SCROLL_SENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery(in);
			ResultSetMetaData rsmd = rs.getMetaData ();
			int numberOfColumns = rsmd.getColumnCount ();
			rs.last();
			int numberOfRows = rs.getRow ();
			rs.beforeFirst();
			if (numberOfRows==0) return null;
			int j=0;
			//cludge implementation because of limits of Java generics
			if (x instanceof Double)
			{
				double arr0[][]=new double[numberOfColumns][numberOfRows];
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
			else if (x instanceof String)
			{
				String arr0[][]=new String[numberOfColumns][numberOfRows];
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
			else  if (x instanceof Byte)
			{
				byte arr0[][]=new byte[numberOfColumns][numberOfRows];
				while (rs.next())
				{
					for (int i = 1; i <= numberOfColumns; i++) 
					{
						arr0[i-1][j] = rs.getByte(i);
					}
					j++;
				}
				return arr0;
			}
			else if (x instanceof Integer)
			{
				int arr0[][]=new int[numberOfColumns][numberOfRows];
				while (rs.next())
				{
					for (int i = 1; i <= numberOfColumns; i++) 
					{
						arr0[i-1][j] = rs.getInt(i);
					}
					j++;
				}
				return arr0;
			}
			return null;// unreachable
		}
		catch (SQLException e) { throw e;      }
		catch (Exception e) { throw e;      }
		finally { try { conn.close(); } catch(Exception e){} }

	}


	/* Execute the query and return the 2D array of doubles */
	public double[][] get_sqlf(String in,String url, String user, String pass, String driver) throws Exception
	{
		return (double [][])get_sql(in, url, user, pass, driver, new Double((double)1.));
	}
	/* Execute the query and return the 2D array of bytes */
	public byte[][] get_sqlb(String in,String url, String user, String pass, String driver) throws Exception
	{
		return (byte [][])get_sql(in, url, user, pass, driver, new Byte((byte)1.));
	}
	/* Execute the query and return the 2D array of ints */
	public int[][] get_sqli(String in,String url, String user, String pass, String driver) throws Exception
	{
		return (int [][])get_sql(in, url, user, pass, driver, new Integer((int)1.));
	}
	/* Execute the query and return the 2D array of Strings */
	public String[][] get_sqls(String in,String url, String user, String pass, String driver) throws Exception
	{
		return (String [][])get_sql(in, url, user, pass, driver, new String(""));
	}


	/* Execute the query and return the 3D array of doubles */
   public <T>Object get_sqlar(String in, String url, String user,
   											String pass, String driver, T x)
					throws Exception 
	{
		Connection conn = null; 
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e; }
		try
		{
			conn = DriverManager.getConnection(url, user, pass);
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery(in);
			ResultSetMetaData rsmd = rs.getMetaData ();
			int numberOfColumns = rsmd.getColumnCount ();
			rs.last();
			int numberOfRows = rs.getRow ();
			rs.beforeFirst();
			if (numberOfRows == 0) return null;			
			Object arr1 = null;
			IdlSqlType curType = IdlSqlType.ISDouble;
			
			if (x instanceof Double)
			{
				arr1 = new double[numberOfColumns][numberOfRows][];
				curType = IdlSqlType.ISDouble;
			}
			else if (x instanceof Integer)
			{
				arr1 = new int[numberOfColumns][numberOfRows][];
				curType = IdlSqlType.ISInt;
			}
			else if (x instanceof String)
			{
				arr1 = new String[numberOfColumns][numberOfRows][];
				curType = IdlSqlType.ISString;
			}
			else if (x instanceof Byte)
			{
				arr1 = new Byte[numberOfColumns][numberOfRows][];
				curType = IdlSqlType.ISByte;
			}

			int j=0;
			int arrayLength = 0;

			ArrayList<String> colTypeNames = new ArrayList<String>();
			while (rs.next())
			{

				/* Type checking */
				if (j==0)
				{
					for (int i = 1; i <= numberOfColumns; i++)
					{
						if (Types.ARRAY != rsmd.getColumnType(i))
						{
							System.out.println("Column: "+rsmd.getColumnName(i)+" is NOT an ARRAY");
							/* TODO I should throw the exception instead ...*/ 
							double [][][] arr0 ={{{-1.0}}};
							return arr0;
						}
						colTypeNames.add(rsmd.getColumnTypeName(i));
					}				
				}
				
				for (int i = 1; i <= numberOfColumns; i++)
				{
					Array tmp0 = rs.getArray(i);
					if (tmp0 == null)
					{
						throw new Exception("Nulls aren't allowed");
					}
					String columnTypeName = colTypeNames.get(i-1);

					Object arr0[] = null;
					if (columnTypeName.contentEquals("_float8"))
					{
						arr0 = (Double [])tmp0.getArray(); 
					} 
					else if (columnTypeName.contentEquals("_float4"))
					{
						arr0 = (Float [])tmp0.getArray(); 
					} 
					else if (columnTypeName.contentEquals("_numeric"))
					{
						arr0 = (BigDecimal [])tmp0.getArray();
					}
					else if (columnTypeName.contentEquals("_int4"))
					{
						arr0 = (Integer [])tmp0.getArray();
					}
					else if (columnTypeName.contentEquals("_int2"))
					{
						arr0 = (Integer [])tmp0.getArray();
					}
					else if (columnTypeName.contentEquals("_text"))
					{
						arr0 = (String [])tmp0.getArray();
					}
					else
					{
						arr0 = (Double [])tmp0.getArray();
					}	

					if (curType==IdlSqlType.ISString)
					{
						String xarr0[] = (String [])(arr0);
						String [][][] arr2 = (String[][][])(arr1);
						arr2[i-1][j] = new String[xarr0.length];
						for (int k=0; k<xarr0.length; k++)
						{
							arr2[i-1][j][k] = xarr0[k];
						}
					}
					else
					{
						Number xarr0[] = (Number [])(arr0);
						if (curType == IdlSqlType.ISDouble)
						{
							double [][][] arr2 = (double[][][])(arr1);
							arr2[i-1][j] = new double[xarr0.length];
							
							for (int k=0; k<xarr0.length; k++)
							{
								arr2[i-1][j][k] = xarr0[k].doubleValue();
							}
						}
						else if (curType == IdlSqlType.ISInt)
						{
							int [][][] arr2 = (int[][][])(arr1);
							arr2[i-1][j] = new int[xarr0.length];
							for (int k=0; k<xarr0.length; k++)
							{
								arr2[i-1][j][k] = xarr0[k].intValue();
							}
						}
						else if (curType == IdlSqlType.ISByte)
						{
							byte [][][] arr2 = (byte[][][])(arr1);
							arr2[i-1][j] = new byte[xarr0.length];
							for (int k=0; k<xarr0.length; k++)
							{
								arr2[i-1][j][k] = xarr0[k].byteValue();
							}
						}
					}
				}
				j++;
			}
			return arr1;
		}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally
		{
			try { conn.close(); }
			catch(Exception e){}
		}
	}
	

	/* Execute the query and return the 3D array of doubles */
	public double[][][] get_sqlfar(String in, String url, String user,
   											String pass, String driver)
					throws Exception
	{
		return (double[][][]) get_sqlar(in, url, user, pass, driver, new Double(1.));	
	}

	/* Execute the query and return the 3D array of doubles */
	public String[][][] get_sqlsar(String in, String url, String user,
   											String pass, String driver)
					throws Exception
	{
		return (String[][][]) get_sqlar(in, url, user, pass, driver, new String(""));	
	}
	public int[][][] get_sqliar(String in,String url, String user, String pass, String driver) throws Exception 
	{
		return (int[][][]) get_sqlar(in, url, user, pass, driver, new Integer(1));	
	}


   public double[][][][] get_sqlf2d(String in,String url, String user, String pass, String driver) throws Exception 
	{
		Connection conn = null; 
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e; }
		try
		{
			conn=DriverManager.getConnection(url, user, pass);
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery(in);
			ResultSetMetaData rsmd = rs.getMetaData ();
			int numberOfColumns = rsmd.getColumnCount ();
			rs.last();
			int numberOfRows = rs.getRow ();
			rs.beforeFirst();
			if (numberOfRows==0) return null;
				
            double arr1[][][][] = new double[numberOfColumns][numberOfRows][][];
			int j=0;
            int arrayLength = 0;

			while (rs.next())
			{
				for (int i = 1; i <= numberOfColumns; i++)
				{
					Array tmp0 = rs.getArray(i);
					if (tmp0 == null) continue;
					String columnTypeName = rsmd.getColumnTypeName(i);
					//System.out.println("columnTypeName=*"+columnTypeName+"*");
					//ResultSet arrRS = tmp0.getResultSet();
					//ResultSetMetaData arrRSMD = arrRS.getMetaData();
					//System.out.println("MetaData ColumnTypeName = "+arrRSMD.getColumnTypeName(2));
					if (Types.ARRAY != rsmd.getColumnType(i))
					{
						System.out.println("Column: "+rsmd.getColumnName(i)+" is NOT an ARRAY");
						/* TODO 
						Throw the exception instead 
						*/
						return null;
					}
					
					ResultSet arrRS = tmp0.getResultSet();
					ResultSetMetaData arrRSMD = arrRS.getMetaData();
					if (Types.ARRAY != arrRSMD.getColumnType(2)) continue;

					Number arr0[][] = null;
					if (columnTypeName.contentEquals("_float8"))
					{
						arr0  = (Double [][])tmp0.getArray();
					} 
					else if (columnTypeName.contentEquals("_float4"))
					{
						arr0 = (Float [][])tmp0.getArray();
                    }
                    else if (columnTypeName.contentEquals("_numeric"))
                    {
                    	arr0 = (BigDecimal [][])tmp0.getArray();
					}
					else if (columnTypeName.contentEquals("_int4"))
					{
						arr0 = (Integer [][])tmp0.getArray();
					}
					else if (columnTypeName.contentEquals("_int2"))
					{
						arr0 = (Integer [][])tmp0.getArray();
					}
					else
					{
						arr0 = (Double [][])tmp0.getArray();
					}

					arr1[i-1][j] = new double[arr0.length][arr0[0].length];
					
					for (int k=0; k<arr0.length; k++)
					{
						for (int p=0; p<arr0[0].length; p++)
						{
							arr1[i-1][j][k][p] = arr0[k][p].doubleValue();
						}
					}
				}
				j++;
			}
			return arr1;
		}
		catch (SQLException e) { throw e;}
		catch (Exception e) { throw e;}
		finally
		{
			try { conn.close(); } catch(Exception e){}
		}
	}
	

   public int[][][][] get_sqli2d(String in,String url, String user, String pass, String driver) throws Exception 
	{
		Connection conn = null; 
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e; }
		try {
				conn=DriverManager.getConnection(url, user, pass);
				Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = stmt.executeQuery(in);
				ResultSetMetaData rsmd = rs.getMetaData ();
				int numberOfColumns = rsmd.getColumnCount ();
				rs.last();
				int numberOfRows = rs.getRow ();
				rs.beforeFirst();
				if (numberOfRows==0) return null;
				
            int arr1[][][][] = new int[numberOfColumns][numberOfRows][][];
				int j=0;
            int arrayLength = 0;

				while (rs.next()) {
               for (int i = 1; i <= numberOfColumns; i++) {
                  Array tmp0 = rs.getArray(i);
                  if (tmp0 == null) continue;
                  String columnTypeName = rsmd.getColumnTypeName(i);
                  //System.out.println("columnTypeName=*"+columnTypeName+"*");
                  //ResultSet arrRS = tmp0.getResultSet();
                  //ResultSetMetaData arrRSMD = arrRS.getMetaData();
                  //System.out.println("MetaData ColumnTypeName = "+arrRSMD.getColumnTypeName(2));
                  if (Types.ARRAY != rsmd.getColumnType(i)) {
                     System.out.println("Column: "+rsmd.getColumnName(i)+" is NOT an ARRAY");
                     return null;
                  }

                  if (columnTypeName.contentEquals("_float8")) {
                     ResultSet arrRS = tmp0.getResultSet();
                     ResultSetMetaData arrRSMD = arrRS.getMetaData();
                     if (Types.ARRAY != arrRSMD.getColumnType(2)) continue;
                     Double arr0[][] = (Double [][])tmp0.getArray(); 
                     arr1[i-1][j] = new int[arr0.length][arr0[0].length];
                     for (int k=0; k<arr0.length; k++) {
                        for (int p=0; p<arr0[0].length; p++) {
                           arr1[i-1][j][k][p] = arr0[k][p].intValue();
                        }
                     }
                  } else if (columnTypeName.contentEquals("_float4")) {
                     ResultSet arrRS = tmp0.getResultSet();
                     ResultSetMetaData arrRSMD = arrRS.getMetaData();
                     if (Types.ARRAY != arrRSMD.getColumnType(2)) continue;
                     Float arr0[][] = (Float [][])tmp0.getArray(); 
                     arr1[i-1][j] = new int[arr0.length][arr0[0].length];
                     for (int k=0; k<arr0.length; k++) {
                        for (int p=0; p<arr0[0].length; p++) {
                           arr1[i-1][j][k][p] = arr0[k][p].intValue();
                        }
                     }
                  } else if (columnTypeName.contentEquals("_numeric")) {
                     ResultSet arrRS = tmp0.getResultSet();
                     ResultSetMetaData arrRSMD = arrRS.getMetaData();
                     if (Types.ARRAY != arrRSMD.getColumnType(2)) continue;
                     BigDecimal arr0[][] = (BigDecimal [][])tmp0.getArray(); 
                     arr1[i-1][j] = new int[arr0.length][arr0[0].length];
                     for (int k=0; k<arr0.length; k++) {
                        for (int p=0; p<arr0[0].length; p++) {
                           arr1[i-1][j][k][p] = arr0[k][p].intValue();
                        }
                     }
                  } else if (columnTypeName.contentEquals("_int4")) {
                     ResultSet arrRS = tmp0.getResultSet();
                     ResultSetMetaData arrRSMD = arrRS.getMetaData();
                     if (Types.ARRAY != arrRSMD.getColumnType(2)) continue;
                     Integer arr0[][] = (Integer [][])tmp0.getArray(); 
                     arr1[i-1][j] = new int[arr0.length][arr0[0].length];
                     for (int k=0; k<arr0.length; k++) {
                        for (int p=0; p<arr0[0].length; p++) {
                           arr1[i-1][j][k][p] = arr0[k][p].intValue();
                        }
                     }
                  } else if (columnTypeName.contentEquals("_int2")) {
                     ResultSet arrRS = tmp0.getResultSet();
                     ResultSetMetaData arrRSMD = arrRS.getMetaData();
                     if (Types.ARRAY != arrRSMD.getColumnType(2)) continue;
                     Integer arr0[][] = (Integer [][])tmp0.getArray(); 
                     arr1[i-1][j] = new int[arr0.length][arr0[0].length];
                     for (int k=0; k<arr0.length; k++) {
                        for (int p=0; p<arr0[0].length; p++) {
                           arr1[i-1][j][k][p] = arr0[k][p].intValue();
                        }
                     }
                  } else {
                     ResultSet arrRS = tmp0.getResultSet();
                     ResultSetMetaData arrRSMD = arrRS.getMetaData();
                     if (Types.ARRAY != arrRSMD.getColumnType(2)) continue;
                     Double arr0[][] = (Double [][])tmp0.getArray(); 
                     arr1[i-1][j] = new int[arr0.length][arr0[0].length];
                     for (int k=0; k<arr0.length; k++) {
                        for (int p=0; p<arr0[0].length; p++) {
                           arr1[i-1][j][k][p] = arr0[k][p].intValue();
                        }
                     }
                  }
               }
               j++;
            }
				return arr1; }
		catch (SQLException e) { throw e;      }
		catch (Exception e) { throw e;      }
		finally { try { conn.close(); } catch(Exception e){} }
	}
	
   public byte[][][][] get_sqlb2d(String in,String url, String user, String pass, String driver) throws Exception 
	{
		Connection conn = null; 
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e; }
		try {
				conn=DriverManager.getConnection(url, user, pass);
				Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = stmt.executeQuery(in);
				ResultSetMetaData rsmd = rs.getMetaData ();
				int numberOfColumns = rsmd.getColumnCount ();
				rs.last();
				int numberOfRows = rs.getRow ();
				rs.beforeFirst();
				if (numberOfRows==0) return null;
				
            byte arr1[][][][] = new byte[numberOfColumns][numberOfRows][][];
				int j=0;
            int arrayLength = 0;

				while (rs.next()) {
               for (int i = 1; i <= numberOfColumns; i++) {
                  Array tmp0 = rs.getArray(i);
                  if (tmp0 == null) continue;
                  String columnTypeName = rsmd.getColumnTypeName(i);
                  //System.out.println("columnTypeName=*"+columnTypeName+"*");
                  //ResultSet arrRS = tmp0.getResultSet();
                  //ResultSetMetaData arrRSMD = arrRS.getMetaData();
                  //System.out.println("MetaData ColumnTypeName = "+arrRSMD.getColumnTypeName(2));
                  if (Types.ARRAY != rsmd.getColumnType(i)) {
                     System.out.println("Column: "+rsmd.getColumnName(i)+" is NOT an ARRAY");
                     return null;
                  }

                  if (columnTypeName.contentEquals("_float8")) {
                     ResultSet arrRS = tmp0.getResultSet();
                     ResultSetMetaData arrRSMD = arrRS.getMetaData();
                     if (Types.ARRAY != arrRSMD.getColumnType(2)) continue;
                     Double arr0[][] = (Double [][])tmp0.getArray(); 
                     arr1[i-1][j] = new byte[arr0.length][arr0[0].length];
                     for (int k=0; k<arr0.length; k++) {
                        for (int p=0; p<arr0[0].length; p++) {
                           arr1[i-1][j][k][p] = arr0[k][p].byteValue();
                        }
                     }
                  } else if (columnTypeName.contentEquals("_float4")) {
                     ResultSet arrRS = tmp0.getResultSet();
                     ResultSetMetaData arrRSMD = arrRS.getMetaData();
                     if (Types.ARRAY != arrRSMD.getColumnType(2)) continue;
                     Float arr0[][] = (Float [][])tmp0.getArray(); 
                     arr1[i-1][j] = new byte[arr0.length][arr0[0].length];
                     for (int k=0; k<arr0.length; k++) {
                        for (int p=0; p<arr0[0].length; p++) {
                           arr1[i-1][j][k][p] = arr0[k][p].byteValue();
                        }
                     }
                  } else if (columnTypeName.contentEquals("_numeric")) {
                     ResultSet arrRS = tmp0.getResultSet();
                     ResultSetMetaData arrRSMD = arrRS.getMetaData();
                     if (Types.ARRAY != arrRSMD.getColumnType(2)) continue;
                     BigDecimal arr0[][] = (BigDecimal [][])tmp0.getArray(); 
                     arr1[i-1][j] = new byte[arr0.length][arr0[0].length];
                     for (int k=0; k<arr0.length; k++) {
                        for (int p=0; p<arr0[0].length; p++) {
                           arr1[i-1][j][k][p] = arr0[k][p].byteValue();
                        }
                     }
                  } else if (columnTypeName.contentEquals("_int4")) {
                     ResultSet arrRS = tmp0.getResultSet();
                     ResultSetMetaData arrRSMD = arrRS.getMetaData();
                     if (Types.ARRAY != arrRSMD.getColumnType(2)) continue;
                     Integer arr0[][] = (Integer [][])tmp0.getArray(); 
                     arr1[i-1][j] = new byte[arr0.length][arr0[0].length];
                     for (int k=0; k<arr0.length; k++) {
                        for (int p=0; p<arr0[0].length; p++) {
                           arr1[i-1][j][k][p] = arr0[k][p].byteValue();
                        }
                     }
                  } else if (columnTypeName.contentEquals("_int2")) {
                     ResultSet arrRS = tmp0.getResultSet();
                     ResultSetMetaData arrRSMD = arrRS.getMetaData();
                     if (Types.ARRAY != arrRSMD.getColumnType(2)) continue;
                     Integer arr0[][] = (Integer [][])tmp0.getArray(); 
                     arr1[i-1][j] = new byte[arr0.length][arr0[0].length];
                     for (int k=0; k<arr0.length; k++) {
                        for (int p=0; p<arr0[0].length; p++) {
                           arr1[i-1][j][k][p] = arr0[k][p].byteValue();
                        }
                     }
                  } else {
                     ResultSet arrRS = tmp0.getResultSet();
                     ResultSetMetaData arrRSMD = arrRS.getMetaData();
                     if (Types.ARRAY != arrRSMD.getColumnType(2)) continue;
                     Double arr0[][] = (Double [][])tmp0.getArray(); 
                     arr1[i-1][j] = new byte[arr0.length][arr0[0].length];
                     for (int k=0; k<arr0.length; k++) {
                        for (int p=0; p<arr0[0].length; p++) {
                           arr1[i-1][j][k][p] = arr0[k][p].byteValue();
                        }
                     }
                  }
               }
               j++;
            }
				return arr1; }
		catch (SQLException e) { throw e;      }
		catch (Exception e) { throw e;      }
		finally { try { conn.close(); } catch(Exception e){} }
	}
	


   public byte[][][] get_blob(String in, String url, String user, String pass, String driver) throws Exception 
	{
      // this is the POSTGRES implementation of BLOB.  Other database may implement this differently in JDBC
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e; }
		try {
				conn=DriverManager.getConnection(url,user,pass);
            conn.setAutoCommit(false);
            LargeObjectManager lobj = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
				Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = stmt.executeQuery(in);
            ResultSetMetaData rsmd = rs.getMetaData ();
				int numberOfColumns = rsmd.getColumnCount ();
				rs.last();
				int numberOfRows = rs.getRow ();
				rs.beforeFirst();
				if (numberOfRows==0) return null;
				
				byte arr0[][][]=new byte[numberOfColumns][numberOfRows][];
				int j=0;
            while (rs.next()) 
            {
               for (int i = 1; i <= numberOfColumns; i++)  
               {
                  long oid = rs.getLong(i);
                  LargeObject obj = lobj.open(oid, LargeObjectManager.READ);
                  byte buf[] = new byte[obj.size()];
                  obj.read(buf, 0, obj.size());
                  arr0[i-1][j] = buf;
                  obj.close();
               }
               j++;
            }
            rs.close();
            stmt.close();
            conn.commit();
				return arr0; }
		catch (SQLException e) { throw e;      }
		catch (Exception e) { throw e;      }
		finally { try { conn.close(); } catch(Exception e){} }

	}

	
	public String[] get_coltype(String in,String url, String user, String pass, String driver) throws Exception 
	{
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;      }
		try {    
				conn=DriverManager.getConnection(url,user,pass);
				Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = stmt.executeQuery(in);
				ResultSetMetaData rsmd = rs.getMetaData ();
				int numberOfColumns = rsmd.getColumnCount ();
				rs.last();
				int numberOfRows = rs.getRow ();
				rs.beforeFirst();
				if (numberOfRows==0) return null;
				String arr0[]=new String[numberOfColumns];
            for (int i = 1; i <= numberOfColumns; i++) 
               {
                  arr0[i-1] = rsmd.getColumnTypeName(i);
               }
				return arr0; }
		catch (SQLException e) { throw e;    }
		catch (Exception e) { throw e;    }
		finally { try { conn.close(); } catch(Exception e){} }

	}
	
	public String[][] get_colinfo(String in,String url, String user, String pass, String driver) throws Exception 
	{
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;      }
		try {    
				conn=DriverManager.getConnection(url,user,pass);
				Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = stmt.executeQuery(in);
				ResultSetMetaData rsmd = rs.getMetaData ();
				int numberOfColumns = rsmd.getColumnCount ();
				rs.last();
				int numberOfRows = rs.getRow ();
				if (numberOfRows==0) return null;
				String arr0[][]=new String[4][numberOfColumns];
				rs.beforeFirst();
            rs.next();
            for (int i = 1; i <= numberOfColumns; i++) {
                  arr0[0][i-1] = rsmd.getColumnName(i);
                  arr0[1][i-1] = rsmd.getColumnTypeName(i);
                  if (Types.ARRAY  == rsmd.getColumnType(i)) {
                     // look for the array datatype
                     Array arr = rs.getArray(i);
                     if (arr == null) continue;
                     ResultSet arrRS = arr.getResultSet();
                     ResultSetMetaData arrRSMD = arrRS.getMetaData();
                     arr0[2][i-1] = arrRSMD.getColumnTypeName(1);
                     Object out [] = (Object [])arr.getArray();
                     int dim1 = out.length;
                     if (Types.ARRAY == arrRSMD.getColumnType(2)) {
                        // 2D array
                        Object i0 [] = (Object [])out[0];
                        int dim2 = i0.length;
                        arr0[3][i-1] = '('+Integer.toString(dim1)+','+Integer.toString(dim2)+')';
                     } else {
                        arr0[3][i-1] = '('+Integer.toString(dim1)+')';
                     }
                  }
               }
				return arr0; }
		catch (SQLException e) { throw e;    }
		catch (Exception e) { throw e;    }
		finally { try { conn.close(); } catch(Exception e){} }

	}
	
	public int[][] get_colsize(String in,String url, String user, String pass, String driver) throws Exception 
	{
      // returns the size for each column as an array of integers (ndim, dim1, dim2, typeNum)
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;      }
		try {    
				conn=DriverManager.getConnection(url,user,pass);
				Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = stmt.executeQuery(in);
				ResultSetMetaData rsmd = rs.getMetaData ();
				int numberOfColumns = rsmd.getColumnCount ();
				rs.last();
				int numberOfRows = rs.getRow ();
				if (numberOfRows==0) return null;
				int arr0[][]=new int [4][numberOfColumns];
				rs.beforeFirst();
            rs.next();
            for (int i = 1; i <= numberOfColumns; i++) {
                  arr0[0][i-1]=0;
                  arr0[1][i-1]=0;
                  arr0[2][i-1]=0;
                  arr0[3][i-1] = rsmd.getColumnType(i);
                  if (Types.ARRAY  == rsmd.getColumnType(i)) {
                     // look for the array datatype
                     arr0[0][i-1]++;
                     Array arr = rs.getArray(i);
                     if (arr == null) continue;
                     ResultSet arrRS = arr.getResultSet();
                     ResultSetMetaData arrRSMD = arrRS.getMetaData();
                     arr0[3][i-1] = arrRSMD.getColumnType(1);
                     Object out [] = (Object [])arr.getArray();
                     arr0[1][i-1] = out.length;
                     if (Types.ARRAY == arrRSMD.getColumnType(2)) {
                        // 2D array
                        arr0[0][i-1]++;
                        Object i0 [] = (Object [])out[0];
                        arr0[2][i-1] = i0.length;
                     } 
                  }
               }
				return arr0; }
		catch (SQLException e) { throw e;    }
		catch (Exception e) { throw e;    }
		finally { try { conn.close(); } catch(Exception e){} }

	}

   public void put_blob(String in, String url, String user, String pass, String driver, byte[] col0) throws Exception 
	{
      // this is the POSTGRES implementation of BLOB.  Other database may implement this differently in JDBC
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e; }
		try {
				conn=DriverManager.getConnection(url,user,pass);
            conn.setAutoCommit(false);
            LargeObjectManager lobj = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
            long oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
            LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
            obj.write(col0, 0, col0.length);
            obj.close();
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setLong(1, oid);
            ps.executeUpdate();
            ps.close();
            conn.commit(); }
		catch (SQLException e) { throw e;      }
		catch (Exception e) { throw e;      }
		finally { try { conn.close(); } catch(Exception e){} }

	}

   public void put_blob(String in, String url, String user, String pass, String driver, byte[] col0, byte[] col1) throws Exception 
	{
      // this is the POSTGRES implementation of BLOB.  Other database may implement this differently in JDBC
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e; }
		try {
				conn=DriverManager.getConnection(url,user,pass);
            conn.setAutoCommit(false);

            LargeObjectManager lobj1 = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
            long oid1 = lobj1.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
            LargeObject obj1 = lobj1.open(oid1, LargeObjectManager.WRITE);
            obj1.write(col0, 0, col0.length);
            obj1.close();

            LargeObjectManager lobj2 = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
            long oid2 = lobj2.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
            LargeObject obj2 = lobj2.open(oid2, LargeObjectManager.WRITE);
            obj2.write(col1, 0, col1.length);
            obj2.close();

            PreparedStatement ps = conn.prepareStatement(in);
            ps.setLong(1, oid1);
            ps.setLong(2, oid2);
            ps.executeUpdate();
            ps.close();
            conn.commit(); }
		catch (SQLException e) { throw e;      }
		catch (Exception e) { throw e;      }
		finally { try { conn.close(); } catch(Exception e){} }

	}


   public void put_blob(String in, String url, String user, String pass, String driver, byte[] col0, byte[] col1, byte[] col2) throws Exception 
	{
      // this is the POSTGRES implementation of BLOB.  Other database may implement this differently in JDBC
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e; }
		try {
				conn=DriverManager.getConnection(url,user,pass);
            conn.setAutoCommit(false);

            LargeObjectManager lobj1 = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
            long oid1 = lobj1.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
            LargeObject obj1 = lobj1.open(oid1, LargeObjectManager.WRITE);
            obj1.write(col0, 0, col0.length);
            obj1.close();

            LargeObjectManager lobj2 = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
            long oid2 = lobj2.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
            LargeObject obj2 = lobj2.open(oid2, LargeObjectManager.WRITE);
            obj2.write(col1, 0, col1.length);
            obj2.close();

            LargeObjectManager lobj3 = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
            long oid3 = lobj3.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
            LargeObject obj3 = lobj3.open(oid3, LargeObjectManager.WRITE);
            obj3.write(col2, 0, col2.length);
            obj3.close();

            PreparedStatement ps = conn.prepareStatement(in);
            ps.setLong(1, oid1);
            ps.setLong(2, oid2);
            ps.setLong(3, oid3);
            ps.executeUpdate();
            ps.close();
            conn.commit(); }
		catch (SQLException e) { throw e;      }
		catch (Exception e) { throw e;      }
		finally { try { conn.close(); } catch(Exception e){} }

	}

   public void put_blob(String in, String url, String user, String pass, String driver, byte[] col0, byte[] col1, byte[] col2, byte[] col3) throws Exception 
	{
      // this is the POSTGRES implementation of BLOB.  Other database may implement this differently in JDBC
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e; }
		try {
				conn=DriverManager.getConnection(url,user,pass);
            conn.setAutoCommit(false);

            LargeObjectManager lobj1 = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
            long oid1 = lobj1.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
            LargeObject obj1 = lobj1.open(oid1, LargeObjectManager.WRITE);
            obj1.write(col0, 0, col0.length);
            obj1.close();

            LargeObjectManager lobj2 = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
            long oid2 = lobj2.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
            LargeObject obj2 = lobj2.open(oid2, LargeObjectManager.WRITE);
            obj2.write(col1, 0, col1.length);
            obj2.close();

            LargeObjectManager lobj3 = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
            long oid3 = lobj3.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
            LargeObject obj3 = lobj3.open(oid3, LargeObjectManager.WRITE);
            obj3.write(col2, 0, col2.length);
            obj3.close();

            LargeObjectManager lobj4 = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
            long oid4 = lobj4.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
            LargeObject obj4 = lobj4.open(oid4, LargeObjectManager.WRITE);
            obj4.write(col3, 0, col3.length);
            obj4.close();

            PreparedStatement ps = conn.prepareStatement(in);
            ps.setLong(1, oid1);
            ps.setLong(2, oid2);
            ps.setLong(3, oid3);
            ps.setLong(4, oid4);
            ps.executeUpdate();
            ps.close();
            conn.commit(); }
		catch (SQLException e) { throw e;      }
		catch (Exception e) { throw e;      }
		finally { try { conn.close(); } catch(Exception e){} }

	}


/* this doesn't work for some reason (variable number of args) - just overload the method instead
   public void put_array(String in, String url, String user, String pass, String driver, byte[] ... cols) throws Exception 
	{
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e; }
		try {
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            for (int i=0; i< cols.length; i++)
               ps.setBytes(i+1, (byte []) cols[i]);
            ps.executeUpdate();
            ps.close(); }
		catch (SQLException e) { throw e;      }
		catch (Exception e) { throw e;      }
		finally { try { conn.close(); } catch(Exception e){} }

	}

*/

   public void put_array(String in, String url, String user, String pass, String driver, byte[] col0) throws Exception 
	{
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e; }
		try {
            System.out.println("Hello World!");
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setBytes(1, col0);
            ps.executeUpdate();
            ps.close(); }
		catch (SQLException e) { throw e;      }
		catch (Exception e) { throw e;      }
		finally { try { conn.close(); } catch(Exception e){} }

	}

	public void put_array(String in, String url, String user, String pass, String driver, byte[] col0, byte[] col1) throws Exception 
	{
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e; }
		try {
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setBytes(1, col0);
            ps.setBytes(2, col1);
            ps.executeUpdate();
            ps.close(); }
		catch (SQLException e) { throw e;      }
		catch (Exception e) { throw e;      }
		finally { try { conn.close(); } catch(Exception e){} }

	}

	public void put_array(String in, String url, String user, String pass, String driver, byte[] col0, byte[] col1, byte[] col2) throws Exception 
	{
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e; }
		try {
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setBytes(1, col0);
            ps.setBytes(2, col1);
            ps.setBytes(3, col2);
            ps.executeUpdate();
            ps.close(); }
		catch (SQLException e) { throw e;      }
		catch (Exception e) { throw e;      }
		finally { try { conn.close(); } catch(Exception e){} }

	}

	public void put_array(String in, String url, String user, String pass, String driver, byte[] col0, byte[] col1, byte[] col2, byte[] col3) throws Exception 
	{
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e; }
		try {
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setBytes(1, col0);
            ps.setBytes(2, col1);
            ps.setBytes(3, col2);
            ps.setBytes(4, col3);
            ps.executeUpdate();
            ps.close(); }
		catch (SQLException e) { throw e;      }
		catch (Exception e) { throw e;      }
		finally { try { conn.close(); } catch(Exception e){} }

	}

   public void put_array(String in, String url, String user, String pass, String driver, String[] col0) throws Exception 
	{
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("varchar", col0));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, String[] col0, String[] col1) throws Exception 
	{
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, col0);
            ps.setObject(2, col1);
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, String[] col0, String[] col1, String[] col2) throws Exception 
	{
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, col0);
            ps.setObject(2, col1);
            ps.setObject(3, col2);
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, String[] col0, String[] col1, String[] col2, String[] col3) throws Exception 
	{
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, col0);
            ps.setObject(2, col1);
            ps.setObject(3, col2);
            ps.setObject(4, col3);
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }

	}

   public void put_array(String in, String url, String user, String pass, String driver, short [] col0) throws Exception 
	{
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Short tmp0 [] = new Short[col0.length];
            for (int i=0; i<col0.length; i++) { tmp0[i] = (Short)col0[i]; }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, short[] col0, short[] col1) throws Exception 
	{
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Short tmp0 [] = new Short[col0.length];
            for (int i=0; i<col0.length; i++) { tmp0[i] = (Short)col0[i]; }
            Short tmp1 [] = new Short[col1.length];
            for (int i=0; i<col1.length; i++) { tmp1[i] = (Short)col1[i]; }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, short[] col0, short[] col1, short[] col2) throws Exception 
	{
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Short tmp0 [] = new Short[col0.length];
            for (int i=0; i<col0.length; i++) { tmp0[i] = (Short)col0[i]; }
            Short tmp1 [] = new Short[col1.length];
            for (int i=0; i<col1.length; i++) { tmp1[i] = (Short)col1[i]; }
            Short tmp2 [] = new Short[col2.length];
            for (int i=0; i<col2.length; i++) { tmp2[i] = (Short)col2[i]; }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.setObject(3, conn.createArrayOf("numeric", tmp2));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, short[] col0, short[] col1, short[] col2, short[] col3) throws Exception 
	{
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Short tmp0 [] = new Short[col0.length];
            for (int i=0; i<col0.length; i++) { tmp0[i] = (Short)col0[i]; }
            Short tmp1 [] = new Short[col1.length];
            for (int i=0; i<col1.length; i++) { tmp1[i] = (Short)col1[i]; }
            Short tmp2 [] = new Short[col2.length];
            for (int i=0; i<col2.length; i++) { tmp2[i] = (Short)col2[i]; }
            Short tmp3 [] = new Short[col3.length];
            for (int i=0; i<col3.length; i++) { tmp3[i] = (Short)col3[i]; }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.setObject(3, conn.createArrayOf("numeric", tmp2));
            ps.setObject(4, conn.createArrayOf("numeric", tmp3));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }

	}

   public void put_array(String in, String url, String user, String pass, String driver, short [][]col0) throws Exception 
	{
      // 2D short array
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Short tmp0 [][] = new Short[col0.length][col0[0].length];
            for (int i=0; i<col0.length; i++) { 
               for (int j=0; j<col0[0].length; j++) { 
                  tmp0[i][j] = (Short)col0[i][j]; 
               }
            }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, short [][]col0, short [][]col1) throws Exception 
	{
      // 2D short array
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Short tmp0 [][] = new Short[col0.length][col0[0].length];
            for (int i=0; i<col0.length; i++) { 
               for (int j=0; j<col0[0].length; j++) { 
                  tmp0[i][j] = (Short)col0[i][j]; 
               }
            }
            Short tmp1 [][] = new Short[col1.length][col1[0].length];
            for (int i=0; i<col1.length; i++) { 
               for (int j=0; j<col1[0].length; j++) { 
                  tmp1[i][j] = (Short)col1[i][j]; 
               }
            }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, short [][]col0, short [][]col1, short [][]col2) throws Exception 
	{
      // 2D short array
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Short tmp0 [][] = new Short[col0.length][col0[0].length];
            for (int i=0; i<col0.length; i++) { 
               for (int j=0; j<col0[0].length; j++) { 
                  tmp0[i][j] = (Short)col0[i][j]; 
               }
            }
            Short tmp1 [][] = new Short[col1.length][col1[0].length];
            for (int i=0; i<col1.length; i++) { 
               for (int j=0; j<col1[0].length; j++) { 
                  tmp1[i][j] = (Short)col1[i][j]; 
               }
            }
            Short tmp2 [][] = new Short[col2.length][col2[0].length];
            for (int i=0; i<col2.length; i++) { 
               for (int j=0; j<col2[0].length; j++) { 
                  tmp2[i][j] = (Short)col2[i][j]; 
               }
            }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.setObject(3, conn.createArrayOf("numeric", tmp2));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, short [][]col0, short [][]col1, short [][]col2, short [][]col3) throws Exception 
	{
      // 2D short array
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Short tmp0 [][] = new Short[col0.length][col0[0].length];
            for (int i=0; i<col0.length; i++) { 
               for (int j=0; j<col0[0].length; j++) { 
                  tmp0[i][j] = (Short)col0[i][j]; 
               }
            }
            Short tmp1 [][] = new Short[col1.length][col1[0].length];
            for (int i=0; i<col1.length; i++) { 
               for (int j=0; j<col1[0].length; j++) { 
                  tmp1[i][j] = (Short)col1[i][j]; 
               }
            }
            Short tmp2 [][] = new Short[col2.length][col2[0].length];
            for (int i=0; i<col2.length; i++) { 
               for (int j=0; j<col2[0].length; j++) { 
                  tmp2[i][j] = (Short)col2[i][j]; 
               }
            }
            Short tmp3 [][] = new Short[col3.length][col3[0].length];
            for (int i=0; i<col3.length; i++) { 
               for (int j=0; j<col3[0].length; j++) { 
                  tmp3[i][j] = (Short)col3[i][j]; 
               }
            }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.setObject(3, conn.createArrayOf("numeric", tmp2));
            ps.setObject(4, conn.createArrayOf("numeric", tmp3));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, int [] col0) throws Exception 
	{
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Integer tmp0 [] = new Integer[col0.length];
            for (int i=0; i<col0.length; i++) { tmp0[i] = (Integer)col0[i]; }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, int[] col0, int[] col1) throws Exception 
	{
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Integer tmp0 [] = new Integer[col0.length];
            for (int i=0; i<col0.length; i++) { tmp0[i] = (Integer)col0[i]; }
            Integer tmp1 [] = new Integer[col1.length];
            for (int i=0; i<col1.length; i++) { tmp1[i] = (Integer)col1[i]; }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, int[] col0, int[] col1, int[] col2) throws Exception 
	{
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Integer tmp0 [] = new Integer[col0.length];
            for (int i=0; i<col0.length; i++) { tmp0[i] = (Integer)col0[i]; }
            Integer tmp1 [] = new Integer[col1.length];
            for (int i=0; i<col1.length; i++) { tmp1[i] = (Integer)col1[i]; }
            Integer tmp2 [] = new Integer[col2.length];
            for (int i=0; i<col2.length; i++) { tmp2[i] = (Integer)col2[i]; }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.setObject(3, conn.createArrayOf("numeric", tmp2));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, int[] col0, int[] col1, int[] col2, int[] col3) throws Exception 
	{
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Integer tmp0 [] = new Integer[col0.length];
            for (int i=0; i<col0.length; i++) { tmp0[i] = (Integer)col0[i]; }
            Integer tmp1 [] = new Integer[col1.length];
            for (int i=0; i<col1.length; i++) { tmp1[i] = (Integer)col1[i]; }
            Integer tmp2 [] = new Integer[col2.length];
            for (int i=0; i<col2.length; i++) { tmp2[i] = (Integer)col2[i]; }
            Integer tmp3 [] = new Integer[col3.length];
            for (int i=0; i<col3.length; i++) { tmp3[i] = (Integer)col3[i]; }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.setObject(3, conn.createArrayOf("numeric", tmp2));
            ps.setObject(4, conn.createArrayOf("numeric", tmp3));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }

	}

   public void put_array(String in, String url, String user, String pass, String driver, int [][]col0) throws Exception 
	{
      // 2D int array
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Integer tmp0 [][] = new Integer[col0.length][col0[0].length];
            for (int i=0; i<col0.length; i++) { 
               for (int j=0; j<col0[0].length; j++) { 
                  tmp0[i][j] = (Integer)col0[i][j]; 
               }
            }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, int [][]col0, int [][]col1) throws Exception 
	{
      // 2D int array
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Integer tmp0 [][] = new Integer[col0.length][col0[0].length];
            for (int i=0; i<col0.length; i++) { 
               for (int j=0; j<col0[0].length; j++) { 
                  tmp0[i][j] = (Integer)col0[i][j]; 
               }
            }
            Integer tmp1 [][] = new Integer[col1.length][col1[0].length];
            for (int i=0; i<col1.length; i++) { 
               for (int j=0; j<col1[0].length; j++) { 
                  tmp1[i][j] = (Integer)col1[i][j]; 
               }
            }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, int [][]col0, int [][]col1, int [][]col2) throws Exception 
	{
      // 2D int array
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Integer tmp0 [][] = new Integer[col0.length][col0[0].length];
            for (int i=0; i<col0.length; i++) { 
               for (int j=0; j<col0[0].length; j++) { 
                  tmp0[i][j] = (Integer)col0[i][j]; 
               }
            }
            Integer tmp1 [][] = new Integer[col1.length][col1[0].length];
            for (int i=0; i<col1.length; i++) { 
               for (int j=0; j<col1[0].length; j++) { 
                  tmp1[i][j] = (Integer)col1[i][j]; 
               }
            }
            Integer tmp2 [][] = new Integer[col2.length][col2[0].length];
            for (int i=0; i<col2.length; i++) { 
               for (int j=0; j<col2[0].length; j++) { 
                  tmp2[i][j] = (Integer)col2[i][j]; 
               }
            }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.setObject(3, conn.createArrayOf("numeric", tmp2));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, int [][]col0, int [][]col1, int [][]col2, int [][]col3) throws Exception 
	{
      // 2D int array
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Integer tmp0 [][] = new Integer[col0.length][col0[0].length];
            for (int i=0; i<col0.length; i++) { 
               for (int j=0; j<col0[0].length; j++) { 
                  tmp0[i][j] = (Integer)col0[i][j]; 
               }
            }
            Integer tmp1 [][] = new Integer[col1.length][col1[0].length];
            for (int i=0; i<col1.length; i++) { 
               for (int j=0; j<col1[0].length; j++) { 
                  tmp1[i][j] = (Integer)col1[i][j]; 
               }
            }
            Integer tmp2 [][] = new Integer[col2.length][col2[0].length];
            for (int i=0; i<col2.length; i++) { 
               for (int j=0; j<col2[0].length; j++) { 
                  tmp2[i][j] = (Integer)col2[i][j]; 
               }
            }
            Integer tmp3 [][] = new Integer[col3.length][col3[0].length];
            for (int i=0; i<col3.length; i++) { 
               for (int j=0; j<col3[0].length; j++) { 
                  tmp3[i][j] = (Integer)col3[i][j]; 
               }
            }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.setObject(3, conn.createArrayOf("numeric", tmp2));
            ps.setObject(4, conn.createArrayOf("numeric", tmp3));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}


   public void put_array(String in, String url, String user, String pass, String driver, float [] col0) throws Exception 
	{
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Float tmp0 [] = new Float[col0.length];
            for (int i=0; i<col0.length; i++) { tmp0[i] = (Float)col0[i]; }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, float[] col0, float[] col1) throws Exception 
	{
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Float tmp0 [] = new Float[col0.length];
            for (int i=0; i<col0.length; i++) { tmp0[i] = (Float)col0[i]; }
            Float tmp1 [] = new Float[col1.length];
            for (int i=0; i<col1.length; i++) { tmp1[i] = (Float)col1[i]; }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, float[] col0, float[] col1, float[] col2) throws Exception 
	{
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Float tmp0 [] = new Float[col0.length];
            for (int i=0; i<col0.length; i++) { tmp0[i] = (Float)col0[i]; }
            Float tmp1 [] = new Float[col1.length];
            for (int i=0; i<col1.length; i++) { tmp1[i] = (Float)col1[i]; }
            Float tmp2 [] = new Float[col2.length];
            for (int i=0; i<col2.length; i++) { tmp2[i] = (Float)col2[i]; }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.setObject(3, conn.createArrayOf("numeric", tmp2));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, float[] col0, float[] col1, float[] col2, float[] col3) throws Exception 
	{
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Float tmp0 [] = new Float[col0.length];
            for (int i=0; i<col0.length; i++) { tmp0[i] = (Float)col0[i]; }
            Float tmp1 [] = new Float[col1.length];
            for (int i=0; i<col1.length; i++) { tmp1[i] = (Float)col1[i]; }
            Float tmp2 [] = new Float[col2.length];
            for (int i=0; i<col2.length; i++) { tmp2[i] = (Float)col2[i]; }
            Float tmp3 [] = new Float[col3.length];
            for (int i=0; i<col3.length; i++) { tmp3[i] = (Float)col3[i]; }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.setObject(3, conn.createArrayOf("numeric", tmp2));
            ps.setObject(4, conn.createArrayOf("numeric", tmp3));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }

	}


   public void put_array(String in, String url, String user, String pass, String driver, float [][] col0) throws Exception 
	{
      // 2D float array
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Float tmp0 [][] = new Float[col0.length][col0[0].length];
            for (int i=0; i<col0.length; i++) { 
               for (int j=0; j<col0[0].length; j++) { 
                  tmp0[i][j] = (Float)col0[i][j]; 
               }
            }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, float [][] col0, float [][] col1) throws Exception 
	{
      // 2D float array
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Float tmp0 [][] = new Float[col0.length][col0[0].length];
            for (int i=0; i<col0.length; i++) { 
               for (int j=0; j<col0[0].length; j++) { 
                  tmp0[i][j] = (Float)col0[i][j]; 
               }
            }
            Float tmp1 [][] = new Float[col1.length][col1[0].length];
            for (int i=0; i<col1.length; i++) { 
               for (int j=0; j<col1[0].length; j++) { 
                  tmp1[i][j] = (Float)col1[i][j]; 
               }
            }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, float [][] col0, float [][] col1, float [][] col2) throws Exception 
	{
      // 2D float array
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Float tmp0 [][] = new Float[col0.length][col0[0].length];
            for (int i=0; i<col0.length; i++) { 
               for (int j=0; j<col0[0].length; j++) { 
                  tmp0[i][j] = (Float)col0[i][j]; 
               }
            }
            Float tmp1 [][] = new Float[col1.length][col1[0].length];
            for (int i=0; i<col1.length; i++) { 
               for (int j=0; j<col1[0].length; j++) { 
                  tmp1[i][j] = (Float)col1[i][j]; 
               }
            }
            Float tmp2 [][] = new Float[col2.length][col2[0].length];
            for (int i=0; i<col2.length; i++) { 
               for (int j=0; j<col2[0].length; j++) { 
                  tmp2[i][j] = (Float)col2[i][j]; 
               }
            }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.setObject(3, conn.createArrayOf("numeric", tmp2));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, float [][] col0, float [][] col1, float [][] col2, float [][] col3) throws Exception 
	{
      // 2D float array
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Float tmp0 [][] = new Float[col0.length][col0[0].length];
            for (int i=0; i<col0.length; i++) { 
               for (int j=0; j<col0[0].length; j++) { 
                  tmp0[i][j] = (Float)col0[i][j]; 
               }
            }
            Float tmp1 [][] = new Float[col1.length][col1[0].length];
            for (int i=0; i<col1.length; i++) { 
               for (int j=0; j<col1[0].length; j++) { 
                  tmp1[i][j] = (Float)col1[i][j]; 
               }
            }
            Float tmp2 [][] = new Float[col2.length][col2[0].length];
            for (int i=0; i<col2.length; i++) { 
               for (int j=0; j<col2[0].length; j++) { 
                  tmp2[i][j] = (Float)col2[i][j]; 
               }
            }
            Float tmp3 [][] = new Float[col3.length][col3[0].length];
            for (int i=0; i<col3.length; i++) { 
               for (int j=0; j<col3[0].length; j++) { 
                  tmp3[i][j] = (Float)col3[i][j]; 
               }
            }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.setObject(3, conn.createArrayOf("numeric", tmp2));
            ps.setObject(4, conn.createArrayOf("numeric", tmp3));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, double [] col0) throws Exception 
	{
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Double tmp0 [] = new Double[col0.length];
            for (int i=0; i<col0.length; i++) { tmp0[i] = (Double)col0[i]; }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, double[] col0, double[] col1) throws Exception 
	{
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Double tmp0 [] = new Double[col0.length];
            for (int i=0; i<col0.length; i++) { tmp0[i] = (Double)col0[i]; }
            Double tmp1 [] = new Double[col1.length];
            for (int i=0; i<col1.length; i++) { tmp1[i] = (Double)col1[i]; }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, double[] col0, double[] col1, double[] col2) throws Exception 
	{
		Connection conn = null;                                          
		try {    Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Double tmp0 [] = new Double[col0.length];
            for (int i=0; i<col0.length; i++) { tmp0[i] = (Double)col0[i]; }
            Double tmp1 [] = new Double[col1.length];
            for (int i=0; i<col1.length; i++) { tmp1[i] = (Double)col1[i]; }
            Double tmp2 [] = new Double[col2.length];
            for (int i=0; i<col2.length; i++) { tmp2[i] = (Double)col2[i]; }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.setObject(3, conn.createArrayOf("numeric", tmp2));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, double[] col0, double[] col1, double[] col2, double[] col3) throws Exception 
	{
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Double tmp0 [] = new Double[col0.length];
            for (int i=0; i<col0.length; i++) { tmp0[i] = (Double)col0[i]; }
            Double tmp1 [] = new Double[col1.length];
            for (int i=0; i<col1.length; i++) { tmp1[i] = (Double)col1[i]; }
            Double tmp2 [] = new Double[col2.length];
            for (int i=0; i<col2.length; i++) { tmp2[i] = (Double)col2[i]; }
            Double tmp3 [] = new Double[col3.length];
            for (int i=0; i<col3.length; i++) { tmp3[i] = (Double)col3[i]; }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.setObject(3, conn.createArrayOf("numeric", tmp2));
            ps.setObject(4, conn.createArrayOf("numeric", tmp3));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }

	}

   public void put_array(String in, String url, String user, String pass, String driver, double [][] col0) throws Exception 
	{
      // 2D double array
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Double tmp0 [][] = new Double[col0.length][col0[0].length];
            for (int i=0; i<col0.length; i++) { 
               for (int j=0; j<col0[0].length; j++) { 
                  tmp0[i][j] = (Double)col0[i][j]; 
               }
            }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, double [][]col0, double [][]col1) throws Exception 
	{
      // 2D double array
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Double tmp0 [][] = new Double[col0.length][col0[0].length];
            for (int i=0; i<col0.length; i++) { 
               for (int j=0; j<col0[0].length; j++) { 
                  tmp0[i][j] = (Double)col0[i][j]; 
               }
            }
            Double tmp1 [][] = new Double[col1.length][col1[0].length];
            for (int i=0; i<col1.length; i++) { 
               for (int j=0; j<col1[0].length; j++) { 
                  tmp1[i][j] = (Double)col1[i][j]; 
               }
            }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, double [][]col0, double [][]col1, double [][]col2) throws Exception 
	{
      // 2D double array
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Double tmp0 [][] = new Double[col0.length][col0[0].length];
            for (int i=0; i<col0.length; i++) { 
               for (int j=0; j<col0[0].length; j++) { 
                  tmp0[i][j] = (Double)col0[i][j]; 
               }
            }
            Double tmp1 [][] = new Double[col1.length][col1[0].length];
            for (int i=0; i<col1.length; i++) { 
               for (int j=0; j<col1[0].length; j++) { 
                  tmp1[i][j] = (Double)col1[i][j]; 
               }
            }
            Double tmp2 [][] = new Double[col2.length][col2[0].length];
            for (int i=0; i<col2.length; i++) { 
               for (int j=0; j<col2[0].length; j++) { 
                  tmp2[i][j] = (Double)col2[i][j]; 
               }
            }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.setObject(3, conn.createArrayOf("numeric", tmp2));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}

   public void put_array(String in, String url, String user, String pass, String driver, double [][]col0, double [][]col1, double [][]col2, double [][]col3) throws Exception 
	{
      // 2D double array
		Connection conn = null;                                          
		try { Class.forName(driver); }
		catch (ClassNotFoundException e) {
				System.out.println("Cannot find the jdbc driver... \n Put the jar file of the jdbc file to your CLASSPATH environment variable");
				throw e;
			}
		try {
            Double tmp0 [][] = new Double[col0.length][col0[0].length];
            for (int i=0; i<col0.length; i++) { 
               for (int j=0; j<col0[0].length; j++) { 
                  tmp0[i][j] = (Double)col0[i][j]; 
               }
            }
            Double tmp1 [][] = new Double[col1.length][col1[0].length];
            for (int i=0; i<col1.length; i++) { 
               for (int j=0; j<col1[0].length; j++) { 
                  tmp1[i][j] = (Double)col1[i][j]; 
               }
            }
            Double tmp2 [][] = new Double[col2.length][col2[0].length];
            for (int i=0; i<col2.length; i++) { 
               for (int j=0; j<col2[0].length; j++) { 
                  tmp2[i][j] = (Double)col2[i][j]; 
               }
            }
            Double tmp3 [][] = new Double[col3.length][col3[0].length];
            for (int i=0; i<col3.length; i++) { 
               for (int j=0; j<col3[0].length; j++) { 
                  tmp3[i][j] = (Double)col3[i][j]; 
               }
            }
				conn=DriverManager.getConnection(url,user,pass);
            PreparedStatement ps = conn.prepareStatement(in);
            ps.setObject(1, conn.createArrayOf("numeric", tmp0));
            ps.setObject(2, conn.createArrayOf("numeric", tmp1));
            ps.setObject(3, conn.createArrayOf("numeric", tmp2));
            ps.setObject(4, conn.createArrayOf("numeric", tmp3));
            ps.executeUpdate();
            ps.close();
			}
		catch (SQLException e) { throw e; }
		catch (Exception e) { throw e; }
		finally { try { conn.close(); } catch(Exception e){} }
	}




	public void exec_sql(String in,String url, String user, String pass, String driver) throws Exception 
	{
		Connection conn = null;                                          
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
				Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
					conn.close();
				}
				catch(Exception e){}
			}

	}
	
}

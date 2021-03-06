/**
 * (C) Copyright IBM Corp. 2010, 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.ibm.bi.dml.runtime.controlprogram.parfor.util;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;

/**
 * Functionalities for extracting numeric IDs from Hadoop taskIDs and other
 * things related to modification of IDs.
 * 
 * NOTE: Many those functionalities rely on a big endian format. This is always given because 
 * Java stores everything as big endian, independent of the platform. Furthermore, we
 * rely on Long.MAX_VALUE in order to prevent numeric overflows with regard to signed data types.
 * 
 */
public class IDHandler 
{

	
	/**
	 * 
	 * @param taskID
	 * @return
	 */
	public static long extractUncheckedLongID( String taskID )
	{
		//in: e.g., task_local_0002_m_000009 or jobID + ...
		//out: e.g., 2000009

		//generic parsing for flexible taskID formats
		char[] c = taskID.toCharArray(); //all chars
		long value = 1; //1 catch leading zeros as well
		for( int i=0; i<c.length; i++ )
		{
			if( c[i] >= 48 && c[i]<=57 )  //'0'-'9'
			{
				long newVal = (c[i]-48);
				
				if( (Long.MAX_VALUE-value*10) < newVal ) 
					throw new RuntimeException("WARNING: extractLongID will produced numeric overflow "+value);
				
				value = value*10 + newVal;
			}
		}
		
		return value;
	}

	/**
	 * 
	 * @param taskID
	 * @return
	 */
	public static int extractIntID( String taskID )
	{
		int maxlen = (int)(Math.log10(Integer.MAX_VALUE));
		int intVal = (int)extractID( taskID, maxlen );
		return intVal;		
		
	}
	
	/**
	 * 
	 * @param taskID
	 * @return
	 */
	public static long extractLongID( String taskID )
	{
		int maxlen = (int)(Math.log10(Long.MAX_VALUE));
		long longVal = extractID( taskID, maxlen );
		return longVal;
	}
	
	/**
	 * 
	 * @param part1
	 * @param part2
	 * @return
	 */
	public static long concatIntIDsToLong( int part1, int part2 )
	{
		//big-endian version (in java uses only big endian)
		long value = ((long)part1) << 32; //unsigned shift of part1 to first 4bytes
		value = value | part2;            //bitwise OR with part2 (second 4bytes)
		
		//*-endian version 
		//long value = ((long)part1)*(long)Math.pow(2, 32);
		//value += part2;
		
		return value;
	}
	
	/**
	 * 
	 * @param part 1 for first 4 bytes, 2 for second 4 bytes
	 * @return
	 */
	public static int extractIntIDFromLong( long val, int part )
	{
		int ret = -1;
		if( part == 1 )
			ret = (int)(val >>> 32);
		else if( part == 2 )
			ret = (int)val; 
				
		return ret;
	}
	
	/**
	 * Creates a unique identifier with the pattern <process_id>_<host_ip>.
	 * 
	 * @return
	 */
	public static String createDistributedUniqueID() 
	{
		String uuid = null;
		
		try
		{
			//get process id		 
		    String pname = ManagementFactory.getRuntimeMXBean().getName(); //pid@hostname
		    String pid = pname.split("@")[0];
		    
		    //get ip address
		    InetAddress addr = InetAddress.getLocalHost();
		    String host = addr.getHostAddress();
		    //addr.getHostName()
		    
			uuid = pid + "_" + host;
		}
		catch(Exception ex)
		{
			uuid = "0_0.0.0.0";
		}
		
		return uuid;
	}

	/**
	 * 
	 * @param taskID
	 * @param maxlen
	 * @return
	 */
	private static long extractID( String taskID, int maxlen )
	{
		//in: e.g., task_local_0002_m_000009 or task_201203111647_0898_m_000001
		//out: e.g., 2000009
		
		//generic parsing for flexible taskID formats
		char[] c = taskID.toCharArray(); //all chars
		long value = 0; //1 catch leading zeros as well		
		int count = 0;
		
		for( int i=c.length-1; i >= 0 && count<maxlen; i-- ) //start at end
		{
			if( c[i] >= 48 && c[i]<=57 )  //'0'-'9'
			{
				long newVal = (c[i]-48);
				newVal = newVal * (long)Math.pow(10, count); //shift to front
				
				value += newVal;
				count++;
			}
		}
		
		return value;
	}
	
}

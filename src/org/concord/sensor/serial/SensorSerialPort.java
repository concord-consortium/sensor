/*
 *  Copyright (C) 2004  The Concord Consortium, Inc.,
 *  10 Concord Crossing, Concord, MA 01742
 *
 *  Web Site: http://www.concord.org
 *  Email: info@concord.org
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * END LICENSE */

/*
 * Created on Feb 25, 2005
 *
 */
package org.concord.sensor.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author scytacki
 *
 */
public interface SensorSerialPort 
{	
	public static final int  FLOWCONTROL_NONE       =0;
	public static final int  FLOWCONTROL_RTSCTS_IN  =1;
	public static final int  FLOWCONTROL_RTSCTS_OUT =2;
	public static final int  FLOWCONTROL_XONXOFF_IN =4;
	public static final int  FLOWCONTROL_XONXOFF_OUT=8;

	public abstract void open(String portName)
		throws IOException;
	
	public abstract void close()
		throws IOException;
	
	public boolean isOpen();
	
	public abstract void setSerialPortParams( int b, int d, int s, int p )
		throws IOException;
	public abstract int getBaudRate();
	public abstract int getDataBits();
	public abstract int getStopBits();
	public abstract int getParity();
	public abstract void setFlowControlMode( int flowcontrol )
		throws IOException;

	public abstract void disableReceiveTimeout();
	public abstract void enableReceiveTimeout( int time )
		throws IOException;

	public abstract InputStream getInputStream() throws IOException;
	public abstract OutputStream getOutputStream() throws IOException;
	
	/**
	 * The read method on input stream does not handle the timeout 
	 * quite the way I want it.  It varies from implementation.  In 
	 * some cases the threshold is set to 1 byte in some cases it is
	 * set to len bytes.  In this method the threshold is always len
	 * bytes.
	 * 
	 * The thresold is the minimum number of bytes that need to be read
	 * before the method returns.  If this is minimum number is not read
	 * before the timeout then the method returns the number read
	 * at that point.   
	 * 
	 * @param buf
	 * @param off
	 * @param len
	 * @param timeout
	 * @return
	 * @throws IOException
	 */
	public int readBytes(byte [] buf, int off, int len, long timeout)
		throws IOException;
}

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
 * Last modification information:
 * $Revision: 1.10 $
 * $Date: 2007-01-26 16:12:53 $
 * $Author: scytacki $
 *
 * Licence Information
 * Copyright 2004 The Concord Consortium 
*/
package org.concord.sensor;

import org.concord.framework.data.DataDimension;


/**
 * SensorConfiguration
 * 
 * This is a configuration of a a sensor attached to a sensor device.
 * The configuration is probably the result of sending a SensorRequest to
 * the sensor device.
 * 
 * Class name and description
 *
 * Date created: Nov 12, 2004
 *
 * @author scott<p>
 *
 */
public interface SensorConfig
{	
	/*
	 * One question is whether there should be differences
	 * between the distance sensors.  One could be a smartwheel
	 * the other could be an ultra sonic sensor.
	 * 
	 * Or acceleration could be a derivative from a distance sensor
	 * or it could be an dedicated acc sensor.   Which would give
	 * instantaeous acceration.
	 * 
	 * Also several of these quantity can be derived from other 
	 * quantities.  So if they are specified in a experiment config
	 * how will the software know what the author wants.
	 * 
	 * Also the technical hints will depend on how the sensors are 
	 * configured.  So there needs to be conection between this
	 * configuration and the technical hints. 
	 * 
	 * Lets say no for now.  Because we are writing most of this
	 * in house I can delay these decisions until they become
	 * a problem.
	 */

	// This is returned by an device if it knows a sensor
	// is attached but it doesn't know which one.
	public static int QUANTITY_UNKNOWN=             -1;
	
	// Required
	public static int QUANTITY_TEMPERATURE=			0;
	public static int QUANTITY_TEMPERATURE_WAND=    1;
	public static int QUANTITY_LIGHT=				2;
	public static int QUANTITY_GAS_PRESSURE= 		3;
	public static int QUANTITY_VOLTAGE= 			4;
	public static int QUANTITY_FORCE=				5;
	public static int QUANTITY_VELOCITY=			6;
	public static int QUANTITY_RELATIVE_HUMIDITY=	7;

	// Recommended
	public static int QUANTITY_ACCELERATION=		8;
	public static int QUANTITY_PULSE_RATE=			9;
		
	// not required
	public static int QUANTITY_CURRENT=				10;
	public static int QUANTITY_POWER=				11;
	public static int QUANTITY_ENERGY=				12;
	
	public static int QUANTITY_DISTANCE=			13;
	public static int QUANTITY_SOUND_INTENSITY=		14;
	public static int QUANTITY_COMPASS= 			15;
	
	public static int QUANTITY_ANGULAR_VELOCITY=	16;
	
	public static int QUANTITY_WIND_SPEED=			17;
	
	public static int QUANTITY_CO2_GAS=             18;
	
	/**
	 * If the attached sensor can be identified by the device
	 * and it matches the requested sensors this should return
	 * true.
	 * If the device does not support auto id then this should
	 * always return true.
	 * @return
	 */
	public boolean isConfirmed();
		
	/**
	 * This is the type of quantity one of the types above.
	 * @return
	 */
	public int getType();
	
	/**
	 * This is the absolute value of the 
	 * maximum step size between values.  This
	 * is dependent on the units returned by this sensor.  There
	 * will be implicit units for each quantity, and this step
	 * size will be in those units.
	 * 
	 * When the actual config is returned this value should
	 * be the actual step size.  This might not be available 
	 * for a particular sensor in this case it might be -1.
	 * @return
	 */
	public float getStepSize();
	
	/**
	 * This is the port the sensor is or should be plugged into.
	 * This value ranges from 0 on up.  This value might be ignored
	 * if the ports can figure out which sensor is attached.  
	 * 
	 * Also there could be more than one "sensor config" for a single
	 * port.  If the author wants distance and velocity from the same
	 * sensor.
	 * 
	 * The ports in a experiment should be continuous starting at 0. 
	 * The SensorDevice implementation should assign these ports to the 
	 * first available physical port that has the correct type.  Ports
	 * types could be analog, digital or some other special type.
	 *        
	 * @return
	 */
	public int getPort();
	
	/**
	 * This is the name of the port the sensor is plugged into.
	 * It should only be set by the interface.
	 * @return
	 */
	public String getPortName();
	
	/**
	 * This is the name of sensor that is plugged in.  It should
	 * only be set by the interface.
	 * 
	 * @param key
	 * @return
	 */
	public String getName();
	
	/**
	 * The unit of the sensor plugged in, or the unit
	 * of the requested sensor.
	 * 
	 * This value can probably be ignored in the request
	 * because the unit is implicit based on the quantity
	 * however it should be set correctly incase someone 
	 * wants to use it.
	 * @return
	 */
	public DataDimension getUnit();
		
	/**
	 * These parameters can be used to customize a sensor.  If a parameter
	 * is device specific then the key should start with a device specific
	 * id.  
	 * @param key
	 * @return
	 */
	public String getSensorParam(String key);
}

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
 * Created on Dec 10, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.concord.sensor.test;


import org.concord.framework.data.stream.DataListener;
import org.concord.framework.data.stream.DataStreamEvent;
import org.concord.framework.data.stream.DataChannelDescription;
import org.concord.framework.text.UserMessageHandler;
import org.concord.sensor.DeviceConfig;
import org.concord.sensor.ExperimentConfig;
import org.concord.sensor.SensorConfig;
import org.concord.sensor.SensorDataManager;
import org.concord.sensor.SensorDataProducer;
import org.concord.sensor.SensorRequest;
import org.concord.sensor.device.SensorDevice;
import org.concord.sensor.device.impl.DeviceConfigImpl;
import org.concord.sensor.device.impl.InterfaceManager;
import org.concord.sensor.device.impl.JavaDeviceFactory;
import org.concord.sensor.impl.ExperimentRequestImpl;
import org.concord.sensor.impl.SensorRequestImpl;
import org.concord.sensor.impl.SensorUtilJava;
import org.concord.sensor.state.PrintUserMessageHandler;

/**
 * @author Information Services
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestInterfaceManager 
{
	public static void main(String[] args) 
	{
		testTemperature(JavaDeviceFactory.VERNIER_LAB_QUEST);
	}
	
	public static void testTemperature(int deviceId){
		UserMessageHandler messenger = new PrintUserMessageHandler();
		SensorDataManager  sdManager = new InterfaceManager(messenger);
		
		// setup a single config for the passed in device
		DeviceConfig [] dConfigs = new DeviceConfig[1];
		dConfigs[0] = new DeviceConfigImpl(deviceId, null);		
		((InterfaceManager)sdManager).setDeviceConfigs(dConfigs);
				
		// Check what is attached, this isn't necessary if you know what you want
		// to be attached.  But sometimes you want the user to see what is attached
		SensorDevice sensorDevice = sdManager.getSensorDevice();
		ExperimentConfig currentConfig = sensorDevice.getCurrentConfig();
		SensorUtilJava.printExperimentConfig(currentConfig);
		
		
		ExperimentRequestImpl request = new ExperimentRequestImpl();
		request.setPeriod(0.1f);
		request.setNumberOfSamples(-1);
		
		SensorRequestImpl sensor = new SensorRequestImpl();
		sensor.setDisplayPrecision(-2);
		sensor.setRequiredMax(Float.NaN);
		sensor.setRequiredMin(Float.NaN);
		sensor.setPort(0);
		sensor.setStepSize(0.1f);
		sensor.setType(SensorConfig.QUANTITY_TEMPERATURE);

		request.setSensorRequests(new SensorRequest [] {sensor});
				
		SensorDataProducer sDataProducer = 
		    sdManager.createDataProducer();
		sDataProducer.configure(request);
		sDataProducer.addDataListener(new DataListener(){
			public void dataReceived(DataStreamEvent dataEvent)
			{
				int numSamples = dataEvent.getNumSamples();
				float [] data = dataEvent.getData();
				if(numSamples > 0) {
					System.out.println("" + numSamples + " " +
								data[0]);
					System.out.flush();
				} 
				else {
					System.out.println("" + numSamples);
				}
			}

			public void dataStreamEvent(DataStreamEvent dataEvent)
			{				
				String eventString;
				int eventType = dataEvent.getType();
				
				if(eventType == 1001) return;
				
				switch(eventType) {
					case DataStreamEvent.DATA_DESC_CHANGED:
						eventString = "Description changed";
					break;
					default:
						eventString = "Unknown event type";					
				}
				
				System.out.println("Data Event: " + eventString); 
			}
		});
		
		sDataProducer.start();
		
		System.out.println("started device");
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		sDataProducer.stop();
		
		sDataProducer.close();
		
		System.exit(0);		
	}

	/**
	 * Test collecting data from a Temperature and a Light probe simultaneously
	 * from a device which supports multiple sensors.
	 */
	public static void testTemperatureAndLight(int deviceId){
		UserMessageHandler messenger = new PrintUserMessageHandler();
		SensorDataManager  sdManager = new InterfaceManager(messenger);
		
		// setup a single config for the passed in device
		DeviceConfig [] dConfigs = new DeviceConfig[1];
		dConfigs[0] = new DeviceConfigImpl(deviceId, null);
		((InterfaceManager)sdManager).setDeviceConfigs(dConfigs);
				
		// Check what is attached.
		SensorDevice sensorDevice = sdManager.getSensorDevice();
		ExperimentConfig currentConfig = sensorDevice.getCurrentConfig();
		SensorUtilJava.printExperimentConfig(currentConfig);
		
		SensorConfig [] sensorConfigArr = currentConfig.getSensorConfigs();
		if(sensorConfigArr.length < 2) {
			String mesg = "Must have at least two sensors attached, found: " + sensorConfigArr.length;
			System.out.println(mesg);
			throw new RuntimeException(mesg);
		}

		SensorConfig sensorConfigTemp = null;
		SensorConfig sensorConfigLight = null;

		for(Integer i=0; i<sensorConfigArr.length; i++){
			if(sensorConfigArr[i].getType() == SensorConfig.QUANTITY_TEMPERATURE) {
				sensorConfigTemp = sensorConfigArr[i];
				break;
			}
		}
		for(Integer i=0; i<sensorConfigArr.length; i++){
			if(sensorConfigArr[i].getType() == SensorConfig.QUANTITY_LIGHT) {
				sensorConfigLight = sensorConfigArr[i];
				break;
			}
		}
		
		if(sensorConfigTemp == null) {
			String mesg = "Must have at at least one temperature probe attached";
			System.out.println(mesg);
			throw new RuntimeException(mesg);
		}
		
		if(sensorConfigLight == null) {
			String mesg = "Must have at at least one light probe attached";
			System.out.println(mesg);
			throw new RuntimeException(mesg);
		}

		ExperimentRequestImpl request = new ExperimentRequestImpl();
		request.setPeriod(0.1f);
		request.setNumberOfSamples(-1);
		
		SensorRequestImpl sensorTemp = new SensorRequestImpl();
		sensorTemp.setDisplayPrecision(-2);
		sensorTemp.setRequiredMax(50);
		sensorTemp.setRequiredMin(-20);
		sensorTemp.setUnit(sensorConfigTemp.getUnit());
		sensorTemp.setPort(sensorConfigTemp.getPort());
		sensorTemp.setStepSize(0.1f);
		sensorTemp.setType(sensorConfigTemp.getType());

		SensorRequestImpl sensorLight = new SensorRequestImpl();
		sensorLight.setDisplayPrecision(-2);
		sensorLight.setRequiredMax(100000);
		sensorLight.setRequiredMin(0);
		sensorLight.setUnit(sensorConfigLight.getUnit());
		sensorLight.setPort(sensorConfigLight.getPort());
		sensorLight.setStepSize(0.1f);
		sensorLight.setType(sensorConfigLight.getType());

		request.setSensorRequests(new SensorRequest [] {sensorTemp, sensorLight});
				
		SensorDataProducer sDataProducer =
		    sdManager.createDataProducer();
		sDataProducer.configure(request);
		sDataProducer.addDataListener(new DataListener(){
			public void dataReceived(DataStreamEvent dataEvent)
			{
				int numSamples = dataEvent.getNumSamples();
				DataChannelDescription channel0 = dataEvent.getDataDescription().getChannelDescription(0);
				DataChannelDescription channel1 = dataEvent.getDataDescription().getChannelDescription(1);
				float [] data = dataEvent.getData();
				if(numSamples > 0) {
					System.out.println(
							String.format("samples: %d: %s: %3.1f %s, %s: %4.2f %s",
									numSamples,
									channel0.getName(), data[0], channel0.getUnit().getDimension(),
									channel1.getName(), data[1], channel1.getUnit().getDimension()
							));

					System.out.flush();
				}
				else {
					System.out.println("" + numSamples);
				}
			}

			public void dataStreamEvent(DataStreamEvent dataEvent)
			{
				String eventString;
				int eventType = dataEvent.getType();
				
				if(eventType == 1001) return;
				
				switch(eventType) {
					case DataStreamEvent.DATA_DESC_CHANGED:
						eventString = "Description changed";
					break;
					default:
						eventString = "Unknown event type";
				}
				
				System.out.println("Data Event: " + eventString);
			}
		});
		
		sDataProducer.start();
		
		System.out.println("started device");
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		sDataProducer.stop();
		
		sDataProducer.close();
		
		System.exit(0);
	}

}
package org.concord.sensor.device.impl;

import org.concord.framework.data.stream.DataChannelDescription;
import org.concord.framework.data.stream.DataListener;
import org.concord.framework.data.stream.DataStreamDescription;
import org.concord.framework.data.stream.DataStreamEvent;
import org.concord.framework.text.UserMessageHandler;
import org.concord.sensor.ExperimentConfig;
import org.concord.sensor.ExperimentRequest;
import org.concord.sensor.SensorConfig;
import org.concord.sensor.SensorDataProducer;
import org.concord.sensor.SensorRequest;
import org.concord.sensor.device.DeviceReader;
import org.concord.sensor.device.SensorDevice;

import waba.sys.Vm;

public class SensorDataProducerImpl
	implements SensorDataProducer, DeviceReader
{
	public int		startTimer =  0;
	protected Ticker ticker = null;
	protected UserMessageHandler messageHandler;
	protected 		waba.util.Vector 	dataListeners = null;
	
	protected waba.util.Vector sensorConfigs = new waba.util.Vector();
	
	public DataStreamDescription dDesc = new DataStreamDescription();
	public DataStreamEvent	processedDataEvent = new DataStreamEvent();

	protected float [] processedData;
	private static final int DEFAULT_BUFFERED_SAMPLE_NUM = 1000;
	private boolean prepared;
	
	int timeWithoutData = 0;
	protected String [] okOptions = {"Ok"};
	protected String [] continueOptions = {"Continue"};	
	public final static int DATA_TIME_OUT = 40;
	private boolean inDeviceRead;
	private int totalDataRead;
	private SensorDevice device;

	public SensorDataProducerImpl(SensorDevice device, Ticker t, UserMessageHandler h)
	{
		this.device = device;
		
		ticker = t;
		ticker.setInterfaceManager(this);
		
		messageHandler = h;
		
		processedData = new float[DEFAULT_BUFFERED_SAMPLE_NUM];
		processedDataEvent.setData(processedData);
	}

	public void tick()
	{
	    int ret;

	    // reset the total data read so we can track data coming from
	    // flushes
	    totalDataRead = 0;

	    // track when we are in the device read so if flush
	    // is called outside of this we can complain
	    inDeviceRead = true;
	    ret = device.read(processedData, 0, dDesc.getChannelsPerSample(),
	    		this);
	    inDeviceRead = false;
	    
	    if(ret < 0) {
			stop();
			String message = device.getErrorMessage(ret);
			messageHandler.showOptionMessage(message, "Interface Error",
					continueOptions, continueOptions[0]);
			return;
	    }
	    
	    totalDataRead += ret;
	    if(totalDataRead == 0) {
			// we didn't get any data. 
	    	// keep track of this so we can report there is
	    	// is a problem.  If this persists too long
			timeWithoutData++;
			if(timeWithoutData > DATA_TIME_OUT){
				stop();
				messageHandler.showOptionMessage("Serial Read Error: " +
										 "possibly no interface " +
										 "connected", "Interface Error",
										 continueOptions, continueOptions[0]);					
			}
			return;
	    }
	    
	    // We either got data or there was an error
		timeWithoutData = 0;

		if(ret > 0){
			// There was some data that didn't get flushed during the read
			// so send this out to our listeners.
			processedDataEvent.setNumSamples(ret);
			notifyDataListenersReceived(processedDataEvent);				
		} 	
	}
	
	/*
	 * This is a helper method for slow devices.  It be called within deviceRead.
	 * If the data should be written into the values array passed to deviceRead
	 * the values read from the offset passed in until offset+numSamples will 
	 * be attempted to be flushed.
	 * the method returns the new offset into the data. 
	 * 
	 * You don't need to call this, but if your device is going to work on a slow
	 * computer (for example an older palm) then you will probably have to use
	 * this method.  Otherwise you will build up too much data to be processed later
	 * and then while all that data is being processed the serial buffer will overflow.
	 * 
	 * Instead this method will partially process the data.  This will give the device
	 * a better chance to "get ahead" of the serial buffer.  Once the device has gotten
	 * far enough ahead of the serial buffer it can return from deviceRead the
	 * data will be fully processed.
	 */
	public int flushData(int numSamples)
	{
		if(!inDeviceRead) {
			// error we need an assert here but we are in waba land 
			// so no exceptions or asserts for now we'll print 
			// but later we can force a null pointer exception
			System.err.println("calling flush outside of deviceRead");
		}
		
		processedDataEvent.setNumSamples(numSamples);
		notifyDataListenersReceived(processedDataEvent);
		
		totalDataRead += numSamples;
		
		return 0;
	}
	
	protected int getBufferedSampleNum()
	{
		return DEFAULT_BUFFERED_SAMPLE_NUM;
	}
	
	/**
	 * subclasses should use deviceConfig not configure
	 * to setup their devices.
	 */
	public final ExperimentConfig configure(ExperimentRequest request)
	{
		ExperimentConfig result = device.configure(request);
		
		if(result == null) {
			return null;
		}
		
		SensorRequest [] sensRequests =  null;
		if(request != null) {
			sensRequests = request.getSensorRequests();
		}
			
		SensorConfig [] sensConfigs = result.getSensorConfigs();
		dDesc.setChannelsPerSample(sensConfigs.length);
		dDesc.setDt(result.getPeriod());
		dDesc.setDataType(DataStreamDescription.DATA_SEQUENCE);
		
		for(int i=0; i<sensConfigs.length; i++) {
			DataChannelDescription chDescrip = new DataChannelDescription();
			chDescrip.setName(sensConfigs[i].getName());
			chDescrip.setUnit(sensConfigs[i].getUnit());

			if(sensRequests != null) {
				chDescrip.setPrecision(sensRequests[i].getDisplayPrecision());
			}
			
			chDescrip.setNumericData(true);
		}
		
		return result;
	}
	
	public final void start()
	{
		device.start();
		
		timeWithoutData = 0;

		startTimer = Vm.getTimeStamp();
		ticker.startTicking(device.getRightMilliseconds());

	}
	
	/**
	 *  This doesn't really need to do anything if
	 * the sensor isn't storing any cache.
	 */
	public final void reset()
	{		
	}
	
	public final void stop()
	{
		boolean ticking = ticker.isTicking();

		// just to make sure
		// even if we are not ticking just incase
		ticker.stopTicking();

		device.stop(ticking);
	}

	
	/* (non-Javadoc)
	 * @see org.concord.sensor.SensorDataProducer#isAttached()
	 */
	public boolean isAttached()
	{
		// TODO Auto-generated method stub
		return device.isAttached();
	}
	
	
	/* (non-Javadoc)
	 * @see org.concord.sensor.SensorDataProducer#canDetectSensors()
	 */
	public boolean canDetectSensors()
	{
		// TODO Auto-generated method stub
		return device.canDetectSensors();
	}
	
	public ExperimentConfig getCurrentConfig()
	{
		return device.getCurrentConfig();
	}
	
	public void close()
	{
		device.close();
	}
	
	public final DataStreamDescription getDataDescription()
	{
		return dDesc;
	}
		
	public void addDataListener(DataListener l){
		if(dataListeners == null){ dataListeners = new waba.util.Vector();	   }
		if(dataListeners.find(l) < 0){
			dataListeners.add(l);
		}
	}
	
	public void removeDataListener(DataListener l){
		if(dataListeners == null) return;
		int index = dataListeners.find(l);
		if(index >= 0) dataListeners.del(index);
		if(dataListeners.getCount() == 0) dataListeners = null;
	}

	public void notifyDataListenersEvent(DataStreamEvent e){
		if(dataListeners == null) return;
		for(int i = 0; i < dataListeners.getCount(); i++){
			DataListener l = (DataListener)dataListeners.get(i);
			l.dataStreamEvent(e);
		}
	}

	public void notifyDataListenersReceived(DataStreamEvent e)
	{
		if(dataListeners == null) return;
		for(int i = 0; i < dataListeners.getCount(); i++){
			DataListener l = (DataListener)dataListeners.get(i);
			l.dataReceived(e);
		}
	}
}

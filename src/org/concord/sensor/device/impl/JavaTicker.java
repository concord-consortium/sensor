/*
 * Created on Jun 19, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.concord.sensor.device.impl;

import org.concord.sensor.impl.SensorDataProducerImpl;
import org.concord.sensor.impl.TickListener;
import org.concord.sensor.impl.Ticker;


/**
 * @author scott
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class JavaTicker extends Thread
	implements Ticker
{
	int millis;
	boolean ticking = false;
	boolean started = false;
	TickListener tickListener;
		
	/* (non-Javadoc)
	 * @see org.concord.sensor.Ticker#start(int)
	 */
	synchronized public void startTicking(int millis) {
		this.millis = millis;
		ticking = true;
		if(started) {
			notify();
		} else {
			started = true;
			start();
		}
	}

	synchronized public void stopTicking() 
	{
		ticking = false;
	}
	
	/* (non-Javadoc)
	 * @see org.concord.sensor.Ticker#isTicking()
	 */
	synchronized public boolean isTicking() {
		return ticking;
	}

	/* (non-Javadoc)
	 * @see org.concord.sensor.Ticker#setInterfaceManager(org.concord.sensor.InterfaceManager)
	 */
	synchronized public void setTickListener(TickListener tListener) {
	    tickListener = tListener;
	}

	/* (non-Javadoc)
	 * @see org.concord.sensor.Ticker#getInterfaceManager()
	 */
	synchronized public TickListener getTickListener() {
	    return tickListener;
	}

	/* (non-Javadoc)
	 * @see org.concord.sensor.Ticker#createNew()
	 */
	public Ticker createNew() {
		return new JavaTicker();
	}
	
	synchronized public void run()
	{
		while(true) {
			if(!ticking) {
				try {
					wait();
				} 
				catch(InterruptedException e) {					
					e.printStackTrace();
				}
			}
			
			tickListener.tick();
			
			try {
				// We wait so that we release the lock
				wait(millis);										
			}
			catch(InterruptedException e) {				
			}
		}
	}
}

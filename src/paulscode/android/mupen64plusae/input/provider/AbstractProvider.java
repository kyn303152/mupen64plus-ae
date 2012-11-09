/**
 * Mupen64PlusAE, an N64 emulator for the Android platform
 * 
 * Copyright (C) 2012 Paul Lamb
 * 
 * This file is part of Mupen64PlusAE.
 * 
 * Mupen64PlusAE is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * Mupen64PlusAE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with Mupen64PlusAE. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Authors: littleguy77
 */
package paulscode.android.mupen64plusae.input.provider;

import paulscode.android.mupen64plusae.input.map.InputMap;
import paulscode.android.mupen64plusae.util.SubscriptionManager;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * The base class for transforming arbitrary input data into a common format.
 * 
 * @see KeyProvider
 * @see AxisProvider
 * @see SensorProvider
 * @see LazyProvider
 * @see InputMap
 */
public abstract class AbstractProvider
{
    /**
     * The interface for listening to a provider.
     */
    public interface Listener
    {
        /**
         * Called when a single input has been dispatched.
         * 
         * @param inputCode The standardized input code that was dispatched.
         * @param strength The input strength, between 0 and 1, inclusive.
         */
        public void onInput( int inputCode, float strength );
        
        /**
         * Called when multiple inputs have been dispatched simultaneously.
         * 
         * @param inputCodes The standardized input codes that were dispatched.
         * @param strengths The input strengths, between 0 and 1, inclusive.
         */
        public void onInput( int[] inputCodes, float[] strengths );
    }
    
    /** Listener management. */
    private SubscriptionManager<AbstractProvider.Listener> mPublisher;
    
    /**
     * Instantiates a new abstract provider.
     */
    protected AbstractProvider()
    {
        mPublisher = new SubscriptionManager<AbstractProvider.Listener>();
    }
    
    /**
     * Registers a listener to start receiving input notifications.
     * 
     * @param listener The listener to register.
     */
    public void registerListener( AbstractProvider.Listener listener )
    {
        mPublisher.subscribe( listener );
    }
    
    /**
     * Unregisters a listener to stop receiving input notifications.
     * 
     * @param listener The listener to unregister.
     */
    public void unregisterListener( AbstractProvider.Listener listener )
    {
        mPublisher.unsubscribe( listener );
    }
    
    /**
     * Unregisters all listeners.
     */
    public void unregisterAllListeners()
    {
        mPublisher.unsubscribeAll();
    }
    
    /**
     * Gets the human-readable name of the input.
     * 
     * @param inputCode The standardized input code.
     * @return The name of the input.
     */
    @TargetApi( 12 )
    public static String getInputName( int inputCode )
    {
        // TODO: Localize strings.
        boolean isHoneycombMR1 = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1;
        
        if( inputCode > 0 )
        {
            if( isHoneycombMR1 )
                return "KEYCODE_" + inputCode;
            else
                return KeyEvent.keyCodeToString( inputCode );
        }
        else if( inputCode < 0 )
        {
            int axis = AxisProvider.inputToAxisCode( inputCode );
            String direction = AxisProvider.inputToAxisDirection( inputCode )
                    ? " (+)"
                    : " (-)";
            if( isHoneycombMR1 )
                return "AXIS_" + axis + direction;
            else
                return MotionEvent.axisToString( axis ) + direction;
        }
        else
            return "NULL";
    }
    
    /**
     * Gets the human-readable name of the input, appended with strength information.
     * 
     * @param inputCode The standardized input code.
     * @param strength The input strength, ranging from 0 to 1, inclusive.
     * @return The name of the input.
     */
    public static String getInputName( int inputCode, float strength )
    {
        return getInputName( inputCode ) + ( inputCode == 0
                ? ""
                : String.format( " %4.2f", strength ) );
    }
    
    /**
     * Notifies listeners about a single input. Subclasses should invoke this method to publish
     * their input data.
     * 
     * @param inputCode The standardized input code.
     * @param strength The input strength, ranging from 0 to 1, inclusive.
     */
    protected void notifyListeners( int inputCode, float strength )
    {
        for( Listener listener : mPublisher.getSubscribers() )
            listener.onInput( inputCode, strength );
    }
    
    /**
     * Notifies listeners about a batch of inputs. Subclasses should invoke this method to publish
     * their input data.
     * 
     * @param inputCodes The standardized input codes.
     * @param strengths The input strengths, ranging from 0 to 1, inclusive.
     */
    protected void notifyListeners( int[] inputCodes, float[] strengths )
    {
        for( Listener listener : mPublisher.getSubscribers() )
            listener.onInput( inputCodes.clone(), strengths.clone() );
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

/**
 *
 * @author GBEMIRO
 */
public class Utils {
        
     /**
     * @param value The double value to be rounded
     * @param precision The precision.. e.g passing 1 means to 1 decimal place,
     * passing 2 means to 2 decimal places, etc.
     * @return the value rounded to the specified precision
     */
    public static final double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
    
}

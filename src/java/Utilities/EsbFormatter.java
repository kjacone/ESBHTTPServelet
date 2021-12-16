/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Utilities;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
/**
 *
 * @author HESBON
 */
public class EsbFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
//        return record.getThreadID()+"::"+record.getSourceClassName()+"::"
//                +record.getSourceMethodName()+"::"
//                +new Date(record.getMillis())+"::"
//                +record.getMessage()+"\n";
     ///////////////////////////////////////////////////////////////////////////////////////
       
        SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss.SSS");
        return format.format(new Date()) + "::"
                + record.getMessage() + "\n";
    }

}

package ru.vdovin.jsonParser;

import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by Юыху on 25.11.2015.
 */
public class JsonParseReader {

    @NotNull
    private Reader reader;

    private int element;

    public JsonParseReader(Reader reader){
        this.reader=reader;
    }

    public int getElement() {
        return element;
    }

    public void setElement(int element) {
        this.element = element;
    }

    public void nextElement(){
        try{
            setElement(this.reader.read());
        }catch (IOException e) {
            throw new IllegalStateException("Parse Error.", e);
        }
    }


    public Double doubleValue(String value){
        return Double.parseDouble(value);
    }

    public int integerValue(String value){
        return Integer.parseInt(value);
    }

    public long longValue(String value){
        return Long.parseLong(value);
    }


    public boolean isLong(String value) {
        try {
            longValue(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isDouble(String value) {
        try {
            doubleValue(value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean isInteger(String value) {
        try {
            integerValue(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}

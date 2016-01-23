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


}

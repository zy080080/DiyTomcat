package com.zzy.diytomcat.exception;

public class WebConfigDuplicatedException extends Exception{
    // web.xmlに同じservletが複数あるとき
    public WebConfigDuplicatedException(String message){
        super(message);
    }
}

package com.lakomy.tomasz.androidpingclient;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

public class CustomStringRequest extends StringRequest {
    private Priority priority = Priority.LOW;

    CustomStringRequest(String url, Response.Listener successHandler, Response.ErrorListener errorListener) {
        super(Request.Method.POST, url, successHandler, errorListener);
    }

    @Override
    public Priority getPriority(){
        return priority;
    }

    public void setPriority(Priority priority){
        this.priority = priority;
    }
}

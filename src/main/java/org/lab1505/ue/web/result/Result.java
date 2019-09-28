package org.lab1505.ue.web.result;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;

public class Result<T>{

    private int code;
    private String msg;
    private T body;

    private Result(int code,String msg){
        this.code = code;
        this.msg = msg;
        this.body = null;
    }

    private Result(T body){
        this.code = 0;
        this.msg = "Success!";
        this.body = body;
    }

    public Result(CodeMsg codeMsg) {
        if(codeMsg==null){
            return;
        }
        this.code = codeMsg.getCode();
        this.msg = codeMsg.getMsg();
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getBody() {
        return body;
    }

    public static <T> Result test(){
        return new Result(CodeMsg.TEST);
    }

    public static <T> Result error(CodeMsg msg){
        return new Result(CodeMsg.ERROR);
    }

    public static <T> Result success(T body){
        return new Result(body);
    }
}

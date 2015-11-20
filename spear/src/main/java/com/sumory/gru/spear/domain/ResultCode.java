package com.sumory.gru.spear.domain;

public class ResultCode {
    public final static int SUCCESS = 0;//成功
    public final static int FAIL = -1;//失败
    public final static int DEFAULT_ERROR = 1;//默认错误

    public final static int SYSTEM_ERROR = 1001;//系统错误
    public final static int LOGIC_ERROR = 1002;//逻辑错误
    public final static int PARAMS_ERROR = 1003;//参数错误
}
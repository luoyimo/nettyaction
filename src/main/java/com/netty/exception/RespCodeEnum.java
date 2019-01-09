package com.netty.exception;

/**
 * @Author hu
 * @Description: 错误类型统一定义
 * @Date Create In 15:32 2019/1/8 0008
 */
public enum RespCodeEnum {

    SUCCESS(10000, "success"),
    REPEAT_LOGIN(10001, "重复登录"),
    REQUIRE_CLIENTID(10002, "clientId不能为空"),
    NOT_LOGIN(10003, "未登录");

    private int respCode;
    private String respInfo;

    RespCodeEnum(int respCode, String respInfo) {
        this.respCode = respCode;
        this.respInfo = respInfo;
    }

    public int getRespCode() {
        return respCode;
    }

    public void setRespCode(int respCode) {
        this.respCode = respCode;
    }

    public String getRespInfo() {
        return respInfo;
    }

    public void setRespInfo(String respInfo) {
        this.respInfo = respInfo;
    }}

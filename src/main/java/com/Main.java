package com;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;

import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.*;

import static java.time.Year.now;

public class Main {
    private static final HashMap<String, String> sendMap = new HashMap<>();

    /**
     * wechat appid
     */
    private static final String appid = "wxe9ea151f6c2c20f5";

    /**
     * wechat appSecret
     */
    private static final String appSecret = "2246fa905c736f0a0936aad4024f70a6";

    /**
     * wechat token get url
     */
    private static final String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential";

    /**
     * wechat message send post url
     */
    private static final String sendUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send";

    /**
     * weather API url
     */
    private static final String weatherUrl = "https://v0.yiketianqi.com/api?unescape=1&version=v62&appid=31528551&appsecret=NNXe3P39&ext=&cityid=101280601&city=";

    /**
     * love message API url
     */
    private static final String whispersUrl = "https://api.1314.cool/words/api.php?return=json";


    /**
     * data format
     */
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    /**
     * cache love date
     */
    private static Date loveDate;
    /**
     * cache birthday
     */
    private static Date birthday;

    private static Date carBills;

    private static Date houseBills;

    private static Date creditBills;


    /**
     * init send map
     */
    public static void initSendMap() {
        //openid <-> template-id
        sendMap.put("o5gfI6GpqpCOcfFJng4A0U8Rnq2w", "AYGm5N2HXGDtUx91aWiDQAZ8Eopo3fadsOGHWfO3o_g");
        sendMap.put("o5gfI6EVCDjT5MrTqUqNSmt1v6y8", "AYGm5N2HXGDtUx91aWiDQAZ8Eopo3fadsOGHWfO3o_g");
    }

    /**
     * main
     *
     * @param args param
     */
    @SneakyThrows
    public static void main(String[] args) {

        System.out.println("run");
        //init send map
        initSendMap();

        //init love day and birthday
        loveDate = dateFormat.parse("2019-07-23");


        birthday = calculateDate(setDate(7, 1), Calendar.YEAR, 1);
        carBills = calculateDate(setDate(new Date().getMonth(), 15), Calendar.MONTH, 1);
        houseBills = calculateDate(setDate(new Date().getMonth(), 21), Calendar.MONTH, 1);
        creditBills = calculateDate(setDate(new Date().getMonth(), 23), Calendar.MONTH, 1);


        Map<String, Object> params = new HashMap<>();

        params.put("appid", appid);
        params.put("secret", appSecret);
        System.out.println("start");
        JSONObject entries = JSONUtil.parseObj(HttpUtil.get(url, params));
        System.out.println("end");
        System.out.println(entries);
        String token = entries.getStr("access_token");
        System.out.println("Token " + token);
        send(token);
        StaticLog.info("Success send.");

    }

    public static void send(String token) {
        for (String openid : sendMap.keySet()) {
            //join token
            String realUrl = sendUrl + "?access_token=" + token;

            //generate json data
            JSONObject json = generateJsonData(openid);

            //post
            HttpUtil.post(realUrl, json.toString());

            //log
            StaticLog.info("send data " + json);

            StaticLog.info("Send to " + openid + " success.");
        }
    }

    @SneakyThrows
    public static JSONObject generateJsonData(String openid) {
        JSONObject json = JSONUtil.createObj();
        json.set("touser", openid);
        json.set("template_id", sendMap.get(openid));

        JSONObject arrayObject = JSONUtil.createObj();

        arrayObject.set("first", vcObject("小胖胖来了呀～～～～"));

        JSONObject weatherObj = getWeather("深圳");
        arrayObject.set("keyword1", vcObject(weatherObj.getStr("wea")));
        arrayObject.set("keyword2", vcObject(weatherObj.getStr("tem")));

        String loveDays = DateUtil.between(loveDate, new Date(), DateUnit.DAY) + "";
        if (birthday.getTime() < new Date().getTime()) {
            birthday.setYear(birthday.getYear() + 1);
        }
        String birthdayDays = DateUtil.between(new Date(), birthday, DateUnit.DAY) + "";
        arrayObject.set("keyword3", vcObject(loveDays));
        arrayObject.set("keyword4", vcObject(birthdayDays));

        String carLoan = DateUtil.between(carBills, new Date(), DateUnit.DAY) + "";
        arrayObject.set("keyword5", vcObject(carLoan));
        String houseLoan = DateUtil.between(houseBills, new Date(), DateUnit.DAY) + "";
        arrayObject.set("keyword6", vcObject(houseLoan));
        String creditCardLoan = DateUtil.between(creditBills, new Date(), DateUnit.DAY) + "";
        arrayObject.set("keyword7", vcObject(creditCardLoan));
        JSONObject qinghuaObj = getWhispers();
        arrayObject.set("remark", vcObject("\n" + qinghuaObj.getStr("word"), "#f00"));
        json.set("data", arrayObject);

        return json;
    }


    public static JSONObject vcObject(String value) {
        return vcObject(value, "#000");
    }

    public static JSONObject vcObject(String value, String color) {
        JSONObject object = JSONUtil.createObj();
        object.set("value", value);
        object.set("color", color);
        return object;
    }


    public static JSONObject getWeather(String area) {
        return JSONUtil.parseObj(HttpUtil.get(weatherUrl + area));
    }


    public static JSONObject getWhispers() {
        return JSONUtil.parseObj(HttpUtil.get(whispersUrl).replace("<br>", ""));
    }

    private static Date setDate(int month, int day) {
        GregorianCalendar gc = new GregorianCalendar();
        Year now = now();
        gc.set(Calendar.YEAR, now.getValue());//设置年
        gc.set(Calendar.MONTH, month);//这里0是1月..以此向后推
        gc.set(Calendar.DAY_OF_MONTH, day);//设置天
        return gc.getTime();
    }

    private static Date calculateDate(Date date, int flied, int offset) {
        long gap = DateUtil.between(new Date(), date, DateUnit.DAY);
        if (date.getTime() < new Date().getTime() && gap != 0) {
            switch (flied) {
                case Calendar.YEAR:
                    date.setYear(date.getYear() + offset);
                case Calendar.MONTH:
                    date.setMonth(date.getMonth() + offset);
            }
        }
        return date;
    }

}

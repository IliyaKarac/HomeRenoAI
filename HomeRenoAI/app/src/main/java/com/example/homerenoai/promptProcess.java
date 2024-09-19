package com.example.homerenoai;

import android.util.Log;

import java.util.ArrayList;

public class promptProcess {

    //loras
    ArrayList<String> loras = new ArrayList<>();
    {
        //bathroom
        loras.add(" <lora:Modern Bathroom 01:1>");
        //bedroom
        loras.add(" <lora:Loratrain:1>");
        loras.add(" <lora:jiudian:1>");
        loras.add(" <lora:LOVEHOTEL_C_SD15_V1:1>");
        //bookshelf
        loras.add(" <lora:bookpron3:1>");
        //endtable
        loras.add(" <lora:end_table:1>");
        //kitchen
        loras.add(" <lora:Modern_Kitchen_01:1>");
        //lamp
        loras.add(" <lora:民宿1:1>");
        //livingroom
        loras.add(" <lora:Cozy:1>");
        loras.add(" <lora:Model Livingroom 61:1>");
        loras.add(" <lora:20231205-1701765319406:1>");
        //mall
        loras.add(" <lora:wayfu_furniture_v2:1>");
        //office
        loras.add(" <lora:20240219-1708311549958:1>");
        loras.add(" <lora:办公室内30:1>");
        //outside
        loras.add(" <lora:Outdoor_Couch:1>");
        //painting
        loras.add(" <lora:Light oil painting_20231025095300:1>");
        //style
        loras.add(" <lora:JML_SD15_V1:1>");
        loras.add(" <lora:artek_5400_lora:1>");
        //window
        loras.add(" <lora:sitting_on_window:1>");

    }


    //colors
    ArrayList<String> colors = new ArrayList<>();
    {
        colors.add("red");
        colors.add("orange");
        colors.add("yellow");
        colors.add("green");
        colors.add("blue");
        colors.add("purple");
        colors.add("pink");
        colors.add("brown");
        colors.add("gray");
        colors.add("black");
        colors.add("white");


    }



    //edit positive prompt
    public String processPos(String originStr){
        String returnStr = "";
        String[] originList = originStr.split(",");
        ArrayList<String> newList = new ArrayList<>();
        for(String x : originList){
            newList.add(x.strip().toLowerCase());
        }
        ArrayList<String> added = adder(newList);
        String[] split = originStr.split(",");
        String firstStr = "(" + split[0] + ")";




        returnStr = firstStr + ", " + originStr + ", " + added.toString().substring(1, added.toString().length()-1);
        return returnStr;
    }

    //edit negative prompt
    public String processNeg(String originStr){
        String returnStr = originStr + ", " + "Worst quality, Low quality, out of focus";


        return returnStr;
    }


    public ArrayList<String> adder(ArrayList<String> original) {
        ArrayList<String> returnList = new ArrayList<>();
        boolean done = false;

        for (String x : original){
            for (String y : colors){
                if(x.equals(y)){
                    returnList.add("((((" + x + "))))");
                }
            }
        }

        //key word finder
        for(String x : original){
            if(!done){
                if (x.equals("bathroom") || x.equals("toilet") || x.equals("toilet bowl") ||  x.equals("bath") || x.equals("bathtub") || x.equals("shower") || x.equals("shower stall")) {
                    returnList.add(loras.get(0));
                    returnList.add("realistic");
                    done = true;
                }
                if (x.equals("bedroom") || x.equals("bed") || x.equals("sleeping pillow")) {
                    returnList.add(loras.get(1));
                    //returnList.add(loras.get(2));
                    //returnList.add(loras.get(3));
                    returnList.add("realistic");
                    done = true;

                }
                if (x.equals("bookshelf")) {
                    returnList.add(loras.get(4));
                    returnList.add("realistic");
                    done = true;
                }
                if (x.equals("end table") || x.equals("nigth stand")) {
                    returnList.add(loras.get(5));
                    returnList.add("realistic");
                    done = true;
                }
                if (x.equals("kitchen") || x.equals("refrigerator") || x.equals("sink") || x.equals("oven") || x.equals("stove")){
                    returnList.add(loras.get(6));
                    returnList.add("realistic");
                    done = true;
                }
                if (x.equals("lamp")){
                    returnList.add(loras.get(7));
                    returnList.add("realistic");
                    done = true;
                }
                if (x.equals("living room") || x.equals("couch") || x.equals("sofa") || x.equals("coffee table") || x.equals("ottoman") || x.equals("love seat") || x.equals("cushion")){
                    returnList.add(loras.get(8));
                    //returnList.add(loras.get(9));
                    //returnList.add(loras.get(10));
                    returnList.add("realistic");
                    done = true;
                }
                if (x.equals("mall") || x.equals("warehouse")){
                    returnList.add(loras.get(11));
                    returnList.add("realistic");
                    done = true;
                }
                if (x.equals("office") || x.equals("office chair") || x.equals("office desk") || x.equals("cubicle")){
                    returnList.add(loras.get(12));
                    //returnList.add(loras.get(13));
                    returnList.add("realistic");
                    done = true;
                }
                if (x.equals("outside") || x.equals("outdoors") || x.equals("outdoor") || x.equals("patio")){
                    returnList.add(loras.get(14));
                    returnList.add("realistic");
                    done = true;
                }
                if (x.equals("painting")){
                    returnList.add(loras.get(14));
                    done = true;
                }
                if (x.equals("japanese") || x.equals("modern japanese")){
                    returnList.add(loras.get(15));
                    returnList.add("realistic");
                    done = true;
                }
                if (x.equals("modern") || x.equals("modern style")){
                    returnList.add(loras.get(16));
                    returnList.add("realistic");
                    done = true;
                }
                if (x.equals("window")){
                    returnList.add(loras.get(17));
                    returnList.add("realistic");
                    done = true;
                }

            }


        }



        return returnList;
    }
}

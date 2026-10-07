package com.bulkheadengineering;

public final class DoorLabels {
 public static final String DEFAULT_MAIN="C-1";
 public static final String DEFAULT_SUB="LEVEL 28";
 public static final int MAIN_MAX=24;
 public static final int SUB_MAX=32;
 private DoorLabels(){}

 public static String clean(String value,int max){
  if(value==null)return "";
  StringBuilder out=new StringBuilder();
  for(int i=0;i<value.length()&&out.length()<max;i++){
   char c=value.charAt(i);
   if(!Character.isISOControl(c))out.append(c);
  }
  return out.toString().trim();
 }
}

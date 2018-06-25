package ru.sbt.integration.orchestration.projects.teststruct;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class DestinationClass {
    public String parentString1;
    public String parentString2;
    private String string3;
    private String string4;

    public Date date;
    public Date date2;
    public Calendar calendar;

    public HashMap<String, String> parentHashMap2 = new HashMap<>();
    public HashMap<String, InnerDestinationClass> parentHashMapHard = new HashMap<>();
    public int parentInt2;
    public InnerDestinationClass innerDestinationClass = new InnerDestinationClass();
    public List<Integer> integers2 = new ArrayList<>();
    List<InnerDestinationClass> parentDeepArrayDest = new ArrayList<>();
    public List<String> strings2 = new ArrayList<>();
    private DestinationClass innerClass;
    private List<DestinationClass> classesList = new ArrayList<>();

    public int anInt;
    public double aDouble;
    public boolean aBoolean;
    public float aFloat;
    public byte aByte;
    public short aShort;
    public long aLong;
    public char aChar;

    public Integer anInteger1;
    public Double aDouble1;
    public Boolean aBoolean1;
    public Float aFloat1;
    public Byte aByte1;
    public Short aShort1;
    public Long aLong1;
    public Character aChar1;

    public BigDecimal bigDecimal;
    public BigInteger bigInteger;

    public String getParentString1() {
        return parentString1;
    }

    public void setParentString1(String parentString1) {
        this.parentString1 = parentString1;
    }

    public String getParentString2() {
        return parentString2;
    }

    public void setParentString2(String parentString2) {
        this.parentString2 = parentString2;
    }

    public HashMap<String, String> getParentHashMap2() {
        return parentHashMap2;
    }

    public void setParentHashMap2(HashMap<String, String> parentHashMap2) {
        this.parentHashMap2 = parentHashMap2;
    }

    public int getParentInt2() {
        return parentInt2;
    }

    public void setParentInt2(int parentInt2) {
        this.parentInt2 = parentInt2;
    }

    public InnerDestinationClass getInnerDestinationClass() {
        return innerDestinationClass;
    }

    public void setInnerDestinationClass(InnerDestinationClass innerDestinationClass) {
        this.innerDestinationClass = innerDestinationClass;
    }

    public int getAnInt() {
        return anInt;
    }

    public void setAnInt(int anInt) {
        this.anInt = anInt;
    }

    public double getaDouble() {
        return aDouble;
    }

    public void setaDouble(double aDouble) {
        this.aDouble = aDouble;
    }

    public boolean isaBoolean() {
        return aBoolean;
    }

    public void setaBoolean(boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

    public float getaFloat() {
        return aFloat;
    }

    public void setaFloat(float aFloat) {
        this.aFloat = aFloat;
    }

    public byte getaByte() {
        return aByte;
    }

    public void setaByte(byte aByte) {
        this.aByte = aByte;
    }

    public short getaShort() {
        return aShort;
    }

    public void setaShort(short aShort) {
        this.aShort = aShort;
    }

    public long getaLong() {
        return aLong;
    }

    public void setaLong(long aLong) {
        this.aLong = aLong;
    }

    public Integer getAnInteger1() {
        return anInteger1;
    }

    public void setAnInteger1(Integer anInteger1) {
        this.anInteger1 = anInteger1;
    }

    public Double getaDouble1() {
        return aDouble1;
    }

    public void setaDouble1(Double aDouble1) {
        this.aDouble1 = aDouble1;
    }

    public Boolean getaBoolean1() {
        return aBoolean1;
    }

    public void setaBoolean1(Boolean aBoolean1) {
        this.aBoolean1 = aBoolean1;
    }

    public Float getaFloat1() {
        return aFloat1;
    }

    public void setaFloat1(Float aFloat1) {
        this.aFloat1 = aFloat1;
    }

    public Byte getaByte1() {
        return aByte1;
    }

    public void setaByte1(Byte aByte1) {
        this.aByte1 = aByte1;
    }

    public Short getaShort1() {
        return aShort1;
    }

    public void setaShort1(Short aShort1) {
        this.aShort1 = aShort1;
    }

    public Long getaLong1() {
        return aLong1;
    }

    public void setaLong1(Long aLong1) {
        this.aLong1 = aLong1;
    }

    public BigDecimal getBigDecimal() {
        return bigDecimal;
    }

    public void setBigDecimal(BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
    }

    public BigInteger getBigInteger() {
        return bigInteger;
    }

    public void setBigInteger(BigInteger bigInteger) {
        this.bigInteger = bigInteger;
    }

    public char getaChar() {
        return aChar;
    }

    public void setaChar(char aChar) {
        this.aChar = aChar;
    }

    public Character getaChar1() {
        return aChar1;
    }

    public void setaChar1(Character aChar1) {
        this.aChar1 = aChar1;
    }

    public List<Integer> getIntegers2() {
        return integers2;
    }

    public void setIntegers2(List<Integer> integers2) {
        this.integers2 = integers2;
    }

    public List<String> getStrings2() {
        return strings2;
    }

    public void setStrings2(List<String> strings2) {
        this.strings2 = strings2;
    }

    public List<InnerDestinationClass> getParentDeepArrayDest() {
        return parentDeepArrayDest;
    }

    public void setParentDeepArrayDest(List<InnerDestinationClass> parentDeepArrayDest) {
        this.parentDeepArrayDest = parentDeepArrayDest;
    }

    public HashMap<String, InnerDestinationClass> getParentHashMapHard() {
        return parentHashMapHard;
    }

    public void setParentHashMapHard(HashMap<String, InnerDestinationClass> parentHashMapHard) {
        this.parentHashMapHard = parentHashMapHard;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate2() {
        return date2;
    }

    public void setDate2(Date date2) {
        this.date2 = date2;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public DestinationClass getInnerClass() {
        return innerClass;
    }

    public void setInnerClass(DestinationClass innerClass) {
        this.innerClass = innerClass;
    }

    public String getString3() {
        return string3;
    }

    public void setString3(String string3) {
        this.string3 = string3;
    }

    public String getString4() {
        return string4;
    }

    public void setString4(String string4) {
        this.string4 = string4;
    }

    public List<DestinationClass> getClassesList() {
        return classesList;
    }

    public void setClassesList(List<DestinationClass> classesList) {
        this.classesList = classesList;
    }
}

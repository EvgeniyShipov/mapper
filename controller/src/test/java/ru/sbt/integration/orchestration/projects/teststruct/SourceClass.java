package ru.sbt.integration.orchestration.projects.teststruct;

import java.util.*;

public class SourceClass {
    public String parentString1;
    public String parentString2;
    private String string3;
    private String string4;
    int parentInt1;
    public Calendar calendar;
    public Date date;

    public HashMap<String, Object> parentHashMap1;

    private List<SourceClass> classesList = new ArrayList<>();

    ArrayList<HashMap<String, Object>> parentArrayList1;
    List<String> parentArrayList11 = new ArrayList<>();
    List<InnerSourceClass> parentDeepArray = new ArrayList<>();

    InnerSourceClass innerSourceClass = new InnerSourceClass();
    private SourceClass innerClass;

    public String getParentString1() {
        return parentString1;
    }

    public void setParentString1(String parentString1) {
        this.parentString1 = parentString1;
    }

    public HashMap<String, Object> getParentHashMap1() {
        return parentHashMap1;
    }

    public void setParentHashMap1(HashMap<String, Object> parentHashMap1) {
        this.parentHashMap1 = parentHashMap1;
    }

    public int getParentInt1() {
        return parentInt1;
    }

    public void setParentInt1(int parentInt1) {
        this.parentInt1 = parentInt1;
    }

    public InnerSourceClass getInnerSourceClass() {
        return innerSourceClass;
    }

    public void setInnerSourceClass(InnerSourceClass innerSourceClass) {
        this.innerSourceClass = innerSourceClass;
    }

    public ArrayList<HashMap<String, Object>> getParentArrayList1() {
        return parentArrayList1;
    }

    public void setParentArrayList1(ArrayList<HashMap<String, Object>> parentArrayList1) {
        this.parentArrayList1 = parentArrayList1;
    }

    public List<String> getParentArrayList11() {
        return parentArrayList11;
    }

    public void setParentArrayList11(List<String> parentArrayList11) {
        this.parentArrayList11 = parentArrayList11;
    }

    public String getParentString2() {
        return parentString2;
    }

    public void setParentString2(String parentString2) {
        this.parentString2 = parentString2;
    }

    public List<InnerSourceClass> getParentDeepArray() {
        return parentDeepArray;
    }

    public void setParentDeepArray(List<InnerSourceClass> parentDeepArray) {
        this.parentDeepArray = parentDeepArray;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
    public SourceClass getInnerClass() {
        return innerClass;
    }

    public void setInnerClass(SourceClass innerClass) {
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    public void setString4(String string4) {
        this.string4 = string4;
    }

    public List<SourceClass> getClassesList() {
        return classesList;
    }

    public void setClassesList(List<SourceClass> classesList) {
        this.classesList = classesList;
    }
}

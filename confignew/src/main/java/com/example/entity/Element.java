package com.example.entity;

import java.util.Objects;

/**
 * 表达式元素类
 */
public class Element {
    private String result;
    private String left;
    private String middle;
    private String right;

    public Element() {
        this.result = "";
        this.left = "";
        this.middle = "";
        this.right = "";
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getMiddle() {
        return middle;
    }

    public void setMiddle(String middle) {
        this.middle = middle;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Element roiEntry = (Element) o;
        return Objects.equals(result, roiEntry.result) &&
                Objects.equals(left, roiEntry.left) &&
                Objects.equals(middle, roiEntry.middle) &&
                Objects.equals(right, roiEntry.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, left, middle, right);
    }
} 
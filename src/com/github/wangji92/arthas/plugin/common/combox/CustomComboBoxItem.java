package com.github.wangji92.arthas.plugin.common.combox;

/**
 * @author 汪小哥
 * @date 04-05-2021
 */
public class CustomComboBoxItem<T> {
    /**
     * 原始信息
     */
    private T contentObject;

    /**
     * 展示项目
     */
    private String display;

    /**
     * 展示的信息
     */
    private String tipText;

    public T getContentObject() {
        return contentObject;
    }

    public void setContentObject(T contentObject) {
        this.contentObject = contentObject;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getTipText() {
        return tipText;
    }

    public void setTipText(String tipText) {
        this.tipText = tipText;
    }

    @Override
    public String toString() {
        return display;
    }
}

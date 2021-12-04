package com.github.wangji92.arthas.plugin.common.combox;

import javax.swing.*;
import java.awt.*;

/**
 * 自定义 DefaultListCellRenderer
 * https://www.java-forums.org/awt-swing/16196-item-too-big-jshellScriptComboBox.html
 *
 * @author 汪小哥
 * @date 05-05-2021
 */
public class CustomDefaultListCellRenderer extends DefaultListCellRenderer {

    private JComboBox comboBox;

    private JLabel tipLabel;


    public CustomDefaultListCellRenderer(JComboBox shellScriptComboBox, JLabel tipLabel) {
        this.comboBox = shellScriptComboBox;
        this.tipLabel = tipLabel;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index,
                isSelected, cellHasFocus);
        String tipText = value.toString();
        if (comboBox.getModel().getElementAt(index) instanceof CustomComboBoxItem) {
            tipText = ((CustomComboBoxItem) value).getTipText();
        }
        if (isSelected) {
            comboBox.setToolTipText(tipText);
            if (tipLabel != null) {
                tipLabel.setText(tipText);
                tipLabel.setToolTipText(tipText);
            }
        }
        setToolTipText(tipText);
        return this;
    }
}

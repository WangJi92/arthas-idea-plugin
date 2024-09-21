package com.github.idea.arthas.plugin.common.swing;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.function.Consumer;

/**
 * 支持 placeholder 的搜索框
 * @author imyzt
 */
public class PlaceholderTextSearchField extends JTextField {
    private final String placeholder;

    public PlaceholderTextSearchField(String placeholder) {
        this(placeholder, null);
    }
    public PlaceholderTextSearchField(String placeholder, Consumer<String> search) {
        this.placeholder = placeholder;
        Color originForeground = getForeground();
        setForeground(JBColor.GRAY);
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().isEmpty()) {
                    setForeground(originForeground);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setForeground(JBColor.GRAY);
                }
            }
        });

        this.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                search();
            }

            private void search() {
                if (search != null) {
                    search.accept(getText());
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && !hasFocus()) {
            g.setColor(JBColor.GRAY);
            int y = (getHeight() + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2;
            int x = getInsets().left + 1;
            g.drawString(placeholder, x, y);
        }
    }

    public void clearText() {
        setText(null);
    }
}

package com.kinshop.web.form;

import java.util.ArrayList;
import java.util.List;

public class ReturnForm {

    private String note;

    private List<ReturnLineForm> lines = new ArrayList<>();

    public ReturnForm() {
        for (int i = 0; i < 5; i++) {
            lines.add(new ReturnLineForm());
        }
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<ReturnLineForm> getLines() {
        return lines;
    }

    public void setLines(List<ReturnLineForm> lines) {
        this.lines = lines;
    }
}

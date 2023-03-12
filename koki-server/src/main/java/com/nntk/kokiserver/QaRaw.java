package com.nntk.kokiserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
public class QaRaw {
    private int id;
    private String question;
    private String answer;
}

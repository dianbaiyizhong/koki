package com.nntk.kokiserver;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class KokiConfig {


    @Bean
    public KokiSearcher kokiSearcher() {
        KokiSearcher kokiSearcher = new KokiSearcher();
        kokiSearcher.run();
        return kokiSearcher;

    }

    @Bean
    public ITesseract tesseract() {
        ITesseract instance = new Tesseract();
        //指定语言包的位置就是步骤1中解压的语言包路径
        instance.setDatapath("D:\\software\\tessdata_best-main");
        // 我们需要指定识别语种+代表使用多语言识别
        instance.setLanguage("chi_sim");


        // 指定识别图片
        return instance;
    }
}

package com.nntk.kokiserver;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws TesseractException, IOException {
        ITesseract instance = new Tesseract();
        //指定语言包的位置就是步骤1中解压的语言包路径
        instance.setDatapath("D:\\software\\tessdata_best-main");
        // 我们需要指定识别语种+代表使用多语言识别
        instance.setLanguage("chi_sim");
        // 指定识别图片
        File imgDir = new File("C:\\Users\\nntk\\AppData\\Local\\Temp\\49fa7f4860f94632b897c58698f3a4ad.png");


        BufferedImage bufferedImage = ImageIO.read(imgDir);

        BufferedImage after = ImageHelper.convertImageToGrayscale(bufferedImage);


        ImageIO.write(after, "png", new File("D://1.png"));


//        String ocrResult = instance.doOCR(imgDir, new Rectangle(410, 250, 376, 70));
        String ocrResult = instance.doOCR(imgDir);



        // 输出识别结果
        System.out.println(ocrResult);
    }


}

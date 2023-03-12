package com.nntk.kokiserver;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.tokenizer.Result;
import cn.hutool.extra.tokenizer.TokenizerEngine;
import cn.hutool.extra.tokenizer.TokenizerUtil;
import cn.hutool.extra.tokenizer.Word;
import jakarta.annotation.Resource;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
public class KokiController {
    TokenizerEngine engine = TokenizerUtil.createEngine();

    @Resource
    private KokiSearcher kokiSearcher;

    @Resource
    private ITesseract tesseract;

    @RequestMapping("/answer")
    public Object find(@RequestBody KokiForm form) {

        System.out.println(form.getText());
        String s = StrUtil.removeAll(form.getText(), " ");
        Result result = engine.parse(s);
        List<String> queryList = new ArrayList<>();
        while (result.hasNext()) {
            Word next = result.next();
            queryList.add(next.getText());
        }
        return kokiSearcher.search(queryList);
    }


    @RequestMapping("/answer/file")
    public Object find(@RequestParam("file")
                       MultipartFile blobFile) throws IOException, TesseractException {

        String strTmp = System.getProperty("java.io.tmpdir");
        File file = new File(strTmp + File.separator + IdUtil.fastSimpleUUID() + ".png");
        System.out.println(file);
        FileUtils.copyInputStreamToFile(blobFile.getInputStream(), file);
        String s = tesseract.doOCR(file);
        System.out.println("===" + s);
//        FileUtil.del(file);
        return "success";
    }


}

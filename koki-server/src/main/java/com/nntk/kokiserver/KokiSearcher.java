package com.nntk.kokiserver;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.map.TableMap;
import cn.hutool.extra.tokenizer.Result;
import cn.hutool.extra.tokenizer.TokenizerEngine;
import cn.hutool.extra.tokenizer.TokenizerUtil;
import cn.hutool.extra.tokenizer.Word;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KokiSearcher {
    public TableMap<String, Integer> tableMap = new TableMap<>();
    public List<QaRaw> qaRawList = new ArrayList<>();

    public static void main(String[] args) {

        new KokiSearcher().run();

    }


    public List<QaRaw> search(List<String> wordList) {

        List<Integer> allList = new ArrayList<>();
        for (int i = 0; i < wordList.size(); i++) {
            String word = wordList.get(i);
            List<Integer> resultList = tableMap.getValues(word);
            resultList = Lists.newArrayList(Sets.newLinkedHashSet(resultList));
            allList.addAll(resultList);
        }
        Map<Integer, Long> map = allList.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<Map.Entry<Integer, Long>> sorted = new ArrayList<>(map.entrySet());
        //根据value排序
        Collections.sort(sorted, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        List<QaRaw> result = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            result.add(qaRawList.get(sorted.get(i).getKey()));
            if (i == 4) {
                break;
            }
        }
        return result;
    }


    public void run() {
        TokenizerEngine engine = TokenizerUtil.createEngine();
        String s = ResourceUtil.readStr("qa.html", Charset.defaultCharset());
        Document doc = Jsoup.parse(s);
        Elements tr = doc.select("table").select("tr");
        for (int i = 0; i < tr.size(); i++) {
            Elements td = tr.get(i).select("td");
            qaRawList.add(new QaRaw(i, td.get(0).text(), td.get(1).text()));
        }


        for (int i = 0; i < qaRawList.size(); i++) {
            QaRaw qaRaw = qaRawList.get(i);

            Result result = engine.parse(qaRaw.getQuestion());

            result.forEach(new Consumer<Word>() {
                @Override
                public void accept(Word word) {
                    tableMap.put(word.getText(), qaRaw.getId());
                }
            });
        }

//        search(Lists.newArrayList("门派", "角色", "可能", "精灵"));


    }
}

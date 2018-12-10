package com.stephen.lab.util;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Stephen
 * @date 2018/08/19 16:31
 */
public class DocUtils {
    public static String readWord(String path) {
        String buffer = "";
        try {
            if (path.endsWith(".doc")) {
                InputStream is = new FileInputStream(new File(path));
                WordExtractor ex = new WordExtractor(is);
                buffer = ex.getText();
                ex.close();
            } else if (path.endsWith("docx")) {
                OPCPackage opcPackage = POIXMLDocument.openPackage(path);
                POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
                buffer = extractor.getText();
                extractor.close();
            } else {
                LogRecod.print("此文件不是word文:" + path);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return buffer;
    }

    public static String readWordFromFile(String path) {
        if (path.endsWith("txt")) {
            List<String> lines = null;
            try {
                lines = Files.readLines(new File(path), Charsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Joiner.on(" ").join(lines);
        } else {
            return readWord(path);
        }
    }
}

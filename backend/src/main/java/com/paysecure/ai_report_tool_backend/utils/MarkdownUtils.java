package com.paysecure.ai_report_tool_backend.utils;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.ast.Node;

public class MarkdownUtils {

    private static final Parser parser = Parser.builder().build();
    private static final HtmlRenderer renderer = HtmlRenderer.builder().build();

    public static String toHtml(String markdown) {
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
package org.example;

import org.example.pojo.Plurk;
import org.example.pojo.Response;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HtmlConverter {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

    public static String generateHtmlContent(List<Plurk> plurks) {
        StringBuilder htmlContent = new StringBuilder();

        //開始 HTML 文件
        htmlContent.append("<html><head><title>Plurks</title></head><body>");
        htmlContent.append("<h1>Plurk List</h1>");
        htmlContent.append("<table border='1'>");
        htmlContent.append("<tr><th>Plurk ID</th><th>Posted</th><th>Content</th><th>Responses</th><th>Favorite Count</th></tr>");

        //遍歷 Plurk List，並將每個 Plurk 的資料添加到表格中
        for (Plurk plurk : plurks) {
            htmlContent.append("<tr>");
            htmlContent.append("<td>").append(plurk.getPlurk_id()).append("</td>");
            htmlContent.append("<td>").append(plurk.getPosted()).append("</td>");
            htmlContent.append("<td>").append(plurk.getContent()).append("</td>");
            htmlContent.append("<td>").append(plurk.getResponse_count()).append("</td>");
            htmlContent.append("<td>").append(plurk.getFavorite_count()).append("</td>");
            htmlContent.append("</tr>");

            //處理 responses，如果有的話
            if (plurk.getResponses() != null && !plurk.getResponses().isEmpty()) {
                htmlContent.append("<tr><td colspan='5'><table border='1'>");
                htmlContent.append("<tr><th>Response ID</th><th>Posted</th><th>Content</th><th>Handle</th></tr>");
                for (Response response : plurk.getResponses()) {
                    htmlContent.append("<tr>");
                    htmlContent.append("<td>").append(response.getId()).append("</td>");
                    htmlContent.append("<td>").append(response.getPosted()).append("</td>");
                    htmlContent.append("<td>").append(response.getContent()).append("</td>");
                    htmlContent.append("<td>").append(response.getHandle()).append("</td>");
                    htmlContent.append("</tr>");
                }
                htmlContent.append("</table></td></tr>");
            }
        }

        //結束 HTML 文件
        htmlContent.append("</table>");
        htmlContent.append("</body></html>");

        return htmlContent.toString();
    }

    public static void saveHtmlToFile(List<Plurk> plurks) throws IOException {
        String fileName = formatter.format(new Date()) + ".html";
        String htmlContent = generateHtmlContent(plurks);
        String folderPath = new File(System.getProperty("user.dir")).getParent() + File.separator + "crawling_data";

        //建立資料夾（如果它不存在）
        File folder = new File(folderPath);
        if (!folder.exists()) {
            if (folder.mkdir()) {
                System.out.println("資料夾建立成功: " + folderPath);
            }
        }

        File file = new File(folderPath + File.separator + fileName);

        //寫入 HTML 內容到檔案
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(htmlContent);
            System.out.println("HTML 檔案已儲存到: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

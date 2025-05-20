/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.ReadMail;

/**
 *
 * @author LG
 */
public class PageCalculator {
    public static PageRange calculatePageRange(int totalCount, int page, int pageSize) {
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int start = totalCount - (page - 1) * pageSize;
        int end = Math.max(start - pageSize + 1, 1);
        return new PageRange(start, end, totalPages);
    }

    public static class PageRange {
        public int start;
        public int end;
        public int totalPages;

        public PageRange(int start, int end, int totalPages) {
            this.start = start;
            this.end = end;
            this.totalPages = totalPages;
        }
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.ReadMail;

import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;

/**
 *
 * @author LG
 */
public class PageCalculatorTest {
    @Test
    public void testPage1() {
        PageCalculator.PageRange range = PageCalculator.calculatePageRange(25, 1, 10);
        assertEquals(25, range.start);
        assertEquals(16, range.end);
        assertEquals(3, range.totalPages);
    }

    @Test
    public void testPage2() {
        PageCalculator.PageRange range = PageCalculator.calculatePageRange(25, 2, 10);
        assertEquals(15, range.start);
        assertEquals(6, range.end);
        assertEquals(3, range.totalPages);
    }

    @Test
    public void testPage3() {
        PageCalculator.PageRange range = PageCalculator.calculatePageRange(25, 3, 10);
        assertEquals(5, range.start);
        assertEquals(1, range.end);
        assertEquals(3, range.totalPages);
    }

    @Test
    public void testPageOverLimit() {
        PageCalculator.PageRange range = PageCalculator.calculatePageRange(5, 2, 10);
        assertEquals(-5, range.start); // 페이지 초과 시 이런 값이 나올 수 있음
        assertEquals(1, range.end);
        assertEquals(1, range.totalPages);
    }
}
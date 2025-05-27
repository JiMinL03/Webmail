/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.ReadMail;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;

import static org.junit.Assert.*;

/**
 *
 * @author LG
 */
public class DateCheckerTest {

    public boolean isThirtyDaysOld(String date) {
        Date d1 = new Date(); // 현재 날짜

        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss", Locale.ENGLISH);
        date = date.replaceAll("\\s+", " ").trim();

        try {
            Date inputDate = sdf.parse(date);
            long inputDateMillis = inputDate.getTime();
            long thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000;
            long differenceInMillis = Math.abs(d1.getTime() - inputDateMillis);
            return differenceInMillis == thirtyDaysInMillis;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 현재 날짜에서 정확히 30일 전의 날짜를 입력했을 때 true를 반환하는지 테스트
     */
    @Test
    public void testExactly30DaysOld() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -30); // 30일 전 날짜
        Date thirtyDaysAgo = calendar.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss", Locale.ENGLISH);
        String dateString = sdf.format(thirtyDaysAgo);

        assertTrue(isThirtyDaysOld(dateString));
    }

    /**
     * 현재 날짜에서 29일 전인 경우 false를 반환하는지 테스트
     */
    @Test
    public void testLessThan30DaysOld() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -29); // 29일 전
        Date date = calendar.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss", Locale.ENGLISH);
        String dateString = sdf.format(date);

        assertFalse(isThirtyDaysOld(dateString));
    }

    /**
     * 현재 날짜에서 31일 전인 경우 false를 반환하는지 테스트
     */
    @Test
    public void testMoreThan30DaysOld() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -31); // 31일 전
        Date date = calendar.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss", Locale.ENGLISH);
        String dateString = sdf.format(date);

        assertFalse(isThirtyDaysOld(dateString));
    }

    /**
     * 잘못된 날짜 형식 입력 시 false를 반환하는지 테스트
     */
    @Test
    public void testInvalidDateFormat() {
        String invalidDate = "not a real date";
        assertFalse(isThirtyDaysOld(invalidDate));
    }
}
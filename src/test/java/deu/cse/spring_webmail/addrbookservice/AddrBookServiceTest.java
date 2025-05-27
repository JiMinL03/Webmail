/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.addrbookservice;

/**
 *
 * @author yeong
 */
import deu.cse.spring_webmail.model.AddrBook;
import deu.cse.spring_webmail.repository.AddrBookRepository;
import deu.cse.spring_webmail.service.AddrBookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class AddrBookServiceTest {

    @Mock
    private AddrBookRepository addrBookRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AddrBookService addrBookService;

    private final String userEmail = "me@user.com";
    private final String friendEmail = "friend@user.com";

    @Test
    void getAddr() {
        List<AddrBook> mockList = List.of(
                new AddrBook(userEmail, friendEmail, "홍길동", "010-1111-1111")
        );
        when(addrBookRepository.findAllByEmail(userEmail)).thenReturn(mockList);

        List<AddrBook> result = addrBookService.getAddr(userEmail);

        assertEquals(1, result.size());
        assertEquals("홍길동", result.get(0).getName());
    }

    @Test
    void saveAddr_WhenUserDoesNotExist() {
        mockUserList(Collections.emptyList());

        boolean result = addrBookService.saveAddr(userEmail, friendEmail, "동길홍", "010-2222-2222");

        assertFalse(result);
        verify(addrBookRepository, never()).save(any());
    }

    @Test
    void saveAddr_AlreadyExists() {
        mockUserList(List.of(Map.of("username", friendEmail)));
        when(addrBookRepository.existsByEmailAndConcatEmail(userEmail, friendEmail)).thenReturn(true);

        boolean result = addrBookService.saveAddr(userEmail, friendEmail, "동길홍", "010-2222-2222");

        assertFalse(result);
        verify(addrBookRepository, never()).save(any());
    }

    @Test
    void saveAddr_ValidAndNotExists() {
        mockUserList(List.of(Map.of("username", friendEmail)));
        when(addrBookRepository.existsByEmailAndConcatEmail(userEmail, friendEmail)).thenReturn(false);

        boolean result = addrBookService.saveAddr(userEmail, friendEmail, "동길홍", "010-2222-2222");

        assertTrue(result);
        verify(addrBookRepository).save(any(AddrBook.class));
    }

    @Test
    void deleteAddr() {
        Long id = 1L;

        addrBookService.deleteAddr(id);

        verify(addrBookRepository).deleteById(id);
    }

    @Test
    void isUserExists_UserExists() {
        mockUserList(List.of(
                Map.of("username", "abc"),
                Map.of("username", friendEmail)
        ));

        assertTrue(addrBookService.isUserExists(friendEmail));
    }

    @Test
    void isUserExists_UserNotExists() {
        mockUserList(List.of(
                Map.of("username", "abc"),
                Map.of("username", "other@example.com")
        ));

        assertFalse(addrBookService.isUserExists(friendEmail));
    }

    // 공통 유틸
    private void mockUserList(List<Map<String, String>> userList) {
        ResponseEntity<List<Map<String, String>>> response = new ResponseEntity<>(userList, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(org.springframework.http.HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);
    }
}


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.service;

import deu.cse.spring_webmail.model.AddrBook;
import deu.cse.spring_webmail.model.UserAdminAgent;
import deu.cse.spring_webmail.repository.AddrBookRepository;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;

/**
 *
 * @author yeong
 */
@Service
@Slf4j
public class AddrBookService {
    @Autowired
    private AddrBookRepository addrBookRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    private final String userListUrl = "http://localhost:8000/users";
    
    public List<AddrBook> getAddr(String email){
        List<AddrBook> addrBooks = addrBookRepository.findAllByEmail(email);
        return addrBooks;
    }
    
     public boolean saveAddr(String email, String concatEmail) {
        if (!isUserExists(concatEmail)) {
            log.warn("존재하지 않는 사용자: {}", concatEmail);
            return false;
        }

        if (addrBookRepository.existsByEmailAndConcatEmail(email, concatEmail)) {
            log.info("이미 주소록에 존재함: {} -> {}", email, concatEmail);
            return false;
        }

        addrBookRepository.save(new AddrBook(email, concatEmail));
        return true;
    }
    
    public void deleteAddr(Long id){
        addrBookRepository.deleteById(id);
    }
    
    
public boolean isUserExists(String userId) {
        try {
            ResponseEntity<List<Map<String, String>>> response = restTemplate.exchange(
                    userListUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, String>>>() {}
            );

            List<Map<String, String>> body = response.getBody();

            if (body == null) {
                log.warn("사용자 목록 응답 본문이 null입니다.");
                return false;
            }

            for (Map<String, String> user : body) {
                String username = user.get("username");
                if (userId.equals(username)) {
                    return true;
                }
            }

            log.warn("사용자 존재하지 않음: {}", userId);
        } catch (Exception e) {
            log.error("사용자 존재 여부 확인 중 오류 발생", e);
        }

        return false;
    }


}

    


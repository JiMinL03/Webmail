/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.AddrBook;
import deu.cse.spring_webmail.service.AddrBookService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author yeong
 */
@Controller
@RequestMapping("/addrbook")
@Slf4j
public class AddrController {
    @Autowired
    private AddrBookService addrBookService;

    @GetMapping
    public String showAddrBook(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userid");
        if (email == null) {
            return "redirect:/login";
        }

        List<AddrBook> addrbookList = addrBookService.getAddr(email);
        model.addAttribute("addrbookList", addrbookList);

        return "addrbook";  
    }

    @PostMapping("/add")
    public String addAddress(@RequestParam String concatEmail,
                             @RequestParam String name,
                             @RequestParam String phoneNum,                 
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        String email = (String) session.getAttribute("userid");
        log.info("추가하려는 주소록 데이터: name={}, phoneNum={}, concatEmail={}", name, phoneNum, concatEmail);
        boolean success = addrBookService.saveAddr(email, concatEmail, name, phoneNum);

        if (success) {
            redirectAttributes.addFlashAttribute("msg", "주소가 추가되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("msg", "이미 등록된 이메일이거나 존재하지 않는 사용자입니다.");
        }

        return "redirect:/addrbook";
    }

    @PostMapping("/delete")
    public String deleteEntry(@RequestParam Long id) {
        addrBookService.deleteAddr(id);
        return "redirect:/addrbook";
    }
}


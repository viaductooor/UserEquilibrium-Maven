package org.lab1505.ue.web.controller;

import org.lab1505.ue.web.result.CodeMsg;
import org.lab1505.ue.web.result.Result;
import org.lab1505.ue.web.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class MainController {
    @Autowired
    private LoginService loginService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/login")
    @ResponseBody
    public Result<String> login(@RequestParam("username") String username,
                                @RequestParam("password") String password,
                                HttpServletResponse response) {
        System.out.println(username + "@" + password);
        if (loginService.login(username, password)) {
            Cookie cookie = new Cookie("cachedUsername", username);
            cookie.setMaxAge(10 * 60);
            response.addCookie(cookie);
            return Result.success("Login success: " + username);
        }
        return Result.error(CodeMsg.ERROR);
    }

    @GetMapping("/loginPage")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/ue")
    public String ue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("cachedUsername")) {
                    return "ue";
                }
            }
        }
        return "login";
    }

    @GetMapping("/game")
    public String game(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("cachedUsername")) {
                    return "game";
                }
            }
        }
        return "login";
    }

    @GetMapping("/changedemand")
    public String changedemand(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("cachedUsername")) {
                    return "changedemand";
                }
            }
        }
        return "login";
    }

    @GetMapping("/emergency")
    public String emergency(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("cachedUsername")) {
                    return "emergency";
                }
            }
        }
        return "login";
    }

    @GetMapping("/vulnerability")
    public String vulnerability(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("cachedUsername")) {
                    return "vulnerability";
                }
            }
        }
        return "login";
    }

    @GetMapping("/resolvetrips")
    public String resolvetrips(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("cachedUsername")) {
                    return "resolvetrips";
                }
            }
        }
        return "login";
    }

}

package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {

	@Autowired
	private EmailService emailService;
	
	Random random=new Random(1000);
	
//	email id form open handler
	@GetMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session) {
		System.out.println("EMAIL"+email);
		
//		generating otp of 4 digitsF
		int otp = random.nextInt(9999);
		System.out.println("OTP"+otp);
		
//		code for sent otp to email
		String subject="OTP From SCM";
		String message="OTP = "+otp;
		String to=email;
		boolean flag = this.emailService.sendEmail(subject, message, to);
		
		if(flag) {
			session.setAttribute("otp", otp);
			return "verify_otp";
		}else {
			session.setAttribute("message", "Check your email id");
			return "forgot_email_form";
		}
		
	}
}

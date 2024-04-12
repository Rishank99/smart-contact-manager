package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private UserRepository userRepository;
	
//	method for adding common data to response
	@ModelAttribute
//	ModelAttribute annotation will run for all handlers like "/index" , "/add_contact" ,etc.
	public void addCommonData(Model model, Principal principal) {
//		Principal is used to get username(Email) of person who has logged in
		String username = principal.getName();
		System.out.println(username);
		
//		To get all the details of user from database who has logged in
		User user = userRepository.getUserByUserName(username);
		System.out.println(user);
		
//		To send user details to user dashboard
		model.addAttribute("user",user);
	}
	
//	dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
//	open add form handler
	@GetMapping("/add_contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	
//	processing add form data
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Principal principal,HttpSession session) {
		
		try {
//			contact will give the data which is submitted by filling the form on add_contact_form
			String name = principal.getName();
			User user = userRepository.getUserByUserName(name);
			
//			processing and uploading file...
			
			if(file.isEmpty()) {
//				if the file is empty then try our message
				System.out.println("File is empty");
				contact.setImage("contact.png");
			}else {
//				uploading file to folder and updating the name to contact
			    contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}
//			this will add user_id in database
			contact.setUser(user);
			
//			this will save contact details in database
			user.getContacts().add(contact);
			this.userRepository.save(user);
			
//			to print contact information
			System.out.println("Contact" + contact);
			System.out.println("Added to database");
			
//			message success.....
			session.setAttribute("message", new Message("Your contact is added !! Add more...","success"));
		}catch(Exception e) {
			System.out.println("ERROR "+ e.getMessage());
			e.printStackTrace();
//			message error
			session.setAttribute("message", new Message("Something went wrong !! Try again...","danger"));
		}
		return "normal/add_contact_form";
	}
	
//		View contacts handler
//	    per page = 5[n]
//	    current page =0[n]
	@GetMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model model,Principal principal){
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		
//		pageable has current page and number of contacts per page
		PageRequest pageable = PageRequest.of(page, 2);
		Page<Contact> contacts = contactRepository.findContactsByUser(user.getId(),pageable);
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		model.addAttribute("title","View Contacts");
		return "normal/show_contacts";
	}
	
//	handler for showing particular contact details
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal) {
		System.out.println("Cid"+cId);
		
		Optional<Contact> optional = contactRepository.findById(cId);
		Contact contact = optional.get();
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
		model.addAttribute("contact",contact);
		model.addAttribute("title",contact.getName());
		}
		
		return "normal/contact_detail";
	}
	
//	delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Model model,Principal principal,HttpSession session) {
		Optional<Contact> optional = contactRepository.findById(cId);
		Contact contact = optional.get();
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		
		
//		check
		if(user.getId()==contact.getUser().getId()) {
			user.getContacts().remove(contact);
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Contact deleted successfully","success"));
		}
		
		return "redirect:/user/show_contacts/0";
	}

//	open update form handler
	@PostMapping("/update_contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId,Model model) {
		model.addAttribute("title","Update Contact");
		
		Contact contact = contactRepository.findById(cId).get();
		model.addAttribute("contact",contact);
		
		return "normal/update_form";
	}
	
//	update contact handler
	@PostMapping("/process-update")
	public String updateContact(@ModelAttribute Contact contact , @RequestParam("profileImage") MultipartFile file , Model m,HttpSession session,Principal principal) {
		
		try {
			
//			old contact details
			Contact oldcontactDetail = this.contactRepository.findById(contact.getcId()).get();
			
//			image
			if(!file.isEmpty()) {
				
//				update new photo
				
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
			}else {
				contact.setImage(oldcontactDetail.getImage());
			}
			
			String userName = principal.getName();
			User user = userRepository.getUserByUserName(userName);
			
			contact.setUser(user);
			contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact is updated","success"));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		
		System.out.println("CONTACT"+contact.getName());
		System.out.println("CONTACT"+contact.getcId());
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
//	your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model m) {
		m.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
//	open settings handler
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}
	
//	change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session) {
		System.out.println(oldPassword);
		System.out.println(newPassword);
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
//			change the password
			
			user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(user);
			
			session.setAttribute("message", new Message("Your password is successfully changed","success"));
		}else {
//			error
			session.setAttribute("message", new Message("Please enter correct old password","danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}
}

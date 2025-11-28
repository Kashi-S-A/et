package com.et.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.et.entity.Expense;
import com.et.entity.User;
import com.et.service.ExpenseService;
import com.et.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class ExpenseController {

	private UserService userService;
	private ExpenseService expenseService;

	public ExpenseController(UserService userService, ExpenseService expenseService) {
		this.userService = userService;
		this.expenseService = expenseService;
	}

	/* ------------------------------------
	   Login + Register Pages (Thymeleaf)
	-------------------------------------- */

	@GetMapping("/")
	public String loginPage() {
		return "login";   // login.html
	}

	@GetMapping("/register")
	public String registerPage(Model model) {
		model.addAttribute("user", new User());
		return "register";   // register.html
	}

	@GetMapping("/addExpense")
	public String addExpensePage(Model model) {
		model.addAttribute("expense", new Expense());
		return "addExpense";  // addExpense.html
	}

	/* ---------------------------
	   Register User
	---------------------------- */
	@PostMapping("/register")
	public String registerUser(User user, Model model) {

		boolean registered = userService.registerUser(user);

		if (registered) {
			model.addAttribute("succmsg", "Registered Successfully");
		} else {
			model.addAttribute("errmsg", "Already Registered");
		}

		return "login"; // login.html
	}

	/* ---------------------------
	    Login User
	---------------------------- */
	@PostMapping("/login")
	public ModelAndView loginUser(HttpServletRequest request) {
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		boolean isValid = userService.login(username, password);

		ModelAndView mv = new ModelAndView();

		if (isValid) {
			mv.setViewName("welcome"); // welcome.html
			HttpSession session = request.getSession(true);
			session.setAttribute("un", username);
		} else {
			mv.setViewName("login");
			mv.addObject("errmsg", "Invalid credentials");
		}
		return mv;
	}
	
	@GetMapping("/welcome")
	public String welcomePage() {
		return "welcome";
	}
	

	/* ---------------------------
	    Add Expense
	---------------------------- */
	@PostMapping("/addExpense")
	public String addExpense(Expense expense, Model model, HttpServletRequest request) {

		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("un");

		User user = userService.findByUsername(username);
		expense.setUser(user);

		String msg = expenseService.addExpense(expense);
		model.addAttribute("succmsg", msg);

		return "welcome"; // welcome.html
	}

	/* ---------------------------
	    List Expenses
	---------------------------- */
	@GetMapping("/expenseList")
	public String expenseList(HttpServletRequest request, Model model) {

		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("un");

		User user = userService.findByUsername(username);

		model.addAttribute("exps", user.getExpenses());

		return "expenseList"; // expenseList.html
	}

	/* ---------------------------
	    Filter Expenses
	---------------------------- */
	@GetMapping("/filter")
	public String filter(HttpServletRequest request, Model model) {

		String fromDate = request.getParameter("fromDate");
		String toDate = request.getParameter("toDate");

		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("un");

		User user = userService.findByUsername(username);

		List<Expense> expenses = expenseService.filter(user, fromDate, toDate);

		model.addAttribute("exps", expenses);
		model.addAttribute("from", fromDate);
		model.addAttribute("to", toDate);

		return "expenseList"; // expenseList.html
	}

	/* ---------------------------
	    Edit Expense Page
	---------------------------- */
	@GetMapping("/editExpense")
	public String updateExpense(HttpServletRequest request, Model model) {
		Integer eid = Integer.parseInt(request.getParameter("eid"));

		Expense expense = expenseService.findById(eid);

		model.addAttribute("expense", expense);

		return "updateExpense"; // updateExpense.html
	}

	/* ---------------------------
	    Update Expense
	---------------------------- */
	@PostMapping("/updateExpense")
	public String updateExpense(Expense expense, Model model) {

		String msg = expenseService.updateExpense(expense);
		model.addAttribute("succmsg", msg);

		return "welcome"; // welcome.html
	}

	/* ---------------------------
	    Delete Expense
	---------------------------- */
	@GetMapping("/deleteExpense")
	public String deleteExpense(HttpServletRequest request, Model model) {
		Integer eid = Integer.parseInt(request.getParameter("eid"));

		String msg = expenseService.deleteExpense(eid);

		model.addAttribute("succmsg", msg);

		return "welcome"; // welcome.html
	}

	/* ---------------------------
	    Forgot Password
	---------------------------- */
	@PostMapping("/forgot-password")
	public String forgotPwd(HttpServletRequest request, Model model) throws Exception {

		String emailusername = request.getParameter("emailusername");

		String msg = userService.forgotPwd(request, emailusername);
		model.addAttribute("msg", msg);

		return "reset-password"; // reset-password.html
	}

	/* ---------------------------
	    Reset Password
	---------------------------- */
	@PostMapping("/reset-password")
	public String resetPassword(HttpServletRequest request, Model model) {

		String newPwd = request.getParameter("newPassword");
		String otp = request.getParameter("otp");

		HttpSession session = request.getSession(false);

		String sessionOtp = (String) session.getAttribute("otp");
		String username = (String) session.getAttribute("un");

		if (otp.equals(sessionOtp)) {
			String message = userService.resetPassword(username, newPwd);
			model.addAttribute("succmsg", message);
		} else {
			model.addAttribute("errmsg", "Incorrect OTP");
		}

		return "login"; // login.html
	}

	/* ---------------------------
	    Logout
	---------------------------- */
	@GetMapping("/logout")
	public String logout(HttpServletRequest request) {

		HttpSession session = request.getSession(false);
		session.invalidate();

		return "login"; // login.html
	}
}
